import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class GetConnection {
	private static Connection c = null;
	private static final String URL = "jdbc:mysql://localhost:3306/hospital";
	private static final String USER = "admin";
	private static final String PASSWORD = "admin123$";

	private GetConnection() {
		// Private constructor to prevent instantiation
	}

	public static synchronized Connection getConnection() {
		if (c == null) {
			try {
				// Load the JDBC driver (optional for some modern versions)
				Class.forName("com.mysql.cj.jdbc.Driver");
				c = DriverManager.getConnection(URL, USER, PASSWORD);
			} catch (ClassNotFoundException e) {
				System.err.println("JDBC Driver not found: " + e.getMessage());
			} catch (SQLException e) {
				System.err.println("Database connection error: " + e.getMessage());
			}
		}
		return c;
	}

	public static void closeConnection() {
		if (c != null) {
			try {
				c.close();
				c = null;
			} catch (SQLException e) {
				System.err.println("Error closing connection: " + e.getMessage());
			}
		}
	}
}
