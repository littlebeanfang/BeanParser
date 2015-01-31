package Parser;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


import DataStructure.DependencyInstance;
import IO.CONLLReader;
import IO.WriteInstanceWithOrder;
import gnu.trove.TIntIntHashMap;
/**
 * failed to improve easy first.
 * But can reorder god and get close performance as malt
 * @author Bean
 *
 */

public class SplitAndReorder {
	
	public TIntIntHashMap Reorder(boolean[] issplit, TIntIntHashMap order_child ){
		int newordercount=1;
		Map<Integer, Integer> secondpass = new TreeMap<Integer, Integer>(
                new Comparator<Integer>() {
                    public int compare(Integer obj1, Integer obj2) {
                        return obj1-obj2;
                    }
         });
		Map<Integer, Integer> firstpass = new TreeMap<Integer, Integer>(
                new Comparator<Integer>() {
                    public int compare(Integer obj1, Integer obj2) {
                        return obj1-obj2;
                    }
         });//order-child, decent order
		TIntIntHashMap child_order=new TIntIntHashMap();
		TIntIntHashMap new_order_child=new TIntIntHashMap();
		for(int i=1;i<=order_child.size();i++){
			child_order.put(order_child.get(i), i);
		}
		for(int j=1;j<=child_order.size();j++){
			//tranverse the sentence spans
			firstpass.put(child_order.get(j), j);
			if(issplit[j]){
//				System.out.println(j+" is split, firstpass size "+firstpass.size());
				//span end
				Set<Integer> keySet = firstpass.keySet();
		        Iterator<Integer> iter = keySet.iterator();
		        for(int k=keySet.size();k>1;k--){
		        	int key = iter.next();
//		        	System.out.println(newordercount+":"+firstpass.get(key)+" oldorder:"+key);
		            new_order_child.put(newordercount++,firstpass.get(key) );
		            
		        }
		        //leave the last one of a span in second pass
		        int key = iter.next();
		        secondpass.put(key, firstpass.get(key));
//		        System.out.println("add to second:"+firstpass.get(key)+" oldorder:"+key);
		        firstpass.clear();
			}
		}
		//process second pass
		Set<Integer> keySet = secondpass.keySet();
        Iterator<Integer> iter = keySet.iterator();
        while(iter.hasNext()){
        	int key = iter.next();
//        	System.out.println(newordercount+":"+secondpass.get(key)+" oldorder:"+key);
            new_order_child.put(newordercount++,secondpass.get(key) );
        }
        return new_order_child;
	}
	
