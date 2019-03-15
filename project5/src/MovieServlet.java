import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import java.io.File;
import java.io.FileWriter;
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

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("application/json"); // Response mime type
        
        // Write a file from servlet.
     	String contextPath = getServletContext().getRealPath("/");
     	String xmlFilePath = contextPath+"movie_res";
     	System.out.println("xmlFilePath: "+ xmlFilePath);
     	File myfile = new File(xmlFilePath);
        myfile.createNewFile();
        
        // Get genre from url.
        String genre = request.getParameter("genre");
        String Title = request.getParameter("Title").replaceAll("\\s+", " ");
        String[] title_arr=Title.split(" ");
        String Year = request.getParameter("Year");
        String Director = request.getParameter("Director");
        String Star_name = request.getParameter("Star_name");
        String sort = request.getParameter("sort");
        String autocom=request.getParameter("autocom");

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();
        //System.out.println(Title);
        
        // parse request and count movie query offset
        String str_p=request.getParameter("p");
        String str_nR=request.getParameter("numRecord");

        int page=0, numRecord_int=20;
        if(str_p != null) page=Integer.parseInt(str_p); 
        if(str_nR != null) numRecord_int=Integer.parseInt(str_nR); 
        
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
        if(genre != null && genre.length() > 1) {
        	searchStr="SELECT q.id, q.title, q.year, q.director, q.rating FROM ("+ baseSelect +") q JOIN `genres_in_movies` gim ON gim.movieId=q.id JOIN `genres` g ON g.id=gim.genreId WHERE g.name=?";
        }
        //Search by firt character.
        else if(genre != null && genre.length() == 1) {
        	searchStr= "SELECT * FROM ("+baseSelect+") q WHERE q.title like ?";
        	// searchStr= "SELECT * FROM ("+baseSelect+") q WHERE q.title like ?" + "%";
        }
        //Advanced search.
        else {
        	searchStr=baseSelect;
        	if(Title != null && !Title.equals("") && !Title.equals("null")) {
        		if(autocom != null && autocom.equals("true")) {
        			//System.out.println("Perform normal search");
        			searchStr="SELECT * FROM ("+searchStr+") q WHERE q.title=?";
        		}
        		else {
        			//System.out.println("Perform full text search");
        			searchStr="SELECT * FROM ("+searchStr+") q WHERE MATCH (q.title) AGAINST (? IN BOOLEAN MODE)";
        		}
        		
        		// fuzzy search
        		String q= " or(";
        		for(int i = 0; i < title_arr.length; ++i) {
        			if(i != 0) q += " and ";
        			int fuzzy_thres = (title_arr[i].length()-1)/5;
        			q+= "(SELECT edrec('" + title_arr[i].toLowerCase() +"', q.title, " + Integer.toString(fuzzy_thres) + ")= 1)";
        		}
        		q += ")";
        		searchStr += q;
            }
            if(Year != null && !Year.equals("") && !Year.equals("null")) {
            	searchStr= "SELECT * FROM ("+searchStr+") q WHERE q.year like ?";
            }
            if(Director != null && !Director.equals("") && !Director.equals("null")) {
            	searchStr= "SELECT * FROM ("+searchStr+") q WHERE q.director like ?";
            }
            if(Star_name != null && !Star_name.equals("") && !Star_name.equals("null")) {
            	searchStr= "SELECT q.id, q.title, q.year, q.director, q.rating FROM ("+searchStr+") q JOIN `stars_in_movies` sim ON q.id=sim.movieId JOIN `stars` s ON s.id=sim.starId WHERE s.name like ?";
            }
        }
        
        
        //Count the number of movies.
        String qSize="SELECT COUNT(*) AS `cnt` FROM "+"("+ searchStr +") AS n";
        
        if(sort != null) {
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
        }
        
        searchStr="SELECT * FROM "+"("+searchStr+") AS n LIMIT ? OFFSET ?"; 

        try {
        	// Start time of query.
        	long startquery = System.nanoTime();
        	
            // Connection pooling and prepared statement.
//            Context initCtx = new InitialContext();
//
//            Context envCtx = (Context) initCtx.lookup("java:comp/env");
//            if (envCtx == null)
//            	response.getWriter().println("envCtx is NULL");
//
//            // Look up our data source
//            DataSource ds = (DataSource) envCtx.lookup("jdbc/moviedb");
//            if (ds == null)
//            	response.getWriter().println("ds is null.");
//
//            Connection dbcon = ds.getConnection();
//            if (dbcon == null)
//            	response.getWriter().println("dbcon is null.");        
        	
        	// Prepared Statement.
            Connection dbcon = dataSource.getConnection();
        	
            // Start time of JDBC.
            long startJDBC = System.nanoTime();
            
            dbcon.setAutoCommit(false);
            searchStatement = dbcon.prepareStatement(searchStr);            
            sizeStatement = dbcon.prepareStatement(qSize);
            
            // Prepare the statement
            //Search by genre.
            int cnt=1; // Count the number of advanced search.
            if(genre != null && genre.length() > 1) {
            	searchStatement.setString(cnt, genre);
            	sizeStatement.setString(cnt, genre);
            	cnt++;
            }
            //Search by firt character.
            else if(genre != null && genre.length() == 1) {
            	searchStatement.setString(cnt, Character.toString(genre.charAt(0)) + "%");
            	sizeStatement.setString(cnt, Character.toString(genre.charAt(0)) + "%");
            	cnt++;
            }
            
            //Advanced search.
            else {
            	if(Title != null && !Title.equals("") && !Title.equals("null")) {
            		String q="";
            		if(autocom != null && autocom.equals("true")) {
            			q=Title;
            		}
            		else {
            			//Full text search
                		for(String s : title_arr) {
                			q+=("+"+s+"* ");
                			//q+=(s+"* ");
                		}
            		}
            		searchStatement.setString(cnt, q);
            		sizeStatement.setString(cnt, q);
                	cnt++;
                }

                if(Year != null && !Year.equals("") && !Year.equals("null")) {
                	//System.out.println("process year");
                	searchStatement.setString(cnt, "%" + Year + "%");
                	sizeStatement.setString(cnt, "%" + Year + "%");
                	cnt++;
                }

                if(Director != null && !Director.equals("") && !Director.equals("null")) {
                	searchStatement.setString(cnt, "%" + Director + "%");
                	sizeStatement.setString(cnt, "%" + Director + "%");
                	cnt++;
                }

                if(Star_name != null && !Star_name.equals("") && !Star_name.equals("null")) {
                	searchStatement.setString(cnt, "%" + Star_name + "%");
                	sizeStatement.setString(cnt, "%" + Star_name + "%");
                	cnt++;
                }
                
            }
            
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
            	searchStatement.setInt(cnt++, Integer.parseInt(movieSize));
            	searchStatement.setInt(cnt++, 0);
            }
            else {
            	searchStatement.setInt(cnt++, numRecord);
                searchStatement.setInt(cnt++, offset);
            }
            
            // Perform the query
            ResultSet rs = searchStatement.executeQuery();
            dbcon.commit();
            //System.out.println("finished");
            // prepare string
            PreparedStatement genreStatement = null;
            String genStr = "SELECT GROUP_CONCAT(g.name) AS genreList FROM  `genres` g JOIN `genres_in_movies` gm ON gm.genreId = g.id AND gm.movieId =?";
            PreparedStatement starStatement = null;
            String starStr = "SELECT * from movies as m, stars_in_movies as sim, stars as s where m.id =? and s.id = sim.starId and m.id = sim.movieId";
            
            long endJDBC=System.nanoTime();
            
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
            	
            	//System.out.println(movie_title);
            	
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
            
            long endquery=System.nanoTime();
            
            // write JSON string to output
            out.write(jsonArray.toString());
            // set response status to 200 (OK)
            response.setStatus(200);
            
            rs.close();
            searchStatement.close();
            dbcon.close();
            
            // calculate the time for query and JDBC part
            long queryTime=endquery-startquery;
            long JDBCTime=endJDBC-startJDBC;
            
            // write the file
            FileWriter writer;
            writer = new FileWriter(myfile, true);
            writer.write(String.valueOf(queryTime)+" "+ String.valueOf(JDBCTime) + "\n");
            writer.close();
            
            System.out.println("TS: "+ String.valueOf(queryTime)+ " TJ: "+ String.valueOf(JDBCTime));
            
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

