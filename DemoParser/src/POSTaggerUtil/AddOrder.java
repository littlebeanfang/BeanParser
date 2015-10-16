package POSTaggerUtil;

import gnu.trove.TIntIntHashMap;

import java.util.Stack;

public class AddOrder {
	public String process(String conllsen){
		String conllsenwithorder="";
		TIntIntHashMap ret=new TIntIntHashMap();
		TIntIntHashMap refcount=new TIntIntHashMap();
		String token[]=conllsen.split("\n");
		int length=token.length;
		int[] heads=new int[length+1];
		heads[0]=-1;//root
		//refcount.put(0, -1);
		for(int i=0;i<length;i++){
			//System.out.println(token[i]);
			heads[i+1]=Integer.parseInt(token[i].split("\t")[6]);
			int headi=heads[i+1];
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
							token[swaptop-1]+="\t"+ordercount+"\n";
							ret.put(ordercount++, swaptop);
							//System.out.println("DO+"+swaptop+" ");
							//System.out.println("swapdepth:"+depthofswap);
							int headref=heads[swaptop];
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
					//System.out.print("SH+"+index+" ");
					buf.push(index++);
				}
			}else{
				//Do
				//System.out.print("DO+"+top+" ");
				token[top-1]+="\t"+ordercount+"\n";
				ret.put(ordercount++, top);
				int headref=heads[top];
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
			//System.out.println("Error: order_child size="+ret.size()+", sentence length:"+length);
		}
		//System.out.println();
		for(String ele:token){
			conllsenwithorder+=ele;
		}
		//System.out.print(conllsenwithorder);
		return conllsenwithorder;
	}
	public static void main(String args[]){
		String conllsen="1\tThis\t_\tDT\tDT\t_\t5\tnsubj\t_\t_\n"
				+ "2\tis\t_\tVBZ\tVBZ\t_\t5\tcop\t_\t_\n"
				+ "3\ta\t_\tDT\tDT\t_\t5\tdet\t_\t_\n"
				+ "4\tsample\t_\tNN\tNN\t_\t5\tnn\t_\t_\n"
				+ "5\ttext\t_\tNN\tNN\t_\t0\troot\t_\t_\n";
		//System.out.println(conllsen);
		AddOrder test=new AddOrder();
		String conllsenwithorder=test.process(conllsen);
	}
}
