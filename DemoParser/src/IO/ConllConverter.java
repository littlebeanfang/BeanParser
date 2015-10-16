package IO;

/**
 * Created by wangyizhong on 2015/3/22.
 */
public class ConllConverter {

    public String toConll(String PairSentence){
        StringBuffer sb = new StringBuffer();
        String[] pairs = PairSentence.trim().split(" ");
        for(int i = 0;i < pairs.length;i++){
            String[] apair = pairs[i].split("/");
            String form = apair[0];
            String pos = apair[1];
            String aline = (i+1)+"\t"+form+"\t"+"_"+"\t"+pos+"\t"+pos+"\t_\t_\t_\t_\t_\n";
            sb.append(aline);
        }
        //1	The	_	DT	DT	_	2	det	_	_	1
        return sb.toString();
    }

    public static void main(String arg[]){
        String pairSentence = "      word1/pos1 word2/pos2 word3/pos3\n\n";
        ConllConverter cc = new ConllConverter();
        String sb = cc.toConll(pairSentence);
        System.out.print(sb);
    }

}
