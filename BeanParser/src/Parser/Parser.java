package Parser;

import DataStructure.*;
import IO.CONLLReader;
import IO.CONLLWriter;
import mstparser.Alphabet;

import java.io.*;

public class Parser {
    public ParserOptions options;
    private DependencyPipe pipe;
    private Decoder decoder;
    private Train trainer;
    private Parameters params;

    //private
    //constractor for decoder
    public Parser(DependencyPipe pipe, ParserOptions options) {
        this.pipe = pipe;
        this.options = options;
        // Set up arrays
        params = new Parameters(pipe.dataAlphabet.size());
        decoder = new Decoder(pipe, params);
    }

    //constructor for trainer
    public Parser(DependencyPipe pipe, ParserOptions options, Parameters params) {
        this.pipe = pipe;
        this.options = options;
        this.params = params;
    }

    public void loadModel(String file) throws Exception {
        ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(System.getenv("CODEDATA") + File.separator + file)));
        params.parameters = (double[]) in.readObject();
        pipe.dataAlphabet = (Alphabet) in.readObject();
        pipe.dataAlphabet.refine(params);
        pipe.typeAlphabet = (Alphabet) in.readObject();
        System.out.println("Model loaded!");
        in.close();
        pipe.closeAlphabets();
    }

    public void saveModel(String file) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(System.getenv("CODEDATA") + File.separator + file));
        out.writeObject(params.parameters);
        out.writeObject(pipe.dataAlphabet);
        out.writeObject(pipe.typeAlphabet);
        out.close();
    }

    public void Parse(String parsefile, String writefile) throws IOException {
        CONLLReader reader = new CONLLReader();
        //String filename="wsj_00_malt_processindex.txt";
        reader.startReading(System.getenv("CODEDATA") + File.separator + parsefile);
        File out = new File(System.getenv("CODEDATA") + File.separator + writefile);
        if (!out.exists()) {
            out.createNewFile();
        }
        CONLLWriter writer = new CONLLWriter(true);
        writer.startWriting(System.getenv("CODEDATA") + File.separator + writefile);

        DependencyInstance di;
        int instcount = 0;
        System.out.println("Process index:");
        long parsestart = System.currentTimeMillis();
        while ((di = reader.getNext()) != null) {
            ++instcount;
            if (instcount % 50 == 0) {
                System.out.print(instcount + "\t");
            }
            //if (instcount % 30 == 0) System.out.print('\n');
            //FeatureVector fv = new FeatureVector();//useless here, just align the param for DecodeInstance

            ParseAgenda pa = (ParseAgenda) decoder.DecodeInstance(di, di.orders)[0];

            writer.write(new DependencyInstance(RemoveRoot(di.forms), RemoveRoot(di.postags), RemoveRoot(di.deprels), RemoveRoot(di.heads)));
        }
        long parseend = System.currentTimeMillis();
        System.out.println("\n==============================================");
        System.out.println("Test File:" + options.testfile);
        System.out.println("Model Name:" + options.modelName);
        System.out.println("Sentence Number:" + instcount);
        System.out.println("Parse Time Total:" + (parseend - parsestart) / 1000.0);
        System.out.println("==============================================");
        writer.finishWriting();
    }

    public void Train() throws IOException, ClassNotFoundException {
        /**
         * Author: Bean
         * call Train class and organize the training process
         */
        this.trainer = new Train(options);
        trainer.callTrain();
    }

    private String[] RemoveRoot(String[] form) {
        String[] ret = new String[form.length - 1];
        System.arraycopy(form, 1, ret, 0, form.length - 1);
//        for (int i = 0; i < ret.length; i++) {
//            ret[i] = form[i + 1];
//        }
        return ret;
    }

    private int[] RemoveRoot(int[] form) {
        int[] ret = new int[form.length - 1];
        System.arraycopy(form, 1, ret, 0, form.length - 1);
//        for (int i = 0; i < ret.length; i++) {
//            ret[i] = form[i + 1];
//        }
        return ret;
    }
}
