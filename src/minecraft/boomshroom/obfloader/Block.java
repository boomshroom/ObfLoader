package boomshroom.obfloader;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import java.lang.reflect.Method;

import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.Type;

public class Block {
	
	public static Object blockObj;
	public static Field blockRegistry;
	
	public static void registerBlocks(){
		System.out.println("Registering Blocks");
		try {
			Class clazz = Class.forName(Classes.getClass("Block"));

			if(Classes.getClass("Material")!=null){
				Constructor constructor=clazz.getDeclaredConstructor(Class.forName(Classes.getClass("Material")));
				
				constructor.setAccessible(true);
				//System.out.println(constructor.toString());
				blockObj=constructor.newInstance(Class.forName(Classes.getClass("Material")).getDeclaredFields()[2].get(null));
				//System.out.println(blockObj);
				for(Field field :clazz.getDeclaredFields()){
					if(field.getType().equals(Class.forName(Classes.getClass("RegistryNamespaced")))){
						blockRegistry=field;
					}
				}
				//System.out.println(int.class.getName());
				Object reg=blockRegistry.get(null);
				//System.out.println(reg.toString());
				Class regClass= Class.forName(Classes.getClass("RegistryNamespaced"));
				for(Method method:regClass.getDeclaredMethods()){
					Class[] pars=method.getParameterTypes();
					if(pars.length==3){
						//System.out.println(method.getName());
						method.invoke(reg, 200,"testBlock",blockObj);
					}
				}
				//System.out.println(((RegistryNamespaced) reg).getObject("testBlock"));
				
			}
			//System.out.println(blockObj);
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}

}
