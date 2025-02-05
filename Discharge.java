import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class Discharge
 */
@WebServlet("/Discharge")
public class Discharge extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public Discharge() {
		super();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		double total = 0;
		String pid = request.getParameter("pid");
		String daysStr = request.getParameter("days");
		String daycostStr = request.getParameter("daycost");
		String medData = request.getParameter("mc");

		if (pid == null || daysStr == null || daycostStr == null || medData == null) {
			out.println("<h3 style='color: red;'>Missing required fields. Please try again.</h3>");
			return;
		}

		int days = Integer.parseInt(daysStr);
		int dayCost = Integer.parseInt(daycostStr);
		String[] medicines = medData.split(";");

		try (Connection c = GetConnection.getConnection()) {
			c.setAutoCommit(false); // Begin transaction

			// Delete patient record
			String deletePatientSQL = "DELETE FROM patient WHERE pid = ?";
			try (PreparedStatement ps = c.prepareStatement(deletePatientSQL)) {
				ps.setInt(1, Integer.parseInt(pid));
				ps.executeUpdate();
			}

			// Calculate medicine costs
			String getMedicinePriceSQL = "SELECT price FROM medicine WHERE mid = ?";
			try (PreparedStatement ps = c.prepareStatement(getMedicinePriceSQL)) {
				for (String med : medicines) {
					String[] medDetails = med.split(",");
					String mid = medDetails[0];
					int count = Integer.parseInt(medDetails[1]);

					ps.setInt(1, Integer.parseInt(mid));
					try (ResultSet rs = ps.executeQuery()) {
						if (rs.next()) {
							double price = rs.getDouble("price");
							total += (price * count);
						} else {
							throw new SQLException("Invalid medicine ID: " + mid);
						}
					}
				}
			}

			// Add hospital stay costs
			total += (days * dayCost);

			// Commit transaction
			c.commit();

			// Display total cost
			out.println("<h1>Total Cost:</h1>");
			out.println("<h3>" + total + "</h3>");

		} catch (Exception e) {
			e.printStackTrace();
			out.println("<h3 style='color: red;'>An error occurred. Please try again.</h3>");
		}
	}
}
