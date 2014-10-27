package DataStructure;

import java.awt.BufferCapabilities;

import gnu.trove.TIntIntHashMap;
import gnu.trove.TIntIntIterator;

/**
 * used to store partial parse, child-head key-value pairs
 * @author Wenjing
 *
 */
public class ParseAgenda {
	public TIntIntHashMap tii;
	public ParseAgenda(){
		this.tii=new TIntIntHashMap();
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
}
