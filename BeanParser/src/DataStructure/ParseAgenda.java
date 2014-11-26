package DataStructure;

import java.awt.BufferCapabilities;

import mstparser.Alphabet;
import gnu.trove.TIntIntHashMap;
import gnu.trove.TIntIntIterator;

/**
 * used to store partial parse, child-head key-value pairs
 * @author Wenjing
 *
 */
public class ParseAgenda {
	
	public TIntIntHashMap tii;
	private Alphabet typealphabet;
	public ParseAgenda(){
		//for mypipe.extractFeatureVector
		this.tii=new TIntIntHashMap();
	}
	public ParseAgenda(Alphabet alphabet){
		this.tii=new TIntIntHashMap();
		typealphabet=alphabet;
	}
	public void AddArc(int child, int head){
		this.tii.put(child, head);
	}
	public String toString(){
		StringBuffer sb=new StringBuffer();
		sb.append("Agenda Size:"+this.tii.size()+"\n");
		for(int key:this.tii.keys()){
			//sb.append("Child:"+key+", Head:"+this.tii.get(key)+"\n");
			sb.append(this.tii.get(key)+"-->"+key+"\n");
		}
		return sb.toString();
	}
	public String toActParseTree(){
		StringBuffer sb=new StringBuffer();
		for(int i = 1; i < tii.size(); i++) {
			//.append(":").append(typeAlphabet.lookupIndex(labs[i]))
			sb.append(tii.get(i)).append("|").append(i).append(" ");
		}
		return sb.substring(0,sb.length()-1);
	}
	public String toActParseTree(DependencyInstance instance){
		StringBuffer sb=new StringBuffer();
		for(int i = 1; i < tii.size(); i++) {
			sb.append(tii.get(i)).append("|").append(i).append(":").append(typealphabet.lookupIndex(instance.deprels[i])).append(" ");
		}
		return sb.substring(0,sb.length()-1);
	}
}
