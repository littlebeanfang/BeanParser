package Parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import mstparser.DependencyParser;
import mstparser.DependencyPipe;
import mstparser.DependencyPipe2O;
import DataStructure.Parameters;
import DataStructure.ParserOptions;

public class Train {
	public ParserOptions options;
	public Parameters params;
	public Train(ParserOptions options, Parameters params){
		this.options = options;
		this.params=params;
	}
	public void callTrain(){
		DependencyPipe pipe = new MyPipe (options);
		/*TODO Yizhong 
		 *Rewrite createInstances function, call our feature extraction function, delete second param
		 *Attentoin: 
		 *1. Add feature according to the gold parse
		 *2. Write function in MyPipe.java
		 */
		int[] instanceLengths = 
				pipe.createInstances(options.trainfile);
		
			    pipe.closeAlphabets();
			    Parser dp = new Parser(pipe, options);
			    
			    int numFeats = pipe.dataAlphabet.size();
			    int numTypes = pipe.typeAlphabet.size();
			    System.out.print("Num Feats: " + numFeats);	
			    System.out.println(".\tNum Edge Labels: " + numTypes);
			    
			    train(instanceLengths,options.trainfile);
			    
			    System.out.print("Saving model...");
			    dp.saveModel(options.modelName);
			    System.out.print("done.");
	}
	public void train(int[] instanceLengths, String trainfile) 
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

			    trainingIter(instanceLengths,trainfile,i+1);

			    long end = System.currentTimeMillis();
			    //System.out.println("Training iter took: " + (end-start));
			    System.out.println("|Time:"+(end-start)+"]");			
			}

			params.averageParams(i*instanceLengths.length);
				
		    }

		    private void trainingIter(int[] instanceLengths, String trainfile, int iter) throws IOException {
		    /**
		     * TODO: Yizhong
		     * create reader for trainfile, for instance reading later
		     */
			int numUpd = 0;
			//ObjectInputStream in = new ObjectInputStream(new FileInputStream(train_forest));
			boolean evaluateI = true;

			int numInstances = instanceLengths.length;

			for(int i = 0; i < numInstances; i++) {
			    if((i+1) % 500 == 0) {
				System.out.print((i+1)+",");
				//System.out.println("  "+(i+1)+" instances");
			    }
			    /**
			     * TODO: Yizhong
			     * Here, 
			     * 1. read an instance from train file: named inst for calling
			     * 2. must fill actParseTree and fv 
			     */
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
			    double upd = (double)(options.numIters*numInstances - (numInstances*(iter-1)+(i+1)) + 1);
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
