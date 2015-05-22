package Test;

import java.util.HashSet;
import java.util.Iterator;

import ArcFilter.JNAArcFilter2;

public class JNATest {

	public static void main(String args[]) {

		String str = "ROOT_ROOT_-1 The_DT_* estimates_NNS_* for_IN_* Du_NNP_* Pont_NNP_* range_NN_* from_IN_* $_$_* 2.25_CD_* to_TO_* $_$_* 2.45_CD_* a_DT_* share_NN_* ._._*";
		JNAArcFilter2 ts = new JNAArcFilter2();
		
		HashSet<String> headmodifier=ts.ArcFilter(str);
		Iterator ite=headmodifier.iterator();
		while(ite.hasNext()){
//			String entry=(String)ite.next();
			System.out.println(ite.next());
		}
	}
}
