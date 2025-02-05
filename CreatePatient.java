import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class CreatePatient
 */
@WebServlet("/CreatePatient")
public class CreatePatient extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public CreatePatient() {
		super();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		String name = request.getParameter("name");
		String email = request.getParameter("email");
		String phone = request.getParameter("phone");
		String ageStr = request.getParameter("age");
		String gender = request.getParameter("gender");
		String blood = request.getParameter("blood");
		String visited = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		String symptom = request.getParameter("symptom");
		String disease = request.getParameter("disease");
		String doctorId = request.getParameter("doctor");

		if (name == null || email == null || phone == null || ageStr == null || doctorId == null) {
			out.println("<h1 align='center' style='color: red;'>Missing required fields. Please try again.</h1>");
			return;
		}

		int age = Integer.parseInt(ageStr);

		try (Connection c = GetConnection.getConnection()) {
			c.setAutoCommit(false); // Start transaction

			// Insert patient data
			String insertPatientSQL = "INSERT INTO patient (name, email, phone, age, gender, blood, visited, symptom, disease, doctor) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			try (PreparedStatement ps = c.prepareStatement(insertPatientSQL, PreparedStatement.RETURN_GENERATED_KEYS)) {
				ps.setString(1, name);
				ps.setString(2, email);
				ps.setString(3, phone);
				ps.setInt(4, age);
				ps.setString(5, gender);
				ps.setString(6, blood);
				ps.setString(7, visited);
				ps.setString(8, symptom);
				ps.setString(9, disease);
				ps.setInt(10, Integer.parseInt(doctorId));

				ps.executeUpdate();

				ResultSet rs = ps.getGeneratedKeys();
				if (!rs.next())
					throw new SQLException("Failed to retrieve patient ID.");
				int patientId = rs.getInt(1);

				// Update doctor's patient list
				String updateDoctorSQL = "UPDATE doctor SET patients = CONCAT(IFNULL(patients, ''), ?, ',') WHERE did = ?";
				try (PreparedStatement ps2 = c.prepareStatement(updateDoctorSQL)) {
					ps2.setString(1, String.valueOf(patientId));
					ps2.setInt(2, Integer.parseInt(doctorId));
					ps2.executeUpdate();
				}
			}

			c.commit(); // Commit transaction
			out.println("<h1 align='center' style='color: green;'>Patient created successfully!</h1>");

		} catch (Exception e) {
			e.printStackTrace();
			out.println("<h1 align='center' style='color: red;'>Error creating patient. Please try again.</h1>");
		}
	}
}
