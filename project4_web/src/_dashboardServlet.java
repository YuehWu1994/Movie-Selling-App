import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.annotation.Resource;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.jasypt.util.password.StrongPasswordEncryptor;


/**
 * This class is declared as LoginServlet in web annotation, 
 * which is mapped to the URL pattern /api/login
 */
@WebServlet(name = "_dashboardServlet", urlPatterns = "/api/_dashboardLogin")
public class _dashboardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    	String username = request.getParameter("username");
        String password = request.getParameter("password");

        
        /**
         * This example only allows username/password to be anteater/123456
         * In real world projects, you should talk to the database to verify username/password
         */ 
        
        int loginStatus = 2; // 0: correct, 1: username not match, 2: password not match

        PreparedStatement userNameStr = null;
        String selectString = "SELECT e.password FROM `employees` e WHERE e.email = ?";
        
        		
        
        try {
        	Connection dbcon = dataSource.getConnection();
        	dbcon.setAutoCommit(false);
        	userNameStr = dbcon.prepareStatement(selectString);
        	userNameStr.setString(1, username);

            ResultSet rs = userNameStr.executeQuery();
            dbcon.commit();
        	
            JsonArray jsonArray = new JsonArray();
            
            if(!rs.next()) {
            	System.out.println("ResultSet in empty, wrong user name");
            	loginStatus = 1;  
            }
            else {
            	do {
                	String _encryptedPassword = rs.getString("password");
        			
        			// use the same encryptor to compare the user input password with encrypted password stored in DB
        			boolean success = new StrongPasswordEncryptor().checkPassword(password, _encryptedPassword);
        			System.out.println("_encryptedPassword=" + _encryptedPassword);
        			System.out.println("typepassword=" + password);
        			
                	if(success) {
                		// Verify reCAPTCHA
                        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
                        System.out.println("gRecaptchaResponse=" + gRecaptchaResponse);
                        
                        try {
                            RecaptchaVerifyUtils.verify(gRecaptchaResponse, RecaptchaConstants.SECRET_KEY);
                        } catch (Exception e) {
                        	System.out.println("reCAPTCHA error");
                            JsonObject responseJsonObject = new JsonObject();
                            responseJsonObject.addProperty("status", "fail");
                            responseJsonObject.addProperty("message", "fail to access reCAPTCHA");
                            response.getWriter().write(responseJsonObject.toString());
                            return;
                        }
                        System.out.println("success");
                        
                		loginStatus = 0;
                		// getSession() : if exist, get the id, if not generate another one
                        String sessionId = ((HttpServletRequest) request).getSession().getId(); 
                        Long lastAccessTime = ((HttpServletRequest) request).getSession().getLastAccessedTime();
                        request.getSession().setAttribute("user", new User(username));

                        JsonObject responseJsonObject = new JsonObject();
                        responseJsonObject.addProperty("status", "success");
                        responseJsonObject.addProperty("message", "success");

                        response.getWriter().write(responseJsonObject.toString());
                	}
            	}while (rs.next());
            	
            }
            
            if(loginStatus != 0) {
        		System.out.println("Error");
                JsonObject responseJsonObject = new JsonObject();
                responseJsonObject.addProperty("status", "fail");
                if (loginStatus == 1) {
                    responseJsonObject.addProperty("message", "user " + username + " doesn't exist");
                } else if(loginStatus == 2) {
                    responseJsonObject.addProperty("message", "incorrect password");
                }
                response.getWriter().write(responseJsonObject.toString());  	
            }
            
            // write JSON string to output
            // set response status to 200 (OK)
            //response.setStatus(200);

            rs.close();
            userNameStr.close();
            dbcon.close();
        } catch (Exception e) {
        	
			// write error message JSON object to output
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("errorMessage", e.getMessage());
			
			// set reponse status to 500 (Internal Server Error)
			response.setStatus(500);

        }
    }
}
