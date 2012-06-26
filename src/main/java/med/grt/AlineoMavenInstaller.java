package med.grt;

import med.grt.maven.MavenProcessRunner;
import med.grt.maven.POMModifier;

/**
 * Ａlineo的mvn install组件，此组件可以进行差异性install,也就是说可以从git或者svn中检查代码的更改
 * 然后只install有代码更改的module
 * @author bmiao
 *
 */
public class AlineoMavenInstaller {

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			MavenProcessRunner mavenProcessRunner = new MavenProcessRunner();
			
			POMModifier pomModifier = new POMModifier(mavenProcessRunner.getLastFinishDateTimeString());
			String pom = pomModifier.createInstallPOM();
			// 执行maven install命令并记录当前完成时间
			
			mavenProcessRunner.install(Globals.getMavenSettings(), pom);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
}
