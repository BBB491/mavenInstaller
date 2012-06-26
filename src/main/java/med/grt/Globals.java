package med.grt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;


/**
 * 全局配置
 * @author bmiao
 *
 */
public class Globals {
	
	private static Properties properties = new Properties();
	private static String propertiesPath = "modifier.properties";
	
	static {
		try {
			InputStream inputStream = getCustomModifierPropertiesLocation();
			if(inputStream == null) {
				inputStream = ClassLoader.getSystemResourceAsStream(propertiesPath);
			}
			properties.load(inputStream);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getProjectHome() {
		return getProperty("project.home");
	}
	
	public static String getProperty(String key) {
		return properties.getProperty(key);
	}
	
	public static InputStream getCustomModifierPropertiesLocation() throws FileNotFoundException {
		String propertyPath = Utils.getCurrentJarDirectory() + "modifier.properties";
		File f = new File(propertyPath);
		FileInputStream fileInputStream = null;
		if(f.exists()) {
			fileInputStream = new FileInputStream(f);
		}
		
		return fileInputStream;
	}

	public static String getMavenSettings() {
		return getProperty("project.mavenSettings");
	}
}
