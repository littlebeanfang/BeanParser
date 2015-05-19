package DataStructure;

import mstparser.Alphabet;

import java.util.Map;

/**
 * used to store partial parse, child-head key-value pairs
 *
 * @author Wenjing
 */
public class ParseAgenda {
    public int[] heads;
    public FeatureVector fv;
    private int[] numofleftchild;
    private int[] numofrightchild;
    private StringBuilder[] rightchilds;
    private StringBuilder[] leftchilds;
    private Alphabet typealphabet;
    private int[] set; // set: disjoint-set data structure, stores the parent of each node
    private int length;
    private double score;

    public ParseAgenda(int length) {
        heads = new int[length];
        numofleftchild = new int[length];
        numofrightchild = new int[length];
        rightchilds = new StringBuilder[length];
        leftchilds = new StringBuilder[length];
        set = new int[length];
        for (int i = 0; i < length; i++) {
            set[i] = i;
            rightchilds[i] = new StringBuilder();
            leftchilds[i] = new StringBuilder();
        }
        this.length = length;
        fv = new FeatureVector();
        score = 0;
    }

    public ParseAgenda(int length, int[] heads, int[] numofleftchild, int[] numofrightchild,
                       Alphabet typealphabet, int[] set, double score) {
        this.length = length;
        this.heads = heads;
        this.numofleftchild = numofleftchild;
        this.numofrightchild = numofrightchild;
        this.typealphabet = typealphabet;
        this.set = set;
        this.score = score;
    }

    public ParseAgenda clone() {
        ParseAgenda pa = new ParseAgenda(length, heads.clone(), numofleftchild.clone(), numofrightchild.clone(),
                typealphabet, set.clone(), score);
        pa.rightchilds = new StringBuilder[length];
        pa.leftchilds = new StringBuilder[length];
        for (int i = 0; i < length; i++) {
            pa.leftchilds[i] = new StringBuilder(this.leftchilds[i].toString());
            pa.rightchilds[i] = new StringBuilder(this.rightchilds[i].toString());
        }
        pa.fv = new FeatureVector(fv.keys());
        return pa;
    }

