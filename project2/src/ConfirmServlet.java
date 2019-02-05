

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.sql.ResultSet;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Servlet implementation class ConfirmServlet
 */
//name = "ShoppingCartServlet", urlPatterns = "/api/shoppingCart"
@WebServlet(name="ConfirmServlet", urlPatterns = "/api/confirm")
public class ConfirmServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	@Resource(name = "jdbc/moviedb")
	private DataSource dataSource;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ConfirmServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		
		try {
			Connection dbcon = dataSource.getConnection();
			Statement statement = dbcon.createStatement();
			HttpSession session = request.getSession();
			
	        // Get user name.
	        User user = (User) session.getAttribute("user");
	        // System.out.println(user.getUsername());
	        
	        //SELECT * FROM customers c, sales s WHERE c.id=s.id AND c.email= 'a@email.com';
	        
			//String query="SELECT * FROM customers c, sales s WHERE c.id=s.id AND c.email='"+user.getUsername()+"'";
	        //Get customer id.
			String query="SELECT * FROM customers c WHERE c.email='"+user.getUsername()+"'";
			ResultSet rs=statement.executeQuery(query);
	        rs.next();
	        String customerId=rs.getString("id");
			
	        //Get today's date.
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Date date = new Date();
			String saleDate=dateFormat.format(date);
	        
			JsonArray jsonArray = new JsonArray();
			
			//Insert record into movies.sales.
	        HashMap<String, Integer> itemMap=(HashMap<String, Integer>) session.getAttribute("itemMap");
	        for(String movie_title : itemMap.keySet()) {
	        	Integer quantity=itemMap.get(movie_title);
	        	//SELECT * FROM movies WHERE movies.title='Chief Zabu';
	        	query="SELECT * FROM movies WHERE movies.title='"+movie_title+"'";
	        	rs=statement.executeQuery(query);
	        	rs.next();
	        	String movie_id=rs.getString("id");
	        	
	        	System.out.println("movie_id: "+movie_id);
	        	System.out.println("quantity: "+quantity);
	        	
	        	//INSERT INTO sales (customerId, movieId, saleDate) VALUES ('135006', 'tt0399582', '2019-02-01');
		        //Insert sale record.
	        	/*
	        	for(int i=0; i<quant; i++) {
	        		query="INSERT INTO sales (customerId, movieId, saleDate) VALUES ('"+customerId+"','"+movie_id+"','"+saleDate+"')";
	        		int r=statement.executeUpdate(query);
	        		
			        query="SELECT * FROM sales WHERE sales.customerId='"+customerId+"' AND sales.movieId='"+movie_id+"' AND sales.saleDate='"+saleDate+"'";
			        rs=statement.executeQuery(query);
			        rs.next();
			        String sale_id=rs.getString("id");
			        
			        JsonObject jsonObject = new JsonObject();
			        jsonObject.addProperty("sale_id", String.valueOf(Integer.valueOf(sale_id)+i));
			        jsonObject.addProperty("movie_title", movie_title);
			        jsonArray.add(jsonObject);
			        
			        System.out.println("sale_id: "+String.valueOf(Integer.valueOf(sale_id)+i));
			        System.out.println("movie_title: "+movie_title);
	        	}*/
	        	query="INSERT INTO sales (customerId, movieId, saleDate, quantity) VALUES ('"+customerId+"','"+movie_id+"','"+saleDate+"','"+quantity+"')";
	        	int r=statement.executeUpdate(query);
	        	
	        	query="SELECT * FROM sales WHERE sales.customerId='"+customerId+"' AND sales.movieId='"+movie_id+"' AND sales.saleDate='"+saleDate+"'";
	        	rs=statement.executeQuery(query);
	        	rs.next();
	        	String sale_id=rs.getString("id");
	        	
	        	JsonObject jsonObject = new JsonObject();
	        	jsonObject.addProperty("sale_id", sale_id);
	        	jsonObject.addProperty("movie_title", movie_title);
	        	jsonObject.addProperty("quantity", quantity);
	        	jsonArray.add(jsonObject);
	        }
            // write JSON string to output
            out.write(jsonArray.toString());
            // set response status to 200 (OK)
            response.setStatus(200);
            
            rs.close();
            statement.close();
            dbcon.close();
		}
		catch (Exception e) {
			// write error message JSON object to output
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("errorMessage", e.getMessage());
			out.write(jsonObject.toString());

			// set reponse status to 500 (Internal Server Error)
			response.setStatus(500);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
