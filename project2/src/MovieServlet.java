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
import java.sql.Statement;

// Declaring a WebServlet called StarsServlet, which maps to url "/api/stars"
@WebServlet(name = "StarsServlet", urlPatterns = "/api/movies")
public class MovieServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.xml
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;
    private String movieSize = "";

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("application/json"); // Response mime type
        
        // Get genre from url.
        String genre = request.getParameter("genre");
        String Title = request.getParameter("Title");
        String Year = request.getParameter("Year");
        String Director = request.getParameter("Director");
        String Star_name = request.getParameter("Star_name");
        String sort = request.getParameter("sort");

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();
        
        // parse request and count movie query offset
        int page = Integer.parseInt(request.getParameter("p"));  
        int numRecord_int = Integer.parseInt(request.getParameter("numRecord"));
        
        String offset = Integer.toString(page*numRecord_int);
        String numRecord = Integer.toString(numRecord_int);

        try {
            // Get a connection from dataSource
            Connection dbcon = dataSource.getConnection();

            // Declare our statement
            Statement statement = dbcon.createStatement();
                                                                
            String query="";
            String qSize="0";
                        
            // Get movies and rating.
            query="SELECT * FROM `movies` m JOIN `ratings` r ON m.id = r.movieId";
            
            //Search by genre.
            if(genre.length() > 1) {
            	query="SELECT q.id, q.title, q.year, q.director, q.rating FROM ("+query+") q JOIN `genres_in_movies` gim ON gim.movieId=q.id JOIN `genres` g ON g.id=gim.genreId WHERE g.name="+"'"+genre+"'";
            }
            //Search by firt character.
            else if(genre.length() == 1) {
            	query="SELECT * FROM ("+query+") q WHERE q.title like '"+genre.charAt(0)+"%'";
            }
            //Advanced search.
            else {
                if(Title != "") {
                	query="SELECT * FROM ("+query+") q WHERE q.title like '%"+Title+"%'";
                }
                else if(Year != "") {
                	query="SELECT * FROM ("+query+") q WHERE q.year like '%"+Year+"%'";
                }
                else if(Director != "") {
                	query="SELECT * FROM ("+query+") q WHERE q.director like '%"+Director+"%'";
                }
                
                else if(Star_name != "") {
                	query="SELECT q.id, q.title, q.year, q.director, q.rating FROM ("+query+") q JOIN `stars_in_movies` sim ON q.id=sim.movieId JOIN `stars` s ON s.id=sim.starId WHERE s.name like "+"'%"+Star_name+"%'";
                }
            }
            
            //Count the number of movies.
            qSize="SELECT COUNT(*) AS `cnt` FROM "+"("+ query +") AS n";
            
            if(sort.equals("title_up")) {
            	query="SELECT * FROM "+"("+query+") AS n ORDER BY n.title DESC";
            }
            else if(sort.equals("title_down")) {
            	query="SELECT * FROM "+"("+query+") AS n ORDER BY n.title ASC";
            }
            else if(sort.equals("rating_up")) {
            	query="SELECT * FROM "+"("+query+") AS n ORDER BY n.rating DESC";
            }
            else if(sort.equals("rating_down")) {
            	query="SELECT * FROM "+"("+query+") AS n ORDER BY n.rating ASC";
            }
            
            query="SELECT * FROM "+"("+query+") AS n LIMIT "+numRecord+" OFFSET "+offset;


            // Count total number of movies.
            ResultSet rsP = statement.executeQuery(qSize);
    		while (rsP.next()) {
    			movieSize = rsP.getString("cnt");
    		}
    		rsP.close();
     
        	JsonArray jsonArray = new JsonArray();
        	JsonObject jsonObjSz = new JsonObject();
        	jsonObjSz.addProperty("movieSize", movieSize);
            jsonArray.add(jsonObjSz);
            
            // Perform the query
            ResultSet rs = statement.executeQuery(query);
            
            // Iterate through each row of rs
            while (rs.next()) {         	
            	String movie_id = rs.getString("id");
            	String movie_title = rs.getString("title");

            	String movie_year = rs.getString("year");
            	String movie_director = rs.getString("director");
            	String genreList = "";
            	String stars_name = "";
            	String stars_id = "";
            	String movie_rating = rs.getString("rating");
            	
            	//Query list of genres.
            	String query_log = "SELECT GROUP_CONCAT(g.name) AS genreList FROM  `genres` g JOIN `genres_in_movies` gm ON gm.genreId = g.id AND gm.movieId ="+"'"+movie_id+"'";
            	Statement statement_log = dbcon.createStatement();
            	ResultSet rs_log = statement_log.executeQuery(query_log);
            	rs_log.next();
   
            	genreList=rs_log.getString("genreList");
            	
            	//Query list of stars.    	
            	String query_los = "SELECT * from movies as m, ratings as r, stars_in_movies as sim, stars as s where s.id = sim.starId and m.id = sim.movieId and r.movieId = m.id and m.id = "+"'"+movie_id+"'";
            	Statement statement_los = dbcon.createStatement();
            	ResultSet rs_los = statement_los.executeQuery(query_los);
            	while (rs_los.next()) {
            		stars_name+=(rs_los.getString("name")+",");
            		stars_id+=(rs_los.getString("starId")+",");
            	}
            	
            	JsonObject jsonObject = new JsonObject();
            	jsonObject.addProperty("movie_id", movie_id);
            	jsonObject.addProperty("movie_title", movie_title);
            	jsonObject.addProperty("movie_year", movie_year);
            	jsonObject.addProperty("movie_director", movie_director);
            	jsonObject.addProperty("genreList", genreList);
            	jsonObject.addProperty("stars_name", stars_name);
            	jsonObject.addProperty("stars_id", stars_id);
            	jsonObject.addProperty("movie_rating", movie_rating);
                jsonArray.add(jsonObject);
                
                rs_log.close();
                rs_los.close();
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