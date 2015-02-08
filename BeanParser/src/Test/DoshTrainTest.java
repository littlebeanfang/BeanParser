package Test;

import java.io.File;
import java.io.IOException;

import DOSH.DOSHTrain;

public class DoshTrainTest {
	public static void main(String args[]) throws IOException{
		DOSHTrain test=new DOSHTrain();
		String conllfile=System.getenv("CODEDATA")+File.separator+"1sen.txt";
		String liblinearfile=System.getenv("CODEDATA")+File.separator+"1sen.libins";
		String orderfile=System.getenv("CODEDATA")+File.separator+"1sen.order";
		test.GenerateLiblinearAndOrderFile(conllfile, liblinearfile, orderfile);
	}
}
