import com.google.gson.JsonObject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.Calendar;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * This IndexServlet is declared in the web annotation below, 
 * which is mapped to the URL pattern /api/index.
 */
@WebServlet(name = "ShoppingCartServlet", urlPatterns = "/api/shoppingCart")
public class ShoppingCartServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    // put all the info a query need at query url
    // store at java. cache the info at the first request, 1) database server is less busy
    
    
    /**
     * handles POST requests to store session information
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        String sessionId = session.getId();
        Long lastAccessTime = session.getLastAccessedTime();
        
        JsonObject responseJsonObject = new JsonObject();
        responseJsonObject.addProperty("sessionID", sessionId);
        responseJsonObject.addProperty("lastAccessTime", new Date(lastAccessTime).toString());

        // write all the data into the jsonObject
        response.getWriter().write(responseJsonObject.toString());
    }

    /**
     * handles GET requests to add and show the item list information
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    	int status = 0; // 0: not remove nor add, 1: add, 2: remove
    	String removeMovie = request.getParameter("removeMovie");
        String item = request.getParameter("item");
        String qty = request.getParameter("qty");
        if(removeMovie != null && item == null && qty == null) status = 2;
        else if(removeMovie == null && item != null && qty != null) status = 1;
  
        HttpSession session = request.getSession();
        
        HashMap<String, Integer> m = (HashMap<String, Integer>) session.getAttribute("itemMap");
        
            if(m == null) {
            	m= new HashMap<String, Integer>();
            	if(status == 1) m.put(item, Integer.parseInt(qty));
            	session.setAttribute("itemMap", m);
            }else {
            	if(status != 0) {
                    synchronized (m) {
                    	if(status == 1){
                        	int oldQty = m.containsKey(item) ? m.get(item) : 0;
                        	m.put(item, oldQty+ Integer.parseInt(qty));            		
                    	}
                    	else m.remove(removeMovie);
                    } 	
            	}       	
            }        	
            
        
        // join by array
        ArrayList<String> arr = new ArrayList();
        Set<String> keys = m.keySet();
        for(String key: keys){
            System.out.println("Value of "+key+" is: "+m.get(key));
            arr.add(key);            
            arr.add(Integer.toString(m.get(key)));
        }
        
        response.getWriter().write(String.join(",", arr));
        
    }
}
