import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// Declaring a WebServlet called SingleStarServlet, which maps to url "/api/single-star"
@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/single-movie")
public class SingleMovieServlet extends HttpServlet {
	private static final long serialVersionUID = 2L;

	// Create a dataSource which registered in web.xml
	@Resource(name = "jdbc/moviedb")
	private DataSource dataSource;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("application/json"); // Response mime type

		// Retrieve parameter id from url request.
		String id = request.getParameter("id");

		// Output stream to STDOUT
		PrintWriter out = response.getWriter();

		try {
			// Get a connection from dataSource
			Connection dbcon = dataSource.getConnection();

			// Construct a query with parameter represented by "?"
			String query = "SELECT * from movies as m, ratings as r, stars_in_movies as sim, stars as s where s.id = sim.starId and m.id = sim.movieId and r.movieId = m.id and m.id = ?";
			// String query2 = "SELECT GROUP_CONCAT(g.name) AS genreList FROM  `genres` g JOIN `genres_in_movies` gm ON gm.genresId = g.id AND gm.movieId = ?"; //Yueh
			String query2 = "SELECT GROUP_CONCAT(g.name) AS genreList FROM  `genres` g JOIN `genres_in_movies` gm ON gm.genreId = g.id AND gm.movieId = ?";
			
			// Declare our statement
			PreparedStatement statement = dbcon.prepareStatement(query);
			PreparedStatement statement2 = dbcon.prepareStatement(query2);

			// Set the parameter represented by "?" in the query to the id we get from url,
			// num 1 indicates the first "?" in the query
			statement.setString(1, id);
			statement2.setString(1, id);

			// Perform the query
			ResultSet rs = statement.executeQuery();
			ResultSet rs2 = statement2.executeQuery();

			JsonArray jsonArray = new JsonArray();

			// Iterate through each row of rs
			rs2.next();
			while (rs.next()) {

				String m_id = rs.getString("id");
				String m_title = rs.getString("title");
				String m_year = rs.getString("year");
				String m_director = rs.getString("director");
				//String m_ratings = rs.getString("ratings"); //Yueh
				String m_ratings = rs.getString("rating");
				String genreList = rs2.getString("genreList");
				String starId = rs.getString("starId");
				String starName = rs.getString("name");
				
				// Create a JsonObject based on the data we retrieve from rs
				JsonObject jsonObject = new JsonObject();
				jsonObject.addProperty("m_id", m_id);
				jsonObject.addProperty("m_title", m_title);
				jsonObject.addProperty("m_year", m_year);
				jsonObject.addProperty("m_director", m_director);
				jsonObject.addProperty("m_ratings", m_ratings);
				jsonObject.addProperty("genreList", genreList);
				jsonObject.addProperty("starId", starId);
				jsonObject.addProperty("starName", starName);

				jsonArray.add(jsonObject);
			}
			
			
            // write JSON string to output
            out.write(jsonArray.toString());
            // set response status to 200 (OK)
            response.setStatus(200);

			rs.close();
			statement.close();
			dbcon.close();
		} catch (Exception e) {
			// write error message JSON object to output
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("errorMessage", e.getMessage());
			out.write(jsonObject.toString());

			// set reponse status to 500 (Internal Server Error)
			response.setStatus(500);
		}
		out.close();

	}

}
