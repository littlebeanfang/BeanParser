package Parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import DataStructure.Alphabet;
import DataStructure.DependencyInstance;
import DataStructure.Parameters;
import DataStructure.ParseAgenda;
import DataStructure.ParserOptions;
import IO.CONLLReader;

public class Parser {
	public ParserOptions options;
	private DependencyPipe pipe;
    private Decoder decoder;
    private Parameters params;
    
	//private 
    public Parser(DependencyPipe pipe, ParserOptions options) {
    	this.pipe=pipe;
    	this.options = options;
    	// Set up arrays
    	params = new Parameters(pipe.dataAlphabet.size());
    	decoder = new Decoder(pipe, params);
        }
	public void loadModel(String file) throws Exception {
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
		params.parameters = (double[])in.readObject();
		pipe.dataAlphabet = (Alphabet)in.readObject();
		pipe.typeAlphabet = (Alphabet)in.readObject();
		in.close();
		pipe.closeAlphabets();
	}
	public void Parse(String parsefile) throws IOException{
		 CONLLReader reader=new CONLLReader();
		 String filename="wsj_00_malt_processindex.txt";
		 reader.startReading(System.getenv("CODEDATA")+File.separator+filename);
		 DependencyInstance di;
		 while((di=reader.getNext())!=null){
			 ParseAgenda pa=decoder.DecodeInstance(di, di.orders);
			 System.out.println(pa);
		 }
	}
}
