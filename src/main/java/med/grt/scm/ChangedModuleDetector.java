package med.grt.scm;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import med.grt.Globals;
import med.grt.Utils;

public abstract class ChangedModuleDetector {
	
	protected static final String FILE_SEPERATOR = ",";
	private static String DEFAULT_SCM = "Git";
	private String excludeModulePartern = null;

	public String getExcludeModulePartern() {
		return excludeModulePartern;
	}

	public void setExcludeModulePartern(String excludeModulePartern) {
		this.excludeModulePartern = excludeModulePartern;
	}

	public static ChangedModuleDetector getDetector() {
		String scm = Globals.getProperty("scm");
		if(scm == null || "".equals(scm.trim())) {
			scm = DEFAULT_SCM;
		}
			
		
		ChangedModuleDetector changedModuleDetector = null;
		try {
			String detectorClassName = ChangedModuleDetector.class.getPackage().getName() + "." + scm + "ChangedModuleDetector";
			//System.out.println(detectorClassName);
			Class detectorClass = Class.forName(detectorClassName);
			changedModuleDetector = (ChangedModuleDetector)detectorClass.newInstance();
			// 初始化excludeModulePartern
			if(!Utils.isEmpty(Globals.getExcludeModules())) {
				changedModuleDetector.setExcludeModulePartern(Globals.getExcludeModules().replace(FILE_SEPERATOR, "|"));
			}	
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return changedModuleDetector;
	}
	
	/**
	 * 
	 * @param lastFinishDateTimeString 
	 * @return
	 * @throws Exception
	 */
	public List<String> getChangedModules(String lastFinishDateTimeString) throws Exception {
		String changeFiles = this.getChangeFileNames(lastFinishDateTimeString);
		List<String> changedModules = new ArrayList<String>();
		if(!"".equals(changeFiles)) {
			String[] changeFilesArray = changeFiles.split(FILE_SEPERATOR);
			String changeModuleName;
			int firstIndex;
			int lastIndex;
			for (String changeFile : changeFilesArray) {
				firstIndex = changeFile.indexOf("Alineo/");
				if(changeFile.lastIndexOf("pom.xml") > 0) {
					lastIndex = changeFile.indexOf("/pom.xml")+1;
				} else {
					lastIndex = changeFile.indexOf("/src")+1;
				}
				
				changeModuleName = changeFile.substring(firstIndex, lastIndex);
				
				if(!isSkiped(changeModuleName) && !changedModules.contains(changeModuleName)) changedModules.add(changeModuleName);
			}		
		} 
		return changedModules;
	}
	
	private boolean isSkiped(String changeModuleName) {
		boolean skip = false;
		if(!Utils.isEmpty(excludeModulePartern)) {
			skip = Pattern.matches(excludeModulePartern, changeModuleName);
		}
		return skip;
	}

	protected abstract String getChangeFileNames(String lastFinishDateTimeString) throws Exception;

	/**
	 * 使用统一的append方法可以避免使用了错误的分隔符
	 * @param changeFiles
	 * @param name
	 */
	protected void appendChangeFiles(String changeFiles, String name) {
		changeFiles += name + FILE_SEPERATOR;
	}
}
