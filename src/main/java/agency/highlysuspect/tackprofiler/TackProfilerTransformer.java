package agency.highlysuspect.tackprofiler;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

@SuppressWarnings("unused")
public class TackProfilerTransformer implements IClassTransformer, Opcodes {
	private static final String EXT = "agency/highlysuspect/tackprofiler/LibLoaderChainedLibraryExt";
	private static final String HOOK = "agency/highlysuspect/tackprofiler/LibLoaderChainedLibraryHook";

	private static final String LIBLOADERCHAINED_LIBRARY = "org/minimallycorrect/libloader/LibLoaderChained$Library";
	private static final String LIBLOADERCHAINED_LIBRARY$DOTS = LIBLOADERCHAINED_LIBRARY.replace('/', '.');

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if(LIBLOADERCHAINED_LIBRARY$DOTS.equals(name)) {
			TackProfiler.LOG.info("Got LibLoaderChained$Library, removing hack that let me transform this class");
			TransformerExceptionsHack.repeal();

			TackProfiler.LOG.info("Transforming LibLoaderChained$Library");

			ClassReader reader = new ClassReader(basicClass);
			ClassNode node = new ClassNode(ASM4);
			reader.accept(node, 0);

			//add extension interface
			node.interfaces.add(EXT);

			//implement the extension interface
			node.methods.add(getter("tack$file", LIBLOADERCHAINED_LIBRARY, "file", "Ljava/lang/String;"));
			node.methods.add(getter("tack$url", LIBLOADERCHAINED_LIBRARY, "url", "Ljava/lang/String;"));

			node.methods.add(setter("tack$setFile", LIBLOADERCHAINED_LIBRARY, "file", "Ljava/lang/String;"));
			node.methods.add(setter("tack$setSource", LIBLOADERCHAINED_LIBRARY, "source", "Ljava/io/File;"));
			node.methods.add(setter("tack$setURL", LIBLOADERCHAINED_LIBRARY, "url", "Ljava/lang/String;"));

			//hook the top of LibLoaderChained$Library.save
			MethodNode save = getMethod(node, "save");
			InsnList hook = new InsnList();
			hook.add(new VarInsnNode(ALOAD, 0)); //this
			hook.add(new TypeInsnNode(CHECKCAST, EXT));
			hook.add(new VarInsnNode(ALOAD, 1)); //extractionDir
			hook.add(new MethodInsnNode(INVOKESTATIC, HOOK, "saveHook", "(L" + EXT + ";Ljava/io/File;)V", false));
			save.instructions.insert(hook);

			ClassWriter writer = new ClassWriter(0); //No need to compute maxs or frames
			node.accept(writer);

			TackProfiler.LOG.info("Done transforming LibLoaderChained$Library");

			return writer.toByteArray();
		}

		return basicClass;
	}

	private static MethodNode getMethod(ClassNode node, String methodName) {
		for(MethodNode m : node.methods) {
			if(m.name.equals(methodName)) return m;
		}

		throw new IllegalStateException("Couldn't find method named " + methodName + " in " + node.name);
	}

	private static int publicize(int access) {
		return access & ~(ACC_PRIVATE | ACC_PROTECTED) | ACC_PUBLIC;
	}

	private static MethodNode getter(String getterName, String owner, String fieldName, String fieldDesc) {
		MethodNode getter = new MethodNode(ASM4,
			ACC_PUBLIC,
			getterName,
			"()" + fieldDesc,
			null, null);

		getter.instructions.add(new VarInsnNode(ALOAD, 0));
		getter.instructions.add(new FieldInsnNode(GETFIELD, owner, fieldName, fieldDesc));
		getter.instructions.add(new InsnNode(ARETURN));

		getter.maxStack = 1;
		getter.maxLocals = 1;

		return getter;
	}

	private static MethodNode setter(String setterName, String owner, String fieldName, String fieldDesc) {
		MethodNode setter = new MethodNode(ASM4,
			ACC_PUBLIC,
			setterName,
			"(" + fieldDesc + ")V",
			null, null);

		setter.instructions.add(new VarInsnNode(ALOAD, 0)); //this
		setter.instructions.add(new VarInsnNode(ALOAD, 1)); //argument
		setter.instructions.add(new FieldInsnNode(PUTFIELD, owner, fieldName, fieldDesc));
		setter.instructions.add(new InsnNode(RETURN));

		setter.maxStack = 2;
		setter.maxLocals = 2;

		return setter;
	}
}
