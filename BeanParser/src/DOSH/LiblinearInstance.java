package DOSH;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.TreeSet;

import DataStructure.FeatureVector;

public class LiblinearInstance {
	private FeatureVector xfeat;
	private int ylabel;
	public LiblinearInstance(FeatureVector x,int label){
		xfeat=x;
		ylabel=label;
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
}
