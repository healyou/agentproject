/**
 * Тестовый агент 1
 * При загрузке изображения
 * - обрабатывает изображение
 * - отправляет сообщение другому агенту(Тестовый агент 2)
 * При получении сообщения от Тестового агента 2
 * - проверка и конец
 */

init = {
    type = INTEGRATION_TEST_AGENT_TYPE_1_AT
    name = "Тестовый агент 1"
    masId = "integration_test_agent_1_masId"
    defaultBodyType = JSON_MBT
}

onLoadImage = { image ->
    executeCondition ("Обновим изображение") {
        execute {
            println "1) Работа над загруженным изображением первым тестовым агентов"
            testUpdateImageWithSleep image: image, sleep: 3000L
        }
    }
}

onEndImageTask = { updateImage ->
    executeCondition ("Отправим сообщение второму тестовому агенту") {
        execute {
            println "2) Работы над изображением закончена. Отправка сообщения второму тестовому агенту первым тестовым агентов"
            sendMessage messageType: INTEGRATION_TEST_MESSAGE_TYPE_1_TEST_GOAL_1_MT,
                    image: updateImage,
                    agentTypes: [INTEGRATION_TEST_AGENT_TYPE_2_AT]
        }
    }
}

onGetMessage = { message ->
    executeCondition ("Если пришло сообщение от второго серверного агента") {
        condition {
            message.senderType == INTEGRATION_TEST_AGENT_TYPE_2_AT
        }
        execute {
            println "5) Получение сообщения с сервиса от второго тестового агента первым тестовым агентом. Конец работы"
        }
    }
}