package Parser;
import DataStructure.Alphabet;
import DataStructure.DependencyInstance;
import DataStructure.FeatureVector;
import DataStructure.ParserOptions;
import DataStructure.ParseAgenda;
import gnu.trove.TIntIntIterator;



public class FeatureExtractor{
    public Alphabet dataAlphabet;
    private ParserOptions options;
    boolean labeled = false;

    public FeatureExtractor(ParserOptions options) throws Exception{
        this.options = options;
        dataAlphabet = new Alphabet();
    }

    public void extractFeatures(DependencyInstance instance,
                                int childindex, int parentindex,
                                ParseAgenda pa, FeatureVector fv){   //given an unparsed instance,extract features from it
        //boolean labeled;
        boolean leftToRight = (childindex > parentindex);
        int small = leftToRight?parentindex:childindex;
        int large = leftToRight?childindex:parentindex;
        addCoreFeatures(instance, small, large, leftToRight, fv);
        
        addTwoOrderFeatures(instance, parentindex, childindex, pa, fv);
        
    }

    private final void addTwoOrderFeatures(DependencyInstance instance, int parentindex, int childindex, ParseAgenda pa, FeatureVector fv){
        if(pa.containsKey(parentindex)){    //this shows that the parent candidate already has head,so we can add grandparent-parent-child feature

        }

        if(pa.containsValue(childindex)){   // this shows that the child candidate is already used as another word's parent

        }

        if(pa.containsValue(parentindex)){       //this shows that the parent candidate has already been another word's parent
            for(TIntIntIterator iter = pa.iterator();iter.hasNext();) {
                iter.advance();
                int existing_child = iter.key();
                int parent = iter.value();
                if(parent == parentindex){
                    addTripFeatures(instance,childindex,existing_child,parentindex,fv);   //parent can be in any place
                    addSiblingFeatures(instance,childindex,existing_child,false,fv);
                    addSiblingFeatures(instance,childindex,existing_child,true,fv);
                }
            }
        }
    };

    public FeatureVector extractParsedFeatures(DependencyInstance instance){  // given a parsed sentence instance,extract features
        final int instanceLength = instance.length();
        String[] labels = instance.deprels;
        int[] heads = instance.heads;
        FeatureVector fv = new FeatureVector();

        for (int i = 0; i < instanceLength; i++) {
            int small = i < heads[i] ? i : heads[i];
            int large = i > heads[i] ? i : heads[i];
            boolean leftToRight = i < heads[i] ? false : true;
            addCoreFeatures(instance, small, large, leftToRight, fv);
            if (labeled) {
                addLabeledFeatures(instance, i, labels[i], leftToRight, true, fv);
                addLabeledFeatures(instance, heads[i], labels[i], leftToRight, false, fv);
            }
        }
        return fv;
    }

    // add with default 1.0
    public final void add(String feat, FeatureVector fv) {
        int num = dataAlphabet.lookupIndex(feat);
        if (num >= 0)
            fv.add(num, 1.0);
    }

    public final void add(String feat, double val, FeatureVector fv) {
        int num = dataAlphabet.lookupIndex(feat);
        if (num >= 0)
            fv.add(num, val);
    }

