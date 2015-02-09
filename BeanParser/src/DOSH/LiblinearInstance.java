package DOSH;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.TreeSet;

import de.bwaldvogel.liblinear.FeatureNode;
import DataStructure.FeatureVector;

public class LiblinearInstance {
	private FeatureVector xfeat;
	private int ylabel;
	public LiblinearInstance(FeatureVector x,int label){
		xfeat=x;
		ylabel=label;
	}
	public LiblinearInstance(FeatureVector fv){
		//for prediction
		xfeat=fv;
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		int indexs[]=xfeat.keys();
		Arrays.sort(indexs);
		String ret=""+ylabel;
		for(int i=0;i<indexs.length;i++){
			ret+=" "+indexs[i]+":1";
		}
		return ret;
	}
	public FeatureNode[] TransformXfeat(){
		
		int keys[]=xfeat.keys();
		FeatureNode[] ret=new FeatureNode[keys.length];
		for(int i=0;i<keys.length;i++){
			ret[i]=new FeatureNode(keys[i], 1.0);
		}
		return ret;
	}
}
