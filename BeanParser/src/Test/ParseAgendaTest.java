package Test;

import DataStructure.DependencyInstance;
import DataStructure.ParseAgenda;
import IO.CONLLReader;
import gnu.trove.TIntIntHashMap;
import gnu.trove.TIntIntIterator;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ParseAgendaTest {
	public static void main(String args[]) throws IOException {
		ParseAgendaTest pat = new ParseAgendaTest();
		//pat.ReadOneTreeTest("wsj_2-21_malt_processindex_new.txt");
		pat.ReadOneTreeTest("1sen.txt");
	}

	public void ReadOneTreeTest(String onetreeconllfile) throws IOException {
		CONLLReader reader = new CONLLReader();
		reader.startReading(System.getenv("CODEDATA") + File.separator + onetreeconllfile);
		DependencyInstance instance = reader.getNext();
		Map<String, Integer> structurecount = new HashMap<String, Integer>();
		while (instance != null) {
			ParseAgenda pa = new ParseAgenda(instance.length());
			TIntIntHashMap ordermap = instance.orders;
			int[] heads = instance.heads;
			for (int orderindex = 1; orderindex < instance.length(); orderindex++) {
				//skip root node
				int parseindex = ordermap.get(orderindex);
                int parsehead = heads[parseindex];

				pa.ChildProcess(parseindex, parsehead);
				pa.AddArc(parseindex, parsehead);
			}
			pa.AddArc(0, -1);//add root
            pa.StoreOnePAInMap(structurecount);
//            System.out.println();
//            ParseAgendaTest paTest=new ParseAgendaTest();
//            paTest.PrintTIntIntMap(pa.numofleftchild, "leftchild map traverse:");
//            paTest.PrintTIntIntMap(pa.numofrightchild, "rightchild map traverse:");
            instance = reader.getNext();
//            System.out.println("5 right child:"+pa.rightchilds.get(5));
//            System.out.println("5 left child:"+pa.leftchilds.get(5));
        }
        PrintMap(structurecount);
	}

	public void PrintMap(Map<String, Integer> map) {
		Iterator iterator = map.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) iterator.next();
			//System.out.println("Structure:"+entry.getKey()+",Count:"+entry.getValue());
			//System.out.println("Structure:"+entry.getKey()+"\t"+entry.getValue());
			System.out.println(entry.getKey() + "\t" + entry.getValue());
		}
	}

	public void PrintTIntIntMap(TIntIntHashMap map, String prefix) {
		System.out.println(prefix);
		for (TIntIntIterator it = map.iterator(); it.hasNext(); ) {
			it.advance();
			System.out.println("Head:" + it.key() + ",count:" + it.value());
		}
	}
}
