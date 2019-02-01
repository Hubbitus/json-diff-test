import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.flipkart.zjsonpatch.JsonDiff
import com.flipkart.zjsonpatch.JsonPatch
import spock.lang.Specification


/**
 * @author Pavel Alexeev.
 * @since 2019-02-01 14:59.
 */
class SimpleTest extends Specification {
	static ObjectMapper mapper = new ObjectMapper();

	def "Simple patch"(){
		when:
			// https://www.baeldung.com/jackson-json-to-jsonnode
			JsonNode source = mapper.readTree('{"k1":"v1","k2":"v2"}')
			JsonNode target = mapper.readTree('{"k1":"v1","k2":"v3"}')

			JsonNode patch = JsonDiff.asJson(source, target)
		then:
			patch.toString() == '[{"op":"replace","path":"/k2","value":"v3"}]'
	}

	def "Patch sub-object"(){
		when:
			JsonNode source = mapper.readTree('{"k1":"v1","k2":"v2"}')
			JsonNode target = mapper.readTree('{"k1":"v1","k2": {"inner": 1}}')

			JsonNode patch = JsonDiff.asJson(source, target)
		then:
			patch.toString() == '[{"op":"replace","path":"/k2","value":{"inner":1}}]'
	}

	def "Simple create patch, apply patch"(){
		when:
			JsonNode source = mapper.readTree('{"k1":"v1","k2":"v2"}');
			JsonNode target = mapper.readTree('{"k1":"v1","k2": {"inner": 1}}')

			JsonNode patch = JsonDiff.asJson(source, target)
		then:
			patch.toString() == '[{"op":"replace","path":"/k2","value":{"inner":1}}]'

		when:
			JsonNode targetRecreated = JsonPatch.apply(patch, source)

		then:
			targetRecreated.toString() == '{"k1":"v1","k2":{"inner":1}}'
	}

	def "Test create patch objects move"(){
		when:
			JsonNode source = mapper.readTree('{"k1":"v1","k2":"v2"}');
			JsonNode target = mapper.readTree('{"k2":"v2","k1":"v1"}')

			JsonNode patch = JsonDiff.asJson(source, target)
		then:
			patch.toString() == '[]'
	}
}