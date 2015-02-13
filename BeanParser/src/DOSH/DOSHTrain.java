package DOSH;

import gnu.trove.TIntIntHashMap;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Stack;

import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.InvalidInputDataException;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.Parameter;
import de.bwaldvogel.liblinear.Problem;
import de.bwaldvogel.liblinear.Train;
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
	public DOSHTrain(String alphabetfile) throws  IOException, ClassNotFoundException{
		//for extract feature
		ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(alphabetfile)));
        doshAlphabet=(Alphabet) in.readObject();
        in.close();
        doshAlphabet.stopGrowth();
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
	public Object[] GenerateMaltLiblinearInstance(DependencyInstance di){
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
			int topL=buf.size()-2>=0?buf.get(buf.size()-2):-1;
			int topR=index>length?-1:index;
//			System.out.println("top="+top);
			if(!refcount.contains(top)&&((topL>=0&&di.heads[top]==topL)||(topR>0&&di.heads[top]==topR))){
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
			}else{
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
						int swaptopL=buf.size()-2>=0?buf.get(buf.size()-2):-1;
						int swaptopR=temp.peek();
						if(!refcount.contains(swaptop)&&(swaptopL>=0&&di.heads[swaptopL]==swaptop)||(swaptopR<length&&di.heads[swaptopR]==swaptop)){
						
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
		System.out.println("Extract:"+node[indexinnode]);
		boolean print=false;
		if(print){
			System.out.println("feature index"+node[indexinnode]);
		}
		//index:stack0-2, input0-2
		int stack0=node[indexinnode];
		int stack1=indexinnode-1>=0?node[indexinnode-1]:-1;
		int stack2=indexinnode-2>=0?node[indexinnode-2]:-1;
		int stack3=indexinnode-3>=0?node[indexinnode-3]:-1;
		int input0=node[indexinnode+1];
		int input1=indexinnode+2<node.length-1?node[indexinnode+2]:-1;
		int input2=indexinnode+3<node.length-1?node[indexinnode+3]:-1;
		int input3=indexinnode+4<node.length-1?node[indexinnode+4]:-1;
		int stack0ldep=leftmostdep(pa, stack0);
		int stack0rdep=rightmostdep(pa, stack0);
		int stack1ldep=stack1==-1?-1:leftmostdep(pa, stack1);
		int stack1rdep=stack1==-1?-1:rightmostdep(pa, stack1);
		//Form: 7
		String stack0formString=stack0==-1?"NULL":di.forms[stack0];
		String stack1formString=stack1==-1?"NULL":di.forms[stack1];
		String stack2formString=stack2==-1?"NULL":di.forms[stack2];
		String input0formString=input0==-1?"NULL":di.forms[input0];
		String input1formString=input1==-1?"NULL":di.forms[input1];
		String stack0ldepformString=stack0ldep==-1?"NULL":di.forms[stack0ldep];
		String stack1rdepformString=stack1rdep==-1?"NULL":di.forms[stack1rdep];
		//POS:11
		String stack0posString=stack0==-1?"NULL":di.postags[stack0];
		String stack1posString=stack1==-1?"NULL":di.postags[stack1];
		String stack2posString=stack2==-1?"NULL":di.postags[stack2];
		String stack3posString=stack3==-1?"NULL":di.postags[stack3];
		String input0posString=input0==-1?"NULL":di.postags[input0];
		String input1posString=input1==-1?"NULL":di.postags[input1];
		String input2posString=input2==-1?"NULL":di.postags[input2];
		String input3posString=input3==-1?"NULL":di.postags[input3];
		String stack0ldepposString=stack0ldep==-1?"NULL":di.postags[stack0ldep];
		String stack0rdepposString=stack0rdep==-1?"NULL":di.postags[stack0rdep];
		String stack1ldepposString=stack1ldep==-1?"NULL":di.postags[stack1ldep];
		String stack1rdepposString=stack1rdep==-1?"NULL":di.postags[stack1rdep];
		//DEPREL:4
		String stack0ldep_rel=donechild.contains(stack0ldep)?di.deprels[stack0ldep]:"NULL";
		String stack0rdep_rel=donechild.contains(stack0rdep)?di.deprels[stack0rdep]:"NULL";
		String stack1ldep_rel=donechild.contains(stack1ldep)?di.deprels[stack1ldep]:"NULL";
		String stack1rdep_rel=donechild.contains(stack1rdep)?di.deprels[stack1rdep]:"NULL";
	/*	
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
		*/
		///////////////////////POS FEAT//////////////////////////
		//FEAT1
		
		if(!stack0posString.equals("NULL")){
			String feature1="DOSHFEAT1:"+stack0posString;
			add(feature1, fv);
		}
		
		//FEAT2
		if(!stack1posString.equals("NULL")){
			String feature2="DOSHFEAT2:"+stack1posString;
			add(feature2, fv);
		}
		//FEAT3
		if(!stack2posString.equals("NULL")){
			String feature3="DOSHFEAT3:"+stack2posString;
			add(feature3, fv);
		}
		//FEAT4
		if(!stack3posString.equals("NULL")){
			String feature4="DOSHFEAT4:"+stack3posString;
			add(feature4, fv);
		}
		//FEAT5
		if(!input0posString.equals("NULL")){
			String feature5="DOSHFEAT5:"+input0posString;
			add(feature5, fv);
		}
		//FEAT6
		if(!input1posString.equals("NULL")){
			String feature6="DOSHFEAT6:"+input1posString;
			add(feature6, fv);
		}
		//FEAT7
		if(!input2posString.equals("NULL")){
			String feature7="DOSHFEAT7:"+input2posString;
			add(feature7, fv);
		}
		//FEAT8
		if(!stack0ldepposString.equals("NULL")){
			String feature8="DOSHFEAT8:"+stack0ldepposString;
			add(feature8, fv);
		}
		//FEAT9
		if(!stack0rdepposString.equals("NULL")){
			String feature9="DOSHFEAT9:"+stack0rdepposString;
			add(feature9, fv);
		}
		//FEAT10
		if(!stack1ldepposString.equals("NULL")){
			String feature10="DOSHFEAT10:"+stack1ldepposString;
			add(feature10, fv);
		}
		//FEAT11
		if(!stack1ldepposString.equals("NULL")){
			String feature11="DOSHFEAT11:"+stack1rdepposString;
			add(feature11, fv);
		}
		///////////////////////REL FEAT//////////////////////////
		//FEAT12
		if(!stack0ldep_rel.equals("NULL")){
			String feature12="DOSHFEAT12:"+stack0ldep_rel;
			add(feature12, fv);
		}
		//FEAT13
		if(!stack1rdep_rel.equals("NULL")){
			String feature13="DOSHFEAT13:"+stack1rdep_rel;
			add(feature13, fv);
		}
		///////////////////////FORM FEAT//////////////////////////
		//FEAT14
		if(!stack0formString.equals("NULL")){
			String feature14="DOSHFEAT14:"+stack0formString;
			add(feature14, fv);
		}
		//FEAT15
		if(!stack1formString.equals("NULL")){
			String feature15="DOSHFEAT15:"+stack1formString;
			add(feature15, fv);
		}
		//FEAT16
		if(!stack2formString.equals("NULL")){
			String feature16="DOSHFEAT16:"+stack2formString;
			add(feature16, fv);
		}
		//FEAT17
		if(!input0formString.equals("NULL")){
			String feature17="DOSHFEAT17:"+input0formString;
			add(feature17, fv);
		}
		//FEAT18
		if(!input1formString.equals("NULL")){
			String feature18="DOSHFEAT18:"+input1formString;
			add(feature18, fv);
		}
		//FEAT19
		if(!stack0ldepformString.equals("NULL")){
			String feature19="DOSHFEAT19:"+stack0ldepformString;
			add(feature19, fv);
		}
		//FEAT20
		if(!stack1rdepformString.equals("NULL")){
			String feature20="DOSHFEAT20:"+stack1rdepformString;
			add(feature20, fv);
		}
		///////////////////////MERGE FEAT//////////////////////////
		//FEAT21
		String feature21=(stack0==-1&&stack1==-1)?"DOSHFEAT21:NULL":"DOSHFEAT21:"+stack0posString+"-"+stack1posString;
		add(feature21, fv);
		//FEAT22
		String feature22=(stack0==-1&&input0==-1)?"DOSHFEAT22:NULL":"DOSHFEAT22:"+stack0formString+"-"+input0formString;
		add(feature22, fv);
		//FEAT23
		String feature23=(stack0==-1)?"DOSHFEAT23:NULL":"DOSHFEAT23:"+stack0posString+"-"+stack0formString;
		add(feature23, fv);
		//FEAT24
		String feature24=(stack1==-1)?"DOSHFEAT24:NULL":"DOSHFEAT24:"+stack1posString+"-"+stack1formString;
		add(feature24, fv);
		//FEAT25
		String feature25=(input0==-1)?"DOSHFEAT25:NULL":"DOSHFEAT25:"+input0posString+"-"+input0formString;
		add(feature25, fv);
		//FEAT26
		String feature26=(stack1rdep==-1&&stack0ldep==-1)?"DOSHFEAT26:NULL":"DOSHFEAT26:"+stack1rdep_rel+"-"+stack0ldep_rel;
		add(feature26, fv);
		//FEAT27
		String feature27=(stack0==-1&&stack1==-1&&input0==-1)?"DOSHFEAT27:NULL":"DOSHFEAT27:"+stack0posString+"-"+stack1posString+"-"+input0posString;
		add(feature27, fv);
		//FEAT28
		String feature28=(stack0==-1&&stack1==-1&&stack2==-1)?"DOSHFEAT28:NULL":"DOSHFEAT28:"+stack0posString+"-"+stack1posString+"-"+stack2posString;
		add(feature28, fv);
		//FEAT29
		String feature29=(stack0==-1&&input0==-1&&input1==-1)?"DOSHFEAT29:NULL":"DOSHFEAT29:"+stack0posString+"-"+input0posString+"-"+input1posString;
		add(feature29, fv);
		//FEAT30
		String feature30=(input0==-1&&input1==-1&&input2==-1)?"DOSHFEAT30:NULL":"DOSHFEAT30:"+input0posString+"-"+input1posString+"-"+input2posString;
		add(feature30, fv);
		//FEAT31
		String feature31=(input1==-1&&input2==-1&&input3==-1)?"DOSHFEAT31:NULL":"DOSHFEAT31:"+input1posString+"-"+input2posString+"-"+input3posString;
		add(feature31, fv);
		//FEAT32
		String feature32=(stack1rdep==-1&&stack1ldep==-1&&stack1==-1)?"DOSHFEAT32:NULL":"DOSHFEAT32:"+stack1rdepposString+"-"+stack1ldepposString+"-"+stack1posString;
		add(feature32, fv);
		//FEAT33
		String feature33=(stack1==-1&&stack1ldep==-1&&stack1rdep==-1)?"DOSHFEAT33:NULL":"DOSHFEAT33:"+stack1posString+"-"+stack1ldep_rel+"-"+stack1rdep_rel;
		add(feature33, fv);
		if(print){
			System.out.println("\t*"+stack0+"\t"+stack0ldep+"\t"+stack0rdep+"\t*"+stack1+"\t"+stack1ldep+"\t"+stack1rdep+"\t*"+stack2+"\t*"+input0+"\t*"+input1+"\t*"+input2);
//			System.out.println("\t"+feature1+"\t"+feature2+"\t"+feature3+"\t"+feature4+"\t"+feature5+"\t"+feature6+"\t"+feature7+"\t"+feature8+"\t"+feature9+"\t"+feature10+
//					"\t"+feature11+"\t"+feature12+"\t"+feature13+"\t"+feature14+"\t"+feature15+"\t"+feature16+"\t"+feature17+"\t"+feature18+"\t"+feature19+"\t"+feature20);
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
    public void GenerateLiblinearAndOrderFile(String conllfile,String liblinearfile, String orderfile,String alphabetfile) throws IOException{
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
    		//Object[] ret=GenerateLiblinearInstance(di);
    		Object[] ret=GenerateMaltLiblinearInstance(di);
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
    	File alphabetout=new File(alphabetfile);
    	if(!alphabetout.exists()){
    		alphabetout.createNewFile();
    	}
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream( alphabetfile));
        out.writeObject(doshAlphabet);
        out.close();
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
	
	public void TrainLiblinear(String libinstfilestring, String modelname) throws IOException, InvalidInputDataException{
		Train train=new Train();
//		File libinstfile=new File(libinstfilestring);
//        train.readProblem(libinstfile,-1.0);
        //train.main(new String[] {"-v", "10", "-c", "10", "-w1", "1.234", "-s","4",libinstfilestring,modelname});
		train.main(new String[] { "-c", "10", "-e", "0.1", "-s","4",libinstfilestring,modelname});
	}
	
	public void TestModelAccuracyOnInstanceFile(String liblinearmodel,String instfile) throws IOException, ClassNotFoundException{
    	Model libModel=Model.load(new File(liblinearmodel));
    	BufferedReader reader=new BufferedReader(new FileReader(instfile));
    	String line=reader.readLine();
    	int rightcount=0;
    	int totalcount=0;
    	while(line!=null){
    		totalcount++;
    		String columns[]=line.split(" ");
    		double[] estimates = new double[libModel.getNrClass()];
    		FeatureNode fn[]=new FeatureNode[columns.length-1];
    		for(int i=1;i<columns.length;i++){
    			String feat_value[]=columns[i].split(":");
    			fn[i-1]=new FeatureNode(Integer.parseInt(feat_value[0]), Double.parseDouble(feat_value[1]));
    		}
            double probabilityPrediction = Linear.predictValues(libModel, fn, estimates);
        	System.out.println("prediction:"+probabilityPrediction);
        	for(double est:estimates){
        		System.out.print("\t"+est);
        	}
        	int labels[]=libModel.getLabels();
        	for(int label:labels){
        		System.out.print("\t"+label);
        	}
        	System.out.println();
        	if(Integer.parseInt(columns[0])==(int)probabilityPrediction){
        		rightcount++;
        	}
    		line=reader.readLine();
    	}
    	
    	System.out.println("Lib ActionAccuracy:"+(double)rightcount/totalcount);
	}
	
	
	
}
