package agency.highlysuspect.tackprofiler;

import java.io.File;

public interface LibLoaderChainedLibraryExt {
	//getters
	String tack$file();
	String tack$url();

	//setters
	void tack$setFile(String file);
	void tack$setSource(File source);
	void tack$setURL(String url);
}
