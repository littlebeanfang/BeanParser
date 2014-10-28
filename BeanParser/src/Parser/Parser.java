package Parser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import IO.CONLLWriter;
import mstparser.Alphabet;
import DataStructure.DependencyInstance;
import DataStructure.FeatureVector;
import DataStructure.Parameters;
import DataStructure.ParseAgenda;
import DataStructure.ParserOptions;
import IO.CONLLReader;

public class Parser {
	public ParserOptions options;
	private DependencyPipe pipe;
    private Decoder decoder;
    private Train trainer;
    private Parameters params;
    
	//private 
    //constractor for decoder
    public Parser(DependencyPipe pipe, ParserOptions options) {
    	this.pipe=pipe;
    	this.options = options;
    	// Set up arrays
    	params = new Parameters(pipe.dataAlphabet.size());
    	decoder = new Decoder(pipe, params);
    }
    //constructor for trainer
    public Parser(DependencyPipe pipe, ParserOptions options, Parameters params){
    	this.pipe=pipe;
    	this.options=options;
    	this.params=params;
    }
	public void loadModel(String file) throws Exception {
		ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(System.getenv("CODEDATA")+File.separator+file)));
		params.parameters = (double[])in.readObject();
		System.out.println("Parameters loaded!");
		pipe.dataAlphabet = (Alphabet)in.readObject();
		System.out.println("dataAlphabet loaded!");
		pipe.typeAlphabet = (Alphabet)in.readObject();
		System.out.println("typeAlphabet loaded!");
		in.close();
		pipe.closeAlphabets();
	}
	public void saveModel(String file) throws IOException {
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(System.getenv("CODEDATA")+File.separator+file));
		out.writeObject(params.parameters);
		out.writeObject(pipe.dataAlphabet);
		out.writeObject(pipe.typeAlphabet);
		out.close();
	    }
	public void Parse(String parsefile,String writefile) throws IOException{
		 CONLLReader reader=new CONLLReader();
		 //String filename="wsj_00_malt_processindex.txt";
		reader.startReading(System.getenv("CODEDATA")+File.separator+parsefile);
        File out=new File(writefile);
        if(!out.exists()){
            out.createNewFile();
        }
        CONLLWriter writer=new CONLLWriter(true);
        writer.startWriting(System.getenv("CODEDATA")+File.separator+writefile);

		 DependencyInstance di;
		 while((di=reader.getNext())!=null){
			 FeatureVector fv=new FeatureVector();//useless here, just align the param for DecodeInstance
			 ParseAgenda pa=decoder.DecodeInstance(di, di.orders,fv);
			 System.out.println(pa);
             writer.write(new DependencyInstance(RemoveRoot(di.forms),RemoveRoot(di.postags),RemoveRoot(di.deprels),RemoveRoot(di.heads)));
		 }
        writer.finishWriting();
	}
	public void Train() throws IOException{
		/**
		 * TODO: Bean
		 * call Train class and organize the training process
		 */
		this.trainer=new Train(options);
		trainer.callTrain();
	}
    private String[] RemoveRoot(String[] form){
        String[] ret=new String[form.length-1];
        for(int i=0;i<ret.length;i++){
            ret[i]=form[i+1];
        }
        return ret;
    }
    private int[] RemoveRoot(int[] form){
        int[] ret=new int[form.length-1];
        for(int i=0;i<ret.length;i++){
            ret[i]=form[i+1];
        }
        return ret;
    }
}
