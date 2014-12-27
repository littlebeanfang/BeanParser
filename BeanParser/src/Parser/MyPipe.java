package Parser;

import DataStructure.DependencyInstance;
import DataStructure.FeatureVector;
import DataStructure.ParseAgenda;
import DataStructure.ParserOptions;
import IO.CONLLReader;
import gnu.trove.TIntIntHashMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class MyPipe extends DependencyPipe {

    //-----------------------------Initialize---------------------------------------------
    public MyPipe(ParserOptions options) throws IOException {
        super(options);
        // TODO:Auto-generated constructor stub
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
        //System.out.println(this.options.secondOrder);
        //this.options.secondOrder = true;
        if (this.options.secondOrder) {
            addTwoOrderSiblingFeatures(instance, parentindex, childindex, pa, fv);
            addBeamFeatures(instance, parentindex, childindex, pa, fv);
        }
        addThreeOrderSiblingFeatures(instance, parentindex, childindex, pa, fv);
        addHMGfeatures(instance, parentindex, childindex, pa, fv);
    }



    private void addTwoOrderSiblingFeatures(DependencyInstance instance,
                                           int parentindex, int childindex, ParseAgenda pa, FeatureVector fv) {

        if (pa.tii.containsValue(parentindex)) { // this shows that the parent
//            // candidate has already been
//            // another word's parent
//            //System.out.print("\nMy: ");
//
//            //TODO: Yizhong---------------Nearest sibling-------------------------
//            if(childindex < parentindex){
//                StringBuffer lsb = pa.leftchilds.get(parentindex);
//                if(lsb != null){
//                    String[] left_childrens = lsb.toString().split("\t");
//                    int right_nearest = parentindex;
//                    for(String lch : left_childrens){
//                        int current_ch = Integer.parseInt(lch);
//                        if(current_ch > childindex && current_ch < right_nearest){
//                            right_nearest = current_ch;          //Structure child-nearest_existing_child-parent
//                        }
//                    }
//                    addTripFeatures(instance, childindex, right_nearest,
//                            parentindex, fv); // parent can be in any place
//                    addSiblingFeatures(instance, childindex, right_nearest,
//                            false, fv);
//                    addSiblingFeatures(instance, childindex, right_nearest,
//                            true, fv);
//                }
//            }
//
//            if(childindex > parentindex){
//                StringBuffer rsb = pa.rightchilds.get(parentindex);
//                if(rsb != null){
//                    String[] right_childrens = rsb.toString().split("\t");
//                    int left_nearest = parentindex;
//                    for(String rch : right_childrens){
//                        int current_ch = Integer.parseInt(rch);
//                        if(current_ch < childindex && current_ch > left_nearest){
//                            left_nearest = current_ch;          //Structure child-nearest_existing_child-parent
//                        }
//                    }
//                    addTripFeatures(instance, childindex, left_nearest,
//                            parentindex, fv); // parent can be in any place
//                    addSiblingFeatures(instance, childindex, left_nearest,
//                            false, fv);
//                    addSiblingFeatures(instance, childindex, left_nearest,
//                            true, fv);
//                }
//            }
            //----------------------------------------------------
                StringBuffer lsb = pa.leftchilds.get(parentindex);
                if (lsb != null) {
                    String[] left_childrens = lsb.toString().split("\t");
                    for (String existing_child : left_childrens) {     //MST 2nd order features
                        int existing_child_index = Integer.parseInt(existing_child);
                        addTripFeatures(instance, childindex, existing_child_index,
                                parentindex, fv);
                        addSiblingFeatures(instance, childindex, existing_child_index,
                                false, fv);
                        //addSiblingFeatures(instance, childindex, existing_child_index,
                        //        true, fv);
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
                        //addSiblingFeatures(instance, childindex, existing_child_index,
                        //        true, fv);
                    }
            }
        }
    }

    private void addThreeOrderSiblingFeatures(DependencyInstance instance,
                                              int parentindex, int childindex, ParseAgenda pa, FeatureVector fv){
        if (pa.tii.containsValue(parentindex)){
            if (childindex < parentindex) {
                StringBuffer lsb = pa.leftchilds.get(parentindex);
                if (lsb != null) {
                    String[] left_childrens = lsb.toString().split("\t");
                    int right_nearest = parentindex;
                    int left_nearest = -1;
                    for (String lch : left_childrens) {
                        int current_ch = Integer.parseInt(lch);
                        if (current_ch > childindex && current_ch < right_nearest) {
                            right_nearest = current_ch;
                        }
                        if (current_ch < childindex && current_ch > left_nearest) {
                            left_nearest = current_ch;
                        }
                    }

                    if (right_nearest != parentindex && left_nearest != -1) {
                        addRightLeftNearestFeatures(instance, parentindex, childindex, left_nearest, right_nearest, fv);
                    }
                }
            }

            if (childindex > parentindex) {
                StringBuffer rsb = pa.rightchilds.get(parentindex);
                if (rsb != null) {
                    String[] right_childrens = rsb.toString().split("\t");
                    int left_nearest = parentindex;
                    int right_nearest = instance.length()+1;
                    //int left_second_nearest = parentindex;
                    for (String rch : right_childrens) {
                        int current_ch = Integer.parseInt(rch);
                        if (current_ch < childindex && current_ch > left_nearest) {
                            left_nearest = current_ch;
                        }
                        if (current_ch > childindex && current_ch < right_nearest) {
                            right_nearest = current_ch;
                        }
                    }
                    if (left_nearest != parentindex && right_nearest != instance.length() + 1) {
                        addRightLeftNearestFeatures(instance, parentindex, childindex, left_nearest, right_nearest, fv);
                    }
                }
            }
        }
    }

    private void addHMGfeatures(DependencyInstance instance, int parentindex, int childindex, ParseAgenda pa, FeatureVector fv){
        //TODO: It still needs further discussion how to add this structure : the GHM or GMH or MGH or MHG and so on.
        if (pa.tii.containsValue(childindex)) { // this shows that the child
            // candidate is already used as another word's parent
            StringBuffer lsb = pa.leftchilds.get(childindex);
            StringBuffer rsb = pa.rightchilds.get(childindex);

            StringBuffer grandchildren_sb = new StringBuffer();

            if(lsb != null){
                grandchildren_sb.append(lsb);
                grandchildren_sb.append("\t");
            }
            if(rsb != null) {
                grandchildren_sb.append(rsb);
            }
            //System.out.println("lsb: "+lsb);
            //System.out.println("rsb: "+rsb);
            //System.out.println("children: "+children.toString());

            String[] grandchildren = grandchildren_sb.toString().split("\t");

/*            if (childindex > parentindex) {
                if (rsb != null) {  // add the structure G-H-M
                    grandchildren = rsb.toString().split("\t");

                }
            } else {
                if (lsb != null) { // add the structure M-H-G
                    grandchildren = lsb.toString().split("\t");
                }
            }

            if (lsb != null && rsb != null) {
                grandchildren = (lsb.append("\t").append(rsb)).toString().split("\t");
            } else {
                if (lsb == null && rsb != null) {
                    grandchildren = rsb.toString().split("\t");
                } else {
                    if (lsb != null && rsb == null) {
                        grandchildren = lsb.toString().split("\t");
                    }
                }
            }*/

            for (String grandchild : grandchildren) {    // add parent-child-grandchild structure features
                //System.out.print(grandchild);
                int grandchild_index = Integer.parseInt(grandchild);
                /*if((parentindex > childindex && grandchild_index > parentindex)     // without cross
                        ||(parentindex < childindex && grandchild_index < parentindex)){
                    continue;
                }*/
                addGrandchildFeatures(instance, parentindex, childindex, grandchild_index, fv);
            }
        }
    }

    private void addRightLeftNearestFeatures(DependencyInstance instance,
                                             int head, int modifier, int left_nearest_index,
                                             int righ_nearest_index,FeatureVector fv){
        String[] forms = instance.forms;
        String[] pos = instance.postags;
        String dir = head > modifier? "LA":"RA";

        String head_pos = pos[head];
        String modifier_pos = pos[modifier];

        String left_nearest_pos;
        String right_nearest_pos;

        if(head > modifier){
            left_nearest_pos = pos[left_nearest_index];
            right_nearest_pos = pos[righ_nearest_index];
        }
        else{
            left_nearest_pos = pos[left_nearest_index];
            right_nearest_pos = pos[righ_nearest_index];
        }

        add("YZ_LRN_LMR_POS=" + left_nearest_pos + "_" + modifier_pos + "_" +right_nearest_pos, 1.0,
                fv);

        add("YZ_LRN_LMRH_POS=" + left_nearest_pos + "_" + modifier_pos + "_" +right_nearest_pos
                + "_" + head_pos, 1.0, fv);

        add("YZ_LRN_LMR_DIR_POS=" + left_nearest_pos + "_" + modifier_pos + "_" +right_nearest_pos
                + "_" +dir , 1.0, fv);

        add("YZ_LRN_LMRH_DIR_POS=" + left_nearest_pos + "_" + modifier_pos + "_" +right_nearest_pos
                + "_" + head_pos + "_" + dir, 1.0, fv);

    }

    private void addSiblingFeatures(DependencyInstance instance, int ch1,
                                          int ch2, boolean isST, FeatureVector fv) {

        String[] forms = instance.forms;
        String[] pos = instance.postags;

        // ch1 is always the closes to par
        String dir = ch1 > ch2 ? "RA" : "LA";

        //String ch1_pos = isST ? "STPOS" : pos[ch1];
        String ch1_pos = pos[ch1];
        String ch2_pos = pos[ch2];
        //String ch1_word = isST ? "STWRD" : forms[ch1];
        String ch1_word = forms[ch1];
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

        //String side = (ch2 - par)*(ch1 - par) < 0 ? "SA":"DI";


        String p_c_dir = par < ch1 ? "RA" : "LA";
        String p_s_dir = par < ch2 ? "RA" : "LA";
        String c_s_dir = ch1 < ch2 ? "RA" : "LA";


        String par_pos = pos[par];
        String ch1_pos = pos[ch1];
        String ch2_pos = pos[ch2];

        String pTrip = par_pos + "_" + ch1_pos + "_" + ch2_pos;
        add("APOS_TRIP=" + pTrip, 1.0, fv);
        add("YZ_PCD_POS_TRIP=" + p_c_dir + "_" + pTrip, 1.0, fv);
        //add("YZ_PSD_POS_TRIP=" + p_s_dir + "_" + pTrip, 1.0, fv);
        add("YZ_PCD_PSD_POS_TRIP=" + p_c_dir + "_" + p_s_dir + "_" + pTrip, 1.0, fv);
        add("YZ_PCD_PSD_CSD_POS_TRIP=" + p_c_dir + "_" + p_s_dir + "_" + c_s_dir + "_" + pTrip, 1.0, fv);
    }

    //New features mentioned in Carreras's paper added to original MST parser for the structure of Grandparent-Head-Modifier
    //Written by Yizhong
    private void addGrandchildFeatures(DependencyInstance instance, int parent_index,
                                      int child_index, int grandchild, FeatureVector fv) {

        String[] forms = instance.forms;
        String[] pos = instance.postags;

        String parent_child_dir = parent_index < child_index ? "RA" : "LA";
        String child_grandchild_dir = child_index < grandchild ? "RA" : "LA";

        String parent_pos = pos[parent_index];
        String parent_word = forms[parent_index];
        String child_pos = pos[child_index];
        String child_word = forms[child_index];
        String grandchild_pos = pos[grandchild];
        String grandchild_word = forms[grandchild];

        add("YZ_PPCPGP_DIR=" + parent_child_dir + "_" + child_grandchild_dir + "_" + parent_pos + "_" + child_pos + "_" + grandchild_pos, 1.0, fv);
        add("YZ_PPGP_DIR=" + parent_child_dir + "_" + child_grandchild_dir + "_" + parent_pos + "_" + grandchild_pos, 1.0, fv);
        add("YZ_PWGW_DIR=" + parent_child_dir + "_" + child_grandchild_dir + "_" + parent_word + "_" + grandchild_word, 1.0, fv);
        add("YZ_PPGW_DIR=" + parent_child_dir + "_" + child_grandchild_dir + "_" + parent_pos + "_" + grandchild_word, 1.0, fv);
        add("YZ_PWGP_DIR=" + parent_child_dir + "_" + child_grandchild_dir + "_" + parent_word + "_" + grandchild_pos, 1.0, fv);

        add("YZ_PPCPGP=" + parent_pos + "_" + child_pos + "_" + grandchild_pos, 1.0, fv);
        add("YZ_PPGP=" + parent_pos + "_" + grandchild_pos, 1.0, fv);
        //add("YZ_CPGP=" + child_pos + "_" + grandchild_pos, 1.0, fv);
        add("YZ_PWGW=" + parent_word + "_" + grandchild_word, 1.0, fv);
        //add("YZ_CWGW=" + child_word + "_" + grandchild_word, 1.0, fv);
        add("YZ_PPGW=" + parent_pos + "_" + grandchild_word, 1.0, fv);
        //add("YZ_CPGW=" + child_pos + "_" + grandchild_word, 1.0, fv);
        add("YZ_PWGP=" + parent_word + "_" + grandchild_pos, 1.0, fv);
        //add("YZ_CWGP=" + child_word + "_" + grandchild_pos, 1.0, fv);
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
//        for (int i = 0; i < forms.length; i++) {
//            System.out.print(forms[i] + " ");
//        }
//        System.out.println(modifier);
//        System.out.println(lsb);
        if (lsb != null) {
            String[] left_children = lsb.toString().split("\t");
            for (String child : left_children) {
                int child_index = Integer.parseInt(child);
                if (child_index < clc_index)
                    clc_index = child_index;
            }
            String CLC_pos = pos[clc_index];
            add("YZ_PtCtCLCt=" + H_pos + "_" + M_pos + "_" + CLC_pos, 1.0, fv);
        }

        int crc_index = modifier;
        StringBuffer rsb = pa.rightchilds.get(modifier);
//        System.out.println(rsb);
        if (rsb != null) {
            String[] right_children = rsb.toString().split("\t");
            for (String child : right_children) {
                int child_index = Integer.parseInt(child);
                if (child_index > crc_index)
                    crc_index = child_index;
            }
            String CRC_pos = pos[crc_index];
            add("YZ_PtCtCRCt=" + H_pos + "_" + M_pos + "_" + CRC_pos, 1.0, fv);
        }

        //these beam features are not effective
        /*
        // The features that include the left children num and right children num of parent
        int left_ch_num = pa.numofleftchild.get(head);
        int right_ch_num = pa.numofrightchild.get(head);

        //System.out.println("The left children num"+left_ch_num);

        if(left_ch_num > 0){
            add("YZ_Ptla=" + H_pos + "_" + left_ch_num, 1.0, fv);
            add("YZ_Pwtla=" + H_word + "_" + H_pos + "_" + left_ch_num, 1.0, fv);
        }
        if(right_ch_num > 0){
            add("YZ_Ptra=" + H_pos + "_" + right_ch_num, 1.0, fv);
            add("YZ_Pwtra=" + H_word + "_" + H_pos + "_" + right_ch_num, 1.0, fv);
        }*/
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

        //System.out.println(System.getenv());
        labeled = reader.startReading(System.getenv("CODEDATA") + File.separator + file);
        
        ObjectOutputStream out = options.createForest ? new ObjectOutputStream(new FileOutputStream(
            options.trainforest)) : null;
        
        int numInstances = 0;
        DependencyInstance instance = reader.getNext();
        while (instance != null) {
            numInstances++;
            String[] labs = instance.deprels;
            for (int i = 0; i < labs.length; i++) {
                typeAlphabet.lookupIndex(labs[i]);
            }
            instance.setFeatureVector(extractFeatureVector(instance));
         
            if (options.createForest) writeInstance(instance, out);
            instance = reader.getNext();
        }
        closeAlphabets();
        
        if (options.createForest) out.close();
        System.out.println("Creating Alphabet Done.");
        return numInstances;
    }

    protected void writeInstance(DependencyInstance instance, ObjectOutputStream out) {
    	try {
    		out.writeObject(instance);
    		out.writeObject(instance.fv.keys());
    		out.writeObject(instance.orders);
    	}
    	catch (Exception e) {}
    }

}
