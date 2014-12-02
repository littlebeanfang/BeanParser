package Test;

import java.io.IOException;

import DataStructure.ParserOptions;
import Parser.DependencyPipe;
import Parser.MyPipe;
import Parser.Parser;

public class LabelFeatWeightFromMSTTest {
	public void ChangeModelWithMSTTypeWeight(String MSTmodel, Parser test, String mstargs[]) throws IOException{
		//step 1: load MST model for same sentence
		ParserOptions options = new ParserOptions(mstargs);
		DependencyPipe dp=new MyPipe(options);
		Parser temp=new Parser(dp,options);
		//step 2: for every type feature, change the parameter with MST modeld
		//typealphabet: relation-index
		String[] types = new String[test.pipe.typeAlphabet.size()];
		Object[] keys = test.pipe.typeAlphabet.toArray();
		for(int i = 0; i < keys.length; i++) {
		    int indx = test.pipe.typeAlphabet.lookupIndex(keys[i]);
		    types[indx] = (String)keys[i];
		}
	}
	public void ParsewithMST(String args[]) throws Exception{
		ParserOptions options = new ParserOptions(args);
		DependencyPipe dp=new MyPipe(options);
		Parser test=new Parser(dp,options);
		test.loadModel(options.modelName);
		
		test.Parse(options.testfile,options.outfile);
	}
}
