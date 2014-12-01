package Parser;

import DataStructure.DependencyInstance;
import DataStructure.FeatureVector;
import DataStructure.ParseAgenda;
import DataStructure.ParserOptions;
import IO.CONLLReader;
import gnu.trove.TIntArrayList;
import gnu.trove.TIntIntHashMap;
import gnu.trove.TIntIntIterator;

import java.io.File;
import java.io.IOException;

public class MyPipe extends DependencyPipe {

    //-----------------------------Initialize---------------------------------------------
    public MyPipe(ParserOptions options) throws IOException {
        super(options);
        // TODO Auto-generated constructor stub
    }


    //-----------------------------Functions for Parsing-------------------------------------
    public void extractFeatures(DependencyInstance instance, int childindex,
                                int parentindex, ParseAgenda pa, FeatureVector fv) { // given an
        // unparsed
        // instance,extract
        // features
        // from it
        // boolean labeled;
        boolean leftToRight = (childindex > parentindex);
        int small = leftToRight ? parentindex : childindex;
        int large = leftToRight ? childindex : parentindex;
        addCoreFeatures(instance, small, large, leftToRight, fv);

        addTwoOrderFeatures(instance, parentindex, childindex, pa, fv);

    }

    private final void addTwoOrderFeatures(DependencyInstance instance,
                                           int parentindex, int childindex, ParseAgenda pa, FeatureVector fv) {
        //System.out.println(childindex + "\t" + parentindex);

        if (pa.tii.containsKey(parentindex)) { // this shows that the parent
            // candidate already has head,so we
            // can add grandparent-parent-child
            // feature

        }

        if (pa.tii.containsValue(childindex)) { // this shows that the child
            // candidate is already used as
            // another word's parent

        }

        //for (TIntIntIterator iter = pa.tii.iterator(); iter.hasNext();){
        //	iter.advance();
        //	System.out.println(iter.key() + "\t" + iter.value());
        //}

        if (pa.tii.containsValue(parentindex)) { // this shows that the parent
            // candidate has already been
            // another word's parent
            for (TIntIntIterator iter = pa.tii.iterator(); iter.hasNext(); ) {
                iter.advance();
                int existing_child = iter.key();
                int parent = iter.value();
                if (parent == parentindex) {
                    addTripFeatures(instance, childindex, existing_child,
                            parentindex, fv); // parent can be in any place
                    addSiblingFeatures(instance, childindex, existing_child,
                            false, fv);
                    addSiblingFeatures(instance, childindex, existing_child,
                            true, fv);
                }
            }
        }
    }

    private final void addSiblingFeatures(DependencyInstance instance, int ch1,
                                          int ch2, boolean isST, FeatureVector fv) {

        String[] forms = instance.forms;
        String[] pos = instance.postags;

        // ch1 is always the closes to par
        String dir = ch1 > ch2 ? "RA" : "LA";

        String ch1_pos = isST ? "STPOS" : pos[ch1];
        String ch2_pos = pos[ch2];
        String ch1_word = isST ? "STWRD" : forms[ch1];
        String ch2_word = forms[ch2];

        add("CH_PAIR=" + ch1_pos + "_" + ch2_pos + "_" + dir, 1.0, fv);
        add("CH_WPAIR=" + ch1_word + "_" + ch2_word + "_" + dir, 1.0, fv);
        add("CH_WPAIRA=" + ch1_word + "_" + ch2_pos + "_" + dir, 1.0, fv);
        add("CH_WPAIRB=" + ch1_pos + "_" + ch2_word + "_" + dir, 1.0, fv);
        add("ACH_PAIR=" + ch1_pos + "_" + ch2_pos, 1.0, fv);
        add("ACH_WPAIR=" + ch1_word + "_" + ch2_word, 1.0, fv);
        add("ACH_WPAIRA=" + ch1_word + "_" + ch2_pos, 1.0, fv);
        add("ACH_WPAIRB=" + ch1_pos + "_" + ch2_word, 1.0, fv);

        int dist = Math.max(ch1, ch2) - Math.min(ch1, ch2);
        String distBool = "0";
        if (dist > 1)
            distBool = "1";
        if (dist > 2)
            distBool = "2";
        if (dist > 3)
            distBool = "3";
        if (dist > 4)
            distBool = "4";
        if (dist > 5)
            distBool = "5";
        if (dist > 10)
            distBool = "10";
        add("SIB_PAIR_DIST=" + distBool + "_" + dir, 1.0, fv);
        add("ASIB_PAIR_DIST=" + distBool, 1.0, fv);
        add("CH_PAIR_DIST=" + ch1_pos + "_" + ch2_pos + "_" + distBool + "_"
                + dir, 1.0, fv);
        add("ACH_PAIR_DIST=" + ch1_pos + "_" + ch2_pos + "_" + distBool, 1.0,
                fv);
    }

