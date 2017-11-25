package dsl

import db.core.servicemessage.ServiceMessage
import service.objects.AgentType
import service.objects.MessageBodyType
import service.objects.MessageGoalType
import service.objects.MessageType

import java.awt.Image

/**
 * @author Nikita Gorodilov
 */
class RuntimeAgentService {

    def agentType = null
    def agentName = null
    def agentMasId = null

    List<AgentType> agentTypes = []
    List<MessageBodyType> messageBodyTypes = []
    List<MessageGoalType> messageGoalTypes = []
    List<MessageType> messageTypes = []

    boolean on_load_image_provided = false
    def onLoadImage = {}

    boolean on_get_message_provided = false
    def onGetMessage = {}

    boolean on_end_image_task_provided = false
    def onEndImageTask = {}

    boolean init_provided = false
    def init = {}

    void loadExecuteRules(path) {
        Binding binding = createLoadBindings()

        GroovyShell shell = new GroovyShell(binding)
        shell.evaluate(new File(String.valueOf(path)))

        checkLoadRules(binding)
    }

    Binding createLoadBindings() {
        Binding binding = new Binding()

        binding.init = init
        binding.onLoadImage = onLoadImage
        binding.onGetMessage = onGetMessage
        binding.onEndImageTask = onEndImageTask

        return binding
    }

    void checkLoadRules(Binding binding) {
        if (bindingFunctionCheck(binding)) {
            init = binding.init
            onLoadImage = binding.onLoadImage
            onGetMessage = binding.onGetMessage
            onEndImageTask = binding.onEndImageTask

            on_load_image_provided = true
            on_get_message_provided = true
            on_end_image_task_provided = true
            init_provided = true
        } else {
            throw new RuntimeException("Неправильная dsl")
        }
    }

    /* Проверка функций */
    boolean bindingFunctionCheck(Binding binding) {
        return binding.init != init && binding.onLoadImage != onLoadImage && binding.onGetMessage != onGetMessage && binding.onEndImageTask != onEndImageTask
    }

    void applyInit() {
        if (init_provided) {
            Binding binding = new Binding()
            prepareInitData(binding)
            binding.init = init

            /**
             * GroovyShell изначально delegate owner = this class
             * а надо script, потому что только через binding можно сохранить изменения в evaluate
             */
            GroovyShell shell = new GroovyShell(binding)
            shell.evaluate("init.delegate = this;init.resolveStrategy = Closure.DELEGATE_FIRST;init()")

            try {
                agentType = binding.type
                agentName = binding.name
                agentMasId = binding.masId
            } catch (ignored) {
                throw new RuntimeException("Нет данных для инициализации агента")
            }
            if (agentType.isEmpty() || agentName.isEmpty() || agentMasId.isEmpty()) {
                throw new RuntimeException("Нет данных для инициализации агента")
            }
            println("masId from groovy " + agentMasId)
        } else {
            throw new RuntimeException("Функция init не загружена")
        }
    }

    void applyOnLoadImage(Image image) {
        if (on_load_image_provided) {
            Binding binding = new Binding()

            prepareTypes(binding)
            prepareClosures(binding)

            binding.image = image

            GroovyShell shell = new GroovyShell(binding)
            shell.evaluate("onLoadImage.delegate = this;onLoadImage.resolveStrategy = Closure.DELEGATE_FIRST;onLoadImage(image)")
        } else {
            throw new RuntimeException("Функция on_load_image не загружена")
        }
    }

    void applyOnGetMessage(ServiceMessage serviceMessage) {
        if (on_get_message_provided) {
            Binding binding = new Binding()

            prepareTypes(binding)
            prepareClosures(binding)

            binding.serviceMessage = serviceMessage

            GroovyShell shell = new GroovyShell(binding)
            shell.evaluate("onGetMessage.delegate = this;onGetMessage.resolveStrategy = Closure.DELEGATE_FIRST;onGetMessage(serviceMessage)")
        } else {
            throw new RuntimeException("Функция on_get_message не загружена")
        }
    }

