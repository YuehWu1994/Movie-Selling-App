import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.CallableStatement;
import java.sql.Types;

/**
 * Servlet implementation class checkoutServlet
 */

@WebServlet(name = "addMovieServlet", urlPatterns = "/api/addMovie")
public class addMovieServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;    
	
	
	@Resource(name = "jdbc/moviedb")
    private DataSource dataSource;   

    public addMovieServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	String title = request.getParameter("tt");
        String year = request.getParameter("year");
    	String director = request.getParameter("dir");
        String star = request.getParameter("st");
        String genre = request.getParameter("gn");
        
        
        HttpSession session = request.getSession();
       
        
        try {
        	Connection dbcon = dataSource.getConnection();
        	
        	CallableStatement cStmt = dbcon.prepareCall("{call addMovie(?,?,?,?,?,?,?,?)}");
        	
        	// set param
        	cStmt.setString(1, title);
        	cStmt.setString(2, year);
        	cStmt.setString(3, director);
        	cStmt.setString(4, star);
        	cStmt.setString(5, genre);
        	cStmt.registerOutParameter(6, Types.INTEGER);
        	cStmt.registerOutParameter(7, Types.INTEGER);
        	cStmt.registerOutParameter(8, Types.INTEGER);
        	
        	// execute callablestatement
        	boolean hadResults = cStmt.execute();
        	
        	// Retrieve output parameters
        	int movieExist = cStmt.getInt(6); // index-based
        	int starExist = cStmt.getInt(7); // index-based
        	int genreExist = cStmt.getInt(8); // index-based
            

            cStmt.close();
    		
        	System.out.println(movieExist);
        	System.out.println(starExist);
        	System.out.println(genreExist);   		
    		
            
            // response output
            JsonObject responseJsonObject = new JsonObject();
            if(movieExist == 1) {
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "The movie already exists");          	
            }
            else {
            	String m = "";
            	if(starExist == 0) m += "Insert new star: " + star + "/ ";
            	if(genreExist == 0) m += "Insert new genre: " + genre + "/ ";
            	m += "Bind star and genre with new movie";
                responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("message", m);
            }
            response.getWriter().write(responseJsonObject.toString());
        }catch (Exception e) {
        	
			// write error message JSON object to output
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("errorMessage", e.getMessage());
			
			// set reponse status to 500 (Internal Server Error)
			response.setStatus(500);

        }
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		// TODO Auto-generated method stub
		//doGet(request, response);
	}

}
