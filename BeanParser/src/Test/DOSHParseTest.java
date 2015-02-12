package Test;

import java.io.File;
import java.io.IOException;

import DOSH.DOSHParse;

public class DOSHParseTest {
	public static void main(String args[]) throws Exception{
		/*
		DOSHParse test=new DOSHParse();
		String libmodelfile=System.getenv("CODEDATA")+File.separator+"wsj_2-21.libmodel";
		String alphabetfile=System.getenv("CODEDATA")+File.separator+"wsj_2-21.libalphabet";
		String conllfile=System.getenv("CODEDATA")+File.separator+"1sen.txt";
		test.TestLibPredict(libmodelfile, alphabetfile, conllfile);
		*/
		String doshalphabetfile="wsj_2-21.maltlibalphabet";
		String libmodelfile="wsj_2-21.maltlibmodel";
		DOSHParse test=new DOSHParse(doshalphabetfile, libmodelfile, args);
		test.Parse(test.beanParser.options.testfile, test.beanParser.options.outfile);
		ResultEvaluator re = new ResultEvaluator();
		re.evaluate(test.beanParser.options.outfile, test.beanParser.options.testfile);
//		ResultEvaluator re = new ResultEvaluator();
//		String outfile="wsj_00-01_doshparse_maltmodel.out";
//		String testfile="wsj_00-01.conll";
//		re.evaluate(outfile, testfile);
	}
}