    void applyOnEndImageTask(Image updateImage) {
        if (on_end_image_task_provided) {
            Binding binding = new Binding()

            prepareTypes(binding)
            prepareClosures(binding)

            binding.updateImage = updateImage

            GroovyShell shell = new GroovyShell(binding)
            shell.evaluate("onEndImageTask.delegate = this;onEndImageTask.resolveStrategy = Closure.DELEGATE_FIRST;onEndImageTask(updateImage)")
        } else {
            throw new RuntimeException("Функция on_end_image_task не загружена")
        }
    }

    private void prepareClosures(Binding binding) {
        binding.init = init
        binding.onLoadImage = onLoadImage
        binding.onGetMessage = onGetMessage
        binding.onEndImageTask = onEndImageTask
        binding.sendMessage = { Map map ->
            def messageType = map.get("messageType")
            def image = map["image"]
            def agentTypes = map["agentTypes"]
            def bodyFormat = map["bodyFormat"]
            def messageGoalType = map["messageGoalType"]

            assert messageType != null && image != null && agentTypes != null
            if (bodyFormat == null) {
                bodyFormat = binding.JSON
            }
            if (messageGoalType == null) {
                messageGoalType = binding.TASK_DECISION
            }

            // TODO отправка сообщения
            println("execute sendMessage")
        }
        binding.executeCondition = { spec, closure ->
            closure.delegate = delegate
            binding.result = true
            binding.and = true
            closure()
        }
        binding.condition = { closure ->
            closure.delegate = delegate
            if (binding.and)
                binding.result = (closure() && binding.result)
            else
                binding.result = (closure() || binding.result)
        }
        binding.allOf = { closure ->
            closure.delegate = delegate
            def storeResult = binding.result
            def storeAnd = binding.and

            binding.result = true // Starting premise is true
            binding.and = true
            closure()

            if (storeAnd) {
                binding.result = (storeResult && binding.result)
            } else {
                binding.result = (storeResult || binding.result)
            }
            binding.and = storeAnd
        }
        binding.anyOf = { closure ->
            closure.delegate = delegate
            def storeResult = binding.result
            def storeAnd = binding.and

            binding.result = false // Starting premise is false
            binding.and = false
            closure()
            if (storeAnd) {
                binding.result = (storeResult && binding.result)
            } else {
                binding.result = (storeResult || binding.result)
            }
            binding.and = storeAnd
        }
        binding.execute = { closure ->
            closure.delegate = delegate

            if (binding.result)
                use(ImagesFunctions) {
                    closure()
                }
        }
    }

    private void prepareTypes(Binding binding) {
        /* Типы агентов */
        agentTypes.each {
            def code = it.getCode().code
            binding."${getAgentTypeVariableByCode(code)}" = code
        }

        /* Типы тела сообщения */
        messageBodyTypes.each {
            def code = it.getCode().code
            binding."${getMessageBodyTypeVariableByCode(code)}" = code
        }

        /* Типы целей общения */
        messageGoalTypes.each {
            def code = it.getCode().code
            binding."${getMessageGoalTypeVariableByCode(code)}" = code
        }

        /* Типы сообщений */
        messageTypes.each {
            def code = it.getCode().code
            binding."${getMessaTypeVariableByCode(code)}" = code
        }
    }

    /* Имена переменных словарей */
    public String getAgentTypeVariableByCode(String code) {
        "${code.toUpperCase()}_AT"
    }
    public String getMessaTypeVariableByCode(String code) {
        "${code.toUpperCase()}_MT"
    }
    public String getMessageGoalTypeVariableByCode(String code) {
        "${code.toUpperCase()}_MGT"
    }
    public String getMessageBodyTypeVariableByCode(String code) {
        "${code.toUpperCase()}_MBT"
    }

    /**
     * Параметры инициализации агента
     */
    private void prepareInitData(Binding binding) {
        binding.type = ""
        binding.name = ""
        binding.masId = ""
    }

    // TODO инициализация всех типов из массивов -  по хорошему сделать
}