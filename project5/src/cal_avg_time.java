import java.util.*;
import java.io.*;

public class cal_avg_time {
	public static void main(String[] args) {
		File file = new File(args[0]);
		//File file = new File("../log/movie_res");
		//File file = new File("log/movie_res"); 
		Scanner sc=null;
		try {
			sc=new Scanner(file);
		}
		catch(FileNotFoundException e) {
			e.printStackTrace();
		}
		
		float TS=0, TJ=0, total=0;
		
		while(sc.hasNextLine()) {
			String nl=sc.nextLine();
			if(nl.equals("")) continue;
			String[] nl_arr=nl.split(" ");
			TS+=(float)Integer.parseInt(nl_arr[0])/1000000;
			TJ+=(float)Integer.parseInt(nl_arr[1])/1000000;
			total+=1;
		}
		
		//System.out.println("Total lines: "+String.valueOf(total));
		System.out.println("Avg TS: "+String.valueOf(TS/total));
		System.out.println("Avg TJ: "+String.valueOf(TJ/total));
	}
}
