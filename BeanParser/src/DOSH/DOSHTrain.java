package DOSH;

import gnu.trove.TIntIntHashMap;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Stack;

import mstparser.Alphabet;
import DataStructure.DependencyInstance;
import DataStructure.FeatureVector;
import DataStructure.ParseAgenda;
import IO.CONLLReader;
import IO.CONLLWriter;

/**
 * this class is used to train a classifier of DO OR SHIFT,
 * DO: find head for one word
 * SH: look for next word
 * @author Wenjing
 *
 */
public class DOSHTrain {
	//TODO don't forget to stopgrowth of alphabet!
	public Alphabet doshAlphabet;
	public DOSHTrain(){
		doshAlphabet=new Alphabet();
		doshAlphabet.lookupIndex("beanbeanjiangO(กษ_กษ)O");
	}
	/**
	 * take dependency instance as input, according to the parse tree to generate:
	 * 1. the instances for liblinear: DO 1, SH 2
	 * 2. the order_child map for beanparser to train model
	 * @param golddi
	 * @return
	 */
	public Object[] GenerateLiblinearInstance(DependencyInstance di){
		HashSet<LiblinearInstance> libinsts=new HashSet<LiblinearInstance>();
		//store the nodes that haven't been processed
		//not -1 indexs:1-(length-order_child.size)
		ParseAgenda pa=new ParseAgenda(di.length());
		int node[]=new int[di.length()+1];
		for(int i=1;i<di.length();i++){
			node[i]=i;
		}
		node[0]=0;
		node[di.length()]=-1;
		
		Object[] ret=new Object[2];
		TIntIntHashMap order_child=new TIntIntHashMap();
		HashSet donechild=new HashSet();
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
		
		int index=1;//shift index
		int ordercount=1;
		buf.add(0);
		while(index<=length||buf.size()>1){
			int top=buf.peek();
//			System.out.println("top="+top);
			if(refcount.contains(top)){
				//cannot do
				if(index>length){
					//end of sentence but cannot shift any more
					//Swap is needed, pop until one node can be done
					Stack<Integer> temp=new Stack<Integer>();
					int depthofswap=0;
					//additional SH+end
					int firstswap=buf.peek();
					int indexinnode1=LookupElemInNode(node, firstswap, length-order_child.size());
//					System.out.println("swaptop2:"+swaptop);
					FeatureVector fv5=new FeatureVector();
					ExtractDoshFeature(di,donechild, pa, node, indexinnode1, fv5);
					libinsts.add(new LiblinearInstance(fv5, 2));
					System.out.println("SH+end");
					while(true){
						temp.push(buf.pop());
						
						depthofswap++;
						int swaptop=buf.peek();
						if(!refcount.contains(swaptop)){
							//DO and recover the buf stack
							
//							System.out.println("swaptop1:"+swaptop);
							int indexinnode=LookupElemInNode(node, swaptop, length-order_child.size());
							//TODO extract feature, label DO
							FeatureVector fv1=new FeatureVector();
							ExtractDoshFeature(di,donechild, pa, node, indexinnode, fv1);
							libinsts.add(new LiblinearInstance(fv1, 1));
							pa.AddArc(swaptop, di.heads[swaptop]);
							pa.ChildProcess(swaptop,  di.heads[swaptop]);
							System.out.println("node:");
//							for(int i=0;i<node.length;i++){
//								System.out.print(node[i]+"\t");
//							}
							for(int i=indexinnode;i<=length-order_child.size();i++){
								node[i]=node[i+1];
							}
//							System.out.println();
//							System.out.println("node:");
//							for(int i=0;i<node.length;i++){
//								System.out.print(node[i]+"\t");
//							}
							System.out.println("DO+"+swaptop+" ");
							order_child.put(ordercount++, swaptop);
							donechild.add(swaptop);
							//System.out.println("swapdepth:"+depthofswap);
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
						}else{
							//TODO end shift, label SH
							int indexinnode=LookupElemInNode(node, swaptop, length-order_child.size());
//							System.out.println("swaptop2:"+swaptop);
							FeatureVector fv2=new FeatureVector();
							ExtractDoshFeature(di,donechild, pa, node, indexinnode, fv2);
							libinsts.add(new LiblinearInstance(fv2, 2));
							System.out.println("SH+end");
						}
					}
					
				}else{
					
					int indexinnode=LookupElemInNode(node, top, length-order_child.size());
					//System.out.println("top2:"+top);
					//TODO extract feature, label SH
					FeatureVector fv3=new FeatureVector();
					ExtractDoshFeature(di,donechild, pa, node, indexinnode, fv3);
					libinsts.add(new LiblinearInstance(fv3, 2));
					
					System.out.println("SH+"+index+" ");
					
					buf.push(index++);
				}
			}else{
				//Do
				
				int indexinnode=LookupElemInNode(node, top, length-order_child.size());
				//System.out.println("top2:"+top);
				//TODO extract feature, label DO
				FeatureVector fv4=new FeatureVector();
				ExtractDoshFeature(di,donechild, pa, node, indexinnode, fv4);
				libinsts.add(new LiblinearInstance(fv4, 1));

				order_child.put(ordercount++, top);
				donechild.add(top);
				pa.AddArc(top, di.heads[top]);
				pa.ChildProcess(top,  di.heads[top]);
				for(int i=indexinnode;i<=length-order_child.size()+1;i++){
					node[i]=node[i+1];
				}
				int headref=di.heads[top];
				int headrefcount=refcount.get(headref);
				if(headrefcount==1){
					refcount.remove(headref);
				}else{
					refcount.put(headref, headrefcount-1);
				}
				System.out.println("DO+"+top+" ");
				buf.pop();
				
			}
		}
		if(order_child.size()!=length){
			System.out.println("Error: order_child size="+order_child.size()+", sentence length:"+length);
		}
		System.out.println();
		ret[0]=libinsts;
		ret[1]=order_child;
		return ret;
	}
	/*
	private String GetPOSOrForm(boolean ispos,int index, DependencyInstance di){
		if(index==0||index==-1){
			
		}
		if(ispos){
			
		}else{
			//form
		}
	}
	*/
	public void ExtractDoshFeature(DependencyInstance di, HashSet donechild,ParseAgenda pa, int[] node, int indexinnode, FeatureVector fv){
		boolean print=true;
		if(print){
			System.out.println("feature index"+node[indexinnode]);
		}
		//index:stack0-2, input0-2
		int stack0=node[indexinnode];
		int stack1=indexinnode-1>=0?node[indexinnode-1]:-1;
		int stack2=indexinnode-2>=0?node[indexinnode-2]:-1;
		int input0=node[indexinnode+1];
		int input1=indexinnode+2<node.length-1?node[indexinnode+2]:-1;
		int input2=indexinnode+3<node.length-1?node[indexinnode+3]:-1;
		int stack0ldep=leftmostdep(pa, stack0);
		int stack0rdep=rightmostdep(pa, stack0);
		int stack1ldep=stack1==-1?-1:leftmostdep(pa, stack1);
		int stack1rdep=stack1==-1?-1:rightmostdep(pa, stack1);
		//Form:
		String input0formString=input0==-1?"NULL":di.forms[input0];
		String stack0formString=stack0==-1?"NULL":di.forms[stack0];
		String stack1formString=stack1==-1?"NULL":di.forms[stack1];
		//POS:
		String stack0posString=stack0==-1?"NULL":di.postags[stack0];
		String stack1posString=stack1==-1?"NULL":di.postags[stack1];
		String stack2posString=stack2==-1?"NULL":di.postags[stack2];
		String input0posString=input0==-1?"NULL":di.postags[input0];
		String input1posString=input1==-1?"NULL":di.postags[input1];
		String input2posString=input2==-1?"NULL":di.postags[input2];
		
		String stack0ldep_rel=donechild.contains(stack0ldep)?di.deprels[stack0ldep]:"NULL";
		String stack0rdep_rel=donechild.contains(stack0rdep)?di.deprels[stack0rdep]:"NULL";
		String stack1ldep_rel=donechild.contains(stack1ldep)?di.deprels[stack1ldep]:"NULL";
		String stack1rdep_rel=donechild.contains(stack1rdep)?di.deprels[stack1rdep]:"NULL";
		
//		DOSHFEAT1:InputColumn(FORM,Lookahead[0])
		String feature1="DOSHFEAT1input0formString:"+input0formString;
		add(feature1, fv);
//		DOSHFEAT2:InputColumn(FORM,Stack[0])
		String feature2="DOSHFEAT2stack0formString:"+stack0formString;
		add(feature2, fv);
//		DOSHFEAT3:InputColumn(FORM,Stack[1])
		String feature3="DOSHFEAT3stack1formString:"+stack1formString;
		add(feature3, fv);
//		DOSHFEAT4:InputColumn(POSTAG,Lookahead[0])
		String feature4="DOSHFEAT4input0posString:"+input0posString;
		add(feature4, fv);
//		DOSHFEAT5:InputColumn(POSTAG,Lookahead[1])
		String feature5="DOSHFEAT5input1posString:"+input1posString;
		add(feature5, fv);
//		DOSHFEAT6:InputColumn(POSTAG,Lookahead[2])
		String feature6="DOSHFEAT6input2posString:"+input2posString;
		add(feature6, fv);
//		DOSHFEAT7:InputColumn(POSTAG,Stack[0])
		String feature7="DOSHFEAT7stack0posString:"+stack0posString;
		add(feature7, fv);
//		DOSHFEAT8:InputColumn(POSTAG,Stack[1])
		String feature8="DOSHFEAT8stack1posString:"+stack1posString;
		add(feature8, fv);
//		DOSHFEAT9:InputColumn(POSTAG,Stack[2])
		String feature9="DOSHFEAT9stack2posString:"+stack2posString;
		add(feature9, fv);
		
//		DOSHFEAT10:Merge(InputColumn(POSTAG,Stack[1]),InputColumn(POSTAG,Stack[0]))
		String feature10=(stack0==-1&&stack1==-1)?"DOSHFEAT10:NULL":"DOSHFEAT10:"+stack1posString+"-"+stack0posString;
		add(feature10, fv);
//		DOSHFEAT11:Merge3(InputColumn(POSTAG,Lookahead[0]),InputColumn(POSTAG,Lookahead[1]),InputColumn(POSTAG,Lookahead[2]))
		String feature11=(input0==-1&&input1==-1&&input2==-1)?"DOSHFEAT11:NULL":"DOSHFEAT11:"+input0posString+"-"+input1posString+"-"+input2posString;
		add(feature11,fv);
//		DOSHFEAT12:Merge3(InputColumn(POSTAG,Stack[0]),InputColumn(POSTAG,Lookahead[0]),InputColumn(POSTAG,Lookahead[1]))
		String feature12=(stack0==-1&&input0==-1&&input1==-1)?"DOSHFEAT12:NULL":"DOSHFEAT12:"+stack0posString+"-"+input0posString+"-"+input1posString;
		add(feature12, fv);
//		DOSHFEAT13:Merge3(InputColumn(POSTAG,Stack[0]),OutputColumn(DEPREL,ldep(Stack[0])),OutputColumn(DEPREL,rdep(Stack[0])))
		String feature13=(stack0==-1&&stack0ldep==-1&&stack0rdep==-1)?"DOSHFEAT13:NULL":"DOSHFEAT13:"+stack0posString+"-"+stack0ldep_rel+"-"+stack0rdep_rel;
		add(feature13, fv);
//		DOSHFEAT14:Merge3(InputColumn(POSTAG,Stack[1]),InputColumn(POSTAG,Stack[0]),InputColumn(POSTAG,Lookahead[0]))
		String feature14=(stack1==-1&&stack0==-1&&input0==-1)?"DOSHFEAT14:NULL":"DOSHFEAT14:"+stack1posString+"-"+stack0posString+"-"+input0posString;
		add(feature14, fv);
//		DOSHFEAT15:Merge3(InputColumn(POSTAG,Stack[1]),OutputColumn(DEPREL,ldep(Stack[1])),OutputColumn(DEPREL,rdep(Stack[1])))
		String feature15=(stack1==-1&&stack1ldep==-1&&stack1rdep==-1)?"DOSHFEAT15:NULL":"DOSHFEAT15:"+stack1posString+"-"+stack1ldep_rel+"-"+stack1rdep_rel;
		add(feature15, fv);
//		DOSHFEAT16:Merge3(InputColumn(POSTAG,Stack[2]),InputColumn(POSTAG,Stack[1]),InputColumn(POSTAG,Stack[0]))
		String feature16=(stack2==-1&&stack1==-1&&stack0==-1)?"DOSHFEAT16:NULL":"DOSHFEAT16:"+stack2posString+"-"+stack1posString+"-"+stack0posString;
		add(feature16, fv);
//		DOSHFEAT17:OutputColumn(DEPREL,ldep(Stack[0]))
		String feature17=stack0ldep==-1?"DOSHFEAT17:NULL":"DOSHFEAT17:"+stack0ldep_rel;
		add(feature17, fv);
//		DOSHFEAT18:OutputColumn(DEPREL,ldep(Stack[1]))
		String feature18=stack1ldep==-1?"DOSHFEAT18:NULL":"DOSHFEAT18:"+stack1ldep_rel;
		add(feature18, fv);
//		DOSHFEAT19:OutputColumn(DEPREL,rdep(Stack[0]))
		String feature19=stack0rdep==-1?"DOSHFEAT19:NULL":"DOSHFEAT19:"+stack0rdep_rel;
		add(feature19, fv);
//		DOSHFEAT20:OutputColumn(DEPREL,rdep(Stack[1]))
		String feature20=stack1rdep==-1?"DOSHFEAT20:NULL":"DOSHFEAT20:"+stack1rdep_rel;
		add(feature20, fv);
		if(print){
			System.out.println("\t*"+stack0+"\t"+stack0ldep+"\t"+stack0rdep+"\t*"+stack1+"\t"+stack1ldep+"\t"+stack1rdep+"\t*"+stack2+"\t*"+input0+"\t*"+input1+"\t*"+input2);
			System.out.println("\t"+feature1+"\t"+feature2+"\t"+feature3+"\t"+feature4+"\t"+feature5+"\t"+feature6+"\t"+feature7+"\t"+feature8+"\t"+feature9+"\t"+feature10+
					"\t"+feature11+"\t"+feature12+"\t"+feature13+"\t"+feature14+"\t"+feature15+"\t"+feature16+"\t"+feature17+"\t"+feature18+"\t"+feature19+"\t"+feature20);
		}
		
	}
	/**
	 * operate on node, and return the deleted element index
	 * @param node
	 * @param elem
	 * @param end
	 * @return
	 */
	public int LookupElemInNode(int[] node, int elem, int end){
		if(node.length==2){
			System.out.println("node only 2 elem, should not delete.");
		}else{
//			System.out.println("elem:"+elem);
//			for(int i=0;i<node.length;i++){
//				System.out.print(node[i]+"\t");
//			}
//			
			int left=0,right=end;
			int mid;
			//mid cannot reach end
			if(elem==node[end]){
				return end;
			}
			while(left!=right){
				mid=(left+right)/2;
//				System.out.println("left:"+left+",right:"+right+",mid:"+mid);
				if(elem>node[mid]){
					left=mid;
				}else if(elem<node[mid]){
					right=mid;
				}else{
					/*
					//delete elem in index mid
					for(int i=mid;i<=end;i++){
						node[i]=node[i+1];
					}
					*/
					return mid;
				}
			}
			System.out.println("Error: no elem "+elem+" in node.");
		}
		return 0;
	}
	// add with default 1.0
    public final void add(String feat, FeatureVector fv) {
        int num = doshAlphabet.lookupIndex(feat);
        if (num >= 0)
            fv.add(num, 1.0);
    }
    public int leftmostdep(ParseAgenda pa, int index){
    	//boolean print=true;
    	if(index==0||index==-1){
    		return -1;
    	}
    	return pa.leftmostchilds[index];
    	/*
//    	System.out.println("index:"+index);
    	String leftchilds[]=pa.getLeftChilds(index).split(" ");
    	if(leftchilds.length==1){
    		//empty: length==1
    		return -1;//not exsist
    	}
//    	System.out.println("=="+pa.getLeftChilds(index)+"=="+leftchilds.length);
    	int leftmostchild=Integer.parseInt(leftchilds[0]);
    	for(int i=1;i<leftchilds.length;i++){
    		int lchild=Integer.parseInt(leftchilds[i]);
    		if(lchild<leftmostchild){
    			leftmostchild=lchild;
    		}
    	}
    	if(print){
    		System.out.println("index:"+index+", leftmost:"+leftmostchild);
    	}
    	return leftmostchild;
    	*/
    }
    public int rightmostdep(ParseAgenda pa, int index){
    	boolean print=true;
    	if(index==0||index==-1){
    		return -1;
    	}
    	return pa.rightmostchilds[index];
    	/*
    	String rightchilds[]=pa.getRightChilds(index).split(" ");
    	if(rightchilds.length==1){
    		return -1;//not exsist
    	}
    	int rightmostchild=Integer.parseInt(rightchilds[0]);
    	for(int i=1;i<rightchilds.length;i++){
    		int rchild=Integer.parseInt(rightchilds[i]);
    		if(rchild<rightmostchild){
    			rightmostchild=rchild;
    		}
    	}
    	if(print){
    		System.out.println("index:"+index+", rightmost:"+rightmostchild);
    	}
    	return rightmostchild;
    	*/
    }
    public void GenerateLiblinearAndOrderFile(String conllfile,String liblinearfile, String orderfile) throws IOException{
    	CONLLReader reader=new CONLLReader();
    	reader.ordered=false;
    	System.out.println(conllfile);
    	reader.startReading(conllfile);
    	CONLLWriter writer=new CONLLWriter(true);
    	writer.startWriting(orderfile);
    	File lib=new File(liblinearfile);
    	if(!lib.exists()){
    		lib.createNewFile();
    	}
    	FileWriter libwriter=new FileWriter(lib);
    	DependencyInstance di=reader.getNext();
    	int sentcount=1;
    	while(di!=null){
    		System.out.println("sent====="+sentcount);
    		Object[] ret=GenerateLiblinearInstance(di);
    		HashSet<LiblinearInstance> libsfordi=(HashSet<LiblinearInstance>) ret[0];
    		TIntIntHashMap order_child=(TIntIntHashMap) ret[1];
    		Iterator iterator=libsfordi.iterator();
    		while(iterator.hasNext()){
    			LiblinearInstance libinst=(LiblinearInstance) iterator.next();
    			libwriter.write(libinst+"\n");
    		}
    		writer.write(new DependencyInstance(RemoveRoot(di.forms), RemoveRoot(di.postags), RemoveRoot(di.deprels), RemoveRoot(di.heads)), order_child);
    		di=reader.getNext();
    		sentcount++;
    	}
    	doshAlphabet.stopGrowth();
    	System.out.println("Number of Doshfeature:"+(doshAlphabet.size()-1));
    	writer.finishWriting();
    	libwriter.close();
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
}