//Without prepared statement
/*
@WebServlet(name = "StarsServlet", urlPatterns = "/api/movies")
public class MovieServlet extends HttpServlet {
 private static final long serialVersionUID = 1L;

 // Create a dataSource which registered in web.xml
 @Resource(name = "jdbc/moviedb")
 private DataSource dataSource;
 private String movieSize = "";

 protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

     response.setContentType("application/json"); // Response mime type
     
     // Write a file from servlet.
  	 String contextPath = getServletContext().getRealPath("/");
  	 String xmlFilePath = contextPath+"movie_res";
  	 System.out.println("xmlFilePath: "+ xmlFilePath);
  	 File myfile = new File(xmlFilePath);
     myfile.createNewFile();
     
     // Get genre from url.
     String genre = request.getParameter("genre");
     String Title = request.getParameter("Title").replaceAll("\\s+", " ");
     String[] title_arr=Title.split(" ");
     String Year = request.getParameter("Year");
     String Director = request.getParameter("Director");
     String Star_name = request.getParameter("Star_name");
     String sort = request.getParameter("sort");
     String autocom=request.getParameter("autocom");

     // Output stream to STDOUT
     PrintWriter out = response.getWriter();
     //System.out.println(Title);
     
     // parse request and count movie query offset
     String str_p=request.getParameter("p");
     String str_nR=request.getParameter("numRecord");

     int page=0, numRecord_int=20;
     if(str_p != null) page=Integer.parseInt(str_p); 
     if(str_nR != null) numRecord_int=Integer.parseInt(str_nR); 
     
     int offset = page*numRecord_int;
     int numRecord = numRecord_int;
     
     Statement searchStatement = null;
     Statement sizeStatement = null;
     
     String baseSelect = "SELECT * FROM `movies` m LEFT JOIN `ratings` r ON m.id = r.movieId";
     String searchStr = "";
     
     // Get movies and rating.
     //query="SELECT * FROM `movies` m JOIN `ratings` r ON m.id = r.movieId";

     //Search by genre.
     if(genre != null && genre.length() > 1) {
     	//searchStr="SELECT q.id, q.title, q.year, q.director, q.rating FROM ("+ baseSelect +") q JOIN `genres_in_movies` gim ON gim.movieId=q.id JOIN `genres` g ON g.id=gim.genreId WHERE g.name=?";
    	 searchStr="SELECT q.id, q.title, q.year, q.director, q.rating FROM ("+ baseSelect +") q JOIN `genres_in_movies` gim ON gim.movieId=q.id JOIN `genres` g ON g.id=gim.genreId WHERE g.name="+"'"+genre+"'";
     }
     //Search by firt character.
     else if(genre != null && genre.length() == 1) {
     	//searchStr= "SELECT * FROM ("+baseSelect+") q WHERE q.title like ?";
    	 searchStr= "SELECT * FROM ("+baseSelect+") q WHERE q.title like '"+genre.charAt(0)+"%'";
     }
     //Advanced search.
     else {
     	searchStr=baseSelect;
     	if(Title != null && !Title.equals("") && !Title.equals("null")) {
     		if(autocom != null && autocom.equals("true")) {
     			//System.out.println("Perform normal search");
     			//searchStr="SELECT * FROM ("+searchStr+") q WHERE q.title=?";
     			searchStr="SELECT * FROM ("+searchStr+") q WHERE q.title="+"'"+Title+"'";
     		}
     		else {
     			String q="";
         		if(autocom != null && autocom.equals("true")) {
         			q=Title;
         		}
         		else {
         			//Full text search
             		for(String s : title_arr) {
             			q+=("+"+s+"* ");
             			//q+=(s+"* ");
             		}
         		}
     			//System.out.println("Perform full text search");
     			//searchStr="SELECT * FROM ("+searchStr+") q WHERE MATCH (q.title) AGAINST (? IN BOOLEAN MODE)";
         		searchStr="SELECT * FROM ("+searchStr+") q WHERE MATCH (q.title) AGAINST ("+"'"+q+"'"+"IN BOOLEAN MODE)";
     		}
     		
     		// fuzzy search
     		String q= " or(";
     		for(int i = 0; i < title_arr.length; ++i) {
     			if(i != 0) q += " and ";
     			int fuzzy_thres = (title_arr[i].length()-1)/5;
     			q+= "(SELECT edrec('" + title_arr[i].toLowerCase() +"', q.title, " + Integer.toString(fuzzy_thres) + ")= 1)";
     		}
     		q += ")";
     		searchStr += q;
         }
         if(Year != null && !Year.equals("") && !Year.equals("null")) {
         	//searchStr= "SELECT * FROM ("+searchStr+") q WHERE q.year like ?";
          	searchStr= "SELECT * FROM ("+searchStr+") q WHERE q.year like "+"'%"+Year+"%'";
         }
         if(Director != null && !Director.equals("") && !Director.equals("null")) {
         	//searchStr= "SELECT * FROM ("+searchStr+") q WHERE q.director like ?";          	
        	searchStr= "SELECT * FROM ("+searchStr+") q WHERE q.director like "+"'%"+Director+"%'";
         }
         if(Star_name != null && !Star_name.equals("") && !Star_name.equals("null")) {
         	//searchStr= "SELECT q.id, q.title, q.year, q.director, q.rating FROM ("+searchStr+") q JOIN `stars_in_movies` sim ON q.id=sim.movieId JOIN `stars` s ON s.id=sim.starId WHERE s.name like ?";
        	searchStr= "SELECT q.id, q.title, q.year, q.director, q.rating FROM ("+searchStr+") q JOIN `stars_in_movies` sim ON q.id=sim.movieId JOIN `stars` s ON s.id=sim.starId WHERE s.name like "+"'%"+Star_name+"%'";
         }
     }
     
     //Count the number of movies.
     String qSize="SELECT COUNT(*) AS `cnt` FROM "+"("+ searchStr +") AS n";
     
     if(sort != null) {
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
     }
     
     //searchStr="SELECT * FROM "+"("+searchStr+") AS n LIMIT ? OFFSET ?";
     searchStr="SELECT * FROM "+"("+searchStr+") AS n LIMIT "+numRecord+" OFFSET "+offset;

     try {
     	 // Start time of query.
     	 long startquery = System.nanoTime();
     	
         Connection dbcon = dataSource.getConnection();
     	
         // Start time of JDBC.
         long startJDBC = System.nanoTime();
         
         dbcon.setAutoCommit(false);
         
         searchStatement=dbcon.createStatement();
         sizeStatement=dbcon.createStatement();
         
         // Count total number of movies.
         ResultSet rsP = sizeStatement.executeQuery(qSize);
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
         ResultSet rs = searchStatement.executeQuery(searchStr);
         dbcon.commit();

         PreparedStatement genreStatement = null;
         String genStr = "SELECT GROUP_CONCAT(g.name) AS genreList FROM  `genres` g JOIN `genres_in_movies` gm ON gm.genreId = g.id AND gm.movieId =?";
         PreparedStatement starStatement = null;
         String starStr = "SELECT * from movies as m, stars_in_movies as sim, stars as s where m.id =? and s.id = sim.starId and m.id = sim.movieId";
         
         long endJDBC=System.nanoTime();
         
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
         	
         	//System.out.println(movie_title);
         	
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
         
         long endquery=System.nanoTime();
         
         // write JSON string to output
         out.write(jsonArray.toString());
         // set response status to 200 (OK)
         response.setStatus(200);
         
         rs.close();
         searchStatement.close();
         dbcon.close();
         
         // calculate the time for query and JDBC part
         long queryTime=endquery-startquery;
         long JDBCTime=endJDBC-startJDBC;
         
         // write the file
         FileWriter writer;
         writer = new FileWriter(myfile, true);
         writer.write(String.valueOf(queryTime)+" "+ String.valueOf(JDBCTime) + "\n");
         writer.close();
         
         System.out.println("TS: "+ String.valueOf(queryTime)+ " TJ: "+ String.valueOf(JDBCTime));
         
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
}*/