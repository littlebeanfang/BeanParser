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
	
	public ParseAgenda DecodeInstance(DependencyInstance inst, TIntIntHashMap ordermap,FeatureVector fvforinst) throws IOException{
		ParseAgenda pa=new ParseAgenda();
		for(int orderindex=1;orderindex<inst.length();orderindex++){
			//skip root node
			int parseindex=ordermap.get(orderindex);
			int parsehead=this.FindHeadForOneWord(inst, parseindex, pa, fvforinst);
			pa.AddArc(parseindex, parsehead);
            inst.heads[parseindex]=parsehead;
		}
		pa.AddArc(0, -1);//add root
		
		//PrintScores(inst, pa);
		return pa;
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
	
	public int FindHeadForOneWord(DependencyInstance inst,int childindex, ParseAgenda pa, FeatureVector fvforinst){
		boolean verbose=false;
		int headindex=-1;
		double score=Double.NEGATIVE_INFINITY;
		FeatureVector actfv=new FeatureVector();
		for(int head=0; head<inst.length();head++){
			if(head!=childindex){
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
		//inst.fv.cat(actfv);
		//Bean: store feature vector in fvforinst, for Object d[][]
		fvforinst.cat(actfv);
		return headindex;
	}
}
