import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.flipkart.zjsonpatch.JsonPatchApplicationException
import spock.lang.Issue
import spock.lang.Specification

import static com.flipkart.zjsonpatch.DiffFlags.ADD_ORIGINAL_VALUE_ON_REPLACE
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
			JsonNode source = toJson '{"k1":"v1","k2":"v2"}'
			JsonNode target = toJson '{"k1":"v1","k2":"v3"}'

			JsonNode patch = asJson(source, target)
		then:
			patch as String == '[{"op":"replace","path":"/k2","value":"v3"}]'
	}

	def "Patch: value to object"(){
		when:
			JsonNode source = toJson '{"k1":"v1","k2":"v2"}'
			JsonNode target = toJson '{"k1":"v1","k2": {"inner": 1}}'

			JsonNode patch = asJson(source, target)
		then:
			patch as String == '[{"op":"replace","path":"/k2","value":{"inner":1}}]'
	}

	def "Patch sub-object"(){
		when:
			JsonNode source = toJson '{"k1":"v1","k2": {"inner": 1}}}'
			JsonNode target = toJson '{"k1":"v1","k2": {"inner": {"deeper-inner": 2} }}'

			JsonNode patch = asJson(source, target)
		then:
			patch as String == '[{"op":"replace","path":"/k2/inner","value":{"deeper-inner":2}}]'
	}

	def "Simple create/apply patch"(){
		when:
			JsonNode source = toJson '{"k1":"v1","k2":"v2"}'
			JsonNode target = toJson '{"k1":"v1","k2": {"inner": 1}}'

			JsonNode patch = asJson(source, target)
		then:
			patch as String == '[{"op":"replace","path":"/k2","value":{"inner":1}}]'

		when:
			JsonNode targetRecreated = apply(patch, source)

		then:
			targetRecreated as String == '{"k1":"v1","k2":{"inner":1}}'
	}

	def "Error: Apply patch on non-existent node failed"(){
		when:
			JsonNode source = toJson '{"k1":"v1","k2":"v2"}'
			JsonNode patch  = toJson '[{"op":"replace","path":"/k3","value":{"inner":1}}]'
			apply(patch, source)

		then:
			JsonPatchApplicationException e = thrown(JsonPatchApplicationException)
			e.message == '[REPLACE Operation] noSuchPath in source, path provided : //k3'
	}

	def "Test create patch objects move"(){
		when:
			JsonNode source = toJson '{"k1":"v1","k2":"v2"}'
			JsonNode target = toJson '{"k2":"v2","k1":"v1"}'

			JsonNode patch = asJson(source, target)
		then:
			patch as String == '[]'
	}

	@Issue("https://github.com/flipkart-incubator/zjsonpatch/issues/91")
	def "Test create patch with context, and fail on apply when original object changed"(){
		when:
			JsonNode source = toJson '{"k1":"v1","k2":"v2"}'
			JsonNode target = toJson '{"k1":"v1","k2": {"inner": 1}}'

			JsonNode patch = asJson(source, target, EnumSet.of(ADD_ORIGINAL_VALUE_ON_REPLACE))
		then:
			patch as String == '[{"op":"replace","fromValue":"v2","path":"/k2","value":{"inner":1}}]'

		when:
			source = toJson '{"k1":"v1","k2":"v3 changed"}' // Our external changes in source object (configuration in production by customer)
			JsonNode targetRecreated = apply(patch, source)

		then:
//			JsonPatchApplicationException e = thrown(JsonPatchApplicationException) // I want exception there, but it succeed! @see @bug https://github.com/flipkart-incubator/zjsonpatch/issues/91
			targetRecreated as String == '{"k1":"v1","k2":{"inner":1}}'
	}
}
