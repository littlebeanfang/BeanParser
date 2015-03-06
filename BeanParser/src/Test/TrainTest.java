package Test;

import DataStructure.ParserOptions;
import Parser.DependencyPipe;
import Parser.MyPipe;
import Parser.Parser;

public class TrainTest {
    public static void main(String args[]) throws Exception {

        ParserOptions options = new ParserOptions(args);
        DependencyPipe pipe = new MyPipe(options);
        Parser test = new Parser(pipe, options);
        if (options.train) {
            test.Train();
        }
        if (options.test) {
            test.loadModel(options.modelName);
            test.Parse(options.testfile, options.outfile);
            ResultEvaluator re = new ResultEvaluator();
            re.evaluate(options.outfile, options.testfile);
        }
    }
}
