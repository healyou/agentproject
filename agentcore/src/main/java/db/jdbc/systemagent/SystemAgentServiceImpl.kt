package db.jdbc.systemagent

import db.core.file.dslfile.DslFileAttachment
import db.core.sc.SystemAgentSC
import db.core.systemagent.SystemAgent
import db.core.systemagent.SystemAgentService
import db.jdbc.file.dslfile.DslFileAttachmentDao
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * @author Nikita Gorodilov
 */
@Component
open class SystemAgentServiceImpl : SystemAgentService {

    @Autowired
    private lateinit var dao: SystemAgentDao
    @Autowired
    private lateinit var dslDao: DslFileAttachmentDao

    override fun save(systemAgent: SystemAgent): Long {
        return if (systemAgent.isNew) {
            dao.create(systemAgent)
        }
        else {
            dao.update(systemAgent)
            systemAgent.id!!
        }
    }

    override fun getDslAttachment(systemAgentServiceLogin: String): DslFileAttachment? {
        return dslDao.getDslWorkingFileBySystemAgentServiceLogin(systemAgentServiceLogin)
    }

    override fun get(isDeleted: Boolean, isSendAndGetMessages: Boolean): List<SystemAgent> =
            dao.get(isDeleted, isSendAndGetMessages)

    override fun get(sc: SystemAgentSC): List<SystemAgent> = dao.get(sc)

    override fun get(id: Long): SystemAgent {
        return dao.get(id)
    }

    override fun getByServiceLogin(serviceLogin: String): SystemAgent = dao.getByServiceLogin(serviceLogin)

    override fun isExistsAgent(serviceLogin: String): Boolean = dao.isExistsAgent(serviceLogin)
}