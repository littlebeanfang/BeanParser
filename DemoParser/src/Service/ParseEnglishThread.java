package Service;

import DataStructure.ParserOptions;
import IO.ConllConverter;
import Parser.DependencyPipe;
import Parser.MyPipe;
import Parser.Parser;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import POSTaggerUtil.*;
import org.maltparser.MaltParserService;
import org.maltparser.core.exception.MaltChainedException;


/**
 * Created by wangyizhong on 2015/3/18.
 */
public class ParseEnglishThread extends ServiceThread {
    public static final String SERVICE_NAME = "EnglishParse";

    private static final int MAX_OUTPUT = 20;

    private static final String SPLLITER = "###";

    public ParseEnglishThread() {
        super();
    }

    public String toStanford(String ConllSentence){
        String[] lines = ConllSentence.split("\n");
        String[] forms = new String[lines.length+1];
        int[] heads = new int[lines.length+1];
        String[] labels = new String[lines.length+1];
        forms[0] = "<root>";
        for(String line:lines){
            String[] info = line.split("\t");
            int index = Integer.parseInt(info[0]);
            forms[index] = info[1];
            heads[index] = Integer.parseInt(info[6]);
            labels[index] = info[7];
            //1	This	_	DT	DT	_	5	nsubj	_	_
            //label + "(" + head_form + "-" + head_index + "," + child_form + "-" + (i + 1) + ")###"
        }
        StringBuffer sb = new StringBuffer();
        for(int i = 0;i < lines.length;i++){
            sb.append(labels[i+1] + "(" + forms[heads[i+1]] + "-" + heads[i+1] + ", " + forms[i+1] + "-" + (i + 1) + ")###");
        }
        return sb.toString();
    }

    public void run() {
        System.out.println("English Parsing thread is running....");
        try{
            //prepare Stanford Postagger
            CallEngStanfordPOSTagger postagger=new CallEngStanfordPOSTagger("models/wsj-0-18-bidirectional-distsim.tagger");

            //prepare malt parser
            MaltParserService malt_service =  new MaltParserService();

            // Inititalize the parser model 'model0' and sets the working directory to '.' and sets the logging file to 'parser.log'
            //System.out.println(System.getProperty("user.dir"));
            malt_service.initializeParserModel("-c engmalt.linear-1.7.mco -m parse -lfi parser.log -w ./models");
            MaltConllParse MaltParser = new MaltConllParse();
            System.out.println("Malt Model loaded!");
            //prepare bean parser
            String[] bean_option_string = "model-name:models/bean_english_maltpredictedorder_wsj_2-21_withlabel.model".split(" ");
            ParserOptions bean_options = new ParserOptions(bean_option_string);
            DependencyPipe bean_pipe = new MyPipe(bean_options);
            Parser BeanParser = new Parser(bean_pipe, bean_options);
            BeanParser.loadModel(bean_options.modelName);

            //prepare mst parser
            String[] mst_option_string = "order:2 model-name:models/MST_english_wsj_2-21.model".split(" ");
            mstparser.ParserOptions mst_options = new mstparser.ParserOptions(mst_option_string);
            mstparser.DependencyPipe mst_pipe = new mstparser.DependencyPipe2O(mst_options);
            mstparser.DependencyParser MSTParser = new mstparser.DependencyParser(mst_pipe,mst_options);
            MSTParser.loadModel(mst_options.modelName);
            //mst_pipe.closeAlphabets();
            System.out.println("MST model loaded!");



            while (true) {
                RequestWrapper request = buffer.pop();
                if (request == null) {
                    synchronized (this) {
                        wait();
                    }
                } else {
                    BufferedWriter bw = new BufferedWriter(
                            new OutputStreamWriter(
                                    request.client.getOutputStream()));

                    String sentence = request.body;
                    System.out.println("The request body is:");
                    System.out.println(sentence);

                    //stanford pos-tagger
                    String PairSentence = postagger.Call(sentence);

                    //return Tokens:
                    int tokens = PairSentence.split(" ").length;
                    bw.write(tokens+"\n");

                    POSTaggerUtil.ConllConverter conllConverter=new POSTaggerUtil.ConllConverter("_");
                    String ConllSentence = conllConverter.toConll(PairSentence);

                    //MALT parse, return conll format res
                    long maltstart = System.currentTimeMillis();
                    ConllSentence = MaltParser.Parse(ConllSentence,malt_service);
                    String malt_result = toStanford(ConllSentence);
                    System.out.println(malt_result);
                    long maltend = System.currentTimeMillis();
                    double malt_time = (maltend - maltstart) / 1000.0;
                    bw.write(malt_result+"\n");
                    bw.write(malt_time+"\n");

                    //Add order, return conll format sentence
                    AddOrder ranker = new AddOrder();
                    String ConllSentenceWithOrder = ranker.process(ConllSentence);

                    //bean parse
                    long beanstart = System.currentTimeMillis();
                    String bean_result = BeanParser.onlineParse(ConllSentenceWithOrder);
                    System.out.println(bean_result);
                    long beanend = System.currentTimeMillis();
                    double bean_time = (beanend - beanstart) / 1000.0;
                    bw.write(bean_result+"\n");
                    bw.write(bean_time+"\n");

                    //mst parse
                    long mststart = System.currentTimeMillis();
                    String mst_result = MSTParser.onlineParse(ConllSentence);
                    String[] mst_pairs = mst_result.split(",");
                    StringBuffer sb = new StringBuffer();
                    for(int i = 0;i < mst_pairs.length-1;i++){
                        sb.append(mst_pairs[i]+", ");
                    }
                    sb.append(mst_pairs[mst_pairs.length-1]);
                    mst_result = sb.toString();
                    System.out.println(mst_result);
                    long mstend = System.currentTimeMillis();
                    double mst_time = (mstend - mststart) / 1000.0;
                    bw.write(mst_result+"\n");
                    bw.write(mst_time+"\n");

                    //bw.write("Hello world");
                    bw.flush();
                    bw.close();
                    GgrLogger.log("One request finished..");
                    request.client.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (MaltChainedException e){
            e.printStackTrace();
        }
    }
}
