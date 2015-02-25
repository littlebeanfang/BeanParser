package Malt;

import java.io.IOException;
import java.util.HashSet;
import java.util.Stack;

import DataStructure.DependencyInstance;
import IO.CONLLReader;
import IO.CONLLWriter;
import gnu.trove.TIntIntHashMap;

/**
 * based on the Nivre etc. 2009, raised a new oracle function
 * @author Bean
 *
 */
public class MaltConverter {
	public TIntIntHashMap Process(DependencyInstance di){
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
		int[] MPCNodeID=GetMPC(head_dependent);
		Stack<Integer> alpha=new Stack<Integer>();			//****
		alpha.push(0);
		Stack<Integer> beta=new Stack<Integer>();			//****
		for(int i=di.length()-1;i>0;i--){
			beta.push(i);
		}
		int order=1;
//		System.out.println(di.length()-1);
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
					alpha.remove(alpha.size()-2);
					donenodes.add(i);
					order_child.put(order++, i);
					action=true;
					System.out.print("LA+"+di.deprels[i]+" ");
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
					alpha.pop();
					donenodes.add(j);
					order_child.put(order++, j);
					action=true;
					System.out.print("RA+"+di.deprels[j]+" ");
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
				alpha.push(beta.pop());
				System.out.print("SH ");
			}
//			System.out.println("size:"+donenodes.size());
		}
		return order_child;
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
	/**
	 * assign same id of the nodes in same MPC
	 * @param head_dependent
	 */
	public int[] GetMPC(boolean[][] head_dependent){
		int[] nodesid=new int[head_dependent.length];
		for(int i=0;i<head_dependent.length;i++){
			nodesid[i]=i;
		}
		int chgcount=1;
		while(chgcount!=0){
			chgcount=0;
			for(int i=0;i<head_dependent.length;i++){
				if(i-1>=0&&head_dependent[i][nodesid[i-1]]==true&&nodesid[i-1]!=nodesid[i]){
					nodesid[i-1]=nodesid[i];
					chgcount++;
				}
				if(i+1<head_dependent.length&&head_dependent[i][nodesid[i+1]]==true&&nodesid[i+1]!=nodesid[i]){
					nodesid[i+1]=nodesid[i];
					chgcount++;
				}
			}
		}
		return nodesid;
	}
	public void Convert(String conllfile, String orderfile) throws IOException{
		CONLLReader reader=new CONLLReader();
		reader.startReading(conllfile);
		reader.ordered=false;
		DependencyInstance di=reader.getNext();
		CONLLWriter writer=new CONLLWriter(true);
		writer.startWriting(orderfile);
		int i=1;
		while(di!=null){
			System.out.print(""+i+++" ");
			TIntIntHashMap order_child=Process(di);
			System.out.println();
			writer.write(new DependencyInstance(RemoveRoot(di.forms), RemoveRoot(di.postags), RemoveRoot(di.deprels), RemoveRoot(di.heads)), order_child);
			di=reader.getNext();
		}
		writer.finishWriting();
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
}
