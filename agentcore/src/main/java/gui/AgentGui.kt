package gui

import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage
import org.springframework.context.support.ClassPathXmlApplicationContext
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler

/**
 * Отображения списка сообщений для выбраноого агента
 *
 * @author Nikita Gorodilov
 */
class AgentGui : Application() {

    companion object {
        val applicationContext = ClassPathXmlApplicationContext("context.xml")

        @JvmStatic
        fun main(args: Array<String>) {
            launch(AgentGui::class.java)
        }
    }

    override fun start(primaryStage: Stage?) {
        val loader = applicationContext.getBean(AgentSpringFxmlLoader::class.java)
        val root = loader.load(javaClass.getResourceAsStream("login.fxml"))
        primaryStage?.title = "Авторизация"
        primaryStage?.scene = Scene(root, 800.0, 600.0)
        primaryStage?.show()
    }

    override fun stop() {
        super.stop()
        val executor = applicationContext.getBean(ThreadPoolTaskExecutor::class.java)
        val scheduler = applicationContext.getBean(ThreadPoolTaskScheduler::class.java)
        scheduler.shutdown()
        executor.shutdown()
    }
}