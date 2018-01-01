package com.mycompany.db.core.systemagent

import com.mycompany.db.core.file.dslfile.DslFileAttachment
import com.mycompany.db.core.sc.SystemAgentSC

/**
 * @author Nikita Gorodilov
 */
interface SystemAgentService {

    fun save(systemAgent: SystemAgent): Long

    fun getDslAttachment(systemAgentServiceLogin: String): DslFileAttachment?

    fun get(isDeleted: Boolean, isSendAndGetMessages: Boolean): List<SystemAgent>

    fun get(sc: SystemAgentSC): List<SystemAgent>

    fun get(id: Long): SystemAgent

    fun getByServiceLogin(serviceLogin: String): SystemAgent

    fun isExistsAgent(serviceLogin: String): Boolean
}