package Parser;

import ArcFilter.JNAArcFilter2;
import ArcFilter.JNIArcFilter;
import DataStructure.*;
import gnu.trove.TIntIntHashMap;

import java.io.*;
import java.util.HashSet;

public class Decoder {
    private MyPipe pipe;
    private Parameters param;
    private Beam beam;
    //private JNAArcFilter2 arcFilter2;
    private JNIArcFilter arcFilter2;

    public Decoder(DependencyPipe pipe, Parameters param, ParserOptions options) {
        this.pipe = (MyPipe) pipe;
        this.param = param;
        this.beam = new Beam(options.beamwidth);
        //this.arcFilter2=new JNAArcFilter2();
        if( pipe.options.filterarc==true){
        	this.arcFilter2=new JNIArcFilter();
        }
    }

    public Object[] DecodeInstance(DependencyInstance inst, TIntIntHashMap ordermap) throws IOException {
        beam.initialize(inst.length());
        //ParseAgenda pa = new ParseAgenda(inst.length());
        Object[] instret = new Object[3];
        ParseAgenda pa;
        //FeatureVector fvforinst = new FeatureVector();
        HashSet<String> headmodifier=null;
        if( pipe.options.filterarc==true){
        	headmodifier=arcFilter2.ArcFilter(inst);
        }
        //System.out.println("size:"+headmodifier.size());
        for (int orderindex = 1; orderindex < inst.length(); orderindex++) {
            //skip root node
            int childindex = ordermap.get(orderindex);
            pa = beam.getNext();
            while (pa != null) {
                for (int head = 0; head < inst.length(); head++) {
//                	System.out.println("head:"+head+",child:"+childindex);
//                	System.out.println(pa.toActParseTree());
                    if ((head != childindex) && (pa.FindRoot(head) != childindex )) {//
//                    	System.out.println("22head:"+head+",child:"+childindex);
                    	//!size==1,is a JNA bug !
                    	if( pipe.options.filterarc==false||headmodifier.contains(head+"_"+childindex)){//headmodifier.size()==1 ||
                    		FeatureVector fv = new FeatureVector();
                            pipe.extractFeatures(inst, childindex, head, pa, fv);
                            double temp = fv.getScore(param.parameters);
                            beam.addAgenda(pa.getScore() + temp, childindex, head, fv);
                    	}
                    }
                }
                
                pa = beam.getNext();
            }
            beam.finishIteration();
//            inst.heads[childindex] = parsehead;
        }
//        arcFilter2.TranverseHashset(headmodifier);
        pa = beam.findBest();
        //System.out.println(": " + pa.getScore());
        pa.AddArc(0, -1);//add root
        inst.heads = pa.heads;
        //PrintScores(inst, pa);
        instret[0] = pa;
        instret[1] = pa.fv;
        instret[2] = beam.getQueue();
        return instret;
    }

    public Object[] FindHeadForOneWord(DependencyInstance inst, int childindex, ParseAgenda pa) {
        Object[] ret = new Object[2];
        boolean verbose = false;
        int headindex = -1;
        double score = Double.NEGATIVE_INFINITY;
        FeatureVector actfv = new FeatureVector();
        for (int head = 0; head < inst.length(); head++) {
            if ((head != childindex) && (pa.FindRoot(head) != childindex)) { //Jia: if the root of the head is not child
                FeatureVector fv = new FeatureVector();
                //pipe.AddNewFeature(inst, childindex, head, pa, fv);
                pipe.extractFeatures(inst, childindex, head, pa, fv);

                double temp = fv.getScore(param.parameters);
                if (temp > score) {
                    score = temp;
                    headindex = head;
                    //must store best fv in DependencyInstance
                    actfv = fv;

                }
            }
        }
        if (verbose) {
            System.out.println("Instance:/n" + inst);
            System.out.println("Child index:" + childindex);
            System.out.println("Head index:" + headindex);
            System.out.println("Score:" + score);
        }

        //Update the disjoint-set
        pa.UpdateSet(childindex, headindex);

        //inst.fv.cat(actfv);
        //Bean: store feature vector in fvforinst, for Object d[][]
//		System.out.println("==================FindHeadForOneWord");
//		System.out.println("actfv while decoding:"+actfv.toString().split(" ").length+"   fv:"+actfv.toString());
        //System.out.println("fvforinst while decoding before cat:length"+fvforinst.toString().split(" ").length);
        //fvforinst=fvforinst.cat(actfv);
        //System.out.println("fvforinst while decoding:length"+fvforinst.toString().split(" ").length+"   fv:"+fvforinst.toString());
//		System.out.println("==================FindHeadForOneWord END");
        ret[0] = headindex;
        ret[1] = actfv;
        return ret;
    }

    public void PrintScores(DependencyInstance inst, ParseAgenda pa) throws IOException {
        int length = inst.length();
        Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Scores_PA.txt")));
        writer.write("child");
        for (int i = 0; i < length; i++)
            writer.write("\t" + i);
        writer.write("\n");
        for (int i = 0; i < length; i++) {
            writer.write(i + "\t");
            for (int j = 0; j < length; j++) {
                if (i != j) {
                    FeatureVector fv = new FeatureVector();
                    pipe.extractFeatures(inst, i, j, pa, fv);
                    double tmp = fv.getScore(param.parameters);
                    writer.write(String.format("%.2f\t", tmp));
                } else {
                    writer.write("0\t");
                }
            }
            writer.write("\n");
        }
        writer.close();
    }
}
