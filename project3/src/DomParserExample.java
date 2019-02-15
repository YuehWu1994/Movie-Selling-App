
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Statement;
import java.sql.*;

public class DomParserExample {
    
    //List<Employee> myEmpls;
	static List<directorfilms> dirfilms;
    Document dom;

    public DomParserExample() {
        //create a list to hold the employee objects
    	dirfilms = new ArrayList<>();
    }

    public void runExample() {

        //parse the xml file and get the dom object
        parseXmlFile();

        //get each employee element and create a Employee object
        parseDocument();

        //Iterate through the list and print the data
        //printData();
        insert_movies();
        
        insert_genres();
        
        insert_genres_in_movies();
    }

    private void parseXmlFile() {
        //get the factory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            //Using factory get an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();

            //parse using builder to get DOM representation of the XML file
            //dom = db.parse("employees.xml");
            dom = db.parse("mains243.xml");
            //dom = db.parse("test.xml");
            
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (SAXException se) {
            se.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void parseDocument() {
        //get the root elememt
        Element docEle = dom.getDocumentElement();

        //get a nodelist of <employee> elements
        //NodeList nl = docEle.getElementsByTagName("Employee");
        NodeList direc_node = docEle.getElementsByTagName("directorfilms");
        
        if (direc_node != null && direc_node.getLength() > 0) {
            for (int i = 0; i < direc_node.getLength(); i++) {

                //get the directorfilms element
                Element e_direc_film = (Element) direc_node.item(i);

                //get the Employee object
                //Employee e = getEmployee(el);
                directorfilms dir_film = getdirectorfilms(e_direc_film);

                //add it to list
                dirfilms.add(dir_film);
            }
        }
    }

    /**
     * I take an employee element and read the values in, create
     * an Employee object and return it
     * 
     * @param empEl
     * @return
     */
    /*
    private Employee getEmployee(Element empEl) {

        //for each <employee> element get text or int values of 
        //name ,id, age and name
        String name = getTextValue(empEl, "Name");
        System.out.println(getTextValue(empEl, "First"));
        System.out.println(getTextValue(empEl, "Last"));
        int id = getIntValue(empEl, "Id");
        int age = getIntValue(empEl, "Age");

        String type = empEl.getAttribute("type");

        //Create a new Employee with the value read from the xml nodes
        Employee e = new Employee(name, id, age, type);

        return e;
    }*/
    
    
    private directorfilms getdirectorfilms(Element dirfilm) {
    	String director=getTextValue(dirfilm, "dirname");
    	
    	//System.out.printf("director: %s", director);
    	//System.out.println();
    	
    	NodeList films=dirfilm.getElementsByTagName("film");
    	List<film> list_films=new ArrayList<>();
    	
        if (films != null && films.getLength() > 0) {
            for (int i = 0; i < films.getLength(); i++) {
                //Get each film element
                Element fm = (Element) films.item(i);
                
                String movie_id=getTextValue(fm, "fid");
                String title=getTextValue(fm, "t");
                Integer year=getIntValue(fm, "year");
                if(movie_id == null || title == null || year == null || director == null) continue;
                
                director=director.trim();
                movie_id=movie_id.trim();
                title=title.trim();
                if(director.length() == 0 || movie_id.length() == 0 || title.length() == 0) continue;
                
                //Get list of genres.
                List<String> list_genres=new ArrayList<>();
                NodeList genres = fm.getElementsByTagName("cat");
                if(genres != null && genres.getLength() > 0) {
                	for(int j=0; j < genres.getLength(); j++) {
                		if(genres.item(j).getFirstChild() == null) {
                			System.out.println("tag cat is null");
                			continue;
                		}
                		String str_genre=genres.item(j).getFirstChild().getNodeValue();
                		
                		if(str_genre == null) continue;
                		str_genre=str_genre.trim();
                		if(str_genre.length() == 0) continue;
                		list_genres.add(str_genre);
                		//System.out.println(str_genre);
                		//System.out.println(genres.item(j).getFirstChild().getNodeValue());
                	}
                }
                
                
                //NodeList films=dirfilm.getElementsByTagName("film");
                /*               
                System.out.printf("title: %s", title);
                System.out.printf(" year: %s", year);
                System.out.println();*/
                list_films.add(new film(movie_id, title, year, list_genres));
            }
        }
    	return new directorfilms(director, list_films);
    }

    /**
     * I take a xml element and the tag name, look for the tag and get
     * the text content
     * i.e for <employee><name>John</name></employee> xml snippet if
     * the Element points to employee node and tagName is name I will return John
     * 
     * @param ele
     * @param tagName
     * @return
     */
    private String getTextValue(Element ele, String tagName) {    	
        String textVal = null;
        NodeList nl = ele.getElementsByTagName(tagName);
        if (nl != null && nl.getLength() > 0) {
            Element el = (Element) nl.item(0);
            if(el.getFirstChild() == null) {
            	System.out.printf("tag %s is null", tagName);
            	System.out.println();
            	return null;
            }
            textVal = el.getFirstChild().getNodeValue();
        }

        return textVal;
    }

    /**
     * Calls getTextValue and returns a int value
     * 
     * @param ele
     * @param tagName
     * @return
     */
    private Integer getIntValue(Element ele, String tagName) {
        //in production application you would catch the exception
    	Integer res;
    	try {
    		res = Integer.parseInt(getTextValue(ele, tagName));
    	}
    	catch (NumberFormatException e) {
    		System.out.printf("Wrong Integer format: %s", getTextValue(ele, tagName));
    		System.out.println();
    		return null;
    	}
        return res;
    }

    /**
     * Iterate through the list and print the
     * content to console
     */
    /*
    private void printData() {

        System.out.println("No of Employees '" + myEmpls.size() + "'.");

        Iterator<Employee> it = myEmpls.iterator();
        while (it.hasNext()) {
            System.out.println(it.next().toString());
        }
    }*/
    
    private void insert_movies() {
    	try {
    		Class.forName("com.mysql.jdbc.Driver").newInstance();
    		Connection dbcon = DriverManager.getConnection("jdbc:mysql:///moviedb?autoReconnect=true&useSSL=false",
                    "mytestuser", "mypassword");
    		
    		String query = "";  		
    		
    		for(int i=0; i<dirfilms.size(); i++) {
    			directorfilms di_films=dirfilms.get(i);
    			String director=di_films.director;
    			List<film> films=di_films.films;
    			int size=films.size();
    			
    			//scan all the movies made by this director.
    			for(int j=0; j<size; j++) {
    				String movie_id=films.get(j).id;
    				String title = films.get(j).title;
    				Integer year = films.get(j).year;
    				
    				//insert genre.
    				List<String> genres = films.get(j).genres;
    				PreparedStatement insertStatement = null;
    				ResultSet rs = null;
    				
    				//dbcon.setAutoCommit(false);
    				query = "SELECT 1 FROM movies WHERE movies.id=?";
    				insertStatement = dbcon.prepareStatement(query);
    				insertStatement.setString(1, movie_id);
    				rs=insertStatement.executeQuery();
    				if(rs.next()) {
    					System.out.printf("movie id %s exist", movie_id);
    					System.out.println();
    					continue;
    				}
    				
    				//Insert into 'movies' table.
    				query = "INSERT INTO movies (id, title, year, director) VALUES(?,?,?,?);";
    				insertStatement = dbcon.prepareStatement(query);
    				insertStatement.setString(1, movie_id);
    				insertStatement.setString(2, title);
    				insertStatement.setInt(3, year);
    				insertStatement.setString(4, director);
    				//System.out.println(insertStatement);
    				int af = insertStatement.executeUpdate();
    				//dbcon.commit();
    	            if(af != 0) {
    	            	/*
    	                responseJsonObject.addProperty("status", "success");
    	                responseJsonObject.addProperty("message", "Star " + starName + " inserted");*/   
    	            	//System.out.println("success");
    	            }
    	            else {
    	            	/*
    	                responseJsonObject.addProperty("status", "fail");
    	                responseJsonObject.addProperty("message", "Insert Star Fail");*/
    	            	System.out.printf("Fail: %s", insertStatement);
    	            	System.out.println();
    	            }
    	            insertStatement.close();
    			}
    		}
    	}
    	catch (Exception e){
    		System.out.printf("insert movies error %s", e.getMessage());
    	}
    }

    private void insert_genres() {
    	try {
    		Class.forName("com.mysql.jdbc.Driver").newInstance();
    		Connection dbcon = DriverManager.getConnection("jdbc:mysql:///moviedb?autoReconnect=true&useSSL=false",
                    "mytestuser", "mypassword");
    		
    		String query = "";  		

    		for(int i=0; i<dirfilms.size(); i++) {
    			directorfilms di_films=dirfilms.get(i);
    			List<film> films=di_films.films;
    			int size=films.size();
    			
    			//scan all the movies made by this director.
    			for(int j=0; j<size; j++) {
    				
    				//insert genre.
    				List<String> genres = films.get(j).genres;
    				PreparedStatement insertStatement = null;
    				ResultSet rs = null;
    				
    				for(String gre : genres) {
    				    //Insert into genres
    					query = "SELECT * FROM genres WHERE genres.name = ?;";
    					insertStatement = dbcon.prepareStatement(query);
    					insertStatement.setString(1, gre);
    					rs = insertStatement.executeQuery();
    					
    					//Insert the genre that is not in the 'genres' table.
    					if(!rs.next()) {
    						query = "INSERT INTO genres (name) VALUES(?);";
    						insertStatement = dbcon.prepareStatement(query);
    						insertStatement.setString(1, gre);
    						int af = insertStatement.executeUpdate();
    						if(af != 0) {
    							/*
    							System.out.printf("Succeed in inserting data: %s", gre);
    							System.out.println();*/
    						}
    						else {
    							System.out.printf("Failed in inserting data: %s", gre);
    							System.out.println();
    						}
    					}
    					else {
    						System.out.printf("Duplicate genre: %s", gre);
    						System.out.println();
    					}
    					insertStatement.close();
    				}
    			}
    		}
    	}
    	catch (Exception e){
    		System.out.printf("insert genre error %s", e.getMessage());
    	}
    }
    
    private void insert_genres_in_movies() {
    	try {
    		Class.forName("com.mysql.jdbc.Driver").newInstance();
    		Connection dbcon = DriverManager.getConnection("jdbc:mysql:///moviedb?autoReconnect=true&useSSL=false",
                    "mytestuser", "mypassword");
    		
    		String query = "";  		
    		
    		for(int i=0; i<dirfilms.size(); i++) {
    			directorfilms di_films=dirfilms.get(i);
    			String director=di_films.director;
    			List<film> films=di_films.films;
    			int size=films.size();
    			
    			//scan all the movies made by this director.
    			for(int j=0; j<size; j++) {
    				String movie_id=films.get(j).id;
    				
    				//insert genre.
    				List<String> genres = films.get(j).genres;
    				PreparedStatement insertStatement = null;
    				ResultSet rs = null;
    				
    				for(String gre : genres) {			
    					//Get genre id.
    					Integer genre_id=null;
    					query = "SELECT * FROM genres WHERE genres.name = ?;";
    					insertStatement = dbcon.prepareStatement(query);
    					insertStatement.setString(1, gre);
    					rs = insertStatement.executeQuery();
    					while(rs.next()) {
    						genre_id=rs.getInt("id");
    					}
    					
    					if(genre_id == null || movie_id == null) {
    						System.out.printf("Error genre id: %d movie id %s", genre_id, movie_id);
    						System.out.println();
    						continue;
    					}
    					
    					//Insert into 'genres_in_movies' table.
    					query = "INSERT INTO genres_in_movies (genreId, movieId) VALUES(?,?);";
    					insertStatement = dbcon.prepareStatement(query);
    					insertStatement.setInt(1, genre_id);
    					insertStatement.setString(2, movie_id);
    					int af = insertStatement.executeUpdate();
    					
						if(af != 0) {
							/*
							System.out.printf("Succeed in inserting data: genre_id: %s, movie_id: %s", genre_id, movie_id);
							System.out.println();*/
						}
						else {
							System.out.printf("Wrong data: genre_id: %s, movie_id: %s", genre_id, movie_id);
							System.out.println();
						}
						insertStatement.close();
    				}
    				//dbcon.setAutoCommit(false);
    			}
    		}
    	}
    	catch (Exception e){
    		System.out.printf("insert genres in movies error %s", e.getMessage());
    	}
    }	

    private void insert_stars() {
    	try {
    		
    	}
    	catch (Exception e){
    		System.out.printf("insert genres in movies error %s", e.getMessage());
    	}
    }
    
    public static void main(String[] args) throws Exception{
        //create an instance
        DomParserExample dpe = new DomParserExample();

        //call run example
        dpe.runExample();
        
        System.out.println();
    }

}
