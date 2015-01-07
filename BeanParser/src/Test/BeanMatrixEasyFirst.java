package Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import gnu.trove.TIntIntHashMap;

public class BeanMatrixEasyFirst {
	public double[][] ReadMatrixFromFile(String singlescorefile) throws IOException{
		System.out.println("matrix file:"+singlescorefile);
		BufferedReader reader=new BufferedReader(new FileReader(singlescorefile));
		String line=reader.readLine();
		String firstline[]=line.split("\t");
		int length=firstline.length;
		double[][] ret=new double[length][length];
		ret[0][0]=Double.parseDouble(firstline[0]);
		for(int i=1;i<length;i++){
			ret[0][i]=Double.parseDouble(firstline[i]);
			line=reader.readLine();
			String columns[]=line.split("\t");
			for(int j=0;j<length;j++){
				ret[i][j]=Double.parseDouble(columns[j]);
			}
		}
		return ret;
	}
	public double[][] DeleteRootInMatrix(double[][] matrix){
		int length=matrix.length;
		double[][] ret=new double[length-1][length-1];
//		System.out.println(length);
		for(int i=0;i<length-1;i++){
			for(int j=0;j<length-1;j++){
//				System.out.println("b");
				ret[i][j]=matrix[i+1][j+1];
//				System.out.print(ret[i][j]);
			}
//			System.out.println();
		}
		return ret;
	}
	public void GetEasyFirstOrder(double[][] arcscore,TIntIntHashMap map){
		//arcscore must not containing root 
		int length=arcscore.length;
		//init node
		int node[]=new int[length];
		for(int i=0;i<length;i++){
			node[i]=i;
		}
		double maxscore;
		int head;
		int child;
		for(int left=length;left>1;left--){
			//left length of nodes
			//init with node[0]->node[1]
			maxscore=arcscore[node[0]][node[1]];
//			System.out.println("arc:"+node[0]+"->"+node[1]);
			head=0;
			child=1;
			for(int index=1;index<left-1;index++){
				//node[index]->node[index-1]
				double temp=arcscore[node[index]][node[index-1]];
//				System.out.println("arc:"+node[index]+"->"+node[index-1]);
				if(temp>maxscore){
					maxscore=temp;
					head=index;
					child=index-1;
				}
				//node[index]->node[index+1]
				temp=arcscore[node[index]][node[index+1]];
//				System.out.println("arc:"+node[index]+"->"+node[index+1]);
				if(temp>maxscore){
					maxscore=temp;
					head=index;
					child=index+1;
				}
			}
			//node[left-1]->node[left-2]
//			System.out.println("left"+left);
			double temp=arcscore[node[left-1]][node[left-2]];
			
//			System.out.println("arc:"+node[left-1]+"->"+node[left-2]);
			if(temp>maxscore){
				maxscore=temp;
				head=left-1;
				child=left-2;
			}
			//if contain root score
			//map.put(length-left+1, node[child]);
			map.put(length-left+1, node[child]+1);
//			System.out.println(node[head]+"->"+node[child]);
			//update node
			for(int i=child;i<left-1;i++){
				node[i]=node[i+1];
			}
		}
		//if contain root score
		//map.put(length, node[0]);
		map.put(length, node[0]+1);
	}
	public void tranverseTIntIntHashMap(TIntIntHashMap map){
		int len=map.size();
		System.out.println("len"+len);
		for(int i=1;i<=len;i++){
			System.out.println(map.get(i));
		}
	}
	public void PrintMatrix(double[][] matrix){
		for(int i=0;i<matrix.length;i++){
			for(int j=0;j<matrix.length;j++){
				System.out.print(matrix[i][j]+"\t");
			}
			System.out.println();
		}
	}
	public static void main(String args[]) throws IOException{
		BeanMatrixEasyFirst test=new BeanMatrixEasyFirst();

		double[][] matrix=test.DeleteRootInMatrix(test.ReadMatrixFromFile(System.getenv("CODEDATA")+File.separator+"God_2.score"));
		test.PrintMatrix(matrix);
		TIntIntHashMap map=new TIntIntHashMap();
		test.GetEasyFirstOrder(matrix, map);
		test.tranverseTIntIntHashMap(map);
//		double[][] matrix2=test.DeleteRootInMatrix(test.ReadMatrixFromFile(System.getenv("CODEDATA")+File.separator+"God_3.score"));
//		TIntIntHashMap map2=new TIntIntHashMap();
//		test.GetEasyFirstOrder(matrix2, map2);
//		test.tranverseTIntIntHashMap(map2);
//		double[][] matrix3=test.DeleteRootInMatrix(test.ReadMatrixFromFile(System.getenv("CODEDATA")+File.separator+"God_5.score"));
//		TIntIntHashMap map3=new TIntIntHashMap();
//		test.GetEasyFirstOrder(matrix3, map3);
//		test.tranverseTIntIntHashMap(map3);
//		double[][] matrix4=test.DeleteRootInMatrix(test.ReadMatrixFromFile(System.getenv("CODEDATA")+File.separator+"MST_2.score"));
//		TIntIntHashMap map4=new TIntIntHashMap();
//		test.GetEasyFirstOrder(matrix4, map4);
//		test.tranverseTIntIntHashMap(map4);
//		double[][] matrix5=test.DeleteRootInMatrix(test.ReadMatrixFromFile(System.getenv("CODEDATA")+File.separator+"MST_4.score"));
//		TIntIntHashMap map5=new TIntIntHashMap();
//		test.GetEasyFirstOrder(matrix5, map5);
//		test.tranverseTIntIntHashMap(map5);
//		double[][] matrix6=test.DeleteRootInMatrix(test.ReadMatrixFromFile(System.getenv("CODEDATA")+File.separator+"MST_6.score"));
//		TIntIntHashMap map6=new TIntIntHashMap();
//		test.GetEasyFirstOrder(matrix6, map6);
//		test.tranverseTIntIntHashMap(map6);
	}
}
