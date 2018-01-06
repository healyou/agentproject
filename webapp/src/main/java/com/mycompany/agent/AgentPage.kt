package com.mycompany.agent

import com.mycompany.AuthBasePage
import com.mycompany.BootstrapFeedbackPanel
import com.mycompany.agent.panels.DslFileUploadPanel
import com.mycompany.base.AjaxLambdaLink
import com.mycompany.db.core.systemagent.SystemAgent
import com.mycompany.db.core.systemagent.SystemAgentService
import com.mycompany.security.acceptor.AlwaysAcceptedPrincipalAcceptor
import com.mycompany.security.acceptor.PrincipalAcceptor
import com.mycompany.user.Authority
import org.apache.wicket.RestartResponseAtInterceptPageException
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink
import org.apache.wicket.markup.html.WebMarkupContainer
import org.apache.wicket.markup.html.basic.Label
import org.apache.wicket.markup.html.form.CheckBox
import org.apache.wicket.markup.html.form.Form
import org.apache.wicket.markup.html.form.TextField
import org.apache.wicket.model.CompoundPropertyModel
import org.apache.wicket.model.Model
import org.apache.wicket.request.mapper.parameter.PageParameters
import org.apache.wicket.spring.injection.annot.SpringBean
import org.apache.wicket.util.lang.Bytes
import org.apache.wicket.util.string.StringValueConversionException
import java.util.*

/**
 * Страница агента
 *
 * @author Nikita Gorodilov
 */
class AgentPage(parameters: PageParameters) : AuthBasePage(parameters) {

    companion object {
        val AGENT_ID_PARAMETER_NAME = "id"
    }

    private enum class PageMode {
        EDIT, VIEW
    }

    @SpringBean
    private lateinit var agentService: SystemAgentService

    private lateinit var feedback: BootstrapFeedbackPanel
    private lateinit var buttons: WebMarkupContainer
    private lateinit var form: Form<SystemAgent>

    private var agent: SystemAgent
    private var mode = PageMode.VIEW

    init {
        val idParameter = parameters.get(AGENT_ID_PARAMETER_NAME)

        if (!idParameter.isEmpty) {
            try {
                val agentId = idParameter.toLongObject()
                agent = agentService.getById(agentId)
            } catch (e: StringValueConversionException) {
                throw RestartResponseAtInterceptPageException(application.homePage)
            }
        } else {
            throw RestartResponseAtInterceptPageException(application.homePage)
        }
    }

    override fun getPrincipalAcceptor(): PrincipalAcceptor {
        return AlwaysAcceptedPrincipalAcceptor()
    }

    override fun onInitialize() {
        super.onInitialize()

        add(Label("agentInfoLabel", Model.of("Агент № ${agent.id}")))
        feedback = BootstrapFeedbackPanel("feedback")
        add(feedback)

        form = Form<SystemAgent>("form", CompoundPropertyModel(agent))
        add(form)
        form.isMultiPart = true
        form.fileMaxSize = Bytes.megabytes(1)
        form.maxSize = Bytes.megabytes(1.5)

        // agent summary panel
        form.add(object : TextField<String>("serviceLogin") {
            override fun onConfigure() {
                super.onConfigure()
                isEnabled = isEditMode()
            }
        })
        form.add(object : DslFileUploadPanel("dslFile") {
            override fun onConfigure() {
                super.onConfigure()
                isEnabled = isEditMode()
            }
        })
        form.add(TextField<String>("ownerId").setEnabled(false))
        form.add(TextField<String>("createUserId").setEnabled(false))
        form.add(TextField<Date>("createDate").setEnabled(false))
        form.add(TextField<Date>("updateDate").setEnabled(false))
        form.add(object : CheckBox("isDeleted") {
            override fun onConfigure() {
                super.onConfigure()
                isEnabled = isEditMode()
            }
        })
        form.add(object : CheckBox("isSendAndGetMessages") {
            override fun onConfigure() {
                super.onConfigure()
                isEnabled = isEditMode()
            }
        })
        form.add(Label("isDeletedLabel", Model.of(getString("isDeleted"))))
        form.add(Label("isSendAngGetMessagesLabel", Model.of(getString("isSendAndGetMessages"))))

        buttons = WebMarkupContainer("buttons")
        form.add(buttons.setOutputMarkupId(true))
        buttons.add(object : AjaxSubmitLink("save") {
            override fun onConfigure() {
                super.onConfigure()
                isVisible = isEditMode()
            }

            override fun onSubmit(target: AjaxRequestTarget, form: Form<*>) {
                super.onSubmit(target, form)
                saveButtonClick(target)
            }

            override fun onError(target: AjaxRequestTarget, form: Form<*>) {
                super.onError(target, form)
                target.add(feedback)
            }
        })
        buttons.add(object : AjaxLambdaLink<Any>("edit", this::editButtonClick) {
            override fun onConfigure() {
                super.onConfigure()
                isVisible = (isPrincipalHasAnyAuthority(Authority.EDIT_OWN_AGENT) && isOwnAgent(agent, agentService)/* todo редактирование всех агентов-привелегия */) && isViewMode()
            }
        })
        buttons.add(object : AjaxLambdaLink<Any>("cancel", this::cancelButtonClick) {
            override fun onConfigure() {
                super.onConfigure()
                isVisible = isEditMode()
            }
        })
    }

    private fun saveButtonClick(target: AjaxRequestTarget) {
        agentService.save(agent)
        agent = agentService.getById(agent.id!!)
        form.model = CompoundPropertyModel<SystemAgent>(agent)

        mode = PageMode.VIEW
        updatePage(target)
    }

    private fun editButtonClick(target: AjaxRequestTarget) {
        mode = PageMode.EDIT
        updatePage(target)
    }

    private fun cancelButtonClick(target: AjaxRequestTarget) {
        mode = PageMode.VIEW
        updatePage(target)
    }

    private fun updatePage(target: AjaxRequestTarget) {
        target.add(buttons)
        target.add(form)
    }

    private fun isEditMode():Boolean {
        return mode == PageMode.EDIT
    }

    private fun isViewMode():Boolean {
        return mode == PageMode.VIEW
    }
}