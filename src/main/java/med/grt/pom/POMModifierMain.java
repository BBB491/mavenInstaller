package med.grt.pom;


/**
 * 从git log中取得已经修改的Alineo的代码（文件名），并从中筛选出需要构建的项目
 * @author bmiao
 *
 */
public class POMModifierMain {

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			
			POMModifier pomModifier = new POMModifier();
			pomModifier.createInstallPOM();
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
}
