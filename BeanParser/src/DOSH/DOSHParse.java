package DOSH;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import mstparser.Alphabet;
import DataStructure.DependencyInstance;
import DataStructure.FeatureVector;
import DataStructure.ParseAgenda;
import DataStructure.ParserOptions;
import IO.CONLLReader;
import IO.CONLLWriter;
import Parser.Decoder;
import Parser.MyPipe;
import Parser.Parser;

/**
 * This class parsing while predicting order.
 * Load liblinear model to decide whether find head of a node
 * Bean parser find the head of a node using graph-based method
 * @author Bean
 *
 */
public class DOSHParse {
	private Model libModel;
	public Parser beanParser;//store pipe 
	private Decoder decoder;
	private DOSHTrain featextractor;//init with doshalphabet,only used to extract features
	private static final int DO=1;
	private static final int UNDO=2;
	
	public DOSHParse(String doshalphabetfile,  String liblinearmodel, String[] beanargs) throws Exception{
		//load model and init params
		ParserOptions options = new ParserOptions(beanargs);
		MyPipe dp=new MyPipe(options);
		beanParser=new Parser(dp,options);
		beanParser.loadModel(options.modelName);
		decoder=new Decoder(beanParser.pipe, beanParser.GetParameters(), options);
		libModel=Model.load(new File(System.getenv("CODEDATA")+File.separator+liblinearmodel));
		featextractor=new DOSHTrain(System.getenv("CODEDATA")+File.separator+doshalphabetfile);
	}
	/*
	public DOSHParse(){
		//!only for test libpredict
	}
	*/
	public ParseAgenda DecodeOneInstance(DependencyInstance di){
		//params that need not update
		int senlength=di.length()-1;
		//params that need update
		boolean toright=true; //flag for the direction of indexpointer
		boolean backtracefailed=false;
		int indexpointer=0;
		int[] node=new int[senlength+2];
		ParseAgenda pa=new ParseAgenda(di.length());
		HashSet donechild=new HashSet();//store the nodes that has been done
		//store the max predict value of DO action of backtrace nodes
		int backtracemaxindex=-1;
		double backtracemaxvalue=Double.NEGATIVE_INFINITY;
		//init node
		for(int i=0;i<senlength+1;i++){
			node[i]=i;
		}
		node[senlength+1]=-1;
		
		//legend is begining, fighting bean!
		while(donechild.size()<senlength-1){//last element must do
			int endindexinnode=senlength-donechild.size();//0-endindex
			FeatureVector fv=new FeatureVector();
			featextractor.ExtractDoshFeature(di, donechild, pa, node, indexpointer, fv);
			double[] predict=PredictAction(fv);
			int action=(int) predict[0];
//			System.out.println("Actionpredict:"+action);
			if(backtracefailed==true){
				//read backtrace,to get index to do
				if(backtracemaxindex!=-1){
					action=DOSHParse.DO;
					indexpointer=backtracemaxindex;
//					System.out.println("backtracemaxindex:"+indexpointer);
					backtracemaxindex=-1;
					backtracemaxvalue=Double.NEGATIVE_INFINITY;
				}else{
					System.out.println("=====How come backtracemaxindex is -1 ?");
					System.exit(1);
				}
			}
			if(action==DOSHParse.DO){
				//no need to know the fv, and the predicted relation is stored in di
				System.out.println();
				int head=(int) decoder.FindHeadForOneWord(di, node[indexpointer], pa)[0];
				pa.heads[node[indexpointer]]=head;
				di.orders.put(donechild.size()+1, node[indexpointer]);
				pa.ChildProcess(node[indexpointer], head);
				donechild.add(node[indexpointer]);
				System.out.println("DO+"+node[indexpointer]+"\t");
				backtracemaxindex=-1;
				backtracemaxvalue=Double.NEGATIVE_INFINITY;
			}else if(action==DOSHParse.UNDO){
				if(toright==false){
					double predictvalue=predict[1];
//					System.out.println("predict value="+predictvalue+",backtracemaxvalue="+backtracemaxvalue);
					if(predictvalue>backtracemaxvalue){
						backtracemaxindex=indexpointer;
						backtracemaxvalue=predictvalue;
//						System.out.println("update backtracemaxvalue:"+backtracemaxvalue+",backindex:"+backtracemaxindex);
					}
					System.out.println("SH+end\t");
				}else{
					System.out.println("SH+"+node[indexpointer+1]+"\t");
				}
			}else{
				System.out.println(action);
				System.out.println("=====Are you kidding?");
			}
			Object[] ret=UpdateNodeAndPointerAndFlag(node, endindexinnode, toright, indexpointer, action);
//			System.out.println("Update:");
//			System.out.print("Node:");
//			for(int elem:node){
//				System.out.print("\t"+elem);
//			}
//			System.out.println();
			
			indexpointer=(int) ret[0];
//			System.out.println("indexpointer:"+indexpointer);
			toright=(boolean) ret[1];
//			System.out.println("toright:"+toright);
			backtracefailed=(boolean) ret[2];
//			System.out.println("backtracefailed:"+backtracefailed);
		}
		//do last node
		int head=(int) decoder.FindHeadForOneWord(di, node[1], pa)[0];
		pa.heads[node[1]]=head;
		pa.ChildProcess(node[1], head);
		di.heads=pa.heads;
		di.orders.put(donechild.size()+1, node[1]);
		return pa;
	}
	public Object[] UpdateNodeAndPointerAndFlag(int[] node, int endindex, boolean toright,int pointer, int action){
		//return:1.the new pointer 2.new flag
		int newpointer=pointer;
		boolean newtoright=toright;
		boolean backtracefailed=false;
		Object[] ret=new Object[3];
		if(toright){//normal action
			if(action==DOSHParse.DO){
				if(endindex==pointer){
					newtoright=false;
				}
				for(int i=pointer;i<=endindex;i++){
					node[i]=node[i+1];
				}
				newpointer--;
			}else{//UNDO
				if(pointer<endindex){//SH
					newpointer++;
				}else{//pointer=endindex
					newtoright=false;
					newpointer--;
				}
			}
		}else{//backtrace action
			if(action==DOSHParse.DO){//backtrace DO or DO after backtracefailed
				for(int i=pointer;i<=endindex;i++){
					node[i]=node[i+1];
				}
				newpointer=endindex-1;
			}else{//SH
				if(pointer==1){//backtrace fail
					backtracefailed=true;
				}else{
					newpointer--;
				}
			}
		}
		ret[0]=newpointer;
		ret[1]=newtoright;
		ret[2]=backtracefailed;
		return ret;
	}
	public double[] PredictAction(FeatureVector doshfv){
		int doindexinliblabel=1;//get by TestlibPredict()
		LiblinearInstance test=new LiblinearInstance(doshfv);
    	double predict=Linear.predict(libModel, test.TransformXfeat());
    	double[] estimates = new double[libModel.getNrClass()];
        double label = Linear.predictValues(libModel, test.TransformXfeat(), estimates);
        double dovalue=estimates[1];
        double[] ret=new double[2];
        ret[0]=label;
        ret[1]=dovalue;
        return ret;
	}
	public void Parse(String parsefile, String writefile) throws IOException{
		CONLLReader reader = new CONLLReader();
		reader.ordered=false;
        reader.startReading(System.getenv("CODEDATA") + File.separator + parsefile);
        File out = new File(System.getenv("CODEDATA") + File.separator + writefile);
        if (!out.exists()) {
            out.createNewFile();
        }
        CONLLWriter writer = new CONLLWriter(true);
        writer.startWriting(System.getenv("CODEDATA") + File.separator + writefile);

        DependencyInstance di;
        int instcount = 0;
        System.out.println("Process index:");
        long parsestart = System.currentTimeMillis();
        while ((di = reader.getNext()) != null) {
            ++instcount;
            System.out.print(instcount+" ");
            //if (instcount % 50 == 0) {
            //  System.out.print(instcount + "\t");
            //}
            //if (instcount % 30 == 0) System.out.print('\n');
            FeatureVector fv = new FeatureVector();//useless here, just align the param for DecodeInstance

            DecodeOneInstance(di);

            writer.write(new DependencyInstance(RemoveRoot(di.forms), RemoveRoot(di.postags), RemoveRoot(di.deprels), RemoveRoot(di.heads)),di.orders);
        }
        long parseend = System.currentTimeMillis();
        System.out.println("\n==============================================");
        System.out.println("Test File:" + beanParser.options.testfile);
        System.out.println("Model Name:" + beanParser.options.modelName);
        System.out.println("Sentence Number:" + instcount);
        System.out.println("Parse Time Total:" + (parseend - parsestart) / 1000.0);
        System.out.println("==============================================");
        writer.finishWriting();
	}
	private String[] RemoveRoot(String[] form) {
        String[] ret = new String[form.length - 1];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = form[i + 1];
        }
        return ret;
    }
	private int[] RemoveRoot(int[] form) {
        int[] ret = new int[form.length - 1];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = form[i + 1];
        }
        return ret;
    }
	/*
    public void TestLibPredict(String libmodelfile,String alphagetfile,String conllfile) throws IOException, ClassNotFoundException{
    	//try on 10136 on dutch train data
    	CONLLReader reader=new CONLLReader();
    	reader.startReading(conllfile);
    	DependencyInstance di=reader.getNext();
    	Model libModel=Model.load(new File(libmodelfile));
    	DOSHTrain featextract=new DOSHTrain(alphagetfile);
    	FeatureVector fv=new FeatureVector();
    	int node[]={0,1,2,3,4,5,6,7,8,9,10,11,12,13,-1};
    	featextract.ExtractDoshFeature(di, new HashSet(), new ParseAgenda(di.length()), node, 4, fv);
    	LiblinearInstance test=new LiblinearInstance(fv);
    	double predict=Linear.predict(libModel, test.TransformXfeat());
    	double[] estimates = new double[libModel.getNrClass()];
        double probabilityPrediction = Linear.predictValues(libModel, test.TransformXfeat(), estimates);
    	System.out.println("prediction:"+predict);
    	for(double temp:estimates){
    		System.out.println(" "+temp);
    	}
    	System.out.println("prediction:"+probabilityPrediction);
    	System.out.println("model.label[0]="+libModel.getLabels()[0]);
    	System.out.println("model.label[1]="+libModel.getLabels()[1]);
    }
    */
}
