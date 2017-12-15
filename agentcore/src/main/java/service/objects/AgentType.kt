package service.objects

import db.base.Codable
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty


/**
 * Тип агента
 *
 * @author Nikita Gorodilov
 */
class AgentType @JsonCreator constructor (
        @JsonProperty("id")
        override var id: Long?,
        @JsonProperty("code")
        var code: Code,
        @JsonProperty("name")
        var name: String,
        @JsonProperty("deleted")
        var isDeleted: Boolean
): Entity {

    /* Типы агентов */
    enum class Code(override val code: String): Codable<String> {
        worker("worker"),
        server("server"),
        test_agent_type_1("test_agent_type_1"),
        test_agent_type_2("test_agent_type_2");
    }
}