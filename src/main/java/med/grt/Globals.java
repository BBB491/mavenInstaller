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
	
	public static void loadProperties() throws Exception {
		InputStream inputStream = getCustomModifierPropertiesLocation();
		properties.load(inputStream);
	}

	public static String getProjectHome() {
		return getProperty("project.home");
	}
	
	public static String getProperty(String key) {
		return properties.getProperty(key);
	}
	
	public static InputStream getCustomModifierPropertiesLocation() throws FileNotFoundException {
		String propertyPath = Utils.getCurrentJarDirectory() + propertiesPath;
		System.out.println(propertyPath);
		File f = new File(propertyPath);
		FileInputStream fileInputStream = null;
		if(f.exists()) {
			fileInputStream = new FileInputStream(f);
		} else {
			throw new FileNotFoundException(propertyPath + " not found,please be sure this file is placed in the directory " + Utils.getCurrentJarDirectory());
		}
		
		return fileInputStream;
	}

	public static String getMavenSettings() {
		return getProperty("project.mavenSettings");
	}
	
	public static boolean isWindows() {
		return System.getProperty("os.name").contains("Windows");
	}
}
