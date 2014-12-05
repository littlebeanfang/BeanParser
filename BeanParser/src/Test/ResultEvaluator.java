package Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by wangyizhong on 2014/12/4.
 */
public class ResultEvaluator {
    public static void main(String[] args) throws IOException {

        BufferedReader fi_out = new BufferedReader(new FileReader(System.getenv("CODEDATA") + File.separator + "wsj_rand100_features_v2_10iter.txt"));
        BufferedReader fi_gold = new BufferedReader(new FileReader(System.getenv("CODEDATA") + File.separator + "wsj_rand100_processindex_new.txt"));
        int token_num = 0;
        int error_num = 0;

        while (true) {
            String aline_out = fi_out.readLine();
            String aline_gold = fi_gold.readLine();
            if (aline_gold == null) break;
            //System.out.println(aline_gold);
            if (aline_gold.equals("")) {
                //System.out.println("GOOD!");
                continue;
            } else {
                token_num++;
                //System.out.println(aline_gold+'\n'+aline_out+token_num);
                if (!aline_out.split("\t")[6].equals(aline_gold.split("\t")[6])) {
                    error_num++;
                }
            }
        }

        System.out.println(error_num + "\t" + token_num + "\t");
        fi_out.close();
        fi_gold.close();
    }
}
