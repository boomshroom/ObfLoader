package boomshroom.obfloader;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ListIterator;

import org.apache.logging.log4j.Level;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import net.minecraft.client.Minecraft;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.LogWrapper;

public class ClassTransformer implements IClassTransformer {

	@Override
	public byte[] transform(String name, String transformedName, byte[] bytes) {

		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, ClassReader.EXPAND_FRAMES);

		Classes.identifyClass(classNode,name);

		if(name.equals(Classes.getClass("Block"))){
			for(Object methodObj:classNode.methods){
				MethodNode methodNode=(MethodNode) methodObj;
				
				if((methodNode.access&(Opcodes.ACC_STATIC|Opcodes.ACC_PUBLIC))==(Opcodes.ACC_STATIC|Opcodes.ACC_PUBLIC)){
					if(methodNode.desc.equals("()V")){

						MethodInsnNode instruction= new MethodInsnNode(Opcodes.INVOKESTATIC,"me/boomshroom/tweak/Block","registerBlocks","()V");

						ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
						while (iterator.hasNext()) {
							AbstractInsnNode insn = iterator.next();
							if (insn.getOpcode() == Opcodes.RETURN) {
								methodNode.instructions.insertBefore(insn, instruction);
							}
						}
					}
				}
				
			}			
			ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
			classNode.accept(writer);
			return writer.toByteArray();
		}

		return bytes;
	}
}
