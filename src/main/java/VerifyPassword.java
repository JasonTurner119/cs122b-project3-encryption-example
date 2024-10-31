import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.jasypt.util.password.StrongPasswordEncryptor;

public class VerifyPassword {
	
	private static final String loginUser = "mytestuser";
	private static final String loginPasswd = "My6$Password";
	private static final String loginUrl = "jdbc:mysql://localhost:3306/moviedb";
	
	public static void main(String[] args) throws Exception {
		System.out.println(verifyCredentials("a@email.com", "a2"));
		System.out.println(verifyCredentials("a@email.com", "a3"));
	}

	private static boolean verifyCredentials(String email, String password) throws Exception {
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
		Statement statement = connection.createStatement();

		String query = String.format("SELECT * FROM customers WHERE email = '%s'", email);

		ResultSet rs = statement.executeQuery(query);

		boolean success = false;
		if (rs.next()) {
			String encryptedPassword = rs.getString("password");
			success = new StrongPasswordEncryptor().checkPassword(password, encryptedPassword);
		}

		statement.close();
		connection.close();
		
		System.out.println("Verify: " + email + " : " + password);

		return success;
	}

}
