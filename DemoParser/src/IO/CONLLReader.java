///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2007 University of Texas at Austin and (C) 2005
// University of Pennsylvania and Copyright (C) 2002, 2003 University
// of Massachusetts Amherst, Department of Computer Science.
//
// This software is licensed under the terms of the Common Public
// License, Version 1.0 or (at your option) any subsequent version.
// 
// The license is approved by the Open Source Initiative, and is
// available from their website at http://www.opensource.org.
///////////////////////////////////////////////////////////////////////////////

package IO;

import DataStructure.DependencyInstance;
import DataStructure.RelationalFeature;
import gnu.trove.TIntIntHashMap;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


public class CONLLReader extends DependencyReader {

    /*protected boolean discourseMode = false;

    public CONLLReader (boolean discourseMode) {
	this.discourseMode = discourseMode;
    }
*/
    public DependencyInstance SentenceToInstance(String ConllSentence)throws IOException{
        String[] lineList = ConllSentence.split("\n");

        int length = lineList.length;

        if (length == 0) {
            return null;
        }

        String[] forms = new String[length + 1];
        String[] lemmas = new String[length + 1];
        String[] cpos = new String[length + 1];
        String[] pos = new String[length + 1];
        String[][] feats = new String[length + 1][];
        String[] deprels = new String[length + 1];
        int[] heads = new int[length + 1];
        TIntIntHashMap ordermap = new TIntIntHashMap();

        forms[0] = "<root>";
        lemmas[0] = "<root-LEMMA>";
        cpos[0] = "<root-CPOS>";
        pos[0] = "<root-POS>";
        deprels[0] = "<no-type>";
        heads[0] = -1;

        for (int i = 0; i < length; i++) {
            String[] info = lineList[i].split("\t");
            forms[i + 1] = normalize(info[1]);
            lemmas[i + 1] = normalize(info[2]);
            cpos[i + 1] = info[3];
            pos[i + 1] = info[4];
            feats[i + 1] = info[5].split("\\|");
            deprels[i + 1] = labeled ? info[7] : "<no-type>";
            heads[i + 1] = -1;
            if (ordered) {
                ordermap.put(Integer.parseInt(info[10]), i + 1);
            }
        }

        feats[0] = new String[feats[1].length];
        for (int i = 0; i < feats[1].length; i++)
            feats[0][i] = "<root-feat>" + i;

        return new DependencyInstance(forms, lemmas, cpos, pos, feats, deprels, heads, ordermap);
    }

    public DependencyInstance getNext() throws IOException {

        ArrayList<String[]> lineList = new ArrayList<String[]>();

        String line = inputReader.readLine();
        while (line != null && !line.equals("") && !line.startsWith("*")) {
            lineList.add(line.split("\t"));
            line = inputReader.readLine();
            //System.out.println("## "+line);
        }

        int length = lineList.size();

        if (length == 0) {
            inputReader.close();
            return null;
        }

        String[] forms = new String[length + 1];
        String[] lemmas = new String[length + 1];
        String[] cpos = new String[length + 1];
        String[] pos = new String[length + 1];
        String[][] feats = new String[length + 1][];
        String[] deprels = new String[length + 1];
        int[] heads = new int[length + 1];
        double[] confscores = confScores ? new double[length + 1] : null;
        TIntIntHashMap ordermap = new TIntIntHashMap();

        forms[0] = "<root>";
        lemmas[0] = "<root-LEMMA>";
        cpos[0] = "<root-CPOS>";
        pos[0] = "<root-POS>";
        deprels[0] = "<no-type>";
        heads[0] = -1;
        if (confScores)
            confscores[0] = 1;

        for (int i = 0; i < length; i++) {
            String[] info = lineList.get(i);
            forms[i + 1] = normalize(info[1]);
            lemmas[i + 1] = normalize(info[2]);
            cpos[i + 1] = info[3];
            pos[i + 1] = info[4];
            feats[i + 1] = info[5].split("\\|");
            deprels[i + 1] = labeled ? info[7] : "<no-type>";
            heads[i + 1] = Integer.parseInt(info[6]);
            if (confScores)
                confscores[i + 1] = Double.parseDouble(info[10]);
            if (ordered) {
                ordermap.put(Integer.parseInt(info[10]), i + 1);
            }
        }

        feats[0] = new String[feats[1].length];
        for (int i = 0; i < feats[1].length; i++)
            feats[0][i] = "<root-feat>" + i;
    /*
    // The following stuff is for discourse and can be safely
	// ignored if you are doing sentential parsing. (In theory it
	// could be useful for sentential parsing.)
	if (discourseMode) {
	    String[][] extended_feats = new String[feats[0].length][length+1];
	    for (int i=0; i<extended_feats.length; i++) {
		for (int j=0; j<length+1; j++)
		    extended_feats[i][j] = feats[j][i];
	    }
	
	    feats = extended_feats;
	}
*/
        ArrayList<RelationalFeature> rfeats =
                new ArrayList<RelationalFeature>();

        while (line != null && !line.equals("")) {
            rfeats.add(new RelationalFeature(length, line, inputReader));
            line = inputReader.readLine();
        }

        RelationalFeature[] rfeatsList = new RelationalFeature[rfeats.size()];
        rfeats.toArray(rfeatsList);

        // End of discourse stuff.

        return new DependencyInstance(forms, lemmas, cpos, pos, feats, deprels, heads, ordermap);

    }


    protected boolean fileContainsLabels(String file) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(file));
        String line = in.readLine();
        in.close();

        if (line.trim().length() > 0)
            return true;
        else
            return false;
    }

}