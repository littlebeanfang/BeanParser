package ArcFilter;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;

import DataStructure.DependencyInstance;

public class JNIArcFilter {
	//arguments: anystring, ultraLinWeights, ultraPairWeights
	File filterlibFile=new File("libarcfilter.so");
	private String arcfilterlibpath=filterlibFile.getAbsolutePath();//"/home/bean/git/BeanParser/BeanParser/bin/libarcfilter.so";
	public native int InitModel();
	public native String CallFilter(String text);  
	//public native void DeleteLocalRef(String text);
	public JNIArcFilter(){
		System.load(arcfilterlibpath);
		this.InitModel();
		System.out.println("init done..");
	}
	public HashSet<String> ArcFilter(String word_pos_head){
		String out = this.CallFilter(word_pos_head);
		//System.out.println("Java receive:"+out);
		HashSet<String> headmodifier=new HashSet<String>();
		String columns[]=out.split("\t");
		for(int i=0;i<columns.length;i++){
			String[] heads=columns[i].split(",");
			String modifier=""+(i+1);
			for(String head:heads){
//				System.out.println("add:"+head+"_"+modifier);
				headmodifier.add(head+"_"+modifier);
			}
		}
		//System.gc();
		return headmodifier;
	}
	public HashSet<String> ArcFilter(DependencyInstance di){
		//System.out.println("arc fil str:"+di.toArcFilterString());
		return ArcFilter(di.toArcFilterString());
	}
	
	public void TranverseHashset(HashSet<String> headmodifier){
		System.out.println("set size:"+headmodifier.size());
		Iterator ite=headmodifier.iterator();
		while(ite.hasNext()){
//			String entry=(String)ite.next();
			System.out.println(ite.next());
		}
	}
}
