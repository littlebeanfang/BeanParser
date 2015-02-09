package DOSH;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import mstparser.Alphabet;
import DataStructure.DependencyInstance;
import DataStructure.FeatureVector;
import DataStructure.ParseAgenda;
import IO.CONLLReader;

/**
 * This class parsing while predicting order.
 * Load liblinear model to decide whether find head of a node
 * Bean parser find the head of a node using graph-based method
 * @author Bean
 *
 */
public class DOSHParse {
	private Alphabet doshalphabeAlphabet;
	private final int DO=1;
	private final int UNDO=2;
	
	public DOSHParse(String doshalphabetfile,  String liblinearmodel, String beanmodel){
		//load model and init params
	}
	public DOSHParse(){
		
	}
	public void DecodeOneInstance(DependencyInstance di){
		boolean toright=true; //flag for the direction of indexpointer
		boolean reachend=false; //flag for no more shift is allowed
		
	}
	public void PredictAction(){
		//pseudo predictor
	}
	public void ExtractDoshFeature(DependencyInstance di, HashSet donechild,ParseAgenda pa, int[] node, int indexinnode, FeatureVector fv){
		//TODO not finish yet, just test the index
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
		System.out.println("\t*"+stack0+"\t"+stack0ldep+"\t"+stack0rdep+"\t*"+stack1+"\t"+stack1ldep+"\t"+stack1rdep+"\t*"+stack2+"\t*"+input0+"\t*"+input1+"\t*"+input2);
		
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
    public void TestLibPredict(String libmodelfile,String alphagetfile,String conllfile) throws IOException, ClassNotFoundException{
    	//try on 10136 on dutch train data
    	CONLLReader reader=new CONLLReader();
    	reader.startReading(conllfile);
    	DependencyInstance di=reader.getNext();
    	Model libModel=Model.load(new File(libmodelfile));
    	DOSHTrain featextract=new DOSHTrain(alphagetfile);
    	FeatureVector fv=new FeatureVector();
    	int node[]={0,1,2,3,4,5,6,7,8,9,10,11,12,13,-1};
    	featextract.ExtractDoshFeature(di, new HashSet(), new ParseAgenda(di.length()), node, 1, fv);
    	LiblinearInstance test=new LiblinearInstance(fv);
    	double predict=Linear.predict(libModel, test.TransformXfeat());
    	System.out.println("prediction:"+predict);
    }
}
