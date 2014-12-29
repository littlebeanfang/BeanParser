package Test;

import java.io.IOException;

import DataStructure.DependencyInstance;
import DataStructure.FeatureVector;
import DataStructure.ParseAgenda;
import DataStructure.ParserOptions;
import IO.CONLLReader;
import Parser.DependencyPipe;
import Parser.MyPipe;
import Parser.Parser;

public class EasyFirstScoreCheck {
	public void PrintOutAll1orderArcScore(String args[]) throws Exception{
		ParserOptions options = new ParserOptions(args);
		MyPipe dp=new MyPipe(options);
		Parser test=new Parser(dp,options);
		test.loadModel(options.modelName);
		double params[]=test.GetParam();
		CONLLReader reader=new CONLLReader();
		reader.startReading(options.testfile);
		DependencyInstance di=reader.getNext();
		int length=di.length();
		ParseAgenda pa=new ParseAgenda(length);
		for(int head=0;head<length;head++){
			for(int child=0;child<length&&child!=head;child++){
				FeatureVector fv=new FeatureVector();
				dp.extractFeatures(di, child, head, pa, fv);
				double score=fv.getScore(params);
				System.out.println(head+"->"+child+":"+score);
			}
		}
	}
	public static void main(String args[]) throws Exception{
		EasyFirstScoreCheck test=new EasyFirstScoreCheck();
		test.PrintOutAll1orderArcScore(args);
	}
}
