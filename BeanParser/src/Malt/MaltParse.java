package Malt;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Stack;
import java.util.TreeSet;

import mstparser.Alphabet;
import DOSH.DOSHParse;
import DOSH.DOSHTrain;
import DOSH.LiblinearInstance;
import DataStructure.DependencyInstance;
import DataStructure.FeatureVector;
import DataStructure.ParseAgenda;
import DataStructure.ParserOptions;
import IO.CONLLReader;
import IO.CONLLWriter;
import Parser.Decoder;
import Parser.MyPipe;
import Parser.Parser;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;

public class MaltParse {
	private Model libModel;
	public Parser beanParser;//store pipe 
	private Decoder decoder;
	private MaltTrain featextractor;//init with doshalphabet,only used to extract features
	private static final int LA=1;
	private static final int RA=2;
	private static final int SH=3;
	private static final int POP=4;
	
	public MaltParse(String doshalphabetfile,  String liblinearmodel, String[] beanargs) throws Exception{
		//load model and init params
		ParserOptions options = new ParserOptions(beanargs);
		MyPipe dp=new MyPipe(options);
		beanParser=new Parser(dp,options);
		beanParser.loadModel(options.modelName);
		decoder=new Decoder(beanParser.pipe, beanParser.GetParameters(), options);
		libModel=Model.load(new File(System.getenv("CODEDATA")+File.separator+liblinearmodel));
		featextractor=new MaltTrain(System.getenv("CODEDATA")+File.separator+doshalphabetfile);
	}
	
	public ParseAgenda DecodeOneInstance(DependencyInstance di){
		ParseAgenda pa=new ParseAgenda(di.length());
		Stack<Integer> alpha=new Stack<Integer>();			//****
		alpha.push(0);
		Stack<Integer> beta=new Stack<Integer>();			//****
		for(int i=di.length()-1;i>0;i--){
			beta.push(i);
		}
		int ordercount=1;
		TreeSet<Integer> pop=new TreeSet<Integer>();
		//Stack<Integer> pop=new Stack<Integer>();
		while(!(alpha.size()==1&&beta.size()==0)){
			FeatureVector fv=new FeatureVector();
			featextractor.ExtractFeature(alpha, beta, di, pa, fv);
			int action=NextAction(fv, alpha, beta);
			int child,head;
			
			switch (action) {
			case MaltParse.LA:
				
				child=alpha.get(alpha.size()-2);
				System.out.print("LA"+child+"\t");
//				head=(int)decoder.FindHeadForOneWord(di, child, pa)[0];
				//TODO change the head to be gold one
//				pa.heads[child]=head;
//				pa.ChildProcess(child, head);
				pa.heads[child]=di.heads[child];
				pa.ChildProcess(child, di.heads[child]);
				
				di.orders.put(ordercount++, child);
				alpha.remove(alpha.size()-2);
				break;
			case MaltParse.RA:
				child=alpha.peek();
				System.out.print("RA"+child+"\t");
//				head=(int)decoder.FindHeadForOneWord(di, child, pa)[0];
				//TODO
//				pa.heads[child]=head;
//				pa.ChildProcess(child, head);
				pa.heads[child]=di.heads[child];
				pa.ChildProcess(child, di.heads[child]);
				
				di.orders.put(ordercount++, child);
				alpha.pop();
				break;
			case MaltParse.SH:
				System.out.print("SH"+beta.peek()+"\t");
				alpha.push(beta.pop());
				break;
			default:
				child=alpha.peek();
				System.out.print("POP"+child+"\t");
//				head=(int)decoder.FindHeadForOneWord(di, child, pa)[0];
				//TODO
//				pa.heads[child]=head;
//				pa.ChildProcess(child, head);
//				di.orders.put(ordercount++, child);
				
//				pa.heads[child]=di.heads[child];
//				pa.ChildProcess(child, di.heads[child]);
				
				//pop.push(child);
				pop.add(child);
				alpha.pop();
				break;
			}
		}
		/*
		while(pop.size()!=0){
			int child=pop.pop();
			pa.heads[child]=di.heads[child];
			di.orders.put(ordercount++, child);
		}
		*/
		Iterator iterator=pop.iterator();
		while(iterator.hasNext()){
			int child=(int) iterator.next();
			System.out.println("DOPOP"+child+"\t");
			pa.heads[child]=di.heads[child];
			di.orders.put(ordercount++, child);
		}
		System.out.println();
		di.heads=pa.heads;
		return pa;
	}
	
	public int NextAction(FeatureVector fv,Stack<Integer> alpha, Stack<Integer> beta){
		int rank[]=PredictActionRank(fv);
		for(int item:rank){
			if(permissible(alpha, beta, item)){
				return item;
			}
		}
		return MaltParse.POP;
	}
	private boolean permissible(Stack<Integer> alpha, Stack<Integer> beta,int action){
		if((action==MaltParse.LA||action==MaltParse.RA)&&alpha.size()<2){
			return false;
		}
		if(action==MaltParse.LA &&alpha.size()==2){
			return false;
		}
		if(action==MaltParse.SH&&beta.size()==0){
			return false;
		}
		return true;
	}
	/**
	 * return the rank of labels,decending order, index 0 is best
	 * @param fv
	 * @return
	 */
	private int[] PredictActionRank(FeatureVector fv){
		int[] retlabel=new int[3];
		LiblinearInstance test=new LiblinearInstance(fv);
    	//double predict=Linear.predict(libModel, test.TransformXfeat());
    	double[] estimates = new double[libModel.getNrClass()];
        int best=(int)Linear.predictValues(libModel, test.TransformXfeat(), estimates);
        double maxvalue=Double.NEGATIVE_INFINITY;
        double minvalue=Double.POSITIVE_INFINITY;
        int maxindex=-1;
        int minindex=-1;
        for(int i=0;i<estimates.length;i++){
        	if(estimates[i]!=best){
	        	if(estimates[i]<minvalue){
	        		minvalue=estimates[i];
	        		minindex=i;
	        	}
	        	if(estimates[i]>maxvalue){
	        		maxvalue=estimates[i];
	        		maxindex=i;
	        	}
        	}
        }
        	
        int labels[]=libModel.getLabels();
        retlabel[0]=best;
        retlabel[1]=labels[maxindex];
        retlabel[2]=labels[minindex];
        return retlabel;
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
}