    private final void addCoreFeatures(DependencyInstance instance, int small, int large, boolean leftToRight, FeatureVector fv) {
        String[] forms = instance.forms;
        String[] pos = instance.postags;
        String[] cpos = instance.cpostags;
        String orientation = leftToRight ? "RA" : "LA";
        int dist = Math.abs(large - small);
        String distBool = "0";
        if (dist > 10)
            distBool = "10";
        else if (dist > 5)
            distBool = "5";
        else
            distBool = Integer.toString(dist - 1);
        String attDist = "&" + orientation + "&" + distBool;
        addLinearFeatures("POS", pos, small, large, attDist, fv);
        addLinearFeatures("CPOS", cpos, small, large, attDist, fv);
        int headIndex = small;
        int childIndex = large;
        if (!leftToRight) {
            headIndex = large;
            childIndex = small;
        }

        addTwoObsFeatures("HC", forms[headIndex], pos[headIndex], forms[childIndex], pos[childIndex], attDist, fv);
        addTwoObsFeatures("HCA", forms[headIndex], cpos[headIndex], forms[childIndex], cpos[childIndex], attDist, fv);
        addTwoObsFeatures("HCC", instance.lemmas[headIndex], pos[headIndex], instance.lemmas[childIndex], pos[childIndex], attDist, fv);
        addTwoObsFeatures("HCD", instance.lemmas[headIndex], cpos[headIndex], instance.lemmas[childIndex], cpos[childIndex], attDist, fv);
        // Add in features from the feature lists. It assumes
        // the feature lists can have different lengths for
        // each item. For example, nouns might have a
        // different number of morphological features than
        // verbs.

        for (int i = 0; i < instance.feats[headIndex].length; i++) {
            for (int j = 0; j < instance.feats[childIndex].length; j++) {
                addTwoObsFeatures("FF" + i + "*" + j,
                        instance.forms[headIndex],
                        instance.feats[headIndex][i],
                        instance.forms[childIndex],
                        instance.feats[childIndex][j],
                        attDist, fv);
                addTwoObsFeatures("LF" + i + "*" + j,
                        instance.lemmas[headIndex],
                        instance.feats[headIndex][i],
                        instance.lemmas[childIndex],
                        instance.feats[childIndex][j],
                        attDist, fv);
            }
        }
    }

    private final void addLinearFeatures(String type, String[] obsVals, int first, int second, String attachDistance, FeatureVector fv) {
        String pLeft = first > 0 ? obsVals[first - 1] : "STR";
        String pRight = second < obsVals.length - 1 ? obsVals[second + 1] : "END";
        String pLeftRight = first < second - 1 ? obsVals[first + 1] : "MID";
        String pRightLeft = second > first + 1 ? obsVals[second - 1] : "MID";

        // feature posR posMid posL
        StringBuilder featPos =
                new StringBuilder(type + "PC=" + obsVals[first] + " " + obsVals[second]);

        for (int i = first + 1; i < second; i++) {
            String allPos = featPos.toString() + ' ' + obsVals[i];
            add(allPos, fv);
            add(allPos + attachDistance, fv);

        }

        addCorePosFeatures(type + "PT", pLeft, obsVals[first], pLeftRight,
                pRightLeft, obsVals[second], pRight, attachDistance, fv);

    }

    private final void addCorePosFeatures(String prefix,
                                          String leftOf1, String one, String rightOf1,
                                          String leftOf2, String two, String rightOf2,
                                          String attachDistance, FeatureVector fv) {

        // feature posL-1 posL posR posR+1

        add(prefix + "=" + leftOf1 + " " + one + " " + two + "*" + attachDistance, fv);

        StringBuilder feat =
                new StringBuilder(prefix + "1=" + leftOf1 + " " + one + " " + two);
        add(feat.toString(), fv);
        feat.append(' ').append(rightOf2);
        add(feat.toString(), fv);
        feat.append('*').append(attachDistance);
        add(feat.toString(), fv);

        feat = new StringBuilder(prefix + "2=" + leftOf1 + " " + two + " " + rightOf2);
        add(feat.toString(), fv);
        feat.append('*').append(attachDistance);
        add(feat.toString(), fv);

        feat = new StringBuilder(prefix + "3=" + leftOf1 + " " + one + " " + rightOf2);
        add(feat.toString(), fv);
        feat.append('*').append(attachDistance);
        add(feat.toString(), fv);

        feat = new StringBuilder(prefix + "4=" + one + " " + two + " " + rightOf2);
        add(feat.toString(), fv);
        feat.append('*').append(attachDistance);
        add(feat.toString(), fv);

        /////////////////////////////////////////////////////////////
        prefix = "A" + prefix;

        // feature posL posL+1 posR-1 posR
        add(prefix + "1=" + one + " " + rightOf1 + " " + leftOf2 + "*" + attachDistance, fv);

        feat = new StringBuilder(prefix + "1=" + one + " " + rightOf1 + " " + leftOf2);
        add(feat.toString(), fv);
        feat.append(' ').append(two);
        add(feat.toString(), fv);
        feat.append('*').append(attachDistance);
        add(feat.toString(), fv);

        feat = new StringBuilder(prefix + "2=" + one + " " + rightOf1 + " " + two);
        add(feat.toString(), fv);
        feat.append('*').append(attachDistance);
        add(feat.toString(), fv);

        feat = new StringBuilder(prefix + "3=" + one + " " + leftOf2 + " " + two);
        add(feat.toString(), fv);
        feat.append('*').append(attachDistance);
        add(feat.toString(), fv);

        feat = new StringBuilder(prefix + "4=" + rightOf1 + " " + leftOf2 + " " + two);
        add(feat.toString(), fv);
        feat.append('*').append(attachDistance);
        add(feat.toString(), fv);

        ///////////////////////////////////////////////////////////////
        prefix = "B" + prefix;

        //// feature posL-1 posL posR-1 posR
        feat = new StringBuilder(prefix + "1=" + leftOf1 + " " + one + " " + leftOf2 + " " + two);
        add(feat.toString(), fv);
        feat.append('*').append(attachDistance);
        add(feat.toString(), fv);

        //// feature posL posL+1 posR posR+1
        feat = new StringBuilder(prefix + "2=" + one + " " + rightOf1 + " " + two + " " + rightOf2);
        add(feat.toString(), fv);
        feat.append('*').append(attachDistance);
        add(feat.toString(), fv);
    }

