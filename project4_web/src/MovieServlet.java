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
        String[] title_arr=Title.split(" ");
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
        	/*
            if(Title != "") {
            	searchStr= "SELECT * FROM ("+baseSelect+") q WHERE q.title like ?";
            	//Full Text Search
            }
            else if(Year != "") {
            	searchStr= "SELECT * FROM ("+baseSelect+") q WHERE q.year like ?";
            }
            else if(Director != "") {
            	searchStr= "SELECT * FROM ("+baseSelect+") q WHERE q.director like ?";
            }
            
            else if(Star_name != "") {
            	searchStr= "SELECT q.id, q.title, q.year, q.director, q.rating FROM ("+baseSelect+") q JOIN `stars_in_movies` sim ON q.id=sim.movieId JOIN `stars` s ON s.id=sim.starId WHERE s.name like ?";
            }*/
        	searchStr=baseSelect;
        	if(!Title.equals("") && !Title.equals("null")) {
            	searchStr= "SELECT * FROM ("+searchStr+") q WHERE q.title like ?";
            	//Full Text Search
        		//searchStr="SELECT * FROM movies WHERE MATCH (title) AGAINST (?)";
            }
            if(!Year.equals("") && !Year.equals("null")) {
            	searchStr= "SELECT * FROM ("+searchStr+") q WHERE q.year like ?";
            }
            if(!Director.equals("") && !Director.equals("null")) {
            	searchStr= "SELECT * FROM ("+searchStr+") q WHERE q.director like ?";
            }
            if(!Star_name.equals("") && !Star_name.equals("null")) {
            	System.out.println("Star name");
            	System.out.println(Star_name);
            	searchStr= "SELECT q.id, q.title, q.year, q.director, q.rating FROM ("+searchStr+") q JOIN `stars_in_movies` sim ON q.id=sim.movieId JOIN `stars` s ON s.id=sim.starId WHERE s.name like ?";
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
        System.out.println("Search result");
        System.out.println(searchStr);
//        System.out.println(qSize);
        try {
            // Get a connection from dataSource
            Connection dbcon = dataSource.getConnection();
            
            dbcon.setAutoCommit(false);
            searchStatement = dbcon.prepareStatement(searchStr);            
            sizeStatement = dbcon.prepareStatement(qSize);
            
            
            // Prepare the statement
            //Search by genre.
            int cnt=1; // Count the number of advanced search.
            if(genre.length() > 1) {
            	searchStatement.setString(cnt, genre);
            	sizeStatement.setString(cnt, genre);
            	cnt++;
            }
            //Search by firt character.
            else if(genre.length() == 1) {
            	searchStatement.setString(cnt, Character.toString(genre.charAt(0)) + "%");
            	sizeStatement.setString(cnt, Character.toString(genre.charAt(0)) + "%");
            	cnt++;
            }
            
            //Advanced search.
            else {
            	/*
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
                }*/
            	
            	if(!Title.equals("") && !Title.equals("null")) {
            		
                	searchStatement.setString(cnt, "%" + Title + "%");
                	sizeStatement.setString(cnt, "%" + Title + "%");
            		/*
            		String q="";
            		for(int i=0; i<title_arr.length; i++) {
            			q+=(title_arr[i]+"* ");
            		}
            		searchStatement.setString(cnt, q);
            		sizeStatement.setString(cnt, q);*/
                	cnt++;
                }

                if(!Year.equals("") && !Year.equals("null")) {
                	//System.out.println("process year");
                	searchStatement.setString(cnt, "%" + Year + "%");
                	sizeStatement.setString(cnt, "%" + Year + "%");
                	cnt++;
                }

                if(!Director.equals("") && !Director.equals("null")) {
                	searchStatement.setString(cnt, "%" + Director + "%");
                	sizeStatement.setString(cnt, "%" + Director + "%");
                	cnt++;
                }

                if(!Star_name.equals("") && !Star_name.equals("null")) {
                	searchStatement.setString(cnt, "%" + Star_name + "%");
                	sizeStatement.setString(cnt, "%" + Star_name + "%");
                	cnt++;
                }
                
            }
            System.out.println("search statement");
            System.out.println(searchStatement);
            
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
            
            // set limit and offset
            // if offset is negative, show all result
            if(offset < 0) {
            	/*
            	searchStatement.setInt(2, Integer.parseInt(movieSize));
            	searchStatement.setInt(3, 0);*/
            	searchStatement.setInt(cnt++, Integer.parseInt(movieSize));
            	searchStatement.setInt(cnt++, 0);
            }
            else {/*
                searchStatement.setInt(2, numRecord);
                searchStatement.setInt(3, offset);*/
            	searchStatement.setInt(cnt++, numRecord);
                searchStatement.setInt(cnt++, offset);
            }
            
            // Perform the query
            System.out.println("execute query");
            System.out.println(searchStatement);
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
            	
            	System.out.println(movie_title);
            	
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