package com.mycompany.dsl.base.behavior

import com.mycompany.db.core.systemagent.SystemAgentEventHistory
import com.mycompany.db.core.systemagent.SystemAgentEventHistoryService
import com.mycompany.dsl.RuntimeAgent
import com.mycompany.dsl.base.SystemEvent
import com.mycompany.dsl.objects.DslLocalMessage
import com.mycompany.dsl.objects.DslServiceMessage
import com.mycompany.dsl.objects.DslTaskData

/**
 * Сохранение истории действий агента
 *
 * @author Nikita Gorodilov
 */
open class RuntimeAgentHistoryEventBehavior(private val historyService: SystemAgentEventHistoryService): ARuntimeAgentBehavior() {

    protected lateinit var runtimeAgent: RuntimeAgent

    override fun bind(runtimeAgent: RuntimeAgent) {
        super.bind(runtimeAgent)
        this.runtimeAgent = runtimeAgent
        onEvent("Добавление RuntimeAgentHistoryEventBehavior к агенту")
    }

    override fun unbind() {
        super.unbind()
        onEvent("Отсоединение RuntimeAgentHistoryEventBehavior от агента")
    }

    override fun onStart() {
        super.onStart()
        onEvent("Начало работы агента")
    }

    override fun onStop() {
        super.onStop()
        onEvent("Конец работы агента")
    }

    override fun beforeOnGetServiceMessage(serviceMessage: DslServiceMessage) {
        super.beforeOnGetServiceMessage(serviceMessage)
        onEvent("Начало dsl функции onGetServiceMessage")
    }

    override fun afterOnGetServiceMessage(serviceMessage: DslServiceMessage) {
        super.afterOnGetServiceMessage(serviceMessage)
        onEvent("Конец dsl функции onGetServiceMessage")
    }

    override fun beforeOnGetLocalMessage(localMessage: DslLocalMessage) {
        super.beforeOnGetLocalMessage(localMessage)
        onEvent("Начало dsl функции onGetLocalMessage")
    }

    override fun afterOnGetLocalMessage(localMessage: DslLocalMessage) {
        super.afterOnGetLocalMessage(localMessage)
        onEvent("Конец dsl функции onGetLocalMessage")
    }

    override fun beforeOnEndTask(taskData: DslTaskData) {
        super.beforeOnEndTask(taskData)
        onEvent("Начало dsl функции onEndTask")
    }

    override fun afterOnEndTask(taskData: DslTaskData) {
        super.afterOnEndTask(taskData)
        onEvent("Конец dsl функции onEndTask")
    }

    override fun beforeOnGetSystemEvent(systemEvent: SystemEvent) {
        super.beforeOnGetSystemEvent(systemEvent)
        onEvent("Начало dsl функции onGetSystemEvent")
    }

    override fun afterOnGetSystemEvent(systemEvent: SystemEvent) {
        super.afterOnGetSystemEvent(systemEvent)
        onEvent("Конец dsl функции onGetSystemEvent")
    }

    open protected fun onEvent(message: String) {
        createSystemAgentHistory(message)
    }

    private fun createSystemAgentHistory(message: String) {
        historyService.create(
                SystemAgentEventHistory(
                        getSystemAgentId(),
                        message
                )
        )
    }

    private fun getSystemAgentId(): Long {
        return runtimeAgent.getSystemAgent().id!!
    }
}