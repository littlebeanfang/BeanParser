package Parser;

import DataStructure.DependencyInstance;
import DataStructure.FeatureVector;
import DataStructure.Parameters;
import DataStructure.ParseAgenda;

public class Decoder {
	private MyPipe pipe;
	private Parameters param;
	public Decoder(MyPipe pipe, Parameters param){
		this.pipe=pipe;
		this.param=param;
	}
	public ParseAgenda DecodeInstance(DependencyInstance inst){
		ParseAgenda pa=new ParseAgenda();
		for(int parseindex=1;parseindex<inst.length();parseindex++){
			//skip root node
			int parsehead=this.FindHeadForOneWord(inst, parseindex, pa);
			pa.AddArc(parsehead, parsehead);
		}
		pa.AddArc(0, -1);//add root
		return pa;
	}
	public int FindHeadForOneWord(DependencyInstance inst,int childindex, ParseAgenda pa){
		boolean verbose=true;
		int headindex=-1;
		double score=Double.NEGATIVE_INFINITY;
		for(int head=0; head<inst.length();head++){
			if(head!=childindex){
				FeatureVector fv=new FeatureVector();
				pipe.AddNewFeature(inst, childindex, head, pa, fv);
				double temp=fv.getScore(param.parameters);
				if(temp>score){
					score=temp;
					headindex=head;
				}
			}
		}
		if(verbose){
			System.out.println("Instance:/n"+inst);
			System.out.println("Child index:"+childindex);
			System.out.println("Head index:"+headindex);
			System.out.println("Score:"+score);
		}
		return headindex;
	}
}
