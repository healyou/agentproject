package com.mycompany.db.service

import com.mycompany.db.core.systemagent.SystemAgent
import com.mycompany.db.core.systemagent.SystemAgentEventHistory
import com.mycompany.db.core.systemagent.SystemAgentEventHistoryService
import com.mycompany.db.core.systemagent.SystemAgentService
import objects.StringObjects
import objects.initdbobjects.UserObjects
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import com.mycompany.AbstractServiceTest

import static junit.framework.Assert.assertTrue

/**
 * @author Nikita Gorodilov
 */
class SystemAgentHistoryServiceTest extends AbstractServiceTest {

    @Autowired
    private SystemAgentEventHistoryService systemAgentEventHistoryService
    @Autowired
    private SystemAgentService systemAgentService

    /**
     * Создание и получение записи истории
     */
    @Test
    void testCreatedHistory() {
        def createAgentId = createAgent().id
        def historyId = createHistory(createAgentId)
        def historyList = systemAgentEventHistoryService.getLastNumberItems(createAgentId, 1L)

        assertTrue(historyList.size() == 1 && historyId == historyList[0].id && createAgentId == historyList[0].systemAgentId)
    }

    /**
     * Создание и получение последний записей истории
     */
    @Test
    void testGetLastNumberCreatedHistory() {
        def historySize = 5
        def createAgentId = createAgent().id
        def createHistoryIds = createHistory(createAgentId, historySize)
        def historyList = systemAgentEventHistoryService.getLastNumberItems(createAgentId, historySize)

        assertTrue(historyList.size() == createHistoryIds.size())
        historyList.forEach {
            // проверяем id тк последние записи добавили мы
            assertTrue(createHistoryIds.any { createHistoryId ->
                createHistoryId == it.id
            } && createAgentId == it.systemAgentId)
        }
    }

    private def createHistory(Long systemAgentId, Long size) {
        List<Long> historyIds = new ArrayList<>()
        for (i in 0..size - 1) {
            historyIds.add(createHistory(systemAgentId))
        }
        historyIds
    }

    private def createHistory(Long systemAgentId) {
        def history = new SystemAgentEventHistory(
                systemAgentId,
                StringObjects.randomString()
        )
        systemAgentEventHistoryService.create(history)
    }

    private SystemAgent createAgent() {
        def systemAgent = new SystemAgent(
                StringObjects.randomString(),
                StringObjects.randomString(),
                true,
                UserObjects.testActiveUser(),
                UserObjects.testActiveUser()
        )
        systemAgent.isDeleted = false

        return systemAgentService.getById(systemAgentService.save(systemAgent))
    }
}
