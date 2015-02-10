package Test;

import java.io.File;
import java.io.IOException;

import de.bwaldvogel.liblinear.InvalidInputDataException;
import DOSH.DOSHTrain;

public class DoshTrainTest {
	public static void main(String args[]) throws IOException, Exception{
		DOSHTrain test=new DOSHTrain();
//		String conllfile=System.getenv("CODEDATA")+File.separator+"1sen.txt";
//		String liblinearfile=System.getenv("CODEDATA")+File.separator+"1sen.libins";
//		String orderfile=System.getenv("CODEDATA")+File.separator+"1sen.order";
//		String alphabetfile=System.getenv("CODEDATA")+File.separator+"1sen.alphabet";
//		String libmodelfile=System.getenv("CODEDATA")+File.separator+"1sen.libmodel";
		String conllfile=System.getenv("CODEDATA")+File.separator+"wsj_2-21.conll";
		String liblinearfile=System.getenv("CODEDATA")+File.separator+"wsj_2-21.libins";
		String orderfile=System.getenv("CODEDATA")+File.separator+"wsj_2-21.liborder";
		String alphabetfile=System.getenv("CODEDATA")+File.separator+"wsj_2-21.libalphabet";
		String libmodelfile=System.getenv("CODEDATA")+File.separator+"wsj_2-21.libmodel";
		
		test.GenerateLiblinearAndOrderFile(conllfile, liblinearfile, orderfile,alphabetfile);
		test.TrainLiblinear(liblinearfile, libmodelfile);
	}
}