package IO;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public abstract class DependencyReader {
	protected BufferedReader inReader;
	protected boolean labeled=true;
	protected boolean ordered=false;
	/*
	 * CONLL format:(extend an order column)
	 * 0      1       2        3          4         5        6       7         8        9          10
	 * <ID>\t<FORM>\t<LEMMA>\t<CPOSTAG>\t<POSTAG>\t<FEATS>\t<HEAD>\t<DEPREL>\t<PHEAD>\t<PDEPREL>\t(<Order>)\n
	 */
	public boolean InitReader(String Readfile) throws IOException{
		this.inReader=new BufferedReader(new FileReader(Readfile));
		int inputColumnNum=this.CheckNumOfColumn(Readfile);
		if(inputColumnNum==11){
			this.labeled=true;
			this.ordered=true;
		}else if(inputColumnNum==10){
			this.labeled=true;
			this.ordered=false;
		}else if(inputColumnNum<5){
			throw new IOException("Input file is less than 5 columns !");
		}else{
			this.labeled=false;
			this.ordered=false;
		}
		return true;
	}
	private int CheckNumOfColumn(String file) throws IOException{
		//only check the number of columns in inputfile
		BufferedReader reader=new BufferedReader(new FileReader(file));
		String columns[]=reader.readLine().split("\t");
		return columns.length;
	}
	public static DependencyReader CreateReader(String format){
		if(format.equals("CONLL")){
			return new CONLLReader();
		}else{
			return null;
		}
	}
}
