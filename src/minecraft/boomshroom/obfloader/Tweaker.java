package boomshroom.obfloader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Type;

import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

public class Tweaker implements ITweaker {

	private List<String> args=new ArrayList();
	public static PrintWriter writer;
	
	public Tweaker(){
		Classes.init();
	}

	@Override
	public void acceptOptions(List<String> argsArg, File gameDir, File assetsDir, String profile) {
		this.args.addAll(argsArg);
		if(profile!=null){
			args.add("--version");
			args.add(profile);
		}if(gameDir!=null){
			args.add("--gameDir");
			args.add(gameDir.getPath());
		}if(assetsDir!=null){
			args.add("--assetsDir");
			args.add(assetsDir.getPath());
		}
	}

	@Override
	public String[] getLaunchArguments() {
		// TODO Auto-generated method stub
		return args.toArray(new String[args.size()]);
	}

	@Override
	public String getLaunchTarget() {
		// TODO Auto-generated method stub
		return "net.minecraft.client.main.Main";
	}

	@Override
	public void injectIntoClassLoader(LaunchClassLoader classLoader) {
		classLoader.registerTransformer("me.boomshroom.tweak.ClassTransformer");
	}

}
