package Test;

import DataStructure.ParserOptions;
import Parser.DependencyPipe;
import Parser.MyPipe;
import Parser.Parser;
import Parser.Train;

public class TrainTest {
	public static void main(String args[]) throws Exception{
		ParserOptions options = new ParserOptions(args);
		DependencyPipe pipe=new MyPipe(options);
		
		Parser test=new Parser(pipe,options);
		/*
		test.loadModel(options.modelName);
		test.Parse(options.testfile,options.outfile);
		*/
		test.Train();
	}
}
