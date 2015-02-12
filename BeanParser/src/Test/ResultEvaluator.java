package Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by wangyizhong on 2014/12/4.
 */
public class ResultEvaluator{

    public ResultEvaluator() {
    }

    public static void main(String args[]) throws IOException {
        ResultEvaluator re = new ResultEvaluator();
        String test_file = "";
        String gold_file = "wsj_00-01.conll";
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
//0.8539988373058716 MST Original 2nd order features
//0.8487667137280956 with Beam
//0.8106469562328711 1st order
//0.8157129806494477 only beam 2nd order


//0.8488497633086953 in the bracket

//Accuracy: 0.8559089776596628  only the lefmost and rightmost children

//Accuracy: 0.8484345154056971 ptla ptra

//Accuracy: 0.8494311103728927 num > 0

//Accuracy: 0.8500955070176895 only two num > 0

//Accuracy: 0.8505938045012873 2nd nearest sibling

//Num Feats: 1125682

//Accuracy: 0.8568225230462586 three order sibling features

//Accuracy: 0.8583174154970518 all ghm features

//Accuracy: 0.8572377709492567 ghm features without cross

//Accuracy: 0.8601445062702433 ghm features only postags HMG,HG,MG

//Accuracy: 0.8618054978822357 ghm features only HMG postags

//Accuracy: 0.8524208952744788 without correct third order sibling

//Accuracy: 0.8571547213686571 without ghm