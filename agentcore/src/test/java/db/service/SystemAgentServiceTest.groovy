package db.service

import db.core.file.ByteArrayFileContent
import db.core.file.FileContent
import db.core.file.FileContentLocator
import db.core.file.FileContentRef
import db.core.file.dslfile.DslFileAttachment
import db.core.file.dslfile.DslFileContentRef
import db.core.sc.SystemAgentSC
import db.core.systemagent.SystemAgent
import db.core.systemagent.SystemAgentService
import objects.OtherObjects
import objects.StringObjects
import org.jetbrains.annotations.NotNull
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.UncategorizedSQLException
import testbase.AbstractServiceTest

import static junit.framework.Assert.assertEquals
import static junit.framework.TestCase.assertNotNull
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertNull
import static org.junit.Assert.assertTrue

/**
 * @author Nikita Gorodilov
 */
class SystemAgentServiceTest extends AbstractServiceTest {

    @Autowired
    private SystemAgentService systemAgentService
    @Autowired
    private FileContentLocator fileContentLocator

    /* Параметры создаваемого системного агента */
    private Long id = null
    private def serviceLogin = "login"
    private def servicePassword = "password"
    private Date updateDate = null
    private def isDeleted = false
    private def isSendAndGetMessages = false
    private def dslFileContent = [0, 1, 2] as byte[]
    private def dslFilename = StringObjects.randomString()
    private def dslFile = OtherObjects.dslFileAttachment(dslFilename, dslFileContent)

    @Before
    void setup() {
        def systemAgent = new SystemAgent(
                serviceLogin,
                servicePassword,
                isSendAndGetMessages
        )
        systemAgent.dslFile = dslFile
        systemAgent.isDeleted = isDeleted

        id = systemAgentService.save(systemAgent)
    }

    @Test
    void "Проверка создания dsl"() {
        def saveAgent = systemAgentService.get(id)
        def actualDsl = saveAgent.dslFile
        assertDslFiles(dslFile, actualDsl)
    }

    @Test
    void "Обновление данных агента"() {
        def systemAgent = systemAgentService.get(id)

        def newLogin = StringObjects.randomString()
        def newPassword = StringObjects.randomString()
        def newDslFile = OtherObjects.dslFileAttachment(StringObjects.randomString(), [0, 1, 2, 3, 4] as byte[])
        def newIsDeleted = !isDeleted
        def newIsSendAndGetMessages = !isSendAndGetMessages

        systemAgent.serviceLogin = newLogin
        systemAgent.servicePassword = newPassword
        systemAgent.dslFile = newDslFile
        systemAgent.isDeleted = newIsDeleted
        systemAgent.isSendAndGetMessages = newIsSendAndGetMessages

        systemAgentService.save(systemAgent)
        def updateAgent = systemAgentService.get(id)
        assertEquals(newLogin, updateAgent.serviceLogin)
        assertEquals(newPassword, updateAgent.servicePassword)
        assertDslFiles(newDslFile, systemAgent.dslFile)
        assertEquals(newIsDeleted, updateAgent.isDeleted)
        assertEquals(newIsSendAndGetMessages, updateAgent.isSendAndGetMessages)
    }

    @Test
    void "Запись нового dsl файла агента"() {
        def newFileContent = [0, 1, 2, 3, 4] as byte[]
        def newFilename = StringObjects.randomString()
        def newDslFile = OtherObjects.dslFileAttachment(newFilename, newFileContent)

        def systemAgent = systemAgentService.get(id)
        systemAgent.dslFile = newDslFile
        systemAgentService.save(systemAgent)

        def actualDslFile = systemAgentService.get(id).dslFile
        assertDslFiles(newDslFile, actualDslFile)
    }

    @Test
    void "Удаление рабочего dsl файла агента"() {
        def systemAgent = systemAgentService.get(id)
        systemAgent.dslFile = null
        systemAgentService.save(systemAgent)

        assertNull(systemAgentService.get(id).dslFile)
    }

