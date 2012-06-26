package med.grt;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

	public static Date parse(String dateString,String formate) {
		DateFormat format = new SimpleDateFormat(formate);        
		Date date = null;   
	      
		try {   
		    date = format.parse(dateString);
		} catch (ParseException e) {   
		    e.printStackTrace();   
		}
		return date;
	}
	
	public static String formateDate(Date date,String formate) {
		DateFormat format = new SimpleDateFormat(formate);
		String str = null;                 
		    
		str = format.format(date);
		return str;
	}
	
	public static boolean isEmpty(String str) {
		return str == null || str.trim().equals("");
	}
	

	/**
	 * 取得当前jar运行的目录，在IDE运行期间则是target/classes目录
	 * @return
	 */
	public static String getCurrentJarDirectory() {
		String currentLocation = Utils.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		return currentLocation.substring(0, currentLocation.lastIndexOf("/"));
	}
	
	
}
