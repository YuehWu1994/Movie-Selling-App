import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.annotation.Resource;
import javax.sql.DataSource;

import java.sql.*;

import parsexml.*;
import java.util.*;

public class DomParser {
    
	List<directorfilms> list_dirfilms;
	List<actor> list_actors;
	List<stars_in_movies> list_sim;
	
	//Cache query results.
	//Set<String> set_movies_id=new HashSet<>();
	//Record inconsistency
	Map<String, String> map_movies=new HashMap<>();
	Map<String, Integer> map_genres=new HashMap<>();
	Set<String> set_gim=new HashSet<>();
	Set<String> set_stars_id=new HashSet<>();
	Set<String> set_sim=new HashSet<>();
	
    Document mains, actors, casts;

    public DomParser() {
    	list_dirfilms = new ArrayList<>();
    	list_actors = new ArrayList<>();
    	list_sim = new ArrayList<>();
    }
    
    public void runExample() {
    	
    	try {
    		//long t1=System.currentTimeMillis()/1000;
    		
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
            
            /*
            long t2=System.currentTimeMillis()/1000;
            System.out.println(t2-t1);*/
    	}
    	catch (Exception e){
    		System.out.printf("connection error: %s", e.getMessage());
    	}
    }

    private void parseXmlFile() {
        //get the factory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            //Using factory get an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();

            //parse using builder to get DOM representation of the XML file
            mains = db.parse("xml/mains243.xml");
            actors = db.parse("xml/actors63.xml");
            casts = db.parse("xml/casts124.xml");
            
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (SAXException se) {
            se.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void parseDocument() {
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
    
    private directorfilms getdirectorfilms(Element dirfilm) {
    	String director=getTextValue(dirfilm, "dirname");
    	
    	NodeList films=dirfilm.getElementsByTagName("film");
    	List<film> list_films=new ArrayList<>();
    	
        if (films != null && films.getLength() > 0) {
            for (int i = 0; i < films.getLength(); i++) {
                //Get each film element
                Element fm = (Element) films.item(i);
                
                String movie_id=getTextValue(fm, "fid");
                String title=getTextValue(fm, "t");
                Integer year=getIntValue(fm, "year");
                //if(movie_id == null || title == null || year == null || director == null) continue;
                if(movie_id == null) {
                	System.out.println("Invalid: main.xml tag <fid> is null");
                	continue;
                }
                if(title == null) {
                	System.out.println("Invalid: main.xml tag <t> is null");
                	continue;
                }
                if(year == null) {
                	System.out.println("Invalid: main.xml tag <year> is null");
                	continue;
                }
                if(director == null) {
                	System.out.println("Invalid: main.xml tag <dirname> is null");
                	continue;
                }
                
                director=director.trim();
                movie_id=movie_id.trim();
                title=title.trim();
                if(movie_id.length() == 0) {
                	//System.out.println("movies id is empty");
                	System.out.println("Invalid: main.xml tag <fid> is empty");
                	continue;
                }
                if(title.length() == 0) {
                	//System.out.println("movies title is empty");
                	System.out.println("Invalid: main.xml tag <t> is empty");
                	continue;
                }
                if(director.length() == 0) {
                	//System.out.println("movies director is empty");
                	System.out.println("Invalid: main.xml tag <dirname> is empty");
                	continue;
                }
                
                //Get list of genres.
                List<String> list_genres=new ArrayList<>();
                NodeList genres = fm.getElementsByTagName("cat");
                if(genres != null && genres.getLength() > 0) {
                	for(int j=0; j < genres.getLength(); j++) {
                		if(genres.item(j).getFirstChild() == null) {
                			//System.out.println("tag cat is null");
                			continue;
                		}
                		String str_genre=genres.item(j).getFirstChild().getNodeValue();
                		
                		if(str_genre == null) continue;
                		str_genre=str_genre.trim();
                		if(str_genre.length() == 0) continue;
                		list_genres.add(str_genre);
                	}
                }
                list_films.add(new film(movie_id, title, year, list_genres));
            }
        }
    	return new directorfilms(director, list_films);
    }
    
    private actor getactor(Element acs) {
    	String id=getTextValue(acs, "stagename");
    	if(id == null) {
    		return null;
    	}
    	
    	//Check if acs element has a complete firstname and lastname.
    	String first_name=getTextValue(acs, "firstname");
    	String last_name=getTextValue(acs, "familyname");
    	if(first_name == null) {
    		System.out.println("Invalid: actors.xml tag <firstname> is null");
    		return null;
    	}
    	if(last_name == null) {
    		System.out.println("Invalid: actors.xml tag <familyname> is null");
    		return null;
    	}
    	first_name=first_name.trim();
    	last_name=last_name.trim();

    	if(first_name.length() == 0) {
    		System.out.println("Invalid: actors.xml tag <firstname> is empty");
    		return null;
    	}
    	if(last_name.length() == 0) {
    		//System.out.println("actors tag <familyname> is empty");
    		System.out.println("Invalid: actors.xml tag <familyname> is empty");
    		return null;
    	}
    	
    	String name=first_name+" "+last_name;
    	Integer year=getIntValue(acs, "dob");
    	return new actor(id, name, year);
    }
    
    private stars_in_movies get_sim(Element sim) {
    	String starId=getTextValue(sim, "a");
    	if(starId == null) return null;
    	if(starId.length() == 0) {
    		System.out.println("Invalid: casts.xml tag <a> is empty");
    		return null;
    	}
    	
    	String movieId=getTextValue(sim, "f");
    	if(movieId == null) return null;
    	if(movieId.length() == 0) {
    		System.out.println("Invalid: casts.xml tag <f> is empty");
    		return null;
    	}
    	
    	String title=getTextValue(sim, "t");
    	if(title == null) return null;
    	if(title.length() == 0) {
    		System.out.println("Invalid: casts.xml tag <t> is empty");
    		return null;
    	}
    	return new stars_in_movies(starId, movieId, title);
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
            	/*
            	System.out.printf("Invalid: tag <%s> is null", tagName);
            	System.out.println();*/
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
    		//System.out.printf("Invalid: wrong integer format %s", getTextValue(ele, tagName));
    		System.out.printf("Invalid: wrong integer fomrat: tag <%s> : %s", tagName, getTextValue(ele, tagName));
    		System.out.println();
    		return null;
    	}
        return res;
    }
    
    class mv_info{
    	String id;
    	String title;
    	Integer year;
    	String director;
    	mv_info(String id, String title, Integer year, String director){
    		this.id=id;
    		this.title=title;
    		this.year=year;
    		this.director=director;
    	}
    }
    private void insert_movies(Connection dbcon) {
    	try {
    		String query = "";
    		List<mv_info> list_info=new ArrayList<>();
    		
    		query = "INSERT INTO movies (id, title, year, director) VALUES(?,?,?,?);";
    		PreparedStatement psInsertRecord=null;
    		dbcon.setAutoCommit(false);
    		psInsertRecord=dbcon.prepareStatement(query);
    		
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
    				
    				//check is the movie exists.
    				//cache the result.
    				/*
    				if(set_movies_id.contains(movie_id)) {
    					System.out.printf("movies id %s exist", movie_id);
    					System.out.println();
    					continue;
    				}
    				set_movies_id.add(movie_id);*/
    				if(map_movies.containsKey(movie_id)) {
    					/*
    					System.out.printf("movies id %s exist", movie_id);
    					System.out.println();*/
    					continue;
    				}
    				map_movies.put(movie_id, title);
    				
    				query = "SELECT 1 FROM movies WHERE movies.id=?";
    				insertStatement = dbcon.prepareStatement(query);
    				insertStatement.setString(1, movie_id);
    				rs=insertStatement.executeQuery();
    				if(rs.next()) {
    					/*
    					System.out.printf("movies id %s exist", movie_id);
    					System.out.println();*/
    					continue;
    				}
    				insertStatement.close();

    				list_info.add(new mv_info(movie_id, title, year, director));
    			}
    		}
    		Collections.sort(list_info, new Comparator<mv_info>() {
    			public int compare(mv_info a, mv_info b) {
    				return a.id.compareTo(b.id);
    			}
    		});
    		for(mv_info ele : list_info) {
    			psInsertRecord.setString(1, ele.id);
    			psInsertRecord.setString(2, ele.title);
    			psInsertRecord.setInt(3, ele.year);
    			psInsertRecord.setString(4, ele.director);
    			psInsertRecord.addBatch();
    		}
    		psInsertRecord.executeBatch();
    		dbcon.commit();
    	}
    	catch (Exception e){
    		System.out.printf("insert movies error %s", e.getMessage());
    	}
    }

