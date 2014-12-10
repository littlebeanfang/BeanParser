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
	//TODO: change this to decide whether predict label
	boolean parsewithrelation=true;
	
	private MyPipe pipe;
	private Parameters param;
	
	public Decoder(DependencyPipe pipe, Parameters param){
		this.pipe=(MyPipe) pipe;
		this.param=param;
	}
	
	public Object[] DecodeInstance(DependencyInstance inst, TIntIntHashMap ordermap) throws IOException{
		ParseAgenda pa=new ParseAgenda(pipe.typeAlphabet);
		Object[] instret=new Object[3];
		FeatureVector fvforinst=new FeatureVector();
		
		FeatureVector checkparamupdate=new FeatureVector();
		// set: disjoint-set data structure, stores the parent of each node
		int[] set = new int[inst.length()];
		for (int i = 0;i < inst.length();i++) set[i] = i;
		for(int orderindex=1;orderindex<inst.length();orderindex++){
			//skip root node
			int parseindex=ordermap.get(orderindex);
			Object[] ret=this.FindHeadForOneWord(inst, parseindex, pa, set);
			inst.deprels[parseindex]=(String)ret[3];
			checkparamupdate=checkparamupdate.cat((FeatureVector)ret[2]);
			int parsehead=(int) ret[0];
//			System.out.println("parsehead in decodeinstace:"+parsehead+",rel:"+pipe.typeAlphabet.lookupIndex(parsehead));
//			System.out.println("DecodeInstance fvforinst after call findhead:"+ret[1].toString().split(" ").length);
			pa.AddArc(parseindex, parsehead);
			fvforinst=fvforinst.cat((FeatureVector) ret[1]);
            inst.heads[parseindex]=parsehead;
		}
		//System.out.println("=======DecodeInstance checkparamupdate size:"+checkparamupdate.size()+"fvforinst size:"+fvforinst.size());
		pa.AddArc(0, -1);//add root
		//PrintScores(inst, pa);
		instret[0]=pa;
		instret[1]=fvforinst;
		instret[2]=checkparamupdate;
		return instret;
	}
	
	public Object[] FindHeadForOneWord(DependencyInstance inst,int childindex, ParseAgenda pa, int[] set){
		Object[] ret=new Object[4];
		boolean verbose=false;
		int headindex=-1;
		double score=Double.NEGATIVE_INFINITY;
		FeatureVector actfv=new FeatureVector();
		FeatureVector bestlabelfv=new FeatureVector();
		String bestrelation="";
		for(int head=0; head<inst.length();head++){
			if ((head!=childindex) && (FindRoot(head, set) != childindex)) { //Jia: if the root of the head is not child
				FeatureVector fv=new FeatureVector();
				FeatureVector labelfv=new FeatureVector();
				//pipe.AddNewFeature(inst, childindex, head, pa, fv);
				Object[] retfv=new Object[2];
				if(parsewithrelation){
					retfv=pipe.extractParseFeatures(inst, childindex, head, pa, param);
					labelfv=(FeatureVector)retfv[1];
					fv=(FeatureVector)retfv[0];
					//System.out.println("FindHeadForOneWord checkparamupdate size:"+checkparamupdate.size());
					//System.out.println("relation out of addfeature:"+inst.deprels[childindex]);
				}else{
					pipe.extractFeatures(inst, childindex, head, pa, fv);
				}
				double temp=fv.getScore(param.parameters);
				if(temp>score){
					score=temp;
					headindex=head;
					//must store best fv in DependencyInstance
					actfv=fv;
					bestlabelfv=labelfv;
					if(parsewithrelation){
						//System.out.println((String)retfv[2]);
						bestrelation=(String)retfv[2];
					}
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
		ret[2]=bestlabelfv;
		ret[3]=bestrelation;
		//System.out.println("child:"+childindex+",head:"+headindex+",relation:"+bestrelation);
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
