package agency.highlysuspect.tackprofiler;

import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@IFMLLoadingPlugin.Name("TackProfiler loading plugin")
@IFMLLoadingPlugin.SortingIndex(-10) //TickProfiler uses -1
public class TackProfiler implements IFMLLoadingPlugin {
	public static final Logger LOG = LogManager.getLogger("TackProfiler");

	static {
		LOG.info("TackProfiler initializing");

		//Allow transforming org.minimallycorrect.libloader.LibLoader
		TransformerExceptionsHack.apply();

		//Do it here, instead of in getASMTransformerClass, because TickProfiler's CoreMod initializes
		//LibLoader in an IFMLLoadingPlugin static init block
		Launch.classLoader.registerTransformer("agency.highlysuspect.tackprofiler.TackProfilerTransformer");
	}

	@Override
	public String[] getASMTransformerClass() {
		return new String[0];
	}

	@Override
	public String getModContainerClass() {
		return null;
	}

	@Nullable
	@Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {

	}

	@Override
	public String getAccessTransformerClass() {
		return null;
	}
}
