package Parser;

import DataStructure.DependencyInstance;
import DataStructure.FeatureVector;
import DataStructure.ParseAgenda;
import DataStructure.ParserOptions;
import IO.CONLLReader;
import gnu.trove.TIntIntHashMap;

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

        addBeamFeatures(instance, parentindex, childindex, pa, fv);

    }

    private void addTwoOrderFeatures(DependencyInstance instance,
                                           int parentindex, int childindex, ParseAgenda pa, FeatureVector fv) {
        //System.out.println(childindex + "\t" + parentindex);

        if (pa.tii.containsKey(parentindex)) { // this shows that the parent
            // candidate already has head,so we
            // can add grandparent-parent-child
            // feature
        }

        /*//TODO: It still needs further discussion how to add this structure : the GHM or GMH or MGH or MHG and so on.
        if (pa.tii.containsValue(childindex)) { // this shows that the child
            // candidate is already used as
            // another word's parent
            StringBuffer lsb = pa.leftchilds.get(childindex);
            StringBuffer rsb = pa.rightchilds.get(childindex);
            String[] grandchildren = {};

            if (childindex > parentindex) {
                if (rsb != null) {  // add the structure G-H-M
                    grandchildren = rsb.toString().split("\t");
                }
            } else {
                if (lsb != null) { // add the structure M-H-G
                    grandchildren = lsb.toString().split("\t");
                }
            }

            *//*if (lsb != null && rsb != null) {
                grandchildren = (lsb.append("\t").append(rsb)).toString().split("\t");
            } else {
                if (lsb == null && rsb != null) {
                    grandchildren = rsb.toString().split("\t");
                } else {
                    if (lsb != null && rsb == null) {
                        grandchildren = lsb.toString().split("\t");
                    }
                }
            }*//*

            for (String grandchild : grandchildren) {    // add parent-child-grandchild structure features
                //System.out.print(grandchild);
                int grandchild_index = Integer.parseInt(grandchild);
                addGHMFeatures(instance, parentindex, childindex, grandchild_index, fv);
            }

        }*/


        if (pa.tii.containsValue(parentindex)) { // this shows that the parent
            // candidate has already been
            // another word's parent
            //System.out.print("\nMy: ");
            StringBuffer lsb = pa.leftchilds.get(parentindex);
            if (lsb != null) {
                String[] left_childrens = lsb.toString().split("\t");
                for (String existing_child : left_childrens) {     //MST 2nd order features
                    int existing_child_index = Integer.parseInt(existing_child);
                    addTripFeatures(instance, childindex, existing_child_index,
                            parentindex, fv); // parent can be in any place
                    addSiblingFeatures(instance, childindex, existing_child_index,
                            false, fv);
                    addSiblingFeatures(instance, childindex, existing_child_index,
                            true, fv);
                    //System.out.print(existing_child_index+"\t");
                }
            }
            StringBuffer rsb = pa.rightchilds.get(parentindex);
            if (rsb != null) {
                String[] right_childrens = rsb.toString().split("\t");
                for (String existing_child : right_childrens) {    //MST 2nd order features
                    int existing_child_index = Integer.parseInt(existing_child);
                    addTripFeatures(instance, childindex, existing_child_index,
                            parentindex, fv); // parent can be in any place
                    addSiblingFeatures(instance, childindex, existing_child_index,
                            false, fv);
                    addSiblingFeatures(instance, childindex, existing_child_index,
                            true, fv);
                    //System.out.print(existing_child_index+"\t");
                }
            }
            //System.out.print("\nMST: ");
            //Since parseagenda is changed, this laborious work is unnecessary
            /*for (TIntIntIterator iter = pa.tii.iterator(); iter.hasNext(); ) {
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
                    //System.out.print(existing_child+"\t");
                }
            }*/
            //System.out.print("\n");
            //System.out.println(parentindex);
            //System.out.println(childindex);
        }
    }

    private void addSiblingFeatures(DependencyInstance instance, int ch1,
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

    private void addTripFeatures(DependencyInstance instance, int par,
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
    private void addGHMFeatures(DependencyInstance instance, int grandparent,
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
    private void addBeamFeatures(DependencyInstance instance, int head,
                                       int modifier, ParseAgenda pa, FeatureVector fv) {

        String[] forms = instance.forms;
        String[] pos = instance.postags;

        String H_pos = pos[head];
        String H_word = forms[head];
        String M_pos = pos[modifier];

        int clc_index = modifier;
        StringBuffer lsb = pa.leftchilds.get(modifier);
        for (int i = 0; i < forms.length; i++) {
            System.out.print(forms[i] + " ");
        }
        System.out.println(modifier);
        System.out.println(lsb);
        if (lsb != null) {
            String[] left_children = lsb.toString().split("\t");
            for (String child : left_children) {
                int child_index = Integer.parseInt(child);
                if (child_index < clc_index)
                    clc_index = child_index;
            }
        }

        int crc_index = modifier;
        StringBuffer rsb = pa.rightchilds.get(modifier);
        System.out.println(rsb);
        if (rsb != null) {
            String[] right_children = rsb.toString().split("\t");
            for (String child : right_children) {
                int child_index = Integer.parseInt(child);
                if (child_index > crc_index)
                    crc_index = child_index;
            }
        }

        String CLC_pos = pos[clc_index];
        String CRC_pos = pos[crc_index];

        add("YZ_PtCtCLCt=" + H_pos + "_" + M_pos + "_" + CLC_pos, 1.0, fv);
        add("YZ_PtCtCRCt=" + H_pos + "_" + M_pos + "_" + CRC_pos, 1.0, fv);


        // The features that include the left children num and right children num of parent
        int left_ch_num = pa.numofleftchild.get(head);
        int right_ch_num = pa.numofrightchild.get(head);

        //System.out.println("The left children num"+left_ch_num);


        add("YZ_Ptla=" + H_pos + "_" + left_ch_num, 1.0, fv);
        add("YZ_Ptra=" + H_pos + "_" + right_ch_num, 1.0, fv);
        add("YZ_Pwtla=" + H_word + "_" + H_pos + "_" + left_ch_num, 1.0, fv);
        add("YZ_Pwtra=" + H_word + "_" + H_pos + "_" + right_ch_num, 1.0, fv);
    }


    //-----------------------------Functions for Training-------------------------------------

    public FeatureVector extractFeatureVector(DependencyInstance instance) {

        //final int instanceLength = instance.length();

        //String[] labs = instance.deprels;
        int[] heads = instance.heads;
        TIntIntHashMap order_map = instance.orders;
        FeatureVector fv = new FeatureVector();
        ParseAgenda pa = new ParseAgenda();
        for (int order_index = 1; order_index < instance.length(); order_index++) {
            //skip root node
            int parse_index = order_map.get(order_index);
            int parse_head = heads[parse_index];

            extractFeatures(instance, parse_index, parse_head, pa, fv);
            pa.ChildProcess(parse_index, parse_head); //Yizhong: This ChildProcess() should be after the extractFeatures()
            pa.AddArc(parse_index, parse_head);
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
        System.out.println("Creating Alphabet Done.");
        return numInstances;
    }


}
