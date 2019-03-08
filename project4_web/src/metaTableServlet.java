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
@WebServlet(name = "metaTableServlet", urlPatterns = "/api/metaTable")
public class metaTableServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.xml
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("application/json"); // Response mime type
        
        String tableName = request.getParameter("t");
        PreparedStatement showStatement = null;
        // Output stream to STDOUT
        PrintWriter out = response.getWriter();
        
               
        try {
            // Get a connection from dataSource
            Connection dbcon = dataSource.getConnection();
            
            String showTableStr = "SHOW COLUMNS FROM " + tableName;
            
            dbcon.setAutoCommit(false);
            showStatement = dbcon.prepareStatement(showTableStr);
            
            
            System.out.println(showStatement);
                
        	JsonArray jsonArray = new JsonArray();

            // Perform the query
            ResultSet rs = showStatement.executeQuery();
            dbcon.commit();

            
            // Iterate through each row of rs
            while (rs.next()) {         	
            	String field = rs.getString("Field");
            	String type = rs.getString("Type");
            	String checkNull = rs.getString("Null");
            	String key = rs.getString("Key");
            	String checkDefault = rs.getString("Default");
            	String extra = rs.getString("Extra");
            	
//            	System.out.println(field);
//            	System.out.println(checkDefault);
            	
            	JsonObject jsonObject = new JsonObject();
            	jsonObject.addProperty("field", field);
            	jsonObject.addProperty("type", type);
            	jsonObject.addProperty("checkNull", checkNull);
            	jsonObject.addProperty("key", key);
            	jsonObject.addProperty("checkDefault", checkDefault);
            	jsonObject.addProperty("extra", extra);

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