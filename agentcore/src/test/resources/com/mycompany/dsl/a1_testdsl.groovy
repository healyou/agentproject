/**
 * Работает с данными MockObjects - TypesObjects класса
 */

init = {
    type = TEST_AGENT_TYPE_1_AT
    name = "a1_testdsl"
    masId = "a1_testdsl"
    defaultBodyType = JSON_MBT
}

onGetMessage = { message ->
    executeCondition ("Если пришло сообщение от 2 агента") {
        condition {
            message.senderType == TEST_AGENT_TYPE_2_AT
        }
        execute {
            a1_testOnGetMessageFun()
        }
    }
}

onLoadImage = { image ->
    executeCondition ("Выполним функцию над изображением") {
        condition {
            image != null
        }
        execute {
            sendMessage messageType: "search_solution",
                    image: image,
                    agentTypes: ["worker", "server"]
            a1_testOnLoadImageFun()
        }
    }
}

onEndImageTask = { updateImage ->
    executeCondition ("Выполним функцию над изображением") {
        condition {
            updateImage != null
        }
        execute {
            a1_testOnEndImageTaskFun()
        }
    }
}