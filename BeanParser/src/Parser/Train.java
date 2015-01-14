package Parser;

import DataStructure.*;
import IO.CONLLReader;
import gnu.trove.TIntIntHashMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class Train {
    public ParserOptions options;
    public Parameters params;
    
    public Train(ParserOptions options) {
        this.options = options;
        //this.params=params;
    }

    public void callTrain() throws IOException, ClassNotFoundException {
        MyPipe pipe = new MyPipe(options);
        /*Author: Yizhong
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
        params = new Parameters(pipe.dataAlphabet.size());
//			    System.out.println("Init param:"+java.util.Arrays.toString(params.parameters));
        train(numInstances, options.trainfile, pipe, options);
        Parser dp = new Parser(pipe, options, params);
//			    System.out.println("Train param:"+java.util.Arrays.toString(params.parameters));
        System.out.print("Saving model...");
        dp.saveModel(options.modelName);
        System.out.print("done.");
    }

    public void train(int numInstances, String trainfile, MyPipe pipe, ParserOptions options)
            throws IOException, ClassNotFoundException {

        //System.out.print("About to train. ");
        //System.out.print("Num Feats: " + pipe.dataAlphabet.size());
        long traintimestart = System.currentTimeMillis();
        int i;
        for (i = 0; i < options.numIters; i++) {

            System.out.print(" Iteration " + i);
            //System.out.println("========================");
            //System.out.println("Iteration: " + i);
            //System.out.println("========================");
            System.out.print("[");

            long start = System.currentTimeMillis();

            trainingIter(numInstances, trainfile, i + 1, pipe, options);

            long end = System.currentTimeMillis();
            //System.out.println("Training iter took: " + (end-start));
//            System.out.println("|Time:" + (end - start) + "]");
//            Parser dp = new Parser(pipe, options, params);
//            System.out.print("Saving model for iteration "+i+" ...");
//            dp.saveModel(options.modelName+"_iter"+i);
//            System.out.println("done.");
        }

        params.averageParams(i * numInstances);
        long traintimeend = System.currentTimeMillis();
        System.out.println("==============================================");
        System.out.println("Train File:" + options.trainfile);
        System.out.println("Model Name:" + options.modelName);
        System.out.println("Sentence Number:" + numInstances);
        System.out.println("Iteration Number:" + options.numIters);
        System.out.println("Train Time Total:" + (traintimeend - traintimestart) / 1000.0);
        System.out.println("==============================================");
    }

    private void trainingIter(int numInstances, String trainfile, int iter, MyPipe pipe, ParserOptions options)
    		throws IOException, ClassNotFoundException {
        /**
         * Author: Yizhong
         * create reader for trainfile, for instance reading later
         */
        int numUpd = 0;
        //ObjectInputStream in = new ObjectInputStream(new FileInputStream(train_forest));
        boolean evaluateI = true;

        //int numInstances = instanceLengths.length;
        CONLLReader reader = new CONLLReader();
        ObjectInputStream in = null;
        if (!options.createForest)
        	reader.startReading(System.getenv("CODEDATA") + File.separator + trainfile);
        else 
        	in = new ObjectInputStream(new FileInputStream(options.trainforest));
        
        DependencyInstance inst;
        int currentInstance = 0;
        if (!options.createForest)
        	inst = reader.getNext();
        else {
        	inst = (DependencyInstance) in.readObject();
        	inst.fv = new FeatureVector((int[]) in.readObject());
        	inst.orders = (TIntIntHashMap) in.readObject();
        }
        while (inst != null) {
            currentInstance++;
            if (currentInstance % 500 == 0) {
                System.out.print(currentInstance + ",");
            }
            
            if (!options.createForest) inst.setFeatureVector(pipe.extractFeatureVector(inst));
            
            String[] labs = inst.deprels;
            int[] heads = inst.heads;

            StringBuffer spans = new StringBuffer(heads.length * 5);
            for (int i = 1; i < heads.length; i++) {
                spans.append(heads[i]).append("|").append(i).append(":").append(pipe.typeAlphabet.lookupIndex(labs[i])).append(" ");
            }
            inst.actParseTree = spans.substring(0, spans.length() - 1);


            /**
             * Author: Yizhong
             * Here,
             * 1. read an instance from train file: named inst for calling
             * 2. must fill actParseTree and fv
             */


            double upd = (double) (options.numIters * numInstances - (numInstances * (iter - 1) + currentInstance) + 1);
            //int K = options.trainK;
            int K = 1;//let's only consider best parse now
            Object[][] d;
            /**
             * Author: Bean
             * Fill Object[][] d
             * Attention:
             * 1. d[k][0] is feature vector, and d[k][1] is actParseTree
             * 2. may call decoder and turn ParseAgenda into tree string
             * 3. send pipe in createInstance and param in train
             */
            //FeatureVector fvforinst=new FeatureVector();
            Decoder decoder = new Decoder(pipe, params, options);
            Object[] decodeinstret = decoder.DecodeInstance(inst, inst.orders);
            //ParseAgenda parseAgenda = (ParseAgenda) decodeinstret[0];
            //FeatureVector fvforinst = (FeatureVector) decodeinstret[1];
            //d = new Object[1][2];//K=1, means best parse
//				System.out.println("trainingIter===================");
//				System.out.println("fvforinst:"+fvforinst.toString());
//				System.out.println("trainingIter END===================");
            ParseAgenda[] agendaArr = (ParseAgenda[]) decodeinstret[2];
            d = new Object[options.beamwidth][2];
            for (int i = 0;i < options.beamwidth;i++) {
            	d[i][0] = agendaArr[i].fv;
            	d[i][1] = agendaArr[i].toActParseTree();
            }
            //d[0][0] = fvforinst;
            //d[0][1] = parseAgenda.toActParseTree();
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
            params.updateParamsMIRA(inst, d, upd);

            if (!options.createForest)
            	inst = reader.getNext();
            else {
            	try {
            		inst = (DependencyInstance) in.readObject();
            		inst.fv = new FeatureVector((int[]) in.readObject());
            		inst.orders = (TIntIntHashMap) in.readObject();
            	}
            	catch (Exception e) {
            		inst = null;
            	}
            }
        }

        //System.out.println("");
        //System.out.println("  "+numInstances+" instances");
        in.close();
        System.out.print(numInstances);

    }
}