    public void addFeatureVector(FeatureVector fv) {
        this.fv = this.fv.cat(fv);
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
//	public ParseAgenda(Alphabet alphabet){
//		this.tii=new TIntIntHashMap();
//		this.numofleftchild=new TIntIntHashMap();
//		this.numofrightchild=new TIntIntHashMap();
//		typealphabet=alphabet;
//	}

    public void AddArc(int child, int head) {
        heads[child] = head;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        //sb.append("Agenda Size:"+this.tii.size()+"\n");
        sb.append("Agenda Size:" + length + "\n");
//		for(int key:this.tii.keys()){
//			//sb.append("Child:"+key+", Head:"+this.tii.get(key)+"\n");
//			sb.append(this.tii.get(key)+"-->"+key+"\n");
//		}
        for (int i = 0; i < length; i++)
            sb.append(heads[i] + "-->" + i + "\n");
        return sb.toString();
    }

    public String toActParseTree() {
        StringBuffer sb = new StringBuffer();
        //for(int i = 1; i < tii.size(); i++) {
        for (int i = 1; i < length; i++) {
            //.append(":").append(typeAlphabet.lookupIndex(labs[i]))
            //sb.append(tii.get(i)).append("|").append(i).append(" ");
            sb.append(heads[i]).append("|").append(i).append(" ");
        }
        return sb.substring(0, sb.length() - 1);
    }

    public String toActParseTree(DependencyInstance instance) {
        StringBuffer sb = new StringBuffer();
//		for(int i = 1; i < tii.size(); i++) {
//			sb.append(tii.get(i)).append("|").append(i).append(":").append(typealphabet.lookupIndex(instance.deprels[i])).append(" ");
//		}
        for (int i = 1; i < length; i++) {
            sb.append(heads[i]).append("|").append(i).append(":").append(typealphabet.lookupIndex(instance.deprels[i])).append(" ");
        }
        return sb.substring(0, sb.length() - 1);
    }

    public void ChildProcess(int childindex, int parentindex) {
//		System.out.println("Child:"+childindex+",Parent:"+parentindex);
//		if(childindex<parentindex){
//        	//LA
//        	if(this.numofleftchild.containsKey(parentindex)){
//        		this.numofleftchild.put(parentindex, this.numofleftchild.get(parentindex)+1);
////        		System.out.println("parent:"+parentindex+",leftcount:"+this.numofleftchild.get(parentindex)+",rightcount:"+this.numofrightchild.get(parentindex));
//        	}else{
//        		this.numofleftchild.put(parentindex, 1);
////        		System.out.println("parent:"+parentindex+",leftcount:"+this.numofleftchild.get(parentindex)+",rightcount:"+this.numofrightchild.get(parentindex));
//        	}
//        	
//        	if(this.leftchilds.containsKey(parentindex)){
//        		this.leftchilds.get(parentindex).append("\t"+childindex);
////        		System.out.println(this.leftchilds.get(parentindex));
//        	}else{
//        		this.leftchilds.put(parentindex, new StringBuffer().append(childindex));
////        		System.out.println(this.leftchilds.get(parentindex));
//        	}
//        }else{
//        	//RA
//        	if(this.numofrightchild.containsKey(parentindex)){
//        		this.numofrightchild.put(parentindex, this.numofrightchild.get(parentindex)+1);
////        		System.out.println("parent:"+parentindex+",leftcount:"+this.numofleftchild.get(parentindex)+",rightcount:"+this.numofrightchild.get(parentindex));
//        	}else{
//        		this.numofrightchild.put(parentindex, 1);
////        		System.out.println("parent:"+parentindex+",leftcount:"+this.numofleftchild.get(parentindex)+",rightcount:"+this.numofrightchild.get(parentindex));
//        	}
//        	
//        	if(this.rightchilds.containsKey(parentindex)){
//        		this.rightchilds.get(parentindex).append("\t"+childindex);
////        		System.out.println(this.rightchilds.get(parentindex));
//        	}else{
//        		this.rightchilds.put(parentindex, new StringBuffer().append(childindex));
////        		System.out.println(this.rightchilds.get(parentindex));
//        	}
//        }
        if (childindex < parentindex) {
            //LA
            numofleftchild[parentindex]++;

            if (leftchilds[parentindex].length() == 0)
                leftchilds[parentindex].append(childindex);
            else
                leftchilds[parentindex].append("\t" + childindex);
        } else {
            //RA
            numofrightchild[parentindex]++;

            if (rightchilds[parentindex].length() == 0)
                rightchilds[parentindex].append(childindex);
            else
                rightchilds[parentindex].append("\t" + childindex);
        }
    }

    public void StoreOnePAInMap(Map<String, Integer> structurecount) {
        for (int i = 0; i < length; i++) {
            int leftchild = numofleftchild[i];
            int rightchild = numofrightchild[i];
            String structureString = "<" + (leftchild + rightchild) + " children, " + leftchild + " left, " + rightchild + " right>";
            //System.out.println(structureString);
            if (structurecount.containsKey(structureString)) {
                structurecount.put(structureString, structurecount.get(structureString) + 1);
            } else {
                structurecount.put(structureString, 1);
            }
        }
//
//		for(int i=0;i<tii.size();i++){
//			int leftchild=0,rightchild=0;
//			if(numofleftchild.containsKey(i)){
//				leftchild=numofleftchild.get(i);
//			}
//			if(numofrightchild.containsKey(i)){
//				rightchild=numofrightchild.get(i);
//			}
//			String structureString="<"+(leftchild+rightchild)+" children, "+leftchild+" left, "+rightchild+" right>";
//			//System.out.println(structureString);
//			if(structurecount.containsKey(structureString)){
//				structurecount.put(structureString, structurecount.get(structureString)+1);
//			}else{
//				structurecount.put(structureString, 1);
//			}
//		}
    }

    public final int FindRoot(int node) {
        if (set[node] != node) set[node] = this.FindRoot(set[node]);
        return set[node];
    }

    public final void UpdateSet(int child, int head) {
        set[child] = head;
    }

    public String getLeftChilds(int index) {
        return leftchilds[index].toString();
    }

    public String getRightChilds(int index) {
        return rightchilds[index].toString();
    }
}
