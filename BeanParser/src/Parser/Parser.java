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
	public DependencyPipe pipe;
    private Decoder decoder;
    private Train trainer;
    public Parameters params;
    
    boolean relativeaddress=true;
    
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
		//TODO: RELATIVE address
		ObjectInputStream in;
		if(relativeaddress){
			in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
		}else{
			in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(System.getenv("CODEDATA")+File.separator+file)));
		}
		
		params.parameters = (double[])in.readObject();
		//System.out.println("Parameters loaded!");
		pipe.dataAlphabet = (Alphabet)in.readObject();
		//System.out.println("dataAlphabet loaded!");
		pipe.typeAlphabet = (Alphabet)in.readObject();
		//System.out.println("typeAlphabet loaded!");
		System.out.println("Model loaded!");
		in.close();
		pipe.closeAlphabets();
	}
	public void saveModel(String file) throws IOException {
		//TODO: RELATIVE address
		ObjectOutputStream out ;
		if(relativeaddress){
			out = new ObjectOutputStream(new FileOutputStream(file));
		}else{
			out = new ObjectOutputStream(new FileOutputStream(System.getenv("CODEDATA")+File.separator+file));
		}
		out.writeObject(params.parameters);
		out.writeObject(pipe.dataAlphabet);
		out.writeObject(pipe.typeAlphabet);
		out.close();
	    }
	public void Parse(String parsefile,String writefile) throws IOException{
		 CONLLReader reader=new CONLLReader();
		 //String filename="wsj_00_malt_processindex.txt";
		//TODO: RELATIVE address
		 File out;
		 if(relativeaddress){
			 reader.startReading(parsefile);
			 out=new File(writefile);
		 }else{
			 reader.startReading(System.getenv("CODEDATA")+File.separator+parsefile);
			 out=new File(System.getenv("CODEDATA")+File.separator+writefile);
		 }
        if(!out.exists()){
            out.createNewFile();
        }
        CONLLWriter writer=new CONLLWriter(true);
        if(relativeaddress){
        	writer.startWriting(writefile);
        }else{
        	writer.startWriting(System.getenv("CODEDATA")+File.separator+writefile);
        }

		 DependencyInstance di;
		 int instcount=0;
		 System.out.println("Process index:");
		 long parsestart=System.currentTimeMillis();
		 while((di=reader.getNext())!=null){
			 System.out.print((++instcount)+"   ");
			 FeatureVector fv=new FeatureVector();//useless here, just align the param for DecodeInstance
			 ParseAgenda pa=(ParseAgenda) decoder.DecodeInstance(di, di.orders)[0];
			 //System.out.println(pa);
             writer.write(new DependencyInstance(RemoveRoot(di.forms),RemoveRoot(di.postags),RemoveRoot(di.deprels),RemoveRoot(di.heads)));
		 }
		 long parseend=System.currentTimeMillis();
		System.out.println("\n==============================================");
		System.out.println("Test File:"+options.testfile);
		System.out.println("Model Name:"+options.modelName);
		System.out.println("Sentence Number:"+instcount);
		System.out.println("Test Time Total:"+(parseend-parsestart)/1000.0);
		System.out.println("==============================================");
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
