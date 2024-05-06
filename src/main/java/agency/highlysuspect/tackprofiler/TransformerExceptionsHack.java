package agency.highlysuspect.tackprofiler;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;

import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

/**
 * This is really stinky stuff, but I have to block Forge from attempting to transformer-exclude org.minimallycorrect.libloader.LibLoader.
 */
public class TransformerExceptionsHack implements Set<String> {
	public TransformerExceptionsHack(Set<String> inner) {
		this.inner = inner;
	}

	private final Set<String> inner;

	//Just in case someone else is doing the same thing and repeal() fails to find TransformerExceptionsHack:
	private static boolean armed = true;

	private static final Field transformerExceptionsField;
	private static final String LIBLOADER = "org.minimallycorrect.libloader.LibLoader";

	static {
		Field f;
		try {
			f = LaunchClassLoader.class.getDeclaredField("transformerExceptions");
			f.setAccessible(true);
		} catch (Exception e) {
			throw new RuntimeException("Failed to find LaunchClassLoader transformerExceptions field", e);
		}
		transformerExceptionsField = f;
	}

	@SuppressWarnings("unchecked")
	public static void apply() {
		TackProfiler.LOG.info("Applying LaunchClassLoader transformerExceptions hack");

		try {
			armed = true;
			transformerExceptionsField.set(Launch.classLoader,
				new TransformerExceptionsHack((Set<String>) transformerExceptionsField.get(Launch.classLoader)));
		} catch (Exception e) {
			throw new RuntimeException("Failed to apply LaunchClassLoader transformerExceptions hack");
		}
	}

	public static void repeal() {
		TackProfiler.LOG.info("Repealing LaunchClassLoader transformerExceptions hack");

		try {
			//actually add the transformer exclusion now
			armed = false;
			Launch.classLoader.addTransformerExclusion(LIBLOADER);

			//try to remove the hack from the game before i cause more problems
			Object exc = transformerExceptionsField.get(Launch.classLoader);
			if(exc instanceof TransformerExceptionsHack) {
				TackProfiler.LOG.info("Removing TransformerExceptionsHack from LaunchClassLoader");
				transformerExceptionsField.set(Launch.classLoader, ((TransformerExceptionsHack) exc).inner);
			} else {
				TackProfiler.LOG.warn("Couldn't find TransformerExceptionsHack in LaunchClassLoader, it'll stick around");
			}
		} catch (Exception e) {
			TackProfiler.LOG.warn("Failed to repeal transformerExceptions hack");
		}
	}

	@Override
	public boolean add(String s) {
		if(armed && s.equals(LIBLOADER)) {
			//Don't delegate.
			return true;
		} else return inner.add(s);
	}

	//The rest is delegation

	@Override public int size() {return inner.size();}
	@Override public boolean isEmpty() {return inner.isEmpty();}
	@Override public boolean contains(Object o) {return inner.contains(o);}
	@Override public Iterator<String> iterator() {return inner.iterator();}
	@Override public Object[] toArray() {return inner.toArray();}
	@Override public <T> T[] toArray(T[] a) {return inner.toArray(a);}
	@Override public boolean remove(Object o) {return inner.remove(o);}
	@Override public boolean containsAll(Collection<?> c) {return inner.containsAll(c);}
	@Override public boolean addAll(Collection<? extends String> c) {return inner.addAll(c);}
	@Override public boolean retainAll(Collection<?> c) {return inner.retainAll(c);}
	@Override public boolean removeAll(Collection<?> c) {return inner.removeAll(c);}
	@Override public void clear() {inner.clear();}
	@Override public boolean equals(Object o) {return inner.equals(o);}
	@Override public int hashCode() {return inner.hashCode();}
	@Override public Spliterator<String> spliterator() {return inner.spliterator();}
}
