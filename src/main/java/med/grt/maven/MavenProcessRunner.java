package med.grt.maven;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import med.grt.Globals;
import med.grt.Utils;

/**
 * maven的命令运行器
 * @author bmiao
 */
public class MavenProcessRunner {

	private static File buildDateStoreFile;
	
	static {
		buildDateStoreFile = new File(Globals.getProjectHome() + "/.buildDateStoreFile");
	}

	private boolean buildSuccessed;
	
	/**
	 * 取得上一次构建完成时间
	 * @return	
	 * @throws Exception 
	 */
	public String getLastFinishDateTimeString() throws Exception {
		String lastFinishDateTime = null;
		if(buildDateStoreFile.exists()) {
			BufferedReader in = new BufferedReader(new FileReader(buildDateStoreFile));
			String fileContent;
            while ((fileContent = in.readLine()) != null) 
            {
            	lastFinishDateTime  = fileContent;
            }
            in.close();
		}
		return lastFinishDateTime;
	}
	
	/**
	 * 执行mvn clean install命令
	 * @param mavenSettings
	 * @param pom
	 * @throws Exception
	 */
	public void install(String mavenSettings,String pom) throws Exception {
		
		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.directory(new File(Globals.getProjectHome()));
		System.out.println(processBuilder.directory().toString());	
		List<String> command = new ArrayList<String>();
		command.add("mvn" + (Globals.isWindows()?".bat":""));
		command.add("clean");
		command.add("install");
		
		if(!Utils.isEmpty(mavenSettings)) {
			command.add("-s");
			command.add(mavenSettings);
		}
		command.add("-Dmaven.test.skip=true");
		command.add("-f");
		command.add(pom);
		processBuilder.command(command);
		
		printProcess(processBuilder.start());
        
		if(buildSuccessed) {
			this.setFinishDateTime();
		}
		
	}

	private void printProcess(Process process) throws IOException {
		BufferedReader results=new BufferedReader(new InputStreamReader(process.getInputStream()));  
        String s;  
        boolean err=false;
        while((s=results.readLine())!=null) {
        	if(s.endsWith("BUILD SUCCESS")) {
        		buildSuccessed = true;
        	}
        	System.out.println(s);
        }
              
        BufferedReader errors=new BufferedReader(new InputStreamReader(process.getErrorStream()));  
        while((s=errors.readLine())!=null){  
            System.err.println(s);  
            err=true;  
        }
	}
	
	/**
	 * 设置本次构建完成时间
	 * @throws Exception 
	 */
	public void setFinishDateTime() throws Exception {
		Date finishDate = new Date();
		
	    if (buildDateStoreFile.exists()) {
	    	buildDateStoreFile.delete();
	    }
	    buildDateStoreFile.createNewFile();
	    FileWriter fileWriter = new FileWriter(buildDateStoreFile);
	    PrintWriter writer = new PrintWriter(fileWriter);
	    writer.write(Utils.formateDate(finishDate, "yyyy-MM-dd HH:mm:ss"));
	    writer.flush();
	    writer.close();
		
	}
}
