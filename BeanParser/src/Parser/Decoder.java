package Parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import gnu.trove.TIntIntHashMap;
import DataStructure.DependencyInstance;
import DataStructure.FeatureVector;
import DataStructure.Parameters;
import DataStructure.ParseAgenda;

public class Decoder {
	private MyPipe pipe;
	private Parameters param;
	
	public Decoder(DependencyPipe pipe, Parameters param){
		this.pipe=(MyPipe) pipe;
		this.param=param;
	}
	
	public Object[] DecodeInstance(DependencyInstance inst, TIntIntHashMap ordermap) throws IOException{
		ParseAgenda pa=new ParseAgenda();
		Object[] instret=new Object[2];
		FeatureVector fvforinst=new FeatureVector();
		
		// set: disjoint-set data structure, stores the parent of each node
		int[] set = new int[inst.length()];
		for (int i = 0;i < inst.length();i++) set[i] = i;
		
		for(int orderindex=1;orderindex<inst.length();orderindex++){
			//skip root node
			int parseindex=ordermap.get(orderindex);
			Object[] ret=this.FindHeadForOneWord(inst, parseindex, pa, set);
			int parsehead=(int) ret[0];
//			System.out.println("DecodeInstance fvforinst after call findhead:"+ret[1].toString().split(" ").length);
			pa.AddArc(parseindex, parsehead);
			pa.ChildProcess(parseindex, parsehead);
			fvforinst=fvforinst.cat((FeatureVector) ret[1]);
            inst.heads[parseindex]=parsehead;
		}
		
		pa.AddArc(0, -1);//add root
		//PrintScores(inst, pa);
		instret[0]=pa;
		instret[1]=fvforinst;
		return instret;
	}
	
	public Object[] FindHeadForOneWord(DependencyInstance inst,int childindex, ParseAgenda pa, int[] set){
		Object[] ret=new Object[2];
		boolean verbose=false;
		int headindex=-1;
		double score=Double.NEGATIVE_INFINITY;
		FeatureVector actfv=new FeatureVector();
		for(int head=0; head<inst.length();head++){
			if ((head!=childindex) && (FindRoot(head, set) != childindex)) { //Jia: if the root of the head is not child
				FeatureVector fv=new FeatureVector();
				//pipe.AddNewFeature(inst, childindex, head, pa, fv);
				pipe.extractFeatures(inst, childindex, head, pa, fv);
				double temp=fv.getScore(param.parameters);
				if(temp>score){
					score=temp;
					headindex=head;
					//must store best fv in DependencyInstance
					actfv=fv;
					
				}
			}
		}
		if(verbose){
			System.out.println("Instance:/n"+inst);
			System.out.println("Child index:"+childindex);
			System.out.println("Head index:"+headindex);
			System.out.println("Score:"+score);
		}
		
		//Update the disjoint-set
		set[childindex] = headindex;
		
		//inst.fv.cat(actfv);
		//Bean: store feature vector in fvforinst, for Object d[][]
//		System.out.println("==================FindHeadForOneWord");
//		System.out.println("actfv while decoding:"+actfv.toString().split(" ").length+"   fv:"+actfv.toString());
		//System.out.println("fvforinst while decoding before cat:length"+fvforinst.toString().split(" ").length);
		//fvforinst=fvforinst.cat(actfv);
		//System.out.println("fvforinst while decoding:length"+fvforinst.toString().split(" ").length+"   fv:"+fvforinst.toString());
//		System.out.println("==================FindHeadForOneWord END");
		ret[0]=headindex;
		ret[1]=actfv;
		return ret;
	}

	private int FindRoot(int node, int[] set) {
		if (set[node] != node) set[node] = FindRoot(set[node], set);
		return set[node];
	}
	
	public void PrintScores(DependencyInstance inst, ParseAgenda pa) throws IOException {
		int length = inst.length();
		Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Scores_PA.txt")));
		writer.write("child");
		for (int i = 0;i < length;i++)
			writer.write("\t" + i);
		writer.write("\n");
		for (int i = 0;i < length;i++){
			writer.write(i + "\t");
			for (int j = 0;j < length;j++){
				if (i != j) {
					FeatureVector fv = new FeatureVector();
					pipe.extractFeatures(inst, i, j, pa, fv);
					double tmp = fv.getScore(param.parameters);
					writer.write(String.format("%.2f\t",tmp));
				}
				else {
					writer.write("0\t");
				}
			}
			writer.write("\n");
		}
		writer.close();
	}
}
