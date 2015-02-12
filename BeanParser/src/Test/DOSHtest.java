package Test;

import java.io.IOException;

import DOSH.Converter;
import DOSH.DOSHTrain;

public class DOSHtest {
	public static void main(String args[]) throws IOException{
		Converter testConverter=new Converter();
//		testConverter.GenerateDOSHOrder("wsj_00-01.conll", "wsj_00-01.dosh");
//		testConverter.GenerateDOSHOrder("wsj_2-21.conll", "wsj_2-21.dosh");
//		testConverter.GenerateDOSHOrder("Multilingual\\dutch\\dutch_alpino_train.conll", "dutch_train.dosh");
//		testConverter.OrderCompare("1sen.txt","1senmalt.txt");
//		testConverter.OrderCompare("wsj_2-21_malt_processindex.txt", "wsj_2-21.dosh");
//		testConverter.OrderCompare("wsj_00-01_malt_processindex.txt", "wsj_00-01.dosh");
//		testConverter.OrderCompare("WSJ//wsj_2-21_malt_processindex.txt", "wsj_2-21.dosh");
//		testConverter.OrderCompare("wsj_00-01_doshparse_doshbeanmodel.out", "wsj_00-01.dosh");
//		testConverter.OrderCompare("wsj_00-01_doshparse_maltmodel.out", "wsj_00-01.dosh");
//		testConverter.GenerateMaltOrder("wsj_2-21.conll", "wsj_2-21.malt");
		testConverter.OrderCompare("wsj_00-01_malt_processindex.txt", "wsj_00-01.malt");
//		testConverter.GenerateDOSHOrder("1sen.txt", "1senconvert.txt");
//		DOSHTrain testDoshTrain=new DOSHTrain();
//		int node[]={-1,1,2,3,4,5,-1};
//		testDoshTrain.LookupElemInNode(node, 5, 5);
////		testDoshTrain.DeleteElemInNode(node, 1, 4);
////		testDoshTrain.DeleteElemInNode(node, 5, 3);
//		for(int i=0;i<node.length;i++){
//			System.out.print(node[i]+"->");
//		}
	}
}
