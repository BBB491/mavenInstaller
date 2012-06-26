package med.grt.scm;

import java.util.ArrayList;
import java.util.List;

import med.grt.Globals;

public abstract class ChangedModuleDetector {
	
	private static final String FILE_SEPERATOR = ";";
	private static String DEFAULT_SCM = "Git";

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
			for (String changeFile : changeFilesArray) {
				changeModuleName = changeFile.substring(changeFile.indexOf("Alineo/"), changeFile.indexOf("/src")+1);
				if(!changedModules.contains(changeModuleName)) changedModules.add(changeModuleName);
			}		
		} 
		return changedModules;
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
