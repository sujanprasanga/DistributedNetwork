package lk.ac.mrt.distributed.messaging;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class SharedFiles
{
	private static SharedFiles instance = new SharedFiles();
	private final Set<String> shared;
	
	public static SharedFiles getInstance()
	{
		return instance ;
	}
	
	private SharedFiles()
	{
		shared = loadFileList();
	}

	private Set<String> loadFileList()
	{
		Set<String> s = new HashSet<>();
		Path p = FileSystems.getDefault().getPath("FileList");
	    try
	    {
	    	Random rn = new Random();
	    	List<String> files = Files.readAllLines(p);
	    	for(int i=0; i < 3 + new Random().nextInt(3); i++)
	    	{
	    		s.add(files.get(rn.nextInt(files.size())));
	    	}
	    }
	    catch(IOException e)
	    {
	    	e.printStackTrace();
	    }
	    return s;
	}

	public Set<String> getSharedFiles()
	{
		return shared;
	}

	public String[] getSharedFilesArray() {
		String[] a = new String[shared.size()];
		int i = 0;
		for(String s : getSharedFiles())
		{
			a[i] = s;
			i++;
		}
		return a;
	}
}
