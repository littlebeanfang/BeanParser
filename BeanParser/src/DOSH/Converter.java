package DOSH;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import DataStructure.DependencyInstance;
import IO.CONLLReader;
import IO.CONLLWriter;
import gnu.trove.TIntIntHashMap;

/**
 * This class is used to generate the action sequence: DO or SH(Do or Shift)
 * @author Wenjing
 *
 */
public class Converter {
	public void TranverseTIntIntHashMap(TIntIntHashMap map){
		int keys[]=map.keys();
		for(int i=0;i<keys.length;i++){
			System.out.println("key="+keys[i]+",value="+map.get(keys[i]));
		}
	}
	public TIntIntHashMap process(DependencyInstance di){
		TIntIntHashMap ret=new TIntIntHashMap();
		TIntIntHashMap refcount=new TIntIntHashMap();
		int length=di.length()-1;
		for(int i=1;i<=length;i++){
			int headi=di.heads[i];
//			System.out.println("headi="+headi+" ");
			if(refcount.contains(headi)){
				refcount.put(headi, refcount.get(headi)+1);
//				System.out.println("put head="+headi+" count="+(refcount.get(headi)));
			}else{
				refcount.put(headi, 1);
//				System.out.println("put head="+headi+" count=1");
			}
		}
//		System.out.println("Tranverse refcount:");
//		TranverseTIntIntHashMap(refcount);
		Stack<Integer> buf=new Stack<Integer>();
		
		int index=2;
		int ordercount=1;
		buf.add(0);
		buf.add(1);
		while(index<=length||buf.size()>1){
		
			
			int top=buf.peek();
//			System.out.println("top="+top);
			if(refcount.contains(top)){
				//Shift
				if(index>length){
					//end of sentence but cannot shift any more
					//Swap is needed, pop until one node can be done
					Stack<Integer> temp=new Stack<Integer>();
					int depthofswap=0;
					
					while(true){
						temp.push(buf.pop());
						depthofswap++;
						int swaptop=buf.peek();
						if(!refcount.contains(swaptop)){
							//DO and recover the buf stack
							ret.put(ordercount++, swaptop);
							System.out.println("DO+"+swaptop+" ");
							System.out.println("swapdepth:"+depthofswap);
							int headref=di.heads[swaptop];
							int headrefcount=refcount.get(headref);
							if(headrefcount==1){
								refcount.remove(headref);
							}else{
								refcount.put(headref, headrefcount-1);
							}
							buf.pop();
							while(!temp.empty()){
								buf.push(temp.pop());
							}
							break;
						}
					}
					
				}else{
					System.out.print("SH+"+index+" ");
					buf.push(index++);
				}
			}else{
				//Do
				System.out.print("DO+"+top+" ");
				ret.put(ordercount++, top);
				int headref=di.heads[top];
				int headrefcount=refcount.get(headref);
				if(headrefcount==1){
					refcount.remove(headref);
				}else{
					refcount.put(headref, headrefcount-1);
				}
				
				buf.pop();
				
			}
		}
		if(ret.size()!=length){
			System.out.println("Error: order_child size="+ret.size()+", sentence length:"+length);
		}
		System.out.println();
		return ret;
	}
	public void GenerateDOSHOrder(String conllfile, String writefile) throws IOException{
		CONLLReader reader=new CONLLReader();
		reader.ordered=false;
		reader.startReading(System.getenv("CODEDATA")+File.separator+conllfile);
		CONLLWriter writer=new CONLLWriter(true);
		writer.startWriting(System.getenv("CODEDATA")+File.separator+writefile);
		DependencyInstance di=reader.getNext();
		Converter test=new Converter();
		int sencount=1;
		while(di!=null){
			System.out.print("sent:"+sencount+"\t");
			TIntIntHashMap ret=test.process(di);
			writer.write(new DependencyInstance(RemoveRoot(di.forms), RemoveRoot(di.postags), RemoveRoot(di.deprels), RemoveRoot(di.heads)),ret);
			di=reader.getNext();
			sencount++;
		}
		writer.finishWriting();
	}
	private String[] RemoveRoot(String[] form) {
        String[] ret = new String[form.length - 1];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = form[i + 1];
        }
        return ret;
    }
	private int[] RemoveRoot(int[] form) {
        int[] ret = new int[form.length - 1];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = form[i + 1];
        }
        return ret;
    }
	public int CountReverse(TIntIntHashMap hm1, TIntIntHashMap hm2) {
		// reverse pair number as error function
		// get mapping array
//		System.out.println("hm1");
//		tranverseTIntIntMap(hm1);
//		System.out.println("hm2");
//		tranverseTIntIntMap(hm2);
		int array[] = new int[hm1.size()];
		TIntIntHashMap hm2_child_order = new TIntIntHashMap();
		for (int i = 1; i <= hm2.size(); i++) {
			hm2_child_order.put(hm2.get(i), i);
		}
		for (int i = 1; i <= hm2.size(); i++) {
			array[i - 1] = hm1.get(hm2_child_order.get(i));
		}
//		System.out.print("array:");
//		for(int i=0;i<hm1.size();i++){
//			System.out.print(array[i]+"\t");
//		}
//		System.out.println();
		// count reverse
		BinaryMergeSort sort = new BinaryMergeSort();
		sort.mergesort(array, 0, array.length - 1);
		return sort.nixuNum;
	}
	public int CountOrderSquareDist(TIntIntHashMap hm1, TIntIntHashMap hm2) {
		// reverse pair number as error function
		// get mapping array
//		System.out.println("hm1");
//		tranverseTIntIntMap(hm1);
//		System.out.println("hm2");
//		tranverseTIntIntMap(hm2);
		int count=0;
		TIntIntHashMap hm1_child_order = new TIntIntHashMap();
		for (int i = 1; i <= hm1.size(); i++) {
			hm1_child_order.put(hm1.get(i), i);
		}
		TIntIntHashMap hm2_child_order = new TIntIntHashMap();
		for (int i = 1; i <= hm2.size(); i++) {
			hm2_child_order.put(hm2.get(i), i);
		}
		for(int i=1;i<=hm1.size();i++){
			int dist=hm2_child_order.get(i)-hm1_child_order.get(i);
			count+=dist*dist;
		}
		return count;
	}
	
	public int ViolationCount(DependencyInstance di,TIntIntHashMap order_child) throws IOException{
		//check di.order[]
		
		int violationcount=0;
		
		TIntIntHashMap child_order=new TIntIntHashMap();
		for(int i=1;i<=order_child.size();i++){
			child_order.put(order_child.get(i), i);
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

	public void OrderCompare(String orderfile1, String orderfile2) throws IOException{
		System.out.println("Order File 1:"+orderfile1);
		System.out.println("Order File 2:"+orderfile2);
		System.out.println("=======================================");
		CONLLReader reader1=new CONLLReader();
		reader1.startReading(System.getenv("CODEDATA")+File.separator+orderfile1);
		CONLLReader reader2=new CONLLReader();
		reader2.startReading(System.getenv("CODEDATA")+File.separator+orderfile2);
		DependencyInstance di1=reader1.getNext();
		DependencyInstance di2=reader2.getNext();
		int reversecount=0;
		while(di1!=null){
			if(di2==null){
				System.out.println("Error: The sentence is not the same.");
			}
			reversecount+=CountReverse(di1.orders, di2.orders);
			di1=reader1.getNext();
			di2=reader2.getNext();
		}
		System.out.println("ReverseCount:"+reversecount);
		System.out.println("=======================================");
	}
	
}
