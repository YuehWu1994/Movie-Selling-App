import com.google.gson.JsonObject;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import java.io.IOException;
import org.jasypt.util.password.StrongPasswordEncryptor;
import com.google.gson.JsonArray;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;


/**
 * Servlet implementation class Login
 */
@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource; 
    
    public LoginServlet() {
        super();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        
        // determine request origin by HTTP Header User Agent string
        String userAgent = request.getHeader("User-Agent");
        System.out.println("recieved login request");
        System.out.println("userAgent: " + userAgent);

        // only verify recaptcha if login is from Web (not Android)
        if (userAgent != null && !userAgent.contains("Android")) {
            String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
            // verify recaptcha first
            try {
                RecaptchaVerifyUtils.verify(gRecaptchaResponse, RecaptchaConstants.SECRET_KEY);
            } catch (Exception e) {
                System.out.println("recaptcha success");
                JsonObject responseJsonObject = new JsonObject();
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", e.getMessage());
                response.getWriter().write(responseJsonObject.toString());
                return;
            }
        }


        // then verify username / password
        //JsonObject loginResult = LoginVerifyUtils.verifyUsernamePassword(username, password);
        
        
        // after recatpcha verfication, then verify username and password
        int loginStatus = 2; // 0: correct, 1: username not match, 2: password not match
        JsonObject responseJsonObject = new JsonObject();
        PreparedStatement userNameStr = null;
        String selectString = "SELECT c.password FROM `customers` c WHERE c.email = ?";
        try {
        	Connection dbcon = dataSource.getConnection();
        	dbcon.setAutoCommit(false);
        	userNameStr = dbcon.prepareStatement(selectString);
        	userNameStr.setString(1, username);

            ResultSet rs = userNameStr.executeQuery();
            dbcon.commit();
        	
            //JsonArray jsonArray = new JsonArray();
            
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
                		loginStatus = 0;
                        
                        responseJsonObject.addProperty("status", "success");
                        responseJsonObject.addProperty("message", "success");
                	}
            	}while (rs.next());
            	
            }
            
            if(loginStatus != 0) {
        		System.out.println("Error");
                responseJsonObject.addProperty("status", "fail");
                if (loginStatus == 1) {
                    responseJsonObject.addProperty("message", "user " + username + " doesn't exist");
                } else if(loginStatus == 2) {
                    responseJsonObject.addProperty("message", "incorrect password");
                }  	
            }

            rs.close();
            userNameStr.close();
            dbcon.close();
        } catch (Exception e) {
			// write error message JSON object to output
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("errorMessage", e.getMessage());  
		}        
        
        
        if (responseJsonObject.get("status").getAsString().equals("success")) {
            // login success
            request.getSession().setAttribute("user", new User(username));
            response.getWriter().write(responseJsonObject.toString());
        } else {
            response.getWriter().write(responseJsonObject.toString());
        }
    }

}
