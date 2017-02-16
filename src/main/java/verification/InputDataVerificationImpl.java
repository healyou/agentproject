package verification;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import verification.base.InputDataVerification;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Created by user on 16.02.2017.
 */
public class InputDataVerificationImpl implements InputDataVerification {

    @Override
    public JdbcTemplate checkDatabaseConnection(String filePath) throws Exception {
        DriverManagerDataSource ds = new DriverManagerDataSource();

        Properties dbprop = new Properties();
        try {
            dbprop.load(new FileInputStream(filePath));

            String driverClassName = dbprop.getProperty("driverClassName");
            String url = dbprop.getProperty("url");
            String username = dbprop.getProperty("username");
            String password = dbprop.getProperty("password");

            ds.setDriverClassName(driverClassName);
            ds.setUrl(url);
            ds.setUsername(username);
            ds.setPassword(password);
        } catch (IOException e) {
            throw new IOException(e);
        }

        return new JdbcTemplate(ds);
    }

    @Override
    public void checkDatabaseData(String filePath) {

    }

}
