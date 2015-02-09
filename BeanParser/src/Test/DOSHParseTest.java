package Test;

import java.io.File;
import java.io.IOException;

import DOSH.DOSHParse;

public class DOSHParseTest {
	public static void main(String args[]) throws ClassNotFoundException, IOException{
		DOSHParse test=new DOSHParse();
		String libmodelfile=System.getenv("CODEDATA")+File.separator+"";
		String alphabetfile=System.getenv("CODEDATA")+File.separator+"wsj_2-21.libalphabet";
		String conllfile=System.getenv("CODEDATA"+File.separator+"1sen.txt");
		test.TestLibPredict(libmodelfile, alphabetfile, conllfile);
	}
}
