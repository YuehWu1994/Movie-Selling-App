

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonObject;

/**
 * This is a dummy Servlet that will return the username of the user logged in.
 * The purpose of the Servlet is to work with other examples
 *   to test that after you login code works, you should be able to access this servlet.
 */
@WebServlet(name = "UsernameServlet", urlPatterns = "/api/username")
public class UsernameServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

    public UsernameServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    User user = (User) request.getSession().getAttribute("user");
	    
	    JsonObject responseJsonObject = new JsonObject();
	    responseJsonObject.addProperty("username", user.getUsername());
	    
	    response.getWriter().write(responseJsonObject.toString());
	    return;
	}

}
