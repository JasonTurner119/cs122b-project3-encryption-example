import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.jasypt.util.password.PasswordEncryptor;
import org.jasypt.util.password.StrongPasswordEncryptor;

// To be run only once to convert plaintext passwords to encrypted passwords.

public class UpdateSecurePassword {
    
    private static final String loginUser = "mytestuser";
    private static final String loginPasswd = "My6$Password";
    private static final String loginUrl = "jdbc:mysql://localhost:3306/moviedb";
    
    public static void main(String[] args) throws Exception {
        
        Class.forName("com.mysql.jdbc.Driver").newInstance();
        Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
        Statement statement = connection.createStatement();
        
        String alterQuery = "ALTER TABLE customers MODIFY COLUMN password VARCHAR(128)";
        int alterResult = statement.executeUpdate(alterQuery);
        System.out.println("altering customers table schema completed, " + alterResult + " rows affected");
        
        String query = "SELECT id, password from customers";
        
        System.out.println("Retrieving Old Passwords.");
        ResultSet resultSet = statement.executeQuery(query);
        
        PasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
        ArrayList<String> updateQueryList = new ArrayList<>();
        
        System.out.println("Encrypting Passwords.");
        while (resultSet.next()) {
            String id = resultSet.getString("id");
            String password = resultSet.getString("password");
            
            String encryptedPassword = passwordEncryptor.encryptPassword(password);
            
            String updateQuery = String.format(
                "UPDATE customers SET password = '%s' WHERE id = %s;",
                encryptedPassword,
                id
            );
            updateQueryList.add(updateQuery);
        }
        
        System.out.println("Setting New Passwords.");
        int count = 0;
        for (String updateQuery : updateQueryList) {
            int updateResult = statement.executeUpdate(updateQuery);
            count += updateResult;
        }
        System.out.println("Finished. " + count + " Rows Affected.");
        
        statement.close();
        connection.close();
        
    }
    
}