    private final void addTripFeatures(DependencyInstance instance, int par,
                                       int ch1, int ch2, FeatureVector fv) {

        String[] pos = instance.postags;

        // ch1 is always the closest to par
        String dir = par > ch2 ? "RA" : "LA";

        String par_pos = pos[par];
        String ch1_pos = ch1 == par ? "STPOS" : pos[ch1];
        String ch2_pos = pos[ch2];

        String pTrip = par_pos + "_" + ch1_pos + "_" + ch2_pos;
        add("POS_TRIP=" + pTrip + "_" + dir, 1.0, fv);
        add("APOS_TRIP=" + pTrip, 1.0, fv);

    }

    //New features mentioned in Carreras's paper added to original MST parser for the structure of Grandparent-Head-Modifier
    //Written by Yizhong
    private final void addGHMFeatures(DependencyInstance instance, int grandparent,
                                      int head, int modifier, FeatureVector fv) {

        String[] forms = instance.forms;
        String[] pos = instance.postags;

        String G_H_dir = grandparent < head ? "RA" : "LA";
        String H_M_dir = head < modifier ? "RA" : "LA";

        String G_pos = pos[grandparent];
        String G_word = forms[grandparent];
        String H_pos = pos[head];
        String H_word = forms[head];
        String M_pos = pos[modifier];
        String M_word = forms[modifier];

        add("YZ_PGPHPM=" + G_H_dir + "_" + H_M_dir + "_" + G_pos + "_" + H_pos + "_" + M_pos, 1.0, fv);
        add("YZ_PGPM=" + G_H_dir + "_" + H_M_dir + "_" + G_pos + "_" + M_pos, 1.0, fv);
        add("YZ_PHPM=" + G_H_dir + "_" + H_M_dir + "_" + H_pos + "_" + M_pos, 1.0, fv);
        add("YZ_WGWM=" + G_H_dir + "_" + H_M_dir + "_" + G_word + "_" + M_word, 1.0, fv);
        add("YZ_WHWM=" + G_H_dir + "_" + H_M_dir + "_" + H_word + "_" + M_word, 1.0, fv);
        add("YZ_PGWM=" + G_H_dir + "_" + H_M_dir + "_" + G_pos + "_" + M_word, 1.0, fv);
        add("YZ_PHWM=" + G_H_dir + "_" + H_M_dir + "_" + H_pos + "_" + M_word, 1.0, fv);
        add("YZ_WGPM=" + G_H_dir + "_" + H_M_dir + "_" + G_word + "_" + M_pos, 1.0, fv);
        add("YZ_WHPM=" + G_H_dir + "_" + H_M_dir + "_" + H_word + "_" + M_pos, 1.0, fv);
    }


