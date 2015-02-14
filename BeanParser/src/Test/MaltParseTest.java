package Test;

import DOSH.DOSHParse;
import Malt.MaltParse;

public class MaltParseTest {
	public static void main(String args[]) throws Exception{
		String maltalphabetfile="Order//wsj_2-21.maltalphabet";
		String libmodelfile="Order//wsj_2-21.libmodel";
		MaltParse test=new MaltParse(maltalphabetfile, libmodelfile, args);
		test.Parse(test.beanParser.options.testfile, test.beanParser.options.outfile);
		ResultEvaluator re = new ResultEvaluator();
		re.evaluate(test.beanParser.options.outfile, test.beanParser.options.testfile);
	}
}
