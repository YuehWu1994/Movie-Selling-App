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

import parsexml.*;

public class DomParser {
    
    //List<Employee> myEmpls;
	List<directorfilms> list_dirfilms;
	List<actor> list_actors;
	List<stars_in_movies> list_sim;

    Document mains, actors, casts;

    public DomParser() {
    	list_dirfilms = new ArrayList<>();
    	list_actors = new ArrayList<>();
    	list_sim = new ArrayList<>();
    }

    public void runExample() {
    	
    	try {
    		Class.forName("com.mysql.jdbc.Driver").newInstance();
    		/*
    		Connection dbcon = DriverManager.getConnection("jdbc:mysql:///moviedb?autoReconnect=true&useSSL=false",
                    "mytestuser", "mypassword");*/
    		Connection dbcon = DriverManager.getConnection("jdbc:mysql:///moviedb", "mytestuser", "mypassword");
    		//parse the xml file and get the dom object
            parseXmlFile();

            //get each employee element and create a Employee object
            parseDocument();
            
            
            //load mains243.xml
            insert_movies(dbcon);
            insert_genres(dbcon);
            insert_genres_in_movies(dbcon);
            
            //load actors63.xml
            insert_stars(dbcon);
            
            //load casts124.xml
            insert_stars_in_movies(dbcon);
    	}
    	catch (Exception e){
    		System.out.printf("connection error: %s", e.getMessage());
    	}
    	/*
        //parse the xml file and get the dom object
        parseXmlFile();

        //get each employee element and create a Employee object
        parseDocument();
        
        //load mains243.xml
        
        insert_movies();
        insert_genres();
        insert_genres_in_movies();
        
        //load actors.xml
        //insert_stars();*/
    }

    private void parseXmlFile() {
        //get the factory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            //Using factory get an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();

            //parse using builder to get DOM representation of the XML file
            //dom = db.parse("employees.xml");
            mains = db.parse("mains243.xml");
            actors = db.parse("actors63.xml");
            casts = db.parse("casts124.xml");
            //mains = db.parse("test.xml");
            
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
        Element doc_mains = mains.getDocumentElement();
        Element doc_actors = actors.getDocumentElement();
        Element doc_sim = casts.getDocumentElement();

        NodeList direc_node = doc_mains.getElementsByTagName("directorfilms");
        NodeList actor_node = doc_actors.getElementsByTagName("actor");
        NodeList sim_node = doc_sim.getElementsByTagName("m");
        
        if (direc_node != null && direc_node.getLength() > 0) {
            for (int i = 0; i < direc_node.getLength(); i++) {
                Element e_direc_film = (Element) direc_node.item(i);
                directorfilms dir_film = getdirectorfilms(e_direc_film);
                list_dirfilms.add(dir_film);
            }
        }
        
        if (actor_node != null && actor_node.getLength() > 0) {
        	for(int i=0; i<actor_node.getLength(); i++) {
        		Element e_actor = (Element) actor_node.item(i);
        		actor acs = getactor(e_actor);
        		list_actors.add(acs);
        	}
        }
        
        if (sim_node != null && sim_node.getLength() > 0) {
        	for(int i=0; i<sim_node.getLength(); i++) {
        		Element e_sim_node = (Element) sim_node.item(i);
        		stars_in_movies sim = get_sim(e_sim_node);
        		list_sim.add(sim);
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
                list_films.add(new film(movie_id, title, year, list_genres));
            }
        }
    	return new directorfilms(director, list_films);
    }
    
