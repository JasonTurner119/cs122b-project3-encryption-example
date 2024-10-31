import java.sql.*;
import java.util.ArrayList;

import org.jasypt.util.password.PasswordEncryptor;
import org.jasypt.util.password.StrongPasswordEncryptor;

// To be run only once to convert plaintext passwords to encrypted passwords.

public class UpdateSecurePassword {
    
    private static final String loginUser = "mytestuser";
    private static final String loginPasswd = "My6$Password";
    private static final String databaseName = "moviedb";
    private static final String loginUrl = "jdbc:mysql://localhost:3306/" + databaseName;
    
    public static void main(String[] args) throws Exception {
        
        Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
        Statement statement = connection.createStatement();
        
        System.out.println("CUSTOMERS:");
        encryptPasswordsForTable(
            statement,
            "customers",
            "id",
            "password"
        );
        
        System.out.println("EMPLOYEES:");
        encryptPasswordsForTable(
            statement,
            "employees",
            "email",
            "password"
        );
        
    }
    
    private static void encryptPasswordsForTable(
        Statement statement,
        String tableName,
        String tableKeyName,
        String passwordFieldName
    ) throws SQLException {
        
        String checkQuery = String.format(
            "SELECT COLUMN_TYPE " +
            "FROM INFORMATION_SCHEMA.COLUMNS " +
            "WHERE TABLE_SCHEMA = '%s' " +
            "AND TABLE_NAME = '%s' " +
            "AND COLUMN_NAME = '%s'",
            databaseName,
            tableName,
            passwordFieldName
        );
        ResultSet checkResultSet = statement.executeQuery(checkQuery);
        checkResultSet.next();
        if (checkResultSet.getString("COLUMN_TYPE").equalsIgnoreCase("varchar(128)")) {
            String message = String.format(
                "It seems like %s's password column (%s) is already encrypted.",
                tableName,
                passwordFieldName
            );
            System.out.println(message);
            return;
        }
        
        String alterQuery = String.format(
            "ALTER TABLE %s MODIFY COLUMN %s VARCHAR(128)",
            tableName,
            passwordFieldName
        );
        int alterResult = statement.executeUpdate(alterQuery);
        System.out.println("Updated Schema: " + alterResult + " Rows Affected");
        
        String query = String.format(
            "SELECT %s, %s FROM %s",
            passwordFieldName,
            tableKeyName,
            tableName
        );
        
        System.out.println("Retrieving Old Passwords.");
        ResultSet resultSet = statement.executeQuery(query);
        
        PasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
        ArrayList<String> updateQueryList = new ArrayList<>();
        
        System.out.println("Encrypting Passwords.");
        while (resultSet.next()) {
            String key = resultSet.getString(tableKeyName);
            String password = resultSet.getString(passwordFieldName);
            
            String encryptedPassword = passwordEncryptor.encryptPassword(password);
            
            String updateQuery = String.format(
                "UPDATE %s SET %s = '%s' WHERE %s = '%s';",
                tableName,
                passwordFieldName,
                encryptedPassword,
                tableKeyName,
                key
            );
            updateQueryList.add(updateQuery);
        }
        
        System.out.println(updateQueryList);
        
        System.out.println("Setting New Passwords.");
        int count = 0;
        for (String updateQuery : updateQueryList) {
            int updateResult = statement.executeUpdate(updateQuery);
            count += updateResult;
        }
        System.out.println("Finished. " + count + " Rows Affected.");
        
    }
    
}