    //The new features Yue Zhang used in her beam search algorithm parser
    //written by yizhong
    private final void addBeamFeatures(DependencyInstance instance, ParseAgenda pa, int head,
                                       int modifier, FeatureVector fv) {

        String[] forms = instance.forms;
        String[] pos = instance.postags;

        //String H_M_dir = head < modifier ? "RA" : "LA";

        String H_pos = pos[head];
        String H_word = forms[head];
        String M_pos = pos[modifier];
        String M_word = forms[modifier];

        int clc_index;
        int crc_index;
        //TODO: find the clc and crc index of the head-modifier pair
        String CLC_pos = pos[clc_index];
        String CRC_pos = pos[crc_index];

        String la;
        String ra;

        //TODO: la and ra is the number of children to the left or right

        add("YZ_PtCtCLCt=" + H_pos + "_" + M_pos + "_" + CLC_pos, 1.0, fv);
        add("YZ_PtCtCRCt=" + H_pos + "_" + M_pos + "_" + CRC_pos, 1.0, fv);

        add("YZ_Ptla=" + H_pos + "_" + la, 1.0, fv);
        add("YZ_PtRa=" + H_pos + "_" + ra, 1.0, fv);
        add("YZ_Pwtla=" + H_pos + "_" + H_word + "_" + la, 1.0, fv);
        add("YZ_Pwtra=" + H_pos + "_" + H_word + "_" + ra, 1.0, fv);
    }



//-----------------------------Functions for Training-------------------------------------
    public int[] createInstances(String file) throws IOException {

        createAlphabet(file);

        System.out.println("Num Features: " + dataAlphabet.size());

        labeled = depReader.startReading(file);              //There is a question why some funcions are abstract??????

        TIntArrayList lengths = new TIntArrayList();

        /*ObjectOutputStream out = options.createForest
                ? new ObjectOutputStream(new FileOutputStream(featFileName))
                : null;
        */
        DependencyInstance instance = depReader.getNext();
        int num1 = 0;

        System.out.println("Creating Feature Vector Instances: ");
        while (instance != null) {
            System.out.print(num1 + " ");

            instance.setFeatureVector(createFeatureVector(instance));

            String[] labs = instance.deprels;
            int[] heads = instance.heads;

            StringBuffer spans = new StringBuffer(heads.length * 5);
            for (int i = 1; i < heads.length; i++) {
                spans.append(heads[i]).append("|").append(i).append(":").append(typeAlphabet.lookupIndex(labs[i])).append(" ");
            }
            instance.actParseTree = spans.substring(0, spans.length() - 1);

            lengths.add(instance.length());

            /*if (options.createForest)
                writeInstance(instance, out);*/
            //instance = null;
            instance = depReader.getNext();

            num1++;
        }

        System.out.println();

        closeAlphabets();

        /*if (options.createForest)
            out.close();
        */
        return lengths.toNativeArray();

    }

    public FeatureVector extractFeatureVector(DependencyInstance instance) {

        final int instanceLength = instance.length();

        String[] labs = instance.deprels;
        int[] heads = instance.heads;
        TIntIntHashMap ordermap = instance.orders;
        FeatureVector fv = new FeatureVector();
        ParseAgenda pa = new ParseAgenda();
        for (int orderindex = 1; orderindex < instance.length(); orderindex++) {
            //skip root node
            int parseindex = ordermap.get(orderindex);
            int parsehead = heads[parseindex];
            extractFeatures(instance, parseindex, parsehead, pa, fv);
            pa.AddArc(parseindex, parsehead);
        }
        pa.AddArc(0, -1);//add root


        return fv;
    }

    public final int createMyAlphabet(String file) throws IOException {

        System.out.print("Creating Alphabet ... ");
        CONLLReader reader = new CONLLReader();
        labeled = reader.startReading(System.getenv("CODEDATA") + File.separator + file);
        int numInstances = 0;
        DependencyInstance instance = reader.getNext();
        while (instance != null) {
            numInstances++;
            String[] labs = instance.deprels;
            for (int i = 0; i < labs.length; i++) {
                typeAlphabet.lookupIndex(labs[i]);
            }
            extractFeatureVector(instance);
            instance = reader.getNext();
        }
        closeAlphabets();
        System.out.println("Done.");
        return numInstances;
    }


}
