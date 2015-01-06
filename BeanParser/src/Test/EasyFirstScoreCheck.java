package Test;

import gnu.trove.TIntIntHashMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


import java.io.Reader;
import java.lang.reflect.Array;
import java.util.ArrayList;



import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

import DataStructure.DependencyInstance;
import DataStructure.FeatureVector;
import DataStructure.Parameters;
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
	/**
	 * 
	 * @param scorefileofallsents:the score file from PrintOutAll1orderArcScore output
	 * @param folder:the folder to save all the splited small score files
	 * @param prefix:give a prefix to score files, not only the sentnum to seperate different score
	 * @throws IOException
	 */
	public void GenerateArcscoreSingleFile(String scorefileofallsents, String folder,String prefix) throws IOException{
		BufferedReader godreader=new BufferedReader(new FileReader(scorefileofallsents));
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
		ArrayList<String> onegodsent=new ArrayList<String>();
		int cursenlength=0;
		int cursentnum=1;
		while(godString!=null){
			System.out.println(godString);
			if(godString.equals("")){
				System.out.println("writefile");
				//write file, clear arraylist
				File arc=new File(dir+File.separator+prefix+"_"+cursentnum+".score");
				if(!arc.exists()){
					arc.createNewFile();
				}
				FileWriter arcwriter=new FileWriter(arc);
				for(String s:onegodsent){
					arcwriter.write(s+"\n");
				}
				arcwriter.close();
				onegodsent.clear();
			}else if(godString.startsWith("length")){
				cursenlength=Integer.parseInt(godString.split("\t")[1]);
			}else if(godString.startsWith("<root>")){
				sentWriter.write(godString+"\n");
			}else if(godString.startsWith("sent")){
				cursentnum=Integer.parseInt(godString.split("\t")[1]);
			}else{
				onegodsent.add(godString);
			}
			godString=godreader.readLine();
		}
		godreader.close();
		sentWriter.close();
	}

	public TIntIntHashMap GenerateGodOrderByInstance(DependencyInstance di){
		TIntIntHashMap order_child=new TIntIntHashMap();
		Stack<String> ret=new Stack<String>();
		HashMap<String,String> parent_childs=new HashMap<String,String>();
		for(int i=1;i<di.length();i++){
			String head=di.heads[i]+"";
			if(parent_childs.containsKey(head)){
				parent_childs.put(head, parent_childs.get(head)+"\t"+i);
			}else{
				parent_childs.put(head, i+"");
			}
		}
		Queue<String> temp=new LinkedList<String>();//offer,poll
		temp.offer("0");
		while(temp.size()!=0){
			String curString=temp.poll();
			if(parent_childs.containsKey(curString)){
				//subtree
				//sb.append(curString+"\t");
				ret.push(curString);
				String childs[]=parent_childs.get(curString).split("\t");
				for(int i=childs.length-1;i>=0;i--){
					temp.offer(childs[i]);
				}
			}else{
				//leaf node
				//sb.append(curString+"\t");
				ret.push(curString);
			}
		}
		int order=1;
		while(!ret.empty()){
			String child=ret.pop();
			if(child.equals("0")){
				break;
			}
			order_child.put(order++, Integer.parseInt(child));
		}
		return order_child;
	}
	
	public TIntIntHashMap GenerateEasyFirstOrderByInstance(DependencyInstance di,Parameters p) throws IOException{
		int length=di.length();
		ParseAgenda pa=new ParseAgenda(length);
		MyPipe dp=new MyPipe(null);
		double[][] matrix=new double[length-1][length-1];
		for(int head=1;head<length;head++){
			for(int child=1;child<length;child++){
				if(child!=head){
					FeatureVector fv=new FeatureVector();
					dp.extractFeatures(di, child, head, pa, fv);
					double score=fv.getScore(p.parameters);
					matrix[head-1][child-1]=score;
				}else{
					matrix[head-1][child-1]=0;
				}
			}
		}
		BeanMatrixEasyFirst test=new BeanMatrixEasyFirst();
		TIntIntHashMap map=new TIntIntHashMap();
		test.GetEasyFirstOrder(matrix, map);
		test.tranverseTIntIntHashMap(map);
		return map;
	}
	
	public void AddEasyFirstOrderProcessIndex(String modelfile, String conllfile, String writefile) throws Exception{
		CONLLReader reader=new CONLLReader();
		reader.startReading(conllfile);
		File out=new File(writefile);
		if(!out.exists()){
			out.createNewFile();
		}
		FileWriter writer=new FileWriter(writefile);
		DependencyInstance di=reader.getNext();
		ParserOptions options = new ParserOptions(null);
		MyPipe dp=new MyPipe(options);
		Parser test=new Parser(dp,options);
		test.loadModel(modelfile);
		while(di!=null){
			TIntIntHashMap map=this.GenerateEasyFirstOrderByInstance(di, test.GetParameters());
			TIntIntHashMap transper=new TIntIntHashMap();
			for(int i=1;i<=map.size();i++){
				transper.put(map.get(i), i);
			}
			for(int i=1;i<di.length();i++){
				writer.write(i+"\t"+di.forms[i]+"\t_\t"+di.postags[i]+"\t"+di.postags[i]+"\t_\t"+di.heads[i]+"\t"+di.deprels[i]+"\t_\t_\t"+transper.get(i)+"\n");
			}
			writer.write("\n");
			di=reader.getNext();
		}
		writer.close();
	}
	
	public void tranverseTIntIntHashMap(TIntIntHashMap map){
		int len=map.size();
		System.out.println("len"+len);
		for(int i=1;i<=len;i++){
			System.out.println(map.get(i));
		}
	}
	

	public void GenerateCombineBat(String batfilename) throws IOException{
		int startindex=1;
		int endindex=100;
		File outFile=new File(batfilename);
		if(!outFile.exists()){
			outFile.createNewFile();
		}
		FileWriter writer=new FileWriter(outFile);
		for(int i=startindex;i<=endindex;i++){
			writer.write("copy *_"+i+".score "+i+".score\n");
		}
		writer.close();
	}

	public static void main(String args[]) throws Exception{
		//args: Bean parse command, use modelname,test file and output file
		//test file for sentence want to see score
		//model is used to get score
		//output file for write arc score matrix
		EasyFirstScoreCheck test=new EasyFirstScoreCheck();
//		test.PrintOutAll1orderArcScore(args);
//		test.GenerateArcscorePairFiles("ArcScore_God_wsj2-21train_wsj00-01test_first100sent.score", "ArcScore_Increase_wsj2-21train_wsj00-01test_first100sent.score", "Arcscore100sen_GodIncrease");

//		test.GenerateArcscoreSingleFile("ArcScore_God_wsj2-21train_wsj00-01test_first100sent.score", "Arcscore100sen_combinetest", "God");
		/*
		CONLLReader reader=new CONLLReader();
		reader.startReading(System.getenv("CODEDATA")+File.separator+"1sen.txt");
		DependencyInstance di=reader.getNext();
		TIntIntHashMap ret=test.GenerateGodOrderByInstance(di);
		test.tranverseTIntIntHashMap(ret);
		*/
		//test.AddEasyFirstOrderProcessIndex("", conllfile, writefile);

//		test.GenerateArcscoreSingleFile("ArcScore_Increase_wsj2-21train_wsj00-01test_first100sent.score", "Arcscore100sen_combinetest", "Increase");
		test.GenerateCombineBat("combile1-100.bat");

	}
}
