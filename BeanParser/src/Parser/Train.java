package Parser;

import gnu.trove.TIntIntHashMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;



import DataStructure.DependencyInstance;
import DataStructure.FeatureVector;
import DataStructure.Parameters;
import DataStructure.ParseAgenda;
import DataStructure.ParserOptions;
import IO.CONLLReader;

public class Train {
	public ParserOptions options;
	public Parameters params;
	public Train(ParserOptions options){
		this.options = options;
		//this.params=params;
	}
	public void callTrain() throws IOException{
		MyPipe pipe = new MyPipe (options);
		/*TODO Yizhong 
		 *Rewrite createInstances function, call our feature extraction function, delete second param
		 *Attentoin: 
		 *1. Add feature according to the gold parse
		 *2. Write function in MyPipe.java
		 */
		int numInstances = pipe.createMyAlphabet(options.trainfile);
        //pipe.closeAlphabets();
		
			    //pipe.closeAlphabets();
			    
			    int numFeats = pipe.dataAlphabet.size();
			    int numTypes = pipe.typeAlphabet.size();
			    System.out.print("Num Feats: " + numFeats);	
			    System.out.println(".\tNum Edge Labels: " + numTypes);
			    params=new Parameters(pipe.dataAlphabet.size());
			    train(numInstances,options.trainfile,pipe);
			    Parser dp = new Parser(pipe, options,params);
			    System.out.print("Saving model...");
			    dp.saveModel(options.modelName);
			    System.out.print("done.");
	}
	public void train(int numInstances, String trainfile, MyPipe  pipe) 
			throws IOException {
				
			//System.out.print("About to train. ");
			//System.out.print("Num Feats: " + pipe.dataAlphabet.size());
				
			int i = 0;
			for(i = 0; i < options.numIters; i++) {
					
			    System.out.print(" Iteration "+i);
			    //System.out.println("========================");
			    //System.out.println("Iteration: " + i);
			    //System.out.println("========================");
			    System.out.print("[");

			    long start = System.currentTimeMillis();

			    trainingIter(numInstances,trainfile,i+1,pipe);

			    long end = System.currentTimeMillis();
			    //System.out.println("Training iter took: " + (end-start));
			    System.out.println("|Time:"+(end-start)+"]");			
			}

			params.averageParams(i*numInstances);
				
		    }

		    private void trainingIter(int numInstances, String trainfile, int iter, MyPipe pipe) throws IOException {
		    /**
		     * TODO: Yizhong
		     * create reader for trainfile, for instance reading later
		     */
			int numUpd = 0;
			//ObjectInputStream in = new ObjectInputStream(new FileInputStream(train_forest));
			boolean evaluateI = true;

			//int numInstances = instanceLengths.length;

			
	        CONLLReader reader = new CONLLReader();
	        reader.startReading(System.getenv("CODEDATA") + File.separator + trainfile);
	        DependencyInstance inst;
	        int currentInstance = 0;
	        while((inst=reader.getNext())!=null){
	            currentInstance++;
	            if (currentInstance % 500 == 0) {
	                System.out.print(currentInstance + ",");
	            }
	            inst.setFeatureVector(pipe.extractFeatureVector(inst));
	            String[] labs = inst.deprels;
	            int[] heads = inst.heads;

	            StringBuffer spans = new StringBuffer(heads.length * 5);
	            for (int i = 1; i < heads.length; i++) {
	                spans.append(heads[i]).append("|").append(i).append(":").append(pipe.typeAlphabet.lookupIndex(labs[i])).append(" ");
	            }
	            inst.actParseTree = spans.substring(0, spans.length() - 1);
			
		
			    /**
			     * TODO: Yizhong
			     * Here, 
			     * 1. read an instance from train file: named inst for calling
			     * 2. must fill actParseTree and fv 
			     */
			    
			    //bean
			   // TIntIntHashMap ordermap;
			    
			    
			    //int length = instanceLengths[i];

			    /* we don't need to get these vectors
			    // Get production crap.
			    FeatureVector[][][] fvs = new FeatureVector[length][length][2];
			    double[][][] probs = new double[length][length][2];
			    FeatureVector[][][][] nt_fvs = new FeatureVector[length][pipe.types.length][2][2];
			    double[][][][] nt_probs = new double[length][pipe.types.length][2][2];
			    FeatureVector[][][] fvs_trips = new FeatureVector[length][length][length];
			    double[][][] probs_trips = new double[length][length][length];
			    FeatureVector[][][] fvs_sibs = new FeatureVector[length][length][2];
			    double[][][] probs_sibs = new double[length][length][2];

			    DependencyInstance inst;

			    if(options.secondOrder) {
				inst = ((DependencyPipe2O)pipe).readInstance(in,length,fvs,probs,
									     fvs_trips,probs_trips,
									     fvs_sibs,probs_sibs,
									     nt_fvs,nt_probs,params);
			    }

			    else
				inst = pipe.readInstance(in,length,fvs,probs,nt_fvs,nt_probs,params);
*/
			    double upd = (double)(options.numIters*numInstances - (numInstances*(iter-1)+currentInstance) + 1);
			    //int K = options.trainK;
			    int K=1;//let's only consider best parse now
			    Object[][] d = null;
			    /**
			     * TODO: Bean
			     * Fill Object[][] d
			     * Attention:
			     * 1. d[k][0] is feature vector, and d[k][1] is actParseTree
			     * 2. may call decoder and turn ParseAgenda into tree string
			     * 3. send pipe in createInstance and param in train
			     */
			    Decoder decoder=new Decoder(pipe, params);
			    FeatureVector fvforinst=new FeatureVector();
				ParseAgenda parseAgenda=decoder.DecodeInstance(inst, inst.orders,fvforinst);
				d=new Object[1][2];//K=1, means best parse
				d[0][0]=fvforinst;
				d[0][1]=parseAgenda.toActParseTree();
				//Bean: ignore labeled errors in inst
			    /*
			    if(options.decodeType.equals("proj")) {
				if(options.secondOrder)
				    d = ((DependencyDecoder2O)decoder).decodeProjective(inst,fvs,probs,
											fvs_trips,probs_trips,
											fvs_sibs,probs_sibs,
											nt_fvs,nt_probs,K);
				else
				    d = decoder.decodeProjective(inst,fvs,probs,nt_fvs,nt_probs,K);
			    }
			    if(options.decodeType.equals("non-proj")) {
				if(options.secondOrder)
				    d = ((DependencyDecoder2O)decoder).decodeNonProjective(inst,fvs,probs,
										       fvs_trips,probs_trips,
										       fvs_sibs,probs_sibs,
										       nt_fvs,nt_probs,K);
				else
				    d = decoder.decodeNonProjective(inst,fvs,probs,nt_fvs,nt_probs,K);
			    }
			    */
			    params.updateParamsMIRA(inst,d,upd);

			}

			//System.out.println("");	
			//System.out.println("  "+numInstances+" instances");

			System.out.print(numInstances);
				
			//in.close();

		    }
}
