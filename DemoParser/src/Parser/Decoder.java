package Parser;

import DataStructure.*;
import gnu.trove.TIntIntHashMap;

import java.io.*;

public class Decoder {
    public ParserOptions options;
    private MyPipe pipe;
    private Parameters param;

    public Decoder(DependencyPipe pipe, Parameters param, ParserOptions options) {
        this.pipe = (MyPipe) pipe;
        this.param = param;
        this.options = options;
    }

    public Object[] DecodeInstance(DependencyInstance inst, TIntIntHashMap ordermap) throws IOException {
        ParseAgenda pa = new ParseAgenda(pipe.typeAlphabet);
        Object[] instret = new Object[3];
        FeatureVector fvforinst = new FeatureVector();
        FeatureVector labeledFv = new FeatureVector();

        // set: disjoint-set data structure, stores the parent of each node
        int[] set = new int[inst.length()];
        for (int i = 0; i < inst.length(); i++) set[i] = i;

        for (int orderindex = 1; orderindex < inst.length(); orderindex++) {
            //skip root node
            int parseindex = ordermap.get(orderindex);
            Object[] ret = this.FindHeadForOneWord(inst, parseindex, pa, set);
            int parsehead = (Integer) ret[0];
//			System.out.println("DecodeInstance fvforinst after call findhead:"+ret[1].toString().split(" ").length);
            pa.AddArc(parseindex, parsehead);
            //System.out.println("Index: "+parseindex+"Head: "+parsehead);
            pa.ChildProcess(parseindex, parsehead);
            fvforinst = fvforinst.cat((FeatureVector) ret[1]);
            if (options.labeled) {
                labeledFv = labeledFv.cat((FeatureVector) ret[2]);
                inst.deprels[parseindex] = (String) ret[3];
            }
            //System.out.println("FV: \n"+fvforinst.toString());
            inst.heads[parseindex] = parsehead;
        }

        pa.AddArc(0, -1);//add root

        instret[0] = pa;
        instret[1] = fvforinst;
        instret[2] = labeledFv;
        return instret;
    }

    public Object[] FindHeadForOneWord(DependencyInstance inst, int childindex, ParseAgenda pa, int[] set) {
        Object[] ret = new Object[4];
        int headindex = -1;
        double score = Double.NEGATIVE_INFINITY;
        FeatureVector actfv = new FeatureVector();

        FeatureVector bestlabelfv = new FeatureVector();
        String bestrelation = "";

        for (int head = 0; head < inst.length(); head++) {
            if ((head != childindex) && (FindRoot(head, set) != childindex)) { //Jia: if the root of the head is not child
                FeatureVector fv = new FeatureVector();
                pipe.extractFeatures(inst, childindex, head, pa, fv);
                Object[] retfv = new Object[2];
                FeatureVector labelfv = new FeatureVector();
                if (options.labeled) {
                    retfv = pipe.extractLabeledFeatures(inst, childindex, head, param);
                    labelfv = (FeatureVector) retfv[0];
                    fv = fv.cat(labelfv);
                }

                double temp = fv.getScore(param.parameters);
                if (temp > score) {
                    score = temp;
                    headindex = head;   //must store best fv in DependencyInstance
                    actfv = fv;
                    if (options.labeled) {
                        //System.out.println((String)retfv[2]);
                        bestlabelfv = labelfv;
                        bestrelation = (String) retfv[1];
                    }
                }
            }
        }

        //Update the disjoint-set
        set[childindex] = headindex;

        ret[0] = headindex;
        ret[1] = actfv;
        if (options.labeled) {
            ret[2] = bestlabelfv;
            ret[3] = bestrelation;
        }
        return ret;
    }

    private int FindRoot(int node, int[] set) {
        if (set[node] != node) set[node] = FindRoot(set[node], set);
        return set[node];
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
