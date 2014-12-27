package DataStructure;

import gnu.trove.TIntIntHashMap;
import mstparser.Alphabet;

import java.util.HashMap;
import java.util.Map;

/**
 * used to store partial parse, child-head key-value pairs
 *
 * @author Wenjing
 */
public class ParseAgenda {
    public TIntIntHashMap tii;
    public TIntIntHashMap numofleftchild;
    public TIntIntHashMap numofrightchild;
    public Map<Integer, StringBuffer> rightchilds = new HashMap<Integer, StringBuffer>();//split by \t
    public Map<Integer, StringBuffer> leftchilds = new HashMap<Integer, StringBuffer>(); //StringBuffer is unordered
    private Alphabet typealphabet;

    public ParseAgenda() {
        this.tii = new TIntIntHashMap();
        this.numofleftchild = new TIntIntHashMap();
        this.numofrightchild = new TIntIntHashMap();
    }

    public ParseAgenda(Alphabet alphabet) {
        this.tii = new TIntIntHashMap();
        this.numofleftchild = new TIntIntHashMap();
        this.numofrightchild = new TIntIntHashMap();
        typealphabet = alphabet;
    }

    public void AddArc(int child, int head) {
        this.tii.put(child, head);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Agenda Size:" + this.tii.size() + "\n");
        for (int key : this.tii.keys()) {
            //sb.append("Child:"+key+", Head:"+this.tii.get(key)+"\n");
            sb.append(this.tii.get(key) + "-->" + key + "\n");
        }
        return sb.toString();
    }

    public String toActParseTree() {
        StringBuffer sb = new StringBuffer();
        for (int i = 1; i < tii.size(); i++) {
            //.append(":").append(typeAlphabet.lookupIndex(labs[i]))
            sb.append(tii.get(i)).append("|").append(i).append(" ");
        }
        return sb.substring(0, sb.length() - 1);
    }

    public String toActParseTree(DependencyInstance instance) {
        StringBuffer sb = new StringBuffer();
        for (int i = 1; i < tii.size(); i++) {
            sb.append(tii.get(i)).append("|").append(i).append(":").append(typealphabet.lookupIndex(instance.deprels[i])).append(" ");
        }
        return sb.substring(0, sb.length() - 1);
    }

    public void ChildProcess(int childindex, int parentindex) {
//		System.out.println("Child:"+childindex+",Parent:"+parentindex);
        if (childindex < parentindex) {
            //LA
            if (this.numofleftchild.containsKey(parentindex)) {
                this.numofleftchild.put(parentindex, this.numofleftchild.get(parentindex) + 1);
//        		System.out.println("parent:"+parentindex+",leftcount:"+this.numofleftchild.get(parentindex)+",rightcount:"+this.numofrightchild.get(parentindex));
            } else {
                this.numofleftchild.put(parentindex, 1);
//        		System.out.println("parent:"+parentindex+",leftcount:"+this.numofleftchild.get(parentindex)+",rightcount:"+this.numofrightchild.get(parentindex));
            }

            if (this.leftchilds.containsKey(parentindex)) {
                this.leftchilds.get(parentindex).append("\t" + childindex);
//        		System.out.println(this.leftchilds.get(parentindex));
            } else {
                this.leftchilds.put(parentindex, new StringBuffer().append(childindex));
//        		System.out.println(this.leftchilds.get(parentindex));
            }
        } else {
            //RA
            if (this.numofrightchild.containsKey(parentindex)) {
                this.numofrightchild.put(parentindex, this.numofrightchild.get(parentindex) + 1);
//        		System.out.println("parent:"+parentindex+",leftcount:"+this.numofleftchild.get(parentindex)+",rightcount:"+this.numofrightchild.get(parentindex));
            } else {
                this.numofrightchild.put(parentindex, 1);
//        		System.out.println("parent:"+parentindex+",leftcount:"+this.numofleftchild.get(parentindex)+",rightcount:"+this.numofrightchild.get(parentindex));
            }

            if (this.rightchilds.containsKey(parentindex)) {
                this.rightchilds.get(parentindex).append("\t" + childindex);
//        		System.out.println(this.rightchilds.get(parentindex));
            } else {
                this.rightchilds.put(parentindex, new StringBuffer().append(childindex));
//        		System.out.println(this.rightchilds.get(parentindex));
            }
        }
    }

    public void StoreOnePAInMap(Map<String, Integer> structurecount) {
        for (int i = 0; i < tii.size(); i++) {
            int leftchild = 0, rightchild = 0;
            if (numofleftchild.containsKey(i)) {
                leftchild = numofleftchild.get(i);
            }
            if (numofrightchild.containsKey(i)) {
                rightchild = numofrightchild.get(i);
            }
            String structureString = "<" + (leftchild + rightchild) + " children, " + leftchild + " left, " + rightchild + " right>";
            //System.out.println(structureString);
            if (structurecount.containsKey(structureString)) {
                structurecount.put(structureString, structurecount.get(structureString) + 1);
            } else {
                structurecount.put(structureString, 1);
            }
        }
    }
}
