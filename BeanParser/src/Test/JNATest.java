package Test;

import java.util.HashSet;
import java.util.Iterator;

import ArcFilter.JNAArcFilter2;

public class JNATest {

	public static void main(String args[]) {

		String str = "ROOT_ROOT_* Just_RB_* days_NNS_* after_IN_* the_DT_* <num>_CD_*";
		double start=System.currentTimeMillis();
		JNAArcFilter2 ts = new JNAArcFilter2();
		double end=System.currentTimeMillis();
		System.out.println("load time cost:"+(end-start)/1000);
		/*
		HashSet<String> headmodifier=ts.ArcFilter(str);
		Iterator ite=headmodifier.iterator();
		while(ite.hasNext()){
//			String entry=(String)ite.next();
			System.out.println(ite.next());
		}
		*/
		String retString;
		int count=0;
		start=System.currentTimeMillis();
		while(true){
			System.out.println("count:"+(++count));
			retString=ts.SeudoCall(str);
			System.out.println("in JAVA:"+retString);
			if(count==10000){
				end=System.currentTimeMillis();
				System.out.println("run 10000, time cost:"+(end-start)/1000);
				break;
			}
//			if(count==1){
//				System.gc();
//				System.out.println("retString after gc:"+retString);
//			}
//			
//			if(retString.equals("")){
//				System.out.println("break");
//				break;
//			}
		}
	}
}