	public boolean[] Split(String POSs[]){
		boolean[] issplit=new boolean[POSs.length];
		for(int i=0;i<issplit.length;i++){
			issplit[i]=false;
		}
		for(int index=0;index<POSs.length;index++){
			if(this.CheckPos(POSs[index]).equals("NP")&&(index+1<POSs.length)&&!this.CheckPos(POSs[index+1]).equals("NP")){
				issplit[index]=true;
			}else if(this.CheckPos(POSs[index]).equals("VB")){
				issplit[index]=true;
			}else if(this.CheckPos(POSs[index]).equals("PP")&&index!=1&&(index-1>=0)&&issplit[index-1]!=true){
				issplit[index-1]=true;
			}else if(this.CheckPos(POSs[index]).equals("PUNC")&&(index-1>=0)){
				if(issplit[index-1]==true&&this.CheckPos(POSs[index-1]).equals("NP"))
					issplit[index-1]=false;
					issplit[index]=true;
			}
		}
		//fix bug, some fucking sentences have no punct as end !
		issplit[issplit.length-1]=true;
		return issplit;
	}
	public String CheckPos(String POS){
		String type="ELSE";
		if(POS.equals("NN")||POS.equals("NNS")||POS.equals("NNP")||POS.equals("NNPS")||POS.equals("PRP")||POS.equals("FW")||POS.equals("CD")){
			type="NP";
		}else if(POS.equals("IN")||POS.equals("TO")){
			type="PP";
		}else if(POS.equals("VB")||POS.equals("VBD")||POS.equals("VBG")||POS.equals("VBN")||POS.equals("VBP")||POS.equals("VBZ")){
			type="VB";
		}else if(POS.equals(",")||POS.equals(".")||POS.equals("''")){
			type="PUNC";
		}
		return type;
	}
	public void PrintSplit(boolean[] issplit, String forms[]){
		for(int i=0;i<forms.length;i++){
			System.out.print(forms[i]+i+"\t");
			if(issplit[i]){
				System.out.print("|");
			}
		}
		System.out.println();
	}
	public void TestSplitter(String filename) throws IOException{
		CONLLReader reader=new CONLLReader();
		reader.startReading(System.getenv("CODEDATA")+File.separator+filename);
		DependencyInstance di=reader.getNext();
		while(di!=null){
			boolean[] issplit=this.Split(di.postags);
			this.PrintSplit(issplit, di.forms);
			di=reader.getNext();
		}
	}
	public void TranverseIntIntHashMapByValue(TIntIntHashMap map){
		TIntIntHashMap child_order=new TIntIntHashMap();
		for(int i=1;i<=map.size();i++){
			child_order.put(map.get(i), i);
		}
		for(int i=1;i<=map.size();i++){
			System.out.print(child_order.get(i)+"\t");
		}
		System.out.println();
	}
	public void TestReorder(String filename) throws IOException{
		CONLLReader reader=new CONLLReader();
		reader.startReading(System.getenv("CODEDATA")+File.separator+filename);
		DependencyInstance di=reader.getNext();
		while(di!=null){
			boolean[] issplit=this.Split(di.postags);
			this.PrintSplit(issplit, di.forms);
			TIntIntHashMap neworder=this.Reorder(issplit, di.orders);
			this.TranverseIntIntHashMapByValue(neworder);
			di=reader.getNext();
		}
	}
	public void ReorderFile(String oldorderfile, String neworderfile) throws IOException{
		CONLLReader reader=new CONLLReader();
		reader.startReading(System.getenv("CODEDATA")+File.separator+oldorderfile);
		WriteInstanceWithOrder writer=new WriteInstanceWithOrder();
		writer.startWriting(System.getenv("CODEDATA")+File.separator+neworderfile);
		DependencyInstance di=reader.getNext();
		while(di!=null){
			boolean[] issplit=this.Split(di.postags);
//			this.PrintSplit(issplit, di.forms);
			TIntIntHashMap neworder=this.Reorder(issplit, di.orders);
			di.orders=neworder;
			//this.TranverseIntIntHashMapByValue(di.orders);
			writer.write(di);
			di=reader.getNext();
		}
		writer.finishWriting();
	}
	public int ReorderCheckForInstance(DependencyInstance di) throws IOException{
		//check di.order[]
		
		int violationcount=0;
		TIntIntHashMap child_order=new TIntIntHashMap();
		for(int i=1;i<=di.orders.size();i++){
			child_order.put(di.orders.get(i), i);
		}
		
		HashMap<String,String> parent_childs=new HashMap<String,String>();
		for(int i=1;i<di.length();i++){
			String head=di.heads[i]+"";
			if(parent_childs.containsKey(head)){
				parent_childs.put(head, parent_childs.get(head)+"\t"+i);
			}else{
				parent_childs.put(head, i+"");
			}
		}
		Iterator iterator=parent_childs.entrySet().iterator();
		while(iterator.hasNext()){
			Map.Entry<String, String> entry=(java.util.Map.Entry<String, String>) iterator.next();
			int head=Integer.parseInt(entry.getKey());
			String childs[]=entry.getValue().split("\t");
			for(int i=0;i<childs.length;i++){
				//check violation: di.order[head] < di.child[child]
				int child=Integer.parseInt(childs[i]);
				if(child_order.get(head)<child_order.get(child)&&head!=0){
					violationcount++;
//					System.out.println("head:"+head+",child:"+child);
				}
			}
		}
		return violationcount;
	}
	public void ReorderViolationCheck(String conllfile) throws IOException{
		CONLLReader reader=new CONLLReader();
		reader.startReading(conllfile);
		DependencyInstance di=reader.getNext();
		int count=0;
		int instancecount=0;
		while(di!=null){
			instancecount++;
//			System.out.println("sent:"+instancecount);
			count+=ReorderCheckForInstance(di);
			di=reader.getNext();
		}
		System.out.println("reorder check file:"+conllfile);
		System.out.println("Violation count:"+count);
	}
	public static void main(String args[]) throws IOException{
		SplitAndReorder test=new SplitAndReorder();
		//test.TestSplitter("1sen.txt");
//		test.TestReorder("1sen.txt");
//		test.ReorderFile("wsj_2-21_godorder_processindex.txt", "wsj_2-21_godorder_processindex_reorder.txt");
//		test.ReorderFile("wsj_00-01_godorder_processindex.txt", "wsj_00-01_godorder_processindex_reorder.txt");
//		test.ReorderFile("wsj_2-21_easyfirstorder_maltmodel.txt", "wsj_2-21_easyfirstorder_maltmodel_reorder.txt");
		test.ReorderFile("wsj_00-01_easyfirstorder_godreorder.txt", "wsj_00-01_easyfirstorderfromgodreorder_reorder.txt");
//		test.ReorderViolationCheck(System.getenv("CODEDATA")+File.separator+"wsj_2-21_malt_processindex.txt");
	}
}
