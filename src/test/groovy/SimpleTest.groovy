import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.flipkart.zjsonpatch.JsonDiff
import com.flipkart.zjsonpatch.JsonPatch
import spock.lang.Specification

import static com.flipkart.zjsonpatch.JsonDiff.asJson
import static com.flipkart.zjsonpatch.JsonPatch.apply


/**
 * @author Pavel Alexeev.
 * @since 2019-02-01 14:59.
 */
class SimpleTest extends Specification {
	static ObjectMapper mapper = new ObjectMapper()

	static JsonNode toJson(String src){
		mapper.readTree(src)
	}

	def "Simple patch"(){
		when:
			// https://www.baeldung.com/jackson-json-to-jsonnode
			JsonNode source = toJson('{"k1":"v1","k2":"v2"}')
			JsonNode target = toJson('{"k1":"v1","k2":"v3"}')

			JsonNode patch = asJson(source, target)
		then:
			patch.toString() == '[{"op":"replace","path":"/k2","value":"v3"}]'
	}

	def "Patch sub-object"(){
		when:
			JsonNode source = toJson('{"k1":"v1","k2":"v2"}')
			JsonNode target = toJson('{"k1":"v1","k2": {"inner": 1}}')

			JsonNode patch = asJson(source, target)
		then:
			patch.toString() == '[{"op":"replace","path":"/k2","value":{"inner":1}}]'
	}

	def "Simple create patch, apply patch"(){
		when:
			JsonNode source = toJson('{"k1":"v1","k2":"v2"}')
			JsonNode target = toJson('{"k1":"v1","k2": {"inner": 1}}')

			JsonNode patch = asJson(source, target)
		then:
			patch.toString() == '[{"op":"replace","path":"/k2","value":{"inner":1}}]'

		when:
			JsonNode targetRecreated = apply(patch, source)

		then:
			targetRecreated.toString() == '{"k1":"v1","k2":{"inner":1}}'
	}

	def "Test create patch objects move"(){
		when:
			JsonNode source = toJson('{"k1":"v1","k2":"v2"}')
			JsonNode target = toJson('{"k2":"v2","k1":"v1"}')

			JsonNode patch = asJson(source, target)
		then:
			patch.toString() == '[]'
	}
}
