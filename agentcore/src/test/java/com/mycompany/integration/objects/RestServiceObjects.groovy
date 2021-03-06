package com.mycompany.integration.objects

import objects.StringObjects
import com.mycompany.service.objects.*

/**
 * @author Nikita Gorodilov
 */
class RestServiceObjects {

    static def registrationData(String password) {
        new RegistrationData(
                StringObjects.randomString(),
                StringObjects.emptyString(),
                IntegrationTypesObjects.testAgentType1().code,
                password
        )
    }

    static def loginData(RegistrationData registrationData) {
        new LoginData(
                registrationData.masId,
                registrationData.password
        )
    }

    static def loginData(String masId, String password) {
        new LoginData(masId, password)
    }

    static def getAgentsData() {
        new GetAgentsData()
    }

    static def getAgentsDataWithType(String type) {
        new GetAgentsData(type, null, null)
    }

    static def getAgentsDataWithName(String name) {
        new GetAgentsData(null, null, name)
    }

    static def getAgentsDataWithIsDeleted(boolean isDeleted) {
        new GetAgentsData(null, isDeleted, null)
    }

    static def getMessageData() {
        new GetMessagesData()
    }

    static def getMessageData(Long senderId, Boolean isViewed) {
        new GetMessagesData(
                null,
                null,
                null,
                senderId,
                isViewed,
                null,
                null
        )
    }

    static def sendMessageData(String messageType, List<Long> recipientsIds, String messageBodyType, String messageBody) {
        new SendMessageData(
                messageType,
                recipientsIds,
                messageBodyType,
                messageBody
        )
    }

    static def randomDataSendMessageData() {
        new SendMessageData(
                StringObjects.randomString(),
                Collections.emptyList(),
                StringObjects.randomString(),
                StringObjects.randomString()
        )
    }
}
