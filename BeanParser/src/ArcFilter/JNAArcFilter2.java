package ArcFilter;
import java.util.HashSet;
import java.util.Iterator;

import DataStructure.DependencyInstance;

import com.sun.jna.Library;
import com.sun.jna.Native;

public class JNAArcFilter2 {
	public interface LgetLib extends Library {
		// 调用linux下面的so文件,注意，这里只要写test就可以了，不要写libtest，也不要加后缀
		LgetLib INSTANCE = (LgetLib) Native.loadLibrary("arcfilter",
				LgetLib.class);

		int InitModel(String[] argv);

		String CallFilter(String input);
	}

	private int CallInitModel(String[] argv) {
		return LgetLib.INSTANCE.InitModel(argv);
	}

	private String CallFilter(String input) {
		return LgetLib.INSTANCE.CallFilter(input);
	}

	public JNAArcFilter2() {
		String[] param1 = { "run", "ultraLinWeights", "ultraPairWeights" };
		this.CallInitModel(param1);
		System.out.println("init done..");
	}
	
	public HashSet<String> ArcFilter(String word_pos_head){
		String out = this.CallFilter(word_pos_head);
		System.out.println("out:"+out);
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
		return headmodifier;
	}
	public HashSet<String> ArcFilter(DependencyInstance di){
		System.out.println("arc fil str:"+di.toArcFilterString());
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
