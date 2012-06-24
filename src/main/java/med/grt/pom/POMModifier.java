package med.grt.pom;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

import edu.nyu.cs.javagit.api.DotGit;
import edu.nyu.cs.javagit.api.JavaGitException;
import edu.nyu.cs.javagit.api.commands.GitLogOptions;
import edu.nyu.cs.javagit.api.commands.GitLogResponse.Commit;
import edu.nyu.cs.javagit.api.commands.GitLogResponse.CommitFile;

public class POMModifier {

	/**
	 * baseline提交人：程序会以此提交人找到最新的那次baseline提交记录
	 */
	private String baseline_committer;
	
	/**
	 * 项目目录
	 */
	private String project_home;
	
	private Properties properties = new Properties();
	private Document doc;
	
	private String propertiesPath = "modifier.properties";
	
	private List<Element> modulesElement;

	/**
	 * 
	 * 生成用于mvn install的POM
	 * @throws Exception
	 */
	public void createInstallPOM() throws Exception {
		this.init();	
		
		// 取得改动过的文件
		List<String> changedModules = this.getChangedModules();
		//info(changedModules.toString());
		// 从Alineo/Alineo目录中的POM中取出原本要build的项目
		String canidateModulesPattern = this.getCanidateModulesPattern();
		//info(canidateModulesPattern);
		// 从改动过的文件中筛选出要重新build的项目
		List<String> modulesThatNeedToBuild = this.filterOutModulesThatNeedToBeReinstall(changedModules,canidateModulesPattern);
		//info(modulesThatNeedToBuild.toString());
		
		// 在Alineo/Alineo目录下生成一个新的POM文件
		this.rebuildPOM(modulesThatNeedToBuild);
	}

	private void info(String info) {
		System.out.println(info);
	}
	
	private List<String> getChangedModules() throws Exception {
		String changeFiles = this.getChangeFileNames();
		List<String> changedModules = new ArrayList<String>();
		if(!"".equals(changeFiles)) {
			String[] changeFilesArray = changeFiles.split(";");
			String changeModuleName;
			for (String changeFile : changeFilesArray) {
				changeModuleName = changeFile.substring(changeFile.indexOf("Alineo/"), changeFile.indexOf("/src")+1);
				if(!changedModules.contains(changeModuleName)) changedModules.add(changeModuleName);
			}		
		}
		return changedModules;
	}

	/**
	 * 程序初始化
	 * @throws Exception
	 */
	public void init() throws Exception {
		// 初始化配置
		properties.load(ClassLoader.getSystemResourceAsStream(propertiesPath));
		project_home = properties.getProperty("project_home");
		baseline_committer = properties.getProperty("baseline_committer");
		
		//　初始化jdom
		String pomPath = project_home + "/Alineo/Alineo/pom.xml";
		SAXBuilder builder = new SAXBuilder();
		doc = builder.build(pomPath);
		Element project = doc.getRootElement();
		
		modulesElement = getModulesElement(project);
	}

	/**
	 * 根据需要重新构建的项目创建一份新的POM
	 * @param modulesThatNeedToBuild
	 * @throws Exception
	 */
	public  void rebuildPOM(List<String> modulesThatNeedToBuild) throws Exception {
		
		modulesElement.clear();
		Element module;
		for (String moduleName : modulesThatNeedToBuild) {
			module = new Element("module");
			module.setText(moduleName);
			modulesElement.add(module);
		}
		
		List<Element> elements = doc.getRootElement().getChildren();
		for (Element parent : elements) {
			if(parent.getName().equals("parent")) {
				List<Element> parentChildren = parent.getChildren();
				for (Element c : parentChildren) {
					if(c.getName().equals("relativePath")) {
						c.setText(c.getText().replace("../", "Alineo/"));
					}
				}
				
			}
		}
		
		// 创建一份新的POM文件
		String newPomPath = project_home + "/pom_for_install.xml";
		XMLOutputter outputter=new XMLOutputter();
		outputter.output(doc,new FileOutputStream(newPomPath));
		
	}

	/**
	 * 从改动过的文件中筛选出要重新编译的项目
	 * @param changeFiles
	 * @param originBuildModules
	 * @return
	 */
	public  List<String> filterOutModulesThatNeedToBeReinstall(List<String> changedModules,
			String canidateModulesPattern) {
		
		List<String> modulesThatNeedToBuild = new ArrayList<String>();
		
		for (String changedModule : changedModules) {
			if(Pattern.matches(canidateModulesPattern, changedModule)) {
				modulesThatNeedToBuild.add(changedModule);
			}
		}
		
		return modulesThatNeedToBuild;
	}

	private boolean isSkiped(String module) {
		return module.equals("alineo") || module.equals("../ServiceImpl");
	}

	/**
	 * 从Alineo/Alineo目录中的POM中取出原本要build的项目
	 * @return
	 * @throws Exception
	 */
	public  String getCanidateModulesPattern() throws Exception {
		
		String canidateModules = "";
		String moduleName;
		for (Element moduleElement : modulesElement) {
			if(!isSkiped(moduleElement.getText())) {
				// 补全module路径
				if(moduleElement.getText().startsWith("../")) {
					moduleName = moduleElement.getText().replace("../", "Alineo/");
				} else {
					moduleName = "Alineo/Alineo/" + moduleElement.getText() ;
				}
				canidateModules += moduleName.replace("-", "\\-") + "/.*" + "|";
			}					
		}	
		
		return canidateModules;
	}

	/**
	 * 很奇怪，不清楚为什么不能直接去取modules标签,JDOM的Bug??
	 * @param children
	 * @return
	 */
	public  List<Element> getModulesElement(Element project) {
		List<Element> children = project.getChildren();
		//Element a = project.getChild("modules");
		List<Element> modulesElement = null;
		for (Element element : children) {
			if(element.getName().equals("modules")) {
				modulesElement = element.getChildren();
				break;
			}
		}
		return (modulesElement == null?new ArrayList<Element>():modulesElement);
	}

	/**
	 * 取得自最新一次baseline以来改动过的文件
	 * @return
	 * @throws JavaGitException
	 * @throws IOException
	 */
	public  String getChangeFileNames() throws Exception {
		// 设置Git目录
		File repositoryDirectory = new File(project_home);
		DotGit dotGit = DotGit.getInstance(repositoryDirectory);
		// 设置gitlog参数
		GitLogOptions gitLogOptions = new GitLogOptions();
		// 设置baseline提交人
		gitLogOptions.setOptLimitAuthor(true, baseline_committer);
		// 设置只取最新的一次的baseline提交
		gitLogOptions.setOptLimitCommitMax(true, 1);
		String changeFiles = "";
		List<Commit> commits = dotGit.getLog(gitLogOptions);
		if(commits != null && !commits.isEmpty()) {
			// 取得最新一次提交的baseline并以此为起点取得之后所有的改动
			Commit latestBaseline = commits.get(0);
			
			// 不限制谁提交或提交了多少次
			gitLogOptions.setOptLimitCommitMax(false, 0);
			gitLogOptions.setOptLimitAuthor(false, null);
			// 需要取得文件名
			gitLogOptions.setOptFileDetails(true);
			gitLogOptions.setOptLimitCommitSince(true, latestBaseline.getDateString());
			
			for (Commit c : dotGit.getLog(gitLogOptions)) {
				//System.out.println(c.getMessage());
				//System.out.println(c.getDateString());
				if(c.getFiles() != null) {
					for(CommitFile commitFile: c.getFiles()) {
						changeFiles += commitFile.getName()+";";
					}	
				}						    
			}
		}
		
		return changeFiles;
	}

	
}