    /**
     * Add features for two items, each with two observations, e.g. head,
     * head pos, child, and child pos.
     * <p/>
     * The use of StringBuilders is not yet as efficient as it could
     * be, but this is a start. (And it abstracts the logic so we can
     * add other features more easily based on other items and
     * observations.)
     */
    private final void addTwoObsFeatures(String prefix,
                                         String item1F1, String item1F2,
                                         String item2F1, String item2F2,
                                         String attachDistance, FeatureVector fv) {

        StringBuilder feat = new StringBuilder(prefix + "2FF1=" + item1F1);
        add(feat.toString(), fv);
        feat.append('*').append(attachDistance);
        add(feat.toString(), fv);

        feat = new StringBuilder(prefix + "2FF1=" + item1F1 + " " + item1F2);
        add(feat.toString(), fv);
        feat.append('*').append(attachDistance);
        add(feat.toString(), fv);

        feat = new StringBuilder(prefix + "2FF1=" + item1F1 + " " + item1F2 + " " + item2F2);
        add(feat.toString(), fv);
        feat.append('*').append(attachDistance);
        add(feat.toString(), fv);

        feat = new StringBuilder(prefix + "2FF1=" + item1F1 + " " + item1F2 + " " + item2F2 + " " + item2F1);
        add(feat.toString(), fv);
        feat.append('*').append(attachDistance);
        add(feat.toString(), fv);

        feat = new StringBuilder(prefix + "2FF2=" + item1F1 + " " + item2F1);
        add(feat.toString(), fv);
        feat.append('*').append(attachDistance);
        add(feat.toString(), fv);

        feat = new StringBuilder(prefix + "2FF3=" + item1F1 + " " + item2F2);
        add(feat.toString(), fv);
        feat.append('*').append(attachDistance);
        add(feat.toString(), fv);


        feat = new StringBuilder(prefix + "2FF4=" + item1F2 + " " + item2F1);
        add(feat.toString(), fv);
        feat.append('*').append(attachDistance);
        add(feat.toString(), fv);

        feat = new StringBuilder(prefix + "2FF4=" + item1F2 + " " + item2F1 + " " + item2F2);
        add(feat.toString(), fv);
        feat.append('*').append(attachDistance);
        add(feat.toString(), fv);

        feat = new StringBuilder(prefix + "2FF5=" + item1F2 + " " + item2F2);
        add(feat.toString(), fv);
        feat.append('*').append(attachDistance);
        add(feat.toString(), fv);

        feat = new StringBuilder(prefix + "2FF6=" + item2F1 + " " + item2F2);
        add(feat.toString(), fv);
        feat.append('*').append(attachDistance);
        add(feat.toString(), fv);

        feat = new StringBuilder(prefix + "2FF7=" + item1F2);
        add(feat.toString(), fv);
        feat.append('*').append(attachDistance);
        add(feat.toString(), fv);

        feat = new StringBuilder(prefix + "2FF8=" + item2F1);
        add(feat.toString(), fv);
        feat.append('*').append(attachDistance);
        add(feat.toString(), fv);

        feat = new StringBuilder(prefix + "2FF9=" + item2F2);
        add(feat.toString(), fv);
        feat.append('*').append(attachDistance);
        add(feat.toString(), fv);
    }

