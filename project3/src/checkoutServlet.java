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

/**
 * Servlet implementation class checkoutServlet
 */

@WebServlet(name = "checkoutServlet", urlPatterns = "/api/checkout")
public class checkoutServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;    
	
	
	@Resource(name = "jdbc/moviedb")
    private DataSource dataSource;   

    public checkoutServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	String lastName = request.getParameter("ln");
        String firstName = request.getParameter("fn");
        String ccid = request.getParameter("ccid");
        String expiration = request.getParameter("date");
        /*
    	System.out.println(lastName);
    	System.out.println(firstName);
    	System.out.println(ccid);
    	System.out.println(expiration);*/

        
        HttpSession session = request.getSession();
        HashMap<String, Integer> m = (HashMap<String, Integer>) session.getAttribute("itemMap");
        
        
        int transacionStatus = 0; // 0: correct, 1: username not match, 3: card info not match
        
        try {
        	Connection dbcon = dataSource.getConnection();
        	
            // prepare string and statement 
            PreparedStatement statement = null;
            String query = "SELECT * FROM (SELECT c.firstName, c.lastName, c.ccId,  cc.expiration FROM `customers` c JOIN `creditcards` cc ON c.ccId = cc.id) As s WHERE s.firstName =? AND s.lastName = ?";
            
            dbcon.setAutoCommit(false);
            statement = dbcon.prepareStatement(query);
            statement.setString(1, firstName);
            statement.setString(2, lastName);
        	
            // execute the statement
            ResultSet rs = statement.executeQuery();
            dbcon.commit();
            
            JsonArray jsonArray = new JsonArray();
            
            if(!rs.next()) {
            	System.out.println("ResultSet is empty, wrong user name");  
            	transacionStatus = 1;
            }
            else {
            	do {
                	String _ccid = rs.getString("ccId");
                	String _expiration = rs.getString("expiration");
                	if(!ccid.equals(_ccid) || !expiration.equals(_expiration)) {
                		transacionStatus = 3;
                		break;
                	}
                	else {

                        JsonObject responseJsonObject = new JsonObject();
                        responseJsonObject.addProperty("status", "success");
                        responseJsonObject.addProperty("message", "success");

                        response.getWriter().write(responseJsonObject.toString());
                	}
            	}while (rs.next());
            }
            
            System.out.println(transacionStatus);
            
            if(transacionStatus != 0) {
        		System.out.println("Error");
                JsonObject responseJsonObject = new JsonObject();
                responseJsonObject.addProperty("status", "fail");
                if (transacionStatus == 1) {
                    responseJsonObject.addProperty("message", "User " + firstName + " " + lastName + " doesn't exist");
                } else if(transacionStatus == 3) {
                    responseJsonObject.addProperty("message", "Incorrect card information");
                }
                response.getWriter().write(responseJsonObject.toString());  	
            }
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
