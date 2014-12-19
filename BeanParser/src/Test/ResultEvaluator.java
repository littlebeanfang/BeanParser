package Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by wangyizhong on 2014/12/4.
 */
public class ResultEvaluator {

    public ResultEvaluator() {
    }

    public static void main(String args[]) throws IOException {
        ResultEvaluator re = new ResultEvaluator();
        String test_file = "wsj_rand500_features_original_parseout.txt";
        String gold_file = "wsj_2-21_rand500_forprocessindextest.txt";
        re.evaluate(test_file, gold_file);
    }

    public void evaluate(String test_file, String gold_file) throws IOException {
        BufferedReader fi_out = new BufferedReader(new FileReader(System.getenv("CODEDATA") + File.separator + test_file));
        BufferedReader fi_gold = new BufferedReader(new FileReader(System.getenv("CODEDATA") + File.separator + gold_file));
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

        System.out.println("Errors: " + error_num + "\t" + "Tokens: " + token_num + "\t");
        System.out.println("Accuracy: " + (1 - (double) error_num / token_num));
        fi_out.close();
        fi_gold.close();
    }
}
//0.8539988373058716
//0.8487667137280956