package parsexml;
import java.util.ArrayList;
import java.util.List;

public class film {
	public String id;
	public String title;
	public Integer year;
	public List<String> genres;
	
	public film() {}
	
	public film(String id, String title, Integer year, List<String> genres) {
		this.id=id;
		this.title=title;
		this.year=year;
		this.genres=genres;
	}
}