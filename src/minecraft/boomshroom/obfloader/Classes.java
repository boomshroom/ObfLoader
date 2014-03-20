package boomshroom.obfloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import net.minecraft.launchwrapper.Launch;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class Classes {
	
	private static Map<String,String> knownClasses;
	private static List<String> seenClasses;
	private static Map<String,String> configClasses;

	private static File classes;
	public static PrintWriter writer;

	public static void identifyClass(ClassNode classNode,String name){

		if((classNode.methods.size()==2||classNode.methods.size()==1)&&((classNode.access&Opcodes.ACC_INTERFACE)==0)){
			//System.out.println(name);
			boolean hasClinit=false;
			for(Object methodObj:classNode.methods){
				if(((MethodNode)methodObj).name.equals("<clinit>")) hasClinit=true;
			}

			if(hasClinit){

				boolean staticFinal=true;
				for(Object fieldObj:classNode.fields){
					FieldNode field=(FieldNode)fieldObj;
					if((field.access&(Opcodes.ACC_STATIC+Opcodes.ACC_FINAL))!=Opcodes.ACC_STATIC+Opcodes.ACC_FINAL){
						staticFinal=false;
					}
				}
				if(staticFinal){
					if((classNode.access&Opcodes.ACC_FINAL)==0&&!classNode.name.startsWith("com.")&&classNode.fields.size()>10){
						//We've found blocks or items, now which one?
						FieldNode firstField=(FieldNode) classNode.fields.get(0);
						Type type=Type.getType(firstField.desc);

						try {
							Class clazz=Class.forName(type.getClassName());

							if(clazz.getConstructors().length==1){

								ClassNode itemNode=new ClassNode();
								ClassReader blockReader=new ClassReader(clazz.getName());
								blockReader.accept(itemNode, ClassReader.EXPAND_FRAMES);
								
								foundClass("Item", clazz.getName(),itemNode);
								foundClass("Items", name,classNode);
								
							}else{

								ClassNode blockNode=new ClassNode();
								ClassReader blockReader=new ClassReader(clazz.getName());
								blockReader.accept(blockNode, ClassReader.EXPAND_FRAMES);
								
								foundClass("Block", clazz.getName(),blockNode);
								foundClass("Blocks", name,classNode);

							}
						} catch (ClassNotFoundException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}

			}

		}
	}
	
	public static void analyzeClass(ClassNode classNode,String name){
		if(name.equals("Block")){
			//foundClass("Material", );
			try {
				Constructor constructor=Class.forName(classNode.name.replace("/", ".")).getDeclaredConstructors()[0];
				Class material=constructor.getParameterTypes()[0];
				
				ClassNode matNode=new ClassNode();
				ClassReader blockReader=new ClassReader(material.getName());
				blockReader.accept(matNode, ClassReader.EXPAND_FRAMES);
				
				foundClass("Material", material.getName(),matNode);
				
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			for(Object methodObj:classNode.methods){
				MethodNode methodNode=(MethodNode) methodObj;
				
				if((methodNode.access&(Opcodes.ACC_STATIC|Opcodes.ACC_PUBLIC))==(Opcodes.ACC_STATIC|Opcodes.ACC_PUBLIC)){
					if(methodNode.desc.equals("()V")){
						LineNumberNode insn=(LineNumberNode)methodNode.instructions.get(1);
						FieldInsnNode field=(FieldInsnNode) insn.start.getNext().getNext();
						
						try {
							ClassNode regNode=new ClassNode();
							ClassReader blockReader = new ClassReader(Type.getType(field.desc).getClassName());
							blockReader.accept(regNode, ClassReader.EXPAND_FRAMES);
							
							foundClass("RegistryNamespaced",Type.getType(field.desc).getClassName(),regNode);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}else if(Type.getReturnType(methodNode.desc).getClassName().equals(name)&&Type.getArgumentTypes(methodNode.desc).length==1){
					Type par=Type.getArgumentTypes(methodNode.desc)[0];
					System.out.println(par.getClassName());
				}
				
			}	
		}
	}

	public static void init() {
		
		classes=new File("config/classes.conf");
		configClasses=new HashMap();
		knownClasses=new HashMap();
		if(classes.exists()){
			try {

				BufferedReader reader=new BufferedReader(new FileReader(classes));
				boolean moreLines=true;
				while(moreLines){
					String line =reader.readLine();
					if(line==null){
						moreLines=false;
					}else{
						String[] key=line.split(" ");
						configClasses.put(key[0], key[1]);

					}
				}
				reader.close();

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else{
			try {
				classes.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {
			writer=new PrintWriter(classes);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static String getClass(String className){

		if(knownClasses.containsKey(className)) return knownClasses.get(className);
		else if(configClasses.containsKey(className)) return configClasses.get(className);
		else return null;
	}
	
	public static void foundClass(String identifier,String className,ClassNode classNode){
		if(!knownClasses.containsKey(identifier)){
			knownClasses.put(identifier, className);
			writer.println(identifier+" "+className);
			writer.flush();
			analyzeClass(classNode,identifier);
		}
	}

}
