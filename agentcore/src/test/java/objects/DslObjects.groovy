package objects

import com.mycompany.dsl.objects.DslTaskData

/**
 * @author Nikita Gorodilov
 */
class DslObjects {

    /**
     * Данные для тестирования a1, a2 dsl агентов в RuntimeAgentTest
     */
    static final def a1_testdslConditionEventName = "a1_lmt_event1"
    static final def a2_testdslConditionEventName = "a2_lmt_event1"
    static final def a1_testdslTaskType = "a1_tt1"
    static final def a2_testdslTaskType = "a2_tt1"
    static final def a1_testdslTaskData = new DslTaskData(a1_testdslTaskType)
    static final def a2_testdslTaskData = new DslTaskData(a2_testdslTaskType)

    static final def taskType = StringObjects.randomString()

    static final def notInitBlockDsl =
        """
            onGetServiceMessage = { serviceMessage -> }
            onGetLocalMessage = { localMessage -> }
            onEndTask = { taskData -> }
            onEndImageTask = { updateImage -> }
        """

    static def allBlocksDslWithTypeParameterInInitBlock(typeParameter) {
        """
            init = {
                type = $typeParameter
                name = "${StringObjects.randomString()}"
                masId = "${StringObjects.randomString()}"
                defaultBodyType = "${StringObjects.randomString()}"
                defaultGoalType = "${StringObjects.randomString()}"
                localMessageTypes = ${TypesObjects.localMessageTypesAsStringArray()}
                taskTypes = ${TypesObjects.taskTypesAsStringArray()}
            }
            onGetServiceMessage = { serviceMessage -> }
            onGetLocalMessage = { localMessage -> }
            onEndTask = { taskData -> }
            onEndImageTask = { updateImage -> }
        """
    }

    static def allBlocksDslWithInitParams(type, name, masId, bodyType, localMessageTypes, taskTypes) {
        """
            init = {
                type = "$type"
                name = "$name"
                masId = "$masId"
                defaultBodyType = "$bodyType"
                localMessageTypes = $localMessageTypes
                taskTypes = $taskTypes
            }
            onGetServiceMessage = { serviceMessage -> }
            onGetLocalMessage = { localMessage -> }
            onEndTask = { taskData -> }
            onEndImageTask = { updateImage -> }
        """
    }

    static final def allBlocksDslArray = [
            "${randomInitBlock()}",
            "onGetServiceMessage = { message -> }",
            "onGetLocalMessage = { localMessage -> }",
            "onEndTask = { taskData -> }",
            "onEndImageTask = { updateImage -> }"
    ]

    static final def allBlocksDsl = createAllBlocksDsl()
    private static final def createAllBlocksDsl() {
        def dsl = ""
        allBlocksDslArray.each {
            dsl += "$it\n "
        }
        dsl
    }

    static def createDslWithOnGetServiceMessageExecuteConditionBlock(executeConditionBlockBody) {
        """
            ${randomInitBlock()}
            onGetServiceMessage = {
                executeCondition ("BlockBody") {
        """ +
                executeConditionBlockBody +
                """
                }
            }
            onGetLocalMessage = { localMessage -> }
            onEndTask = { taskData -> }
            onEndImageTask = {}
        """
    }

    static def createDslWithOnGetServiceMessageBlock(executeConditionBlockBody) {
        """
            ${randomInitBlock()}
            onGetServiceMessage = {
                """ +
                executeConditionBlockBody +
                """
            }
            onGetLocalMessage = { localMessage -> }
            onEndTask = { taskData -> }
            onEndImageTask = {}
        """
    }

    static def createDslWithExecuteConditionBlocks(onGetServiceMessageBlock, onGetLocalMessageBlock, onEndTaskBlock,
                                                   onEndImageBlock) {
        """
            ${randomInitBlock()}
            onGetServiceMessage = {
                executeCondition ("BlockBody") {
                    """ +
                onGetServiceMessageBlock +
                """
                }
            }
            onGetLocalMessage = { localMessage -> 
                executeCondition ("BlockBody") {
                    """ +
                onGetLocalMessageBlock +
                """
                }
            }
            onEndTask = { taskData ->
                executeCondition ("BlockBody") {
                    """ +
                onEndTaskBlock +
                """
                }
            }
            onEndImageTask = {
                executeCondition ("BlockBody") {
                    """ +
                onEndImageBlock +
                """
                }
            }
        """
    }

