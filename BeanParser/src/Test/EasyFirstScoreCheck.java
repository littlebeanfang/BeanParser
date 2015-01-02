package Test;

import java.io.File;
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
		reader.startReading(System.getenv("CODEDATA")+File.separator+options.testfile);
		DependencyInstance di=reader.getNext();
		int length=di.length();
		int sentcount=1;
		while(di!=null){
			length=di.length();
			System.out.println("sen:"+(sentcount++)+",length:"+length);
			ParseAgenda pa=new ParseAgenda(length);
			for(int head=0;head<length;head++){
				for(int child=0;child<length;child++){
					if(child!=head){
						FeatureVector fv=new FeatureVector();
						dp.extractFeatures(di, child, head, pa, fv);
						double score=fv.getScore(params);
//						System.out.print(head+"->"+child+":"+score+"\t");
						System.out.print(score+"\t");
					}else{
//						System.out.print(head+"->"+child+":/\t");
						System.out.print("/\t");
					}
				}
				System.out.println();
			}
			di=reader.getNext();
			
		}
	}
	public static void main(String args[]) throws Exception{
		EasyFirstScoreCheck test=new EasyFirstScoreCheck();
		test.PrintOutAll1orderArcScore(args);
	}
}
