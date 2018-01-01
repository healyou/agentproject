package objects.initdbobjects

import com.mycompany.db.core.systemagent.SystemAgent
import testbase.CreateDatabaseDataDao

/**
 * @author Nikita Gorodilov
 */
class AgentObjects {

    companion object {
        @JvmStatic
        fun testAgentWithOneDslAttachment(): SystemAgent {
            return CreateDatabaseDataDao.testAgentWithOneDslAttachment!!
        }

        @JvmStatic
        fun testAgentWithManyDslAttachment(): SystemAgent {
            return CreateDatabaseDataDao.testAgentWithManyDslAttachment!!
        }

        @JvmStatic
        fun testAgentWithoutDslAttachment(): SystemAgent {
            return CreateDatabaseDataDao.testAgentWithoutDslAttachment!!
        }
    }
}