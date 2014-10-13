package DataStructure;

import gnu.trove.TIntIntHashMap;
import gnu.trove.TIntIntIterator;

/**
 * used to store partial parse, child-head key-value pairs
 * @author Wenjing
 *
 */
public class ParseAgenda extends TIntIntHashMap{
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
}
