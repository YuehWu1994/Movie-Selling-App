package parsexml;
import java.util.ArrayList;
import java.util.List;

public class directorfilms {
	public String director;
	public List<film> films;
	
	public directorfilms() {}
	
	public directorfilms(String director, List<film> films) {
		this.director=director;
		this.films=films;
	}
}