    @Test
    void "В бд сохраняются актуальные данные агента"() {
        def systemAgent = systemAgentService.get(id)

        /* проверка всех значений создания агента */
        assertEquals(id, systemAgent.id)
        assertEquals(serviceLogin, systemAgent.serviceLogin)
        assertEquals(servicePassword, systemAgent.servicePassword)
        assertNotNull(systemAgent.createDate)
        assertNotNull(systemAgent.dslFile)
        assertEquals(updateDate, systemAgent.updateDate)
        assertEquals(isDeleted, systemAgent.isDeleted)
        assertEquals(isSendAndGetMessages, systemAgent.isSendAndGetMessages)
    }

    /* Получение удалённых агентов */
    @Test
    void testSystemAgentScIsDeleted() {
        createAgentByIdDeletedArgs(true, false)
        def sc = new SystemAgentSC()
        sc.isDeleted = false

        systemAgentService.get(sc).forEach {
            assertTrue(it.isDeleted == sc.isDeleted)
        }

        sc.isDeleted = true
        systemAgentService.get(sc).forEach {
            assertTrue(it.isDeleted == sc.isDeleted)
        }
    }

    /* Получение агентов для отправки сообщений */
    @Test
    void testSystemAgentScIsSendAndGetMessages() {
        createAgentBySendAndGetMessagesArgs(true, false)
        def sc = new SystemAgentSC()
        sc.isSendAndGetMessages = false

        systemAgentService.get(sc).forEach {
            assertTrue(it.isSendAndGetMessages == sc.isSendAndGetMessages)
        }

        sc.isSendAndGetMessages = true
        systemAgentService.get(sc).forEach {
            assertTrue(it.isSendAndGetMessages == sc.isSendAndGetMessages)
        }
    }

    /* Получение агента по логину в сервисе */
    @Test
    void testGetSystemAgentByServiceName() {
        def agent = systemAgentService.getByServiceLogin(serviceLogin)

        assertTrue(agent.serviceLogin == serviceLogin)
    }

    /* Нельзя создать двух агентов с одинаковый service_login */
    @Test(expected = UncategorizedSQLException.class)
    void testCreateTwoAgentWithOneServiceName() {
        def systemAgent = new SystemAgent(
                serviceLogin,
                servicePassword,
                isSendAndGetMessages
        )
        systemAgentService.save(systemAgent)
    }

    /* Проверка существования агента */
    @Test()
    void testIsExistsAgent() {
        assertTrue(systemAgentService.isExistsAgent(serviceLogin))
        assertFalse(systemAgentService.isExistsAgent(UUID.randomUUID().toString()))
    }

    /**
     * Сравнение двух dsl файлов на равенство
     */
    private def assertDslFiles(DslFileAttachment expected, DslFileAttachment actual) {
        assertNotNull(actual)
        assertEquals(expected.filename, actual.filename)
        assertEquals(expected.fileSize, actual.fileSize)
        assertEquals(expected.fileSize, actual.fileSize)

        def expectedData = expected.contentAsByteArray(fileContentLocator)
        def actualData = actual.contentAsByteArray(fileContentLocator)
        for (i in 0..expectedData.length - 1) {
            assertEquals(expectedData[i], actualData[i])
        }
    }

    private SystemAgent createAgent(Boolean isDeleted, Boolean isSendAndGetMessages) {
        def systemAgent = new SystemAgent(
                StringObjects.randomString(),
                StringObjects.randomString(),
                isSendAndGetMessages
        )
        systemAgent.isDeleted = isDeleted

        return systemAgentService.get(systemAgentService.save(systemAgent))
    }

    private def createAgentByIdDeletedArgs(Boolean... isDeletedArgs) {
        isDeletedArgs.each {
            createAgent(it, true)
        }
    }

    private def createAgentBySendAndGetMessagesArgs(Boolean... isSendAngGetMessagesArgs) {
        isSendAngGetMessagesArgs.each {
            createAgent(false, it)
        }
    }
}
