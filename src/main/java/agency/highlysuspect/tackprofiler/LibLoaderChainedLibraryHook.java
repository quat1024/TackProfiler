package agency.highlysuspect.tackprofiler;

import java.io.File;
import java.io.InputStream;

@SuppressWarnings("unused")
public class LibLoaderChainedLibraryHook {
	private static final File TACKPROFILER_JAR;

	static {
		try {
			TACKPROFILER_JAR = new File(TackProfiler.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		} catch (Exception e) {
			throw new RuntimeException("Failed to find my own jar", e);
		}
	}

	public static void saveHook(LibLoaderChainedLibraryExt ext, File extractionDir) {
		String libraryIdent = ext.toString(); //See LibLoaderChained$Library.toString, it's implemented

		TackProfiler.LOG.warn("TackProfiler: found library " + libraryIdent);
		if(ext.tack$file() != null) return; //Original method will pull the file locally

		String myFile = "cached-libloader-libs/" + libraryIdent + ".jar";
		try(InputStream in = TackProfiler.class.getClassLoader().getResourceAsStream(myFile)) {
			if(in == null) {
				TackProfiler.LOG.warn("Don't have a locally cached jar for " + libraryIdent + ", it will be downloaded from " + ext.tack$url());
			} else {
				TackProfiler.LOG.warn("Using TackProfiler-provided jar for " + libraryIdent + " instead of downloading from " + ext.tack$url());

				ext.tack$setFile(myFile); //Use this copy
				ext.tack$setSource(TACKPROFILER_JAR); //from this jar
				ext.tack$setURL(null); //instead of downloading it from a URL
			}
		} catch (Exception e) {
			TackProfiler.LOG.warn("TackProfiler failed to switcheroo a library", e);
		}
	}
}
