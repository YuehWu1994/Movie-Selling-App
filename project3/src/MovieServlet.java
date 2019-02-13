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
        
        int offset = page*numRecord_int;
        int numRecord = numRecord_int;
        
        // prepared string
       	PreparedStatement searchStatement = null;
       	PreparedStatement sizeStatement = null;
        String baseSelect = "SELECT * FROM `movies` m LEFT JOIN `ratings` r ON m.id = r.movieId";
        String searchStr = "";
        
        // Get movies and rating.
        //query="SELECT * FROM `movies` m JOIN `ratings` r ON m.id = r.movieId";
        
        //Search by genre.
        if(genre.length() > 1) {
        	searchStr="SELECT q.id, q.title, q.year, q.director, q.rating FROM ("+ baseSelect +") q JOIN `genres_in_movies` gim ON gim.movieId=q.id JOIN `genres` g ON g.id=gim.genreId WHERE g.name=?";
        }
        //Search by firt character.
        else if(genre.length() == 1) {
        	searchStr= "SELECT * FROM ("+baseSelect+") q WHERE q.title like ?";
        	// searchStr= "SELECT * FROM ("+baseSelect+") q WHERE q.title like ?" + "%";
        }
        //Advanced search.
        else {
            if(Title != "") {
            	searchStr= "SELECT * FROM ("+baseSelect+") q WHERE q.title like ?";
            }
            else if(Year != "") {
            	searchStr= "SELECT * FROM ("+baseSelect+") q WHERE q.year like ?";
            }
            else if(Director != "") {
            	searchStr= "SELECT * FROM ("+baseSelect+") q WHERE q.director like ?";
            }
            
            else if(Star_name != "") {
            	searchStr= "SELECT q.id, q.title, q.year, q.director, q.rating FROM ("+baseSelect+") q JOIN `stars_in_movies` sim ON q.id=sim.movieId JOIN `stars` s ON s.id=sim.starId WHERE s.name like ?";
            }
        }
        
        //Count the number of movies.
        String qSize="SELECT COUNT(*) AS `cnt` FROM "+"("+ searchStr +") AS n";
        
        if(sort.equals("title_up")) {
        	searchStr="SELECT * FROM "+"("+searchStr+") AS n ORDER BY n.title DESC";
        }
        else if(sort.equals("title_down")) {
        	searchStr="SELECT * FROM "+"("+searchStr+") AS n ORDER BY n.title ASC";
        }
        else if(sort.equals("rating_up")) {
        	searchStr="SELECT * FROM "+"("+searchStr+") AS n ORDER BY n.rating DESC";
        }
        else if(sort.equals("rating_down")) {
        	searchStr="SELECT * FROM "+"("+searchStr+") AS n ORDER BY n.rating ASC";
        }
        
        searchStr="SELECT * FROM "+"("+searchStr+") AS n LIMIT ? OFFSET ?";        
//        System.out.println(searchStr);
//        System.out.println(qSize);
        try {
            // Get a connection from dataSource
            Connection dbcon = dataSource.getConnection();
            
            dbcon.setAutoCommit(false);
            searchStatement = dbcon.prepareStatement(searchStr);            
            sizeStatement = dbcon.prepareStatement(qSize);
            
            // Prepare the statement
            //Search by genre.
            if(genre.length() > 1) {
            	searchStatement.setString(1, genre);
            	sizeStatement.setString(1, genre);
            }
            //Search by firt character.
            else if(genre.length() == 1) {
            	searchStatement.setString(1, Character.toString(genre.charAt(0)) + "%");
            	sizeStatement.setString(1, Character.toString(genre.charAt(0)) + "%");
            }
            //Advanced search.
            else {
                if(Title != "") {
                	searchStatement.setString(1, "%" + Title + "%");
                	sizeStatement.setString(1, "%" + Title + "%");
                }
                else if(Year != "") {
                	searchStatement.setString(1, "%" + Year + "%");
                	sizeStatement.setString(1, "%" + Year + "%");
                }
                else if(Director != "") {
                	searchStatement.setString(1, "%" + Director + "%");
                	sizeStatement.setString(1, "%" + Director + "%");
                }
                else if(Star_name != "") {
                	searchStatement.setString(1, "%" + Star_name + "%");
                	sizeStatement.setString(1, "%" + Star_name + "%");
                }
            }
            
            // set limit and offset
            searchStatement.setInt(2, numRecord);
            searchStatement.setInt(3, offset);
           
            
            // Count total number of movies.
            ResultSet rsP = sizeStatement.executeQuery();
            dbcon.commit();
    		while (rsP.next()) {
    			movieSize = rsP.getString("cnt");
    		}
    		rsP.close();
    		sizeStatement.close();
     
        	JsonArray jsonArray = new JsonArray();
        	JsonObject jsonObjSz = new JsonObject();
        	jsonObjSz.addProperty("movieSize", movieSize);
            jsonArray.add(jsonObjSz);
            
            // Perform the query
            ResultSet rs = searchStatement.executeQuery();
            dbcon.commit();
            
            // prepare string
            PreparedStatement genreStatement = null;
            String genStr = "SELECT GROUP_CONCAT(g.name) AS genreList FROM  `genres` g JOIN `genres_in_movies` gm ON gm.genreId = g.id AND gm.movieId =?";
            PreparedStatement starStatement = null;
            String starStr = "SELECT * from movies as m, stars_in_movies as sim, stars as s where m.id =? and s.id = sim.starId and m.id = sim.movieId";
            
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
            	genreStatement = dbcon.prepareStatement(genStr);
            	genreStatement.setString(1, movie_id);
                ResultSet rs_log = genreStatement.executeQuery();
            	rs_log.next();
    	
            	//Query list of stars.    	
            	starStatement = dbcon.prepareStatement(starStr);
            	starStatement.setString(1, movie_id);
            	
            	ResultSet rs_los = starStatement.executeQuery();
            	while (rs_los.next()) {
            		stars_name+=(rs_los.getString("name")+",");
            		stars_id+=(rs_los.getString("starId")+",");
            	}
            	
            	genreList=rs_log.getString("genreList");
            	
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
            searchStatement.close();
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