    private void insert_genres(Connection dbcon) {
    	try {
    		String query = "";  		
    		
    		query = "INSERT INTO genres (name) VALUES(?);";
    		PreparedStatement psInsertRecord=null;
    		dbcon.setAutoCommit(false);
    		psInsertRecord=dbcon.prepareStatement(query);
    		
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
    					if(map_genres.containsKey(gre)) continue;
    					
    					query = "SELECT * FROM genres WHERE genres.name = ?;";
    					insertStatement = dbcon.prepareStatement(query);
    					insertStatement.setString(1, gre);
    					rs = insertStatement.executeQuery();
    					
    					if(rs.next()) {
    						/*
    						System.out.printf("genres %s exists", gre);
    						System.out.println();*/
    						Integer genre_id=rs.getInt("id");
    						map_genres.put(gre, genre_id);
    						continue;
    					}
    					
    					//Insert the genre that is not in the 'genres' table.
    					query = "INSERT INTO genres (name) VALUES(?);";
    					insertStatement = dbcon.prepareStatement(query);
    					insertStatement.setString(1, gre);
    					int af = insertStatement.executeUpdate();
    					/*
    					if(af != 0) {
    						System.out.printf("Succeess: %s", insertStatement);
    						System.out.println();
    					}
    					else {
    						System.out.printf("Fail: %s", insertStatement);
    						System.out.println();
    					}*/

    					//Get genre number and record it.
    					query = "SELECT * FROM genres WHERE genres.name = ?;";
    					insertStatement = dbcon.prepareStatement(query);
    					insertStatement.setString(1, gre);
    					rs = insertStatement.executeQuery();
    					if(rs.next()) {
    						Integer genre_id=rs.getInt("id");
    						map_genres.put(gre, genre_id);
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
    		
    		query = "INSERT INTO genres_in_movies (genreId, movieId) VALUES(?,?);";
    		PreparedStatement psInsertRecord=null;
    		dbcon.setAutoCommit(false);
    		psInsertRecord=dbcon.prepareStatement(query);
    		
    		for(int i=0; i<list_dirfilms.size(); i++) {
    			directorfilms di_films=list_dirfilms.get(i);
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
    					/*
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
    					}*/

    					Integer genre_id=map_genres.get(gre);
    					if(genre_id == null) {
    						//System.out.printf("Genre %s doesn't exist", gre);
    						System.out.printf("inconsistent records: genre %s doesn't exist", gre);
    						System.out.println();
    						continue;
    					}
    					
    					//Check if the query exists. Cache the result.
    					String gim_query=String.valueOf(genre_id)+movie_id;
    					if(set_gim.contains(gim_query)) continue;
    					set_gim.add(gim_query);
    					
    					query = "SELECT * FROM genres_in_movies WHERE genres_in_movies.genreId = ? AND genres_in_movies.movieId = ?;";
    					insertStatement = dbcon.prepareStatement(query);
    					insertStatement.setInt(1, genre_id);
    					insertStatement.setString(2, movie_id);
    					rs = insertStatement.executeQuery();
    					if(rs.next()) {
    						/*
    						System.out.printf("genre_id %d and movie_id %s exists", genre_id, movie_id);
    						System.out.println();*/
    						continue;
    					}
    					insertStatement.close();
    					
    					psInsertRecord.setInt(1, genre_id);
    					psInsertRecord.setString(2, movie_id);
    					psInsertRecord.addBatch();
						
    				}
    			}
    		}
    		psInsertRecord.executeBatch();
            dbcon.commit();
    	}
    	catch (Exception e){
    		System.out.printf("insert genres in movies error %s", e.getMessage());
    	}
    }	
    
    class star_info {
    	String id;
    	String name;
    	Integer year;
    	star_info(String id, String name, Integer year){
    		this.id=id;
    		this.name=name;
    		this.year=year;
    	}
    }
    private void insert_stars(Connection dbcon) {
    	try {
    		PreparedStatement insertStatement = null;
			ResultSet rs = null;
    		String query = "";
    		List<star_info> list_info=new ArrayList<>();
    		
    		query = "INSERT INTO stars (id, name, birthYear) VALUES(?,?,?);";
    		PreparedStatement psInsertRecord=null;
    		dbcon.setAutoCommit(false);
    		psInsertRecord=dbcon.prepareStatement(query);
    		
    		for(int i=0; i<list_actors.size(); i++) {
    			actor ac = list_actors.get(i);
    			if(ac == null) continue;

				String id=ac.id;
			    String name=ac.name;
				Integer year=ac.year;
				
				//check if star's id exists. Cache the result.
				if(set_stars_id.contains(id)) {
					/*
					System.out.printf("stars id %s exists", id);
					System.out.println();*/
					continue;
				}
				set_stars_id.add(id);
				
				query = "SELECT * FROM stars WHERE stars.id = ?;";
				insertStatement = dbcon.prepareStatement(query);
				insertStatement.setString(1, id);
				rs = insertStatement.executeQuery();
				if(rs.next()) {
					/*
					System.out.printf("star's id %s exists", id);
					System.out.println();*/
					continue;
				}
				insertStatement.close();
				list_info.add(new star_info(id, name, year));
    		}
    		
    		Collections.sort(list_info, new Comparator<star_info>() {
    			public int compare(star_info a, star_info b) {
    				return a.id.compareTo(b.id);
    			}
    		});
    		for(star_info ele : list_info) {
    			psInsertRecord.setString(1, ele.id);
				psInsertRecord.setString(2, ele.name);
				if(ele.year == null) psInsertRecord.setNull(3, Types.INTEGER);
				else psInsertRecord.setInt(3, ele.year);
				psInsertRecord.addBatch();
    		}
    		psInsertRecord.executeBatch();
    		dbcon.commit();
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
    		
    		query = "INSERT INTO stars_in_movies (starId, movieId) VALUES(?,?);";
    		PreparedStatement psInsertRecord=null;
    		dbcon.setAutoCommit(false);
    		psInsertRecord=dbcon.prepareStatement(query);
    		
    		for(int i=0; i<list_sim.size(); i++) {
    			stars_in_movies sim = list_sim.get(i);
    			if(sim == null) continue;

				String starId=sim.starId;
				String movieId=sim.movieId;
				String title=sim.title;
				
				//check if both starId and movieId exist.
				/*
				query = "SELECT * FROM stars WHERE stars.id = ?;";
				insertStatement = dbcon.prepareStatement(query);
				insertStatement.setString(1, starId);
				ResultSet rs1 = insertStatement.executeQuery();
				query = "SELECT * FROM movies WHERE movies.id = ?;";
				insertStatement = dbcon.prepareStatement(query);
				insertStatement.setString(1, movieId);
				ResultSet rs2 = insertStatement.executeQuery();
				if(!rs1.next()) {
					System.out.printf("starId %s does't exist", starId);
					System.out.println();
					continue;
				}
				if(!rs2.next()) {
					System.out.printf("movieId %s does't exist", movieId);
					System.out.println();
					continue;
				}*/
				
				//Cache the result
				if(!set_stars_id.contains(starId)) {
					/*
					//System.out.printf("starId %s doesn't exist", starId);
					System.out.println();*/
					//System.out.printf("inconsistent records: starId %s doesn't exist", starId);
					//System.out.println();
					System.out.printf("inconsistent records: casts.xml tag<a>: %s", starId);
					System.out.println();
					continue;
				}
				if(!map_movies.containsKey(movieId)) {
					//System.out.printf("movieId %s doesn't exist", movieId);
					//System.out.println();
					//System.out.printf("inconsistent records: movieId %s doesn't exist", movieId);
					//System.out.println();
					System.out.printf("inconsistent records: casts.xml tag<f>: %s", movieId);
					System.out.println();
					continue;
				}
				if(!map_movies.get(movieId).equals(title)) {
					/*
					System.out.printf("inconsistent records: movid id: %s, movie title: %s", movieId, title);
					System.out.println();*/
					System.out.printf("inconsistent records: casts.xml tag<f>: %s, tag<t>: %s", movieId, title);
					System.out.println();
					continue;
				}
				
				//check if query exists.
				if(set_sim.contains(starId+movieId)) continue;
				set_sim.add(starId+movieId);
				
				query = "SELECT * FROM stars_in_movies WHERE stars_in_movies.starId = ? AND stars_in_movies.movieId = ?;";
				insertStatement = dbcon.prepareStatement(query);
				insertStatement.setString(1, starId);
				insertStatement.setString(2, movieId);
				rs = insertStatement.executeQuery();
				if(rs.next()) {
					/*
					System.out.printf("starId %s AND movieId %s exist", starId, movieId);
					System.out.println();*/
					continue;
				}
				insertStatement.close();

				psInsertRecord.setString(1, starId);
				psInsertRecord.setString(2, movieId);
				psInsertRecord.addBatch();
    		}
    		psInsertRecord.executeBatch();
    		dbcon.commit();
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
