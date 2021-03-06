package com.mycompany.db.core.systemagent

import com.mycompany.db.core.file.dslfile.DslFileAttachment
import com.mycompany.db.core.sc.SystemAgentSC
import com.mycompany.user.User

/**
 * @author Nikita Gorodilov
 */
interface SystemAgentService {

    fun save(systemAgent: SystemAgent): Long

    fun getDslAttachment(systemAgentServiceLogin: String): DslFileAttachment?

    fun get(isDeleted: Boolean, isSendAndGetMessages: Boolean): List<SystemAgent>

    fun get(sc: SystemAgentSC): List<SystemAgent>

    /**
     * Получеть n агентов указанного владельца
     * @param size количество записей
     * @param ownerId владелец агента
     */
    fun get(size: Long, ownerId: Long): List<SystemAgent>

    fun getById(id: Long): SystemAgent

    fun getByServiceLogin(serviceLogin: String): SystemAgent

    fun isExistsAgent(serviceLogin: String): Boolean

    /**
     * @return true - Пользователь является владельцем агента
     */
    fun isOwnAgent(agent: SystemAgent, user: User): Boolean

    /**
     * @return Количество агентов для указанного пользователя
     */
    fun size(ownerId: Long): Long

    /**
     * Количество записей в таблице
     */
    fun size(): Long

    /**
     * Загрузка n записей
     * @param size количество записей
     */
    fun get(size: Long): List<SystemAgent>
}