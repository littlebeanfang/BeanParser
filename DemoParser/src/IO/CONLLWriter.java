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

import java.io.IOException;
import java.text.DecimalFormat;

public class CONLLWriter extends DependencyWriter {

    public CONLLWriter(boolean labeled) {
        this.labeled = labeled;
    }

    public String toSanfordFormat(DependencyInstance instance) throws IOException{
        StringBuffer sb = new StringBuffer();
        for(int i = 1;i < instance.length();i++){
            String label = instance.deprels[i];
            int head_index = instance.heads[i];
            String head_form = instance.forms[head_index];
            int child_index = i;
            String child_form = instance.forms[child_index];
            sb.append(label+"("+head_form+"-"+head_index+", "+child_form+"-"+child_index+")###");
        }
        return sb.toString();
    }

    public void write(DependencyInstance instance) throws IOException {
        DecimalFormat df = null;
        if (instance.confidenceScores != null) {
            df = new DecimalFormat();
            df.setMaximumFractionDigits(3);
        }
        for (int i = 0; i < instance.length(); i++) {
            writer.write(Integer.toString(i + 1));
            writer.write('\t');
            writer.write(instance.forms[i]);
            writer.write('\t');
            writer.write(instance.forms[i]);
            writer.write('\t');
            //writer.write(instance.cpostags[i]);                 writer.write('\t');
            writer.write(instance.postags[i]);
            writer.write('\t');
            writer.write(instance.postags[i]);
            writer.write('\t');
            writer.write("-");
            writer.write('\t');
            writer.write(Integer.toString(instance.heads[i]));
            writer.write('\t');
            writer.write(instance.deprels[i]);
            writer.write('\t');
            writer.write("-\t-");
            if (instance.confidenceScores != null) {
                writer.write('\t');
                writer.write(df.format(instance.confidenceScores[i]));
            }
            writer.newLine();
        }
        writer.newLine();

    }


}
