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
@WebServlet(name = "metaDataServlet", urlPatterns = "/api/metaData")
public class metaDataServlet extends HttpServlet {
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
        

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();
        

        // prepared string
       	PreparedStatement showStatement = null;
        String showTableStr = "SHOW TABLES;";
        String searchStr = "";
               
        try {
            // Get a connection from dataSource
            Connection dbcon = dataSource.getConnection();
            
            dbcon.setAutoCommit(false);
            showStatement = dbcon.prepareStatement(showTableStr);            
                
        	JsonArray jsonArray = new JsonArray();

            
            // Perform the query
            ResultSet rs = showStatement.executeQuery();
            dbcon.commit();
            
            // Iterate through each row of rs
            while (rs.next()) {         	
            	String tableName = rs.getString("Tables_in_moviedb");  
            	
            	
            	JsonObject jsonObject = new JsonObject();
            	jsonObject.addProperty("tableName", tableName);

                jsonArray.add(jsonObject);
          
            }
            
            // write JSON string to output
            out.write(jsonArray.toString());
            // set response status to 200 (OK)
            response.setStatus(200);
            
            rs.close();
            showStatement.close();
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