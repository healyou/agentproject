package agentfoundation.agentcomandanalizer;

import agentcommunication.AgentCommunicationImpl;
import agentcommunication.IAgentCommunication;
import agentcommunication.message.AMessage;
import agentcommunication.message.MCollectiveSolution;
import agentcommunication.message.MSearchSolution;
import agentfoundation.agentbrain.IAgentBrain;
import agentfoundation.localdatabase.AgentDatabaseImpl;
import database.dao.LocalDataDao;
import database.dto.DtoEntityImpl;
import inputdata.inputdataverification.inputdata.InputDataTableDesc;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Observable;
import java.util.Observer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by user on 21.02.2017.
 */
public class ComAnalizerImpl implements IComAnalizer, Observer {

    private AgentCommunicationImpl mAgentCom;
    private InputDataTableDesc mTableDesc;
    private AgentDatabaseImpl mAgentDatabase;

    public ComAnalizerImpl(InputDataTableDesc tableDesc, AgentCommunicationImpl agentCom,
                           AgentDatabaseImpl agentDatabase) {
        this.mTableDesc = tableDesc;
        this.mAgentCom = agentCom;
        this.mAgentDatabase = agentDatabase;
    }

    /**
     * Получение выходного сигнала с мозга агента
     * @param o от кого получен(AgentCommunicationImpl or AgentBrainImpl)
     * @param arg аргемент(DtoEntityImpl)
     */
    @Override
    public void update(Observable o, Object arg) {
        System.out.println("Пришло сообщение в ComAnalizerImpl" +
            o.getClass() + " " + arg.getClass());

        if (o instanceof IAgentCommunication && arg instanceof AMessage)
            updateAgentCommunication((AMessage) arg);
        if (o instanceof IAgentBrain && arg instanceof DtoEntityImpl)
            updateAgentOutput((DtoEntityImpl) arg);
    }

    /**
     * Сигнал пришёл от модуля вз-ия с сервером
     * сохраняем данные в локальной бд
     * @param message данные с сервера
     */
    private void updateAgentCommunication(AMessage message) {
        if (message instanceof MSearchSolution) {
            try {
                MSearchSolution solution = (MSearchSolution) message;
                DtoEntityImpl entity = solution.getDtoEntity();
                if (entity != null)
                    mAgentDatabase.updateSolution(entity);
            } catch (SQLException e) {
                System.out.println(e.toString() + " ошибка обновления данных локальной бд");
            }
        }

        if (message instanceof MCollectiveSolution) {
            // ищем своё решение по входным данным
            DtoEntityImpl dtoEntity = ((MCollectiveSolution) message).getDtoEntity();
            //updateSolution(dtoEntity);
            sendComMassage(new MCollectiveSolution(dtoEntity,
                    ((MCollectiveSolution) message).getSolutionId()));
        }
    }

    /**
     * Агент вносит свой ответ в collective_answer и отправляет на сервер
     * @param dtoEntity данные, которые надо пересмотреть агенту
     */
    private void updateSolution(DtoEntityImpl dtoEntity) {
        throw new UnsupportedOperationException("Операция обновления данных для коллективного решения не поддерживается");
    }

    /**
     * Сигнал пришёл от мозга агента
     * @param entity данные решения мозга агента
     */
    private void updateAgentOutput(DtoEntityImpl entity) {
        if (isSendComMessage(entity))
            sendComMassage(new MSearchSolution(entity));
    }

    /**
     * Проерка необходимости отправки сообщения на сервак
     * @param entity данные для отправки
     * @return true - отправляем за помощью - найдём общее решение
     */
    private boolean isSendComMessage(DtoEntityImpl entity) {
        // находим выходное значение и проверяем его с регулярным выражением
        Object value = entity.getValueByColumnName(AgentDatabaseImpl.ANSWER_COLUMN_NAME);

        Pattern pattern = mTableDesc.getComRegExp();
        Matcher matcher = pattern.matcher(value.toString());

        return !matcher.matches();
    }

    /**
     * отправка сигнала на модуль вз-ия с сервером
     */
    private void sendComMassage(AMessage message) {
        if (!mAgentCom.isConnect())
            return;

        try {
            mAgentCom.sendMassege(message);
        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }

}
