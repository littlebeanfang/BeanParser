package Parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;

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
	
	public ParseAgenda DecodeInstance(DependencyInstance inst, TIntIntHashMap ordermap) throws FileNotFoundException{
		ParseAgenda pa=new ParseAgenda();
		for(int orderindex=1;orderindex<inst.length();orderindex++){
			//skip root node
			int parseindex=ordermap.get(orderindex);
			int parsehead=this.FindHeadForOneWord(inst, parseindex, pa);
			pa.AddArc(parseindex, parsehead);
		}
		pa.AddArc(0, -1);//add root
		return pa;
	}
	
	public int FindHeadForOneWord(DependencyInstance inst,int childindex, ParseAgenda pa){
		boolean verbose=true;
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
					//actfv=fv;
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
		return headindex;
	}
}