    private final void addLabeledFeatures(DependencyInstance instance, int word, String label, boolean leftToRight, boolean isChildFeatures, FeatureVector fv) {
        if (!labeled) return;

        String[] forms = instance.forms;
        String[] pos = instance.postags;

        String orientation = "";
        if (leftToRight)
            orientation = "RA";
        else
            orientation = "LA";

        orientation += "&" + isChildFeatures;
        String w = forms[word];
        String wP = pos[word];

        String wPm1 = word > 0 ? pos[word - 1] : "STR";
        String wPp1 = word < pos.length - 1 ? pos[word + 1] : "END";

        add("NTS1=" + label + "&" + orientation, fv);
        add("ANTS1=" + label, fv);
        for (int i = 0; i < 2; i++) {
            String suff = i < 1 ? "&" + orientation : "";
            suff = "&" + label + suff;
            add("NTH=" + w + " " + wP + suff, fv);
            add("NTI=" + wP + suff, fv);
            add("NTIA=" + wPm1 + " " + wP + suff, fv);
            add("NTIB=" + wP + " " + wPp1 + suff, fv);
            add("NTIC=" + wPm1 + " " + wP + " " + wPp1 + suff, fv);
            add("NTJ=" + w + suff, fv); //this
        }
    }



    private final void addSiblingFeatures(DependencyInstance instance,
                                          int ch1, int ch2,
                                          boolean isST,
                                          FeatureVector fv) {

        String[] forms = instance.forms;
        String[] pos = instance.postags;

        // ch1 is always the closes to par
        String dir = ch1 > ch2 ? "RA" : "LA";

        String ch1_pos = isST ? "STPOS" : pos[ch1];
        String ch2_pos = pos[ch2];
        String ch1_word = isST ? "STWRD" : forms[ch1];
        String ch2_word = forms[ch2];

        add("CH_PAIR="+ch1_pos+"_"+ch2_pos+"_"+dir,1.0,fv);
        add("CH_WPAIR="+ch1_word+"_"+ch2_word+"_"+dir,1.0,fv);
        add("CH_WPAIRA="+ch1_word+"_"+ch2_pos+"_"+dir,1.0,fv);
        add("CH_WPAIRB="+ch1_pos+"_"+ch2_word+"_"+dir,1.0,fv);
        add("ACH_PAIR="+ch1_pos+"_"+ch2_pos,1.0,fv);
        add("ACH_WPAIR="+ch1_word+"_"+ch2_word,1.0,fv);
        add("ACH_WPAIRA="+ch1_word+"_"+ch2_pos,1.0,fv);
        add("ACH_WPAIRB="+ch1_pos+"_"+ch2_word,1.0,fv);

        int dist = Math.max(ch1,ch2)-Math.min(ch1,ch2);
        String distBool = "0";
        if(dist > 1)
            distBool = "1";
        if(dist > 2)
            distBool = "2";
        if(dist > 3)
            distBool = "3";
        if(dist > 4)
            distBool = "4";
        if(dist > 5)
            distBool = "5";
        if(dist > 10)
            distBool = "10";
        add("SIB_PAIR_DIST="+distBool+"_"+dir,1.0,fv);
        add("ASIB_PAIR_DIST="+distBool,1.0,fv);
        add("CH_PAIR_DIST="+ch1_pos+"_"+ch2_pos+"_"+distBool+"_"+dir,1.0,fv);
        add("ACH_PAIR_DIST="+ch1_pos+"_"+ch2_pos+"_"+distBool,1.0,fv);
    }


    private final void addTripFeatures(DependencyInstance instance,
                                       int par,
                                       int ch1, int ch2,
                                       FeatureVector fv) {

        String[] pos = instance.postags;

        // ch1 is always the closest to par
        String dir = par > ch2 ? "RA" : "LA";

        String par_pos = pos[par];
        String ch1_pos = ch1 == par ? "STPOS" : pos[ch1];
        String ch2_pos = pos[ch2];

        String pTrip = par_pos+"_"+ch1_pos+"_"+ch2_pos;
        add("POS_TRIP="+pTrip+"_"+dir,1.0,fv);
        add("APOS_TRIP="+pTrip,1.0,fv);

    }

}