    private actor getactor(Element acs) {
    	String id=getTextValue(acs, "stagename");
    	if(id == null) {
    		System.out.println("actor's id(stagename) is null");
    		return null;
    	}
    	
    	//Check if acs element has a complete firstname and lastname.
    	String first_name=getTextValue(acs, "firstname");
    	String last_name=getTextValue(acs, "familyname");
    	if(first_name == null || last_name == null) {
    		System.out.println("actor's name is null");
    		return null;
    	}
    	first_name=first_name.trim();
    	last_name=last_name.trim();
    	if(first_name.length() == 0 || last_name.length() == 0) {
    		System.out.println("actor's name is empty");
    		return null;
    	}
    	
    	String name=first_name+" "+last_name;
    	Integer year=getIntValue(acs, "dob");
    	return new actor(id, name, year);
    }
    
    private stars_in_movies get_sim(Element sim) {
    	String starId=getTextValue(sim, "a");
    	if(starId == null || starId.length() == 0) {
    		System.out.println("casts' tag <a> is null");
    		return null;
    	}
    	String movieId=getTextValue(sim, "f");
    	if(movieId == null || movieId.length() == 0) {
    		System.out.println("casts' tag <f> is null");
    		return null;
    	}
    	return new stars_in_movies(starId, movieId);
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
    
    private void insert_movies(Connection dbcon) {
    	try {
    		String query = "";  		
    		
    		for(int i=0; i<list_dirfilms.size(); i++) {
    			directorfilms di_films=list_dirfilms.get(i);
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
    				int af = insertStatement.executeUpdate();
    				//dbcon.commit();
    	            if(af != 0) {
    	            	System.out.printf("Success %s", insertStatement);
    	            	System.out.println();
    	            }
    	            else {
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

    private void insert_genres(Connection dbcon) {
    	try {
    		String query = "";  		

    		for(int i=0; i<list_dirfilms.size(); i++) {
    			directorfilms di_films=list_dirfilms.get(i);
    			List<film> films=di_films.films;
    			int size=films.size();
    			
    			//scan all the movies made by this director.
    			for(int j=0; j<size; j++) {
    				
    				//insert genre.
    				List<String> genres = films.get(j).genres;
    				PreparedStatement insertStatement = null;
    				ResultSet rs = null;
    				
    				for(String gre : genres) {
    				    //Check if genre exists.
    					query = "SELECT * FROM genres WHERE genres.name = ?;";
    					insertStatement = dbcon.prepareStatement(query);
    					insertStatement.setString(1, gre);
    					rs = insertStatement.executeQuery();
    					
    					if(rs.next()) {
    						System.out.printf("Genre: %s exists", gre);
    						System.out.println();
    						continue;
    					}
    					
    					//Insert the genre that is not in the 'genres' table.
    					query = "INSERT INTO genres (name) VALUES(?);";
    					insertStatement = dbcon.prepareStatement(query);
    					insertStatement.setString(1, gre);
    					int af = insertStatement.executeUpdate();
    					if(af != 0) {
    						System.out.printf("Succeess: %s", insertStatement);
    						System.out.println();
    					}
    					else {
    						System.out.printf("Fail: %s", insertStatement);
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
    
    private void insert_genres_in_movies(Connection dbcon) {
    	try {
    		String query = "";  		
    		
    		for(int i=0; i<list_dirfilms.size(); i++) {
    			directorfilms di_films=list_dirfilms.get(i);
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
    					
    					if(genre_id == null || movie_id == null || movie_id.length() == 0) {
    						System.out.printf("Error genre id: %d movie id %s", genre_id, movie_id);
    						System.out.println();
    						continue;
    					}
    					
    					//Check if the query exists.
    					query = "SELECT * FROM genres_in_movies WHERE genres_in_movies.genreId = ? AND genres_in_movies.movieId = ?;";
    					insertStatement = dbcon.prepareStatement(query);
    					insertStatement.setInt(1, genre_id);
    					insertStatement.setString(2, movie_id);
    					rs = insertStatement.executeQuery();
    					if(rs.next()) {
    						System.out.printf("genre_id %s and movie_id %s exists", genre_id, movie_id);
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
							System.out.printf("Succees: %s", insertStatement);
							System.out.println();
						}
						else {
							System.out.printf("Fail: %s", insertStatement);
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

    private void insert_stars(Connection dbcon) {
    	try {
    		PreparedStatement insertStatement = null;
			ResultSet rs = null;
    		String query = "";
    		for(int i=0; i<list_actors.size(); i++) {
    			actor ac = list_actors.get(i);
    			if(ac == null) continue;

				String id=ac.id;
			    String name=ac.name;
				Integer year=ac.year;
				
				//check if star's id exists.
				query = "SELECT * FROM stars WHERE stars.id = ?;";
				insertStatement = dbcon.prepareStatement(query);
				insertStatement.setString(1, id);
				rs = insertStatement.executeQuery();
				if(rs.next()) {
					System.out.printf("star's id %s exists", id);
					System.out.println();
					continue;
				}
				
				//insert star into stars table.
				query = "INSERT INTO stars (id, name, birthYear) VALUES(?,?,?);";
				insertStatement = dbcon.prepareStatement(query);
				insertStatement.setString(1, id);
				insertStatement.setString(2, name);
				
				//check if year is null.
				if(year == null) insertStatement.setNull(3, Types.INTEGER);
				else insertStatement.setInt(3, year);

				int af = insertStatement.executeUpdate();
				if(af != 0) {
					System.out.printf("Success: %s", insertStatement);
					System.out.println();
				}
				else {
					System.out.printf("Fail %s", insertStatement);
					System.out.println();
				}
    		}
    	}
    	catch (Exception e){
    		System.out.printf("insert stars error %s", e.getMessage());
    	}
    }
    
    private void insert_stars_in_movies(Connection dbcon) {
    	try {
    		PreparedStatement insertStatement = null;
			ResultSet rs = null;
    		String query = "";
    		
    		for(int i=0; i<list_sim.size(); i++) {
    			stars_in_movies sim = list_sim.get(i);
    			if(sim == null) continue;

				String starId=sim.starId;
				String movieId=sim.movieId;
				
				//check if both starId and movieId exist.
				query = "SELECT * FROM stars WHERE stars.id = ?;";
				insertStatement = dbcon.prepareStatement(query);
				insertStatement.setString(1, starId);
				ResultSet rs1 = insertStatement.executeQuery();
				query = "SELECT * FROM movies WHERE movies.id = ?;";
				insertStatement = dbcon.prepareStatement(query);
				insertStatement.setString(1, movieId);
				ResultSet rs2 = insertStatement.executeQuery();
				if(!rs1.next() || ! rs2.next()) {
					System.out.printf("starId %s or movieId %s does't exist", starId, movieId);
					System.out.println();
					continue;
				}
				
				//check if star's id exists.
				query = "SELECT * FROM stars_in_movies WHERE stars_in_movies.starId = ? AND stars_in_movies.movieId = ?;";
				insertStatement = dbcon.prepareStatement(query);
				insertStatement.setString(1, starId);
				insertStatement.setString(2, movieId);
				rs = insertStatement.executeQuery();
				if(rs.next()) {
					System.out.printf("starId %s AND movieId %s exist", starId, movieId);
					System.out.println();
					continue;
				}
				
				query = "INSERT INTO stars_in_movies (starId, movieId) VALUES(?,?);";
				insertStatement = dbcon.prepareStatement(query);
				insertStatement.setString(1, starId);
				insertStatement.setString(2, movieId);
				int af = insertStatement.executeUpdate();
				if(af != 0) {
					System.out.printf("Success: %s", insertStatement);
					System.out.println();
				}
				else {
					System.out.printf("Fail %s", insertStatement);
					System.out.println();
				}
    		}
    	}
    	catch (Exception e){
    		System.out.printf("insert stars in movies error %s", e.getMessage());
    	}
    }
    
    public static void main(String[] args) throws Exception{
        DomParser dpe = new DomParser();
        dpe.runExample();
        System.out.println();
    }

}
