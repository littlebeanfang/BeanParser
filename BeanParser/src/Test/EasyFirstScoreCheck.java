package Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


import java.io.Reader;
import java.lang.reflect.Array;
import java.util.ArrayList;


import DataStructure.DependencyInstance;
import DataStructure.FeatureVector;
import DataStructure.ParseAgenda;
import DataStructure.ParserOptions;
import IO.CONLLReader;
import Parser.DependencyPipe;
import Parser.MyPipe;
import Parser.Parser;

public class EasyFirstScoreCheck {
	//how many sentece to get arc score
	private int extractsentnum=100;
	public void PrintOutAll1orderArcScore(String args[]) throws Exception{
		ParserOptions options = new ParserOptions(args);
		MyPipe dp=new MyPipe(options);
		Parser test=new Parser(dp,options);
		
		File outFile=new File(options.outfile);
		if(!outFile.exists()){
			outFile.createNewFile();
		}
		FileWriter writer=new FileWriter(outFile);
		
		test.loadModel(options.modelName);
		double params[]=test.GetParam();
		CONLLReader reader=new CONLLReader();
		reader.startReading(options.testfile);
		DependencyInstance di=reader.getNext();
		int length;
		int sentcount=1;
		while(di!=null){
			//
			if(sentcount==this.extractsentnum+1){
				return ;
			}
			length=di.length();
			writer.write("sent\t"+sentcount+"\n");
			System.out.println("sen:"+(sentcount++)+",length:"+length);
			writer.write("length\t"+length+"\n");
			for(String form:di.forms){
				System.out.print(form+"\t");
				writer.write(form+"\t");
			}
			System.out.println();
			writer.write("\n");
			ParseAgenda pa=new ParseAgenda(length);
			for(int head=0;head<length;head++){
				for(int child=0;child<length;child++){
					if(child!=head){
						FeatureVector fv=new FeatureVector();
						dp.extractFeatures(di, child, head, pa, fv);
						double score=fv.getScore(params);
//						System.out.print(head+"->"+child+":"+score+"\t");
						System.out.print(score+"\t");
						writer.write(score+"\t");
					}else{
//						System.out.print(head+"->"+child+":/\t");
						System.out.print("0\t");
						writer.write("0\t");
					}
				}
				System.out.println();
				writer.write("\n");
			}
			writer.write("\n");
			writer.flush();
			di=reader.getNext();
		}
		writer.close();
	}
	/**
	 * Generate small files for matlab program to show the arc score
	 * @param godscorefile:god order model score for arcs
	 * @param maltscorefile:malt order model score for same sentence as godscorefile
	 * @param folder:place to generate small matrix files
	 * @throws IOException 
	 */
	public void GenerateArcscorePairFiles(String godscorefile, String maltscorefile, String folder) throws IOException{
		BufferedReader godreader=new BufferedReader(new FileReader(godscorefile));
		BufferedReader maltreader=new BufferedReader(new FileReader(maltscorefile));
		File dir=new File(folder);
		if(!dir.exists()){
			dir.mkdir();
		}
		File sentFile=new File(dir+File.separator+"sentfile.txt");
		if(!sentFile.exists()){
			sentFile.createNewFile();
		}
		FileWriter sentWriter=new FileWriter(sentFile);
		
		String godString=godreader.readLine();
		String maltString=maltreader.readLine();
		ArrayList<String> onegodsent=new ArrayList<String>();
		ArrayList<String> onemaltsent=new ArrayList<String>();
		int cursenlength=0;
		int cursentnum=1;
		while(godString!=null){
			System.out.println(godString);
			if(godString.equals("")){
				System.out.println("writefile");
				//write file, clear arraylist
				File arc=new File(dir+File.separator+cursentnum+".score");
				if(!arc.exists()){
					arc.createNewFile();
				}
				FileWriter arcwriter=new FileWriter(arc);
				for(String s:onegodsent){
					arcwriter.write(s+"\n");
				}
				for(String s:onemaltsent){
					arcwriter.write(s+"\n");
				}
				arcwriter.close();
				onegodsent.clear();
				onemaltsent.clear();
			}else if(godString.startsWith("length")){
				cursenlength=Integer.parseInt(godString.split("\t")[1]);
			}else if(godString.startsWith("<root>")){
				sentWriter.write(godString+"\n");
			}else if(godString.startsWith("sent")){
				cursentnum=Integer.parseInt(godString.split("\t")[1]);
			}else{
				onegodsent.add(godString);
				onemaltsent.add(maltString);
			}
			godString=godreader.readLine();
			maltString=maltreader.readLine();
		}
		
		godreader.close();
		maltreader.close();
		sentWriter.close();
	}
	public static void main(String args[]) throws Exception{
		//args: Bean parse command, use modelname,test file and output file
		//test file for sentence want to see score
		//model is used to get score
		//output file for write arc score matrix
		EasyFirstScoreCheck test=new EasyFirstScoreCheck();
//		test.PrintOutAll1orderArcScore(args);
		test.GenerateArcscorePairFiles("ArcScore_God_wsj2-21train_wsj00-01test_first100sent.score", "ArcScore_Increase_wsj2-21train_wsj00-01test_first100sent.score", "Arcscore100sen_GodIncrease");
	}
}