    static def executeConditionDsl(condition, execute) {
        """
                ${randomInitBlock()}
                onGetServiceMessage = {
                    executeCondition ("Успешное выполнение функции") {
                        condition() {
                            $condition
                        }
                        execute() {
                            $execute
                        }
                    }
                }
                onGetLocalMessage = { localMessage -> }
                onEndTask = { taskData -> }
                onEndImageTask = {}
            """
    }

    static def randomInitBlock() {
        """
            init = {
                type = "${TypesObjects.testAgent1TypeCode()}"
                name = "${StringObjects.randomString()}"
                masId = "${StringObjects.randomString()}"
                defaultBodyType = "${StringObjects.randomString()}"
                localMessageTypes = ${TypesObjects.localMessageTypesAsStringArray()}
                taskTypes = ${TypesObjects.taskTypesAsStringArray()}
            }
        """
    }

    /**
     * Блоки dsl, где функция execute может выполнять и не выполняться
     * @param execute выполняемая функция
     * @return информация о выполнение функции
     */
    static def testDslConditionBlocksArray(execute) {
        [
                new TestDslConditionBlocks( // все блоки вернут да
                        rules: """
                        ${randomInitBlock()}
                        onGetServiceMessage = { message ->
                            executeCondition ("Успешное выполнение функции") {
                                anyOf {
                                    allOf {
                                        condition {
                                            true
                                        }
                                        condition {
                                            true
                                        }
                                    }
                                    condition {
                                        false
                                    }
                                }
                                execute {
                                    $execute
                                }
                            }
                        }
                        onGetLocalMessage = { localMessage -> }
                        onEndTask = { taskData -> }
                        onEndImageTask = {}
                    """,
                        expectedExecute: true
                ),
                new TestDslConditionBlocks( // блоки allOf вернёт нет
                        rules: """
                        ${randomInitBlock()}
                        onGetServiceMessage = { message ->
                            executeCondition ("Нет выполнение функции") {
                                anyOf {
                                    allOf {
                                        condition {
                                            true
                                        }
                                        condition {
                                            false
                                        }
                                    }
                                    condition {
                                        false
                                    }
                                }
                                execute {
                                    $execute
                                }
                            }
                        }
                        onGetLocalMessage = { localMessage -> }
                        onEndTask = { taskData -> }
                        onEndImageTask = {}
                    """,
                        expectedExecute: false
                ),
                new TestDslConditionBlocks( // блок вернёт нет
                        rules: """
                        ${randomInitBlock()}
                        onGetServiceMessage = { message ->
                            executeCondition ("Успешное выполнение функции") {
                                anyOf {
                                    allOf {
                                        condition {
                                            true
                                        }
                                        condition {
                                            true
                                        }
                                    }
                                    condition {
                                        false
                                    }
                                }
                                // по and объединяются
                                condition {
                                    false
                                }
                                execute {
                                    $execute
                                }
                            }
                        }
                        onGetLocalMessage = { localMessage -> }
                        onEndTask = { taskData -> }
                        onEndImageTask = {}
                    """,
                        expectedExecute: false
                ),
                new TestDslConditionBlocks(
                        rules: """
                        ${randomInitBlock()}
                        onGetServiceMessage = { message ->
                            executeCondition ("Успешное выполнение функции") {
                                execute {
                                    $execute
                                }
                            }
                        }
                        onGetLocalMessage = { localMessage -> }
                        onEndTask = { taskData -> }
                        onEndImageTask = {}
                    """,
                        expectedExecute: true
                ),
                new TestDslConditionBlocks(
                        rules: """
                        ${randomInitBlock()}
                        onGetServiceMessage = { message ->
                            executeCondition ("Успешное выполнение функции") {
                                condition {
                                    true
                                }
                                execute {
                                    $execute
                                }
                            }
                        }
                        onGetLocalMessage = { localMessage -> }
                        onEndTask = { taskData -> }
                        onEndImageTask = {}
                    """,
                        expectedExecute: true
                )
        ]
    }
    static class TestDslConditionBlocks {
        def rules
        def expectedExecute
    }
}
