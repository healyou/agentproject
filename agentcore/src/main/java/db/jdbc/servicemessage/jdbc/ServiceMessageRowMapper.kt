package db.jdbc.servicemessage.jdbc

import agentcore.utils.Codable
import db.base.AbstractRowMapper
import db.base.toIsDeleted
import db.core.servicemessage.ServiceMessage
import db.core.servicemessage.ServiceMessageObjectType
import db.core.servicemessage.ServiceMessageType
import java.sql.ResultSet
import java.sql.SQLException

/**
 * @author Nikita Gorodilov
 */
class ServiceMessageRowMapper : AbstractRowMapper<ServiceMessage>() {

    @Throws(SQLException::class)
    override fun mapRow(rs: ResultSet, i: Int): ServiceMessage {
        val message = ServiceMessage(
                getString(rs, "json_object"),
                mapObjectType(rs),
                mapMessageType(rs)
        )

        message.createDate = getDate(rs, "create_date")
        message.useDate = getNullDate(rs, "user_date")

        return message
    }

    private fun mapObjectType(rs: ResultSet) : ServiceMessageObjectType {
        return ServiceMessageObjectType(
                getLong(rs, "object_type_id"),
                Codable.find(ServiceMessageObjectType.Code::class.java, getString(rs, "message_object_type_code")),
                getString(rs, "message_object_type_name"),
                getString(rs, "message_object_type_is_deleted").toIsDeleted()
        )
    }

    private fun mapMessageType(rs: ResultSet) : ServiceMessageType {
        return ServiceMessageType(
                getLong(rs, "message_type_id"),
                Codable.find(ServiceMessageType.Code::class.java, getString(rs, "message_type_code")),
                getString(rs, "message_type_name"),
                getString(rs, "message_type_is_deleted").toIsDeleted()
        )
    }
}