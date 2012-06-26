package med.grt.maven;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import med.grt.Globals;
import med.grt.Utils;
import med.grt.scm.ChangedModuleDetector;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

public class POMModifier {
	
	/**
	 * 项目目录
	 */
	private String project_home;
	
	private Document doc;
	private List<Element> modulesElement;
	private String lastFinishDateTimeString;
	
	public POMModifier(String lastFinishDateTimeString) {
		this.lastFinishDateTimeString = lastFinishDateTimeString;
	}

	/**
	 * 
	 * 生成用于mvn install的POM
	 * @return 
	 * @throws Exception
	 */
	public String createInstallPOM() throws Exception {
		this.init();	
		
		String pom;	
		// 如果上次构建时间为空，说明是第一次使用本组件，则要完整的build一次
		if(Utils.isEmpty(this.lastFinishDateTimeString)) {
			pom = this.rebuildPOM(null,true);
		} else {
			// 取得改动过的文件
			List<String> changedModules = ChangedModuleDetector.getDetector().getChangedModules(this.lastFinishDateTimeString);
			info("============================================================");
			info("changedModules:" + changedModules.toString());
			// 从Alineo/Alineo目录中的POM中取出原本要build的项目
			String canidateModulesPattern = this.getCanidateModulesPattern();
			//info(canidateModulesPattern);
			// 从改动过的文件中筛选出要重新build的项目
			List<String> modulesThatNeedToBuild = this.filterOutModulesThatNeedToBeReinstall(changedModules,canidateModulesPattern);
			info("============================================================");
			info("modulesThatNeedToBuild:" + modulesThatNeedToBuild.toString());
			// 在Alineo/Alineo目录下生成一个新的POM文件
			pom = this.rebuildPOM(modulesThatNeedToBuild,false);
		}
		
		return pom;
		
	}

	private void info(String info) {
		System.out.println(info);
	}

	/**
	 * 程序初始化
	 * @throws Exception
	 */
	public void init() throws Exception {
		
		project_home = Globals.getProjectHome();
		
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
	 * @param buildAll
	 * @return
	 * @throws Exception
	 */
	private String rebuildPOM(List<String> modulesThatNeedToBuild,boolean buildAll) throws Exception {
		
		if(!buildAll) {
			modulesElement.clear();
			Element module;
			for (String moduleName : modulesThatNeedToBuild) {
				module = new Element("module");
				module.setText(moduleName);
				modulesElement.add(module);
			}
			
			// 设置project/parent/relativePath
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
		}
		
		
		// 创建一份新的POM文件
		String newPomPath = project_home + "/pom_for_install.xml";
		XMLOutputter outputter=new XMLOutputter();
		outputter.output(doc,new FileOutputStream(newPomPath));
		
		return newPomPath;
	}

	/**
	 * 从改动过的文件中筛选出要重新编译的项目
	 * @param changeFiles
	 * @param originBuildModules
	 * @return
	 */
	private List<String> filterOutModulesThatNeedToBeReinstall(List<String> changedModules,
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
	private String getCanidateModulesPattern() throws Exception {
		
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
	private List<Element> getModulesElement(Element project) {
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

	
}
