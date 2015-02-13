package Malt;

import gnu.trove.TIntIntHashMap;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Stack;

import com.sun.org.apache.xalan.internal.utils.FeatureManager.Feature;

import mstparser.Alphabet;
import DOSH.LiblinearInstance;
import DataStructure.DependencyInstance;
import DataStructure.FeatureVector;
import DataStructure.ParseAgenda;
import IO.CONLLReader;
import IO.CONLLWriter;

public class MaltTrain {
	public final static int LA=1;
	public final static int RA=2;
	public final static int SH=3;
	
	public Alphabet maltAlphabet;
	public MaltTrain(){
		maltAlphabet=new Alphabet();
		maltAlphabet.lookupIndex("beanbeanjiangO(กษ_กษ)O");
	}
	public MaltTrain(String alphabetfile) throws IOException, ClassNotFoundException{
		//for extract feature
		ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(alphabetfile)));
        maltAlphabet=(Alphabet) in.readObject();
        in.close();
        maltAlphabet.stopGrowth();
	}
	
	public Object[] GenerateLiblinearInstance(DependencyInstance di){
		Object[] ret=new Object[2];
		ParseAgenda pa=new ParseAgenda(di.length());
		HashSet instlist=new HashSet();
		//store the generated order-child pairs
		TIntIntHashMap order_child=new TIntIntHashMap();	//****
		//matrix of head-dependent table, index 0 is of no use
		boolean head_dependent[][]=new boolean[di.length()][di.length()];
		for(int i=1;i<di.length();i++){
			int head=di.heads[i];
			head_dependent[head][i]=true;
		}
		//store the nodes that have been done
		HashSet donenodes=new HashSet();					//****
		TIntIntHashMap child_tranverseindex=new TIntIntHashMap();
		GetInnerTranverseOrder(head_dependent, 0, child_tranverseindex);
		//int[] MPCNodeID=GetMPC(head_dependent);
		Stack<Integer> alpha=new Stack<Integer>();			//****
		alpha.push(0);
		Stack<Integer> beta=new Stack<Integer>();			//****
		for(int i=di.length()-1;i>0;i--){
			beta.push(i);
		}
		int order=1;
//				System.out.println(di.length()-1);
		while(donenodes.size()<di.length()-1){
			int i=alpha.size()>1?alpha.get(alpha.size()-2):-1;
			int j=alpha.peek();
			boolean action=false;
			if(i!=-1&&head_dependent[j][i]==true){
				int m;
				for(m=0;m<head_dependent.length;m++){
					if(head_dependent[i][m]==true&&!donenodes.contains(m)){
						break;
					}
				}
				if(m==head_dependent.length){
					//DO LA
					FeatureVector fv=new FeatureVector();
					ExtractFeature(alpha, beta, di, pa, fv);
					pa.AddArc(i, di.heads[i]);
					pa.ChildProcess(i, di.heads[i]);
					instlist.add(new LiblinearInstance(fv, MaltTrain.LA));
					
					alpha.remove(alpha.size()-2);
					donenodes.add(i);
					order_child.put(order++, i);
					action=true;
					System.out.println("LA+"+di.deprels[i]);
				}
			}
			if(!action&&i!=-1&&head_dependent[i][j]==true){
				int m;
				for(m=0;m<head_dependent.length;m++){
					if(head_dependent[j][m]==true&&!donenodes.contains(m)){
						break;
					}
				}
				if(m==head_dependent.length){
					//DO RA
					FeatureVector fv=new FeatureVector();
					ExtractFeature(alpha, beta, di, pa, fv);
					pa.AddArc(j, di.heads[j]);
					pa.ChildProcess(j, di.heads[j]);
					instlist.add(new LiblinearInstance(fv, MaltTrain.RA));
					
					alpha.pop();
					donenodes.add(j);
					order_child.put(order++, j);
					action=true;
					System.out.println("RA+"+di.deprels[j]);
				}
			}
			/*
			if(!action&&i!=-1&&child_tranverseindex.get(j)<child_tranverseindex.get(i)){
				int k=beta.size()>0?beta.peek():-1;
				if(k!=-1&&MPCNodeID[j]!=MPCNodeID[k]){
					//SWAP
					alpha.pop();
					beta.push(alpha.pop());
					alpha.push(j);
					action=true;
					System.out.println("SWAP");
				}
			}
			*/
			if(!action){
				//SH
				FeatureVector fv=new FeatureVector();
				ExtractFeature(alpha, beta, di, pa, fv);
				instlist.add(new LiblinearInstance(fv, MaltTrain.SH));
				
				alpha.push(beta.pop());
				System.out.println("SH");
			}
//					System.out.println("size:"+donenodes.size());
		}
		ret[0]=instlist;
		ret[1]=order_child;
		return ret;
	}
	/**
	 * returns the inner tranverse of a tree
	 * @param head_dependent
	 * @param head
	 * @return
	 */
	public void GetInnerTranverseOrder(boolean[][] head_dependent, int head, TIntIntHashMap child_tranverseindex){
		//ordinally tranverse childs of head before index of head
		for(int lchild=0;lchild<head;lchild++){
			if(head_dependent[head][lchild]==true){
				GetInnerTranverseOrder(head_dependent, lchild, child_tranverseindex);
			}
		}
		
		for(int rchild=head+1;rchild<head_dependent.length;rchild++){
			if(head_dependent[head][rchild]==true){
				GetInnerTranverseOrder(head_dependent, rchild, child_tranverseindex);
			}
		}
		child_tranverseindex.put(head,child_tranverseindex.size()+1);
	}
	public void ExtractFeature(Stack<Integer> alpha, Stack<Integer> beta, DependencyInstance di, ParseAgenda pa, FeatureVector fv){
		boolean print=false;
		//index
		int stack0=alpha.size()>0?alpha.peek():-1;
		int stack1=alpha.size()-2>=0?alpha.get(alpha.size()-2):-1;
		int stack2=alpha.size()-3>=0?alpha.get(alpha.size()-3):-1;
		int stack3=alpha.size()-4>=0?alpha.get(alpha.size()-4):-1;
		int lookahead0=beta.size()>1?beta.peek():-1;
		int lookahead1=beta.size()-2>=0?beta.get(beta.size()-2):-1;
		int lookahead2=beta.size()-3>=0?beta.get(beta.size()-3):-1;
		int lookahead3=beta.size()-4>=0?beta.get(beta.size()-4):-1;
		int stack0ldep=stack0==-1?-1:leftmostdep(pa, stack0);
		int stack0rdep=stack0==-1?-1:rightmostdep(pa, stack0);
		int stack1ldep=stack1==-1?-1:leftmostdep(pa, stack1);
		int stack1rdep=stack1==-1?-1:rightmostdep(pa, stack1);
		//pos
		String stack0posString=stack0!=-1?di.postags[stack0]:null;
		String stack1posString=stack1!=-1?di.postags[stack1]:null;
		String stack2posString=stack2!=-1?di.postags[stack2]:null;
		String stack3posString=stack3!=-1?di.postags[stack3]:null;
		String lookahead0posString=lookahead0!=-1?di.postags[lookahead0]:null;
		String lookahead1posString=lookahead1!=-1?di.postags[lookahead1]:null;
		String lookahead2posString=lookahead2!=-1?di.postags[lookahead2]:null;
		String lookahead3posString=lookahead3!=-1?di.postags[lookahead3]:null;
		String stack0ldepposString=stack0ldep!=-1?di.postags[stack0ldep]:null;
		String stack0rdepposString=stack0rdep!=-1?di.postags[stack0rdep]:null;
		String stack1ldepposString=stack1ldep!=-1?di.postags[stack1ldep]:null;
		String stack1rdepposString=stack1rdep!=-1?di.postags[stack1rdep]:null;
		//form
		String stack0formString=stack0!=-1?di.forms[stack0]:null;
		String stack1formString=stack1!=-1?di.forms[stack1]:null;
		String stack2formString=stack2!=-1?di.forms[stack2]:null;
		String stack3formString=stack3!=-1?di.forms[stack3]:null;
		String lookahead0formString=lookahead0!=-1?di.forms[lookahead0]:null;
		String lookahead1formString=lookahead1!=-1?di.forms[lookahead1]:null;
		String lookahead2formString=lookahead2!=-1?di.forms[lookahead2]:null;
		String lookahead3formString=lookahead3!=-1?di.forms[lookahead3]:null;
		String stack0ldepformString=stack0ldep!=-1?di.forms[stack0ldep]:null;
		String stack1rdepformString=stack1rdep!=-1?di.forms[stack1rdep]:null;
		//deprel
		String stack0ldep_relString=stack0ldep!=-1?di.deprels[stack0ldep]:null;
		String stack0rdep_relString=stack0rdep!=-1?di.deprels[stack0rdep]:null;
		String stack1ldep_relString=stack1ldep!=-1?di.deprels[stack1ldep]:null;
		String stack1rdep_relString=stack1rdep!=-1?di.deprels[stack1rdep]:null;
		
		if(stack0posString!=null){
			String feature1="DOSHFEAT1:"+stack0posString;
			add(feature1, fv);
		}
		
		//FEAT2
		if(stack1posString!=null){
			String feature2="DOSHFEAT2:"+stack1posString;
			add(feature2, fv);
		}
		//FEAT3
		if(stack2posString!=null){
			String feature3="DOSHFEAT3:"+stack2posString;
			add(feature3, fv);
		}
		//FEAT4
		if(stack3posString!=null){
			String feature4="DOSHFEAT4:"+stack3posString;
			add(feature4, fv);
		}
		//FEAT5
		if(lookahead0posString!=null){
			String feature5="DOSHFEAT5:"+lookahead0posString;
			add(feature5, fv);
		}
		//FEAT6
		if(lookahead1posString!=null){
			String feature6="DOSHFEAT6:"+lookahead1posString;
			add(feature6, fv);
		}
		//FEAT7
		if(lookahead2posString!=null){
			String feature7="DOSHFEAT7:"+lookahead2posString;
			add(feature7, fv);
		}
		//FEAT8
		if(stack0ldepposString!=null){
			String feature8="DOSHFEAT8:"+stack0ldepposString;
			add(feature8, fv);
		}
		//FEAT9
		if(stack0rdepposString!=null){
			String feature9="DOSHFEAT9:"+stack0rdepposString;
			add(feature9, fv);
		}
		//FEAT10
		if(stack1ldepposString!=null){
			String feature10="DOSHFEAT10:"+stack1ldepposString;
			add(feature10, fv);
		}
		//FEAT11
		if(stack1ldepposString!=null){
			String feature11="DOSHFEAT11:"+stack1rdepposString;
			add(feature11, fv);
		}
		///////////////////////REL FEAT//////////////////////////
		//FEAT12
		if(stack0ldep_relString!=null){
			String feature12="DOSHFEAT12:"+stack0ldep_relString;
			add(feature12, fv);
		}
		//FEAT13
		if(stack1rdep_relString!=null){
			String feature13="DOSHFEAT13:"+stack1rdep_relString;
			add(feature13, fv);
		}
		///////////////////////FORM FEAT//////////////////////////
		//FEAT14
		if(stack0formString!=null){
			String feature14="DOSHFEAT14:"+stack0formString;
			add(feature14, fv);
		}
		//FEAT15
		if(stack1formString!=null){
			String feature15="DOSHFEAT15:"+stack1formString;
			add(feature15, fv);
		}
		//FEAT16
		if(stack2formString!=null){
			String feature16="DOSHFEAT16:"+stack2formString;
			add(feature16, fv);
		}
		//FEAT17
		if(lookahead0formString!=null){
			String feature17="DOSHFEAT17:"+lookahead0formString;
			add(feature17, fv);
		}
		//FEAT18
		if(lookahead1formString!=null){
			String feature18="DOSHFEAT18:"+lookahead1formString;
			add(feature18, fv);
		}
		//FEAT19
		if(stack0ldepformString!=null){
			String feature19="DOSHFEAT19:"+stack0ldepformString;
			add(feature19, fv);
		}
		//FEAT20
		if(stack1rdepformString!=null){
			String feature20="DOSHFEAT20:"+stack1rdepformString;
			add(feature20, fv);
		}
		///////////////////////MERGE FEAT//////////////////////////
		//FEAT21
		String feature21=(stack0==-1&&stack1==-1)?null:"DOSHFEAT21:"+stack0posString+"-"+stack1posString;
		if(feature21!=null){
			add(feature21, fv);
		}
		//FEAT22
		String feature22=(stack0==-1&&lookahead0==-1)?null:"DOSHFEAT22:"+stack0formString+"-"+lookahead0formString;
		if(feature22!=null){
			add(feature22, fv);
		}
		//FEAT23
		String feature23=(stack0==-1)?null:"DOSHFEAT23:"+stack0posString+"-"+stack0formString;
		if(feature23!=null){
			add(feature23, fv);
		}
		//FEAT24
		String feature24=(stack1==-1)?null:"DOSHFEAT24:"+stack1posString+"-"+stack1formString;
		if(feature24!=null){
			add(feature24, fv);
		}
		//FEAT25
		String feature25=(lookahead0==-1)?null:"DOSHFEAT25:"+lookahead0posString+"-"+lookahead0formString;
		if(feature25!=null){
			add(feature25, fv);
		}
		//FEAT26
		String feature26=(stack1rdep==-1&&stack0ldep==-1)?null:"DOSHFEAT26:"+stack1rdep_relString+"-"+stack0ldep_relString;
		if(feature26!=null){
			add(feature26, fv);
		}
		//FEAT27
		String feature27=(stack0==-1&&stack1==-1&&lookahead0==-1)?null:"DOSHFEAT27:"+stack0posString+"-"+stack1posString+"-"+lookahead0posString;
		if(feature27!=null){
			add(feature27, fv);
		}
		//FEAT28
		String feature28=(stack0==-1&&stack1==-1&&stack2==-1)?null:"DOSHFEAT28:"+stack0posString+"-"+stack1posString+"-"+stack2posString;
		if(feature28!=null){
			add(feature28, fv);
		}
		//FEAT29
		String feature29=(stack0==-1&&lookahead0==-1&&lookahead1==-1)?null:"DOSHFEAT29:"+stack0posString+"-"+lookahead0posString+"-"+lookahead1posString;
		if(feature29!=null){
			add(feature29, fv);
		}
		//FEAT30
		String feature30=(lookahead0==-1&&lookahead1==-1&&lookahead2==-1)?null:"DOSHFEAT30:"+lookahead0posString+"-"+lookahead1posString+"-"+lookahead2posString;
		if(feature30!=null){
			add(feature30, fv);
		}
		//FEAT31
		String feature31=(lookahead1==-1&&lookahead2==-1&&lookahead3==-1)?null:"DOSHFEAT31:"+lookahead1posString+"-"+lookahead2posString+"-"+lookahead3posString;
		if(feature31!=null){
			add(feature31, fv);
		}
		//FEAT32
		String feature32=(stack1rdep==-1&&stack1ldep==-1&&stack1==-1)?null:"DOSHFEAT32:"+stack1rdepposString+"-"+stack1ldepposString+"-"+stack1posString;
		if(feature32!=null){
			add(feature32, fv);
		}
		//FEAT33
		String feature33=(stack1==-1&&stack1ldep==-1&&stack1rdep==-1)?null:"DOSHFEAT33:"+stack1posString+"-"+stack1ldep_relString+"-"+stack1rdep_relString;
		if(feature33!=null){
			add(feature33, fv);
		}
		if(print){
			System.out.println("\t*"+stack0+"\t"+stack0ldep+"\t"+stack0rdep+"\t*"+stack1+"\t"+stack1ldep+"\t"+stack1rdep+"\t*"+stack2+"\t*"+lookahead0+"\t*"+lookahead1+"\t*"+lookahead2+"\t*"+lookahead3);
//			System.out.println("\t"+feature1+"\t"+feature2+"\t"+feature3+"\t"+feature4+"\t"+feature5+"\t"+feature6+"\t"+feature7+"\t"+feature8+"\t"+feature9+"\t"+feature10+
//					"\t"+feature11+"\t"+feature12+"\t"+feature13+"\t"+feature14+"\t"+feature15+"\t"+feature16+"\t"+feature17+"\t"+feature18+"\t"+feature19+"\t"+feature20);
		}
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
	    	maltAlphabet.stopGrowth();
	    	File alphabetout=new File(alphabetfile);
	    	if(!alphabetout.exists()){
	    		alphabetout.createNewFile();
	    	}
	        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream( alphabetfile));
	        out.writeObject(maltAlphabet);
	        out.close();
	    	System.out.println("Number of Doshfeature:"+(maltAlphabet.size()-1));
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
	public int leftmostdep(ParseAgenda pa, int index){
    	if(index==0||index==-1){
    		return -1;
    	}
    	return pa.leftmostchilds[index];
	}
	public int rightmostdep(ParseAgenda pa, int index){
    	if(index==0||index==-1){
    		return -1;
    	}
    	return pa.rightmostchilds[index];
	}
	// add with default 1.0
    public final void add(String feat, FeatureVector fv) {
        int num = maltAlphabet.lookupIndex(feat);
        if (num >= 0)
            fv.add(num, 1.0);
    }
}
