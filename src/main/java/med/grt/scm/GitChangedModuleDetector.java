package med.grt.scm;

import java.io.File;
import java.io.IOException;

import med.grt.Globals;
import edu.nyu.cs.javagit.api.DotGit;
import edu.nyu.cs.javagit.api.JavaGitException;
import edu.nyu.cs.javagit.api.commands.GitLogOptions;
import edu.nyu.cs.javagit.api.commands.GitLogResponse.Commit;
import edu.nyu.cs.javagit.api.commands.GitLogResponse.CommitFile;

/**
 * 基于Git的项目改动检查器
 * @author bmiao
 *
 */
public class GitChangedModuleDetector extends ChangedModuleDetector {

	/**
	 * git repository路径
	 */
	private String git_repository = Globals.getProjectHome();	
	
	/**
	 * 取得自最新一次baseline以来改动过的文件
	 * @return
	 * @throws JavaGitException
	 * @throws IOException
	 */
	protected String getChangeFileNames(String lastFinishDateTimeString) throws Exception {
		// 设置Git目录
		File repositoryDirectory = new File(git_repository);
		DotGit dotGit = DotGit.getInstance(repositoryDirectory);
		
		String changeFiles = "";
	
		System.out.println("Git log:");
		GitLogOptions gitLogOptions = new GitLogOptions();			
		// 不限制谁提交或提交了多少次
		gitLogOptions.setOptLimitCommitMax(false, 0);
		gitLogOptions.setOptLimitAuthor(false, null);
		// 需要取得文件名
		gitLogOptions.setOptFileDetails(true);
		gitLogOptions.setOptLimitCommitSince(true, lastFinishDateTimeString);
		
		for (Commit c : dotGit.getLog(gitLogOptions)) {
			
			System.out.println("Commit:" + c.getMessage());
			System.out.println(c.getDateString());
			
			if(c.getFiles() != null) {
				for(CommitFile commitFile: c.getFiles()) {
					System.out.println(commitFile.getName());
					changeFiles += commitFile.getName() + FILE_SEPERATOR;
				}	
			}						    
		}
		return changeFiles;
	}
}
