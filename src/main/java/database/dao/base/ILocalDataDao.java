package database.dao.base;

import database.dto.base.ABaseDtoEntity;

import java.sql.SQLException;

/**
 * Created by user on 21.02.2017.
 */
public interface ILocalDataDao<T extends ABaseDtoEntity> {

    public abstract T get(int id) throws SQLException;
    public abstract T create() throws SQLException;
    public abstract void update(T entity) throws SQLException;

}
