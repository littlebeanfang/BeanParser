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

public class LabelFeatWeightFromMSTTest {
	public void ChangeModelWithMSTTypeWeight(String MSTmodel, Parser test, String mstargs[]) throws IOException{
		//Not used
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
	
	public void GetOneSenLabelFeat(ParserOptions options) throws IOException{
		String file="wsj_rand100_processindex_new.txt";
		MyPipe pipe=new MyPipe(options);
		DependencyPipe mstpipe=new DependencyPipe(options);
		mstpipe.labeled=true;
		CONLLReader reader = new CONLLReader();
		reader.startReading(System.getenv("CODEDATA")
				+ File.separator + file);//bean: here changed the label
		DependencyInstance instance = reader.getNext();
		FeatureVector[] beanretfv=pipe.extractFeatureVector(instance);
		FeatureVector labelfv=beanretfv[1];
		System.out.println("Bean labelfv:"+labelfv);
		System.out.println("Bean labelfv size:"+labelfv.size());
		FeatureVector fv=mstpipe.createFeatureVector(instance);//comment corefeat in DependencyPipes
		System.out.println("MST createFV:"+fv);
		System.out.println("MST createFV size:"+fv.size());
	}
	public void GetOneArcLabelFeat(ParserOptions options, int childindex, int parentindex) throws Exception{
		//default: reading 1st sentence of this file
		String file="wsj_rand100_processindex_new.txt";
		CONLLReader reader = new CONLLReader();
		reader.startReading(System.getenv("CODEDATA")
				+ File.separator + file);//bean: here changed the label
		
		MyPipe pipe=new MyPipe(options);
		//load params in model
		Parser test=new Parser(pipe,options);
		test.loadModel(options.modelName);
		
		DependencyPipe mstpipe=new DependencyPipe(options);
		mstpipe.labeled=true;
		
		DependencyInstance instance = reader.getNext();
		
		//get nt_probs and nt_fvs, labeled feature
		String[] forms=instance.forms;
		FeatureVector[][][] fvs = new FeatureVector[forms.length][forms.length][2];
		double[][][] probs = new double[forms.length][forms.length][2];
		FeatureVector[][][][] nt_fvs = new FeatureVector[forms.length][pipe.types.length][2][2];
		double[][][][] nt_probs = new double[forms.length][pipe.types.length][2][2];
		mstpipe.types=pipe.types;
		mstpipe.fillFeatureVectors(instance,fvs,probs,nt_fvs,nt_probs,test.params);
		
		int ph=childindex<parentindex?0:1;
		int wh = -1;
		double best = Double.NEGATIVE_INFINITY;
		for(int t = 0; t < pipe.types.length; t++) {
			double score=nt_probs[childindex][t][ph][1]+nt_probs[parentindex][t][ph][0];
			if(score > best) { wh = t; best = score; }
		}
		System.out.println("mst best relation:"+pipe.types[wh]+", best score:"+best);
		FeatureVector labelfVector=nt_fvs[childindex][wh][ph][1].cat(nt_fvs[parentindex][wh][ph][0]);
		System.out.println(labelfVector);
		System.out.println("mst labelfv size:"+labelfVector.size());
		
		//get bean labeled feature
		ParseAgenda pa=new ParseAgenda();
		FeatureVector beanlabelfv=(FeatureVector)pipe.AddMyParseLabeledFeatures(instance,childindex,parentindex,test.params)[0];
		System.out.println("bean best relation:"+instance.deprels[childindex]+", best score:"+beanlabelfv.getScore(test.params.parameters));
		System.out.println(beanlabelfv);
		System.out.println("bean labelfv size:"+beanlabelfv.size());
	}
	
	protected int[][] getTypes(double[][][][] nt_probs, int len,DependencyPipe pipe) {
		int[][] static_types = new int[len][len];
		for(int i = 0; i < len; i++) {
		    for(int j = 0; j < len; j++) {
			if(i == j) {static_types[i][j] = 0; continue; }
			int wh = -1;
			double best = Double.NEGATIVE_INFINITY;
			for(int t = 0; t < pipe.types.length; t++) {
			    double score = 0.0;
			    if(i < j)
				score = nt_probs[i][t][0][1] + nt_probs[j][t][0][0];
			    else
				score = nt_probs[i][t][1][1] + nt_probs[j][t][1][0];
			    
			    if(score > best) { wh = t; best = score; }
			}
			static_types[i][j] = wh;
		    }
		}
		return static_types;
	    }
	
	public void ParsewithMST(String args[]) throws Exception{
		ParserOptions options = new ParserOptions(args);
		DependencyPipe dp=new MyPipe(options);
		Parser test=new Parser(dp,options);
		test.loadModel(options.modelName);
		
		test.Parse(options.testfile,options.outfile);
	}
	public static void main(String args[]) throws Exception{
		LabelFeatWeightFromMSTTest test=new LabelFeatWeightFromMSTTest();
		ParserOptions options = new ParserOptions(args);
		//test.GetOneSenLabelFeat(options);
		test.GetOneArcLabelFeat(options, 7, 5);
	}
}
