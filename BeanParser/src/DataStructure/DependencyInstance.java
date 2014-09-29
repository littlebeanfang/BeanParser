package DataStructure;

import java.awt.datatransfer.StringSelection;

public class DependencyInstance {
	public String forms[];
	public String poss[];
	public int heads[];
	public String deprels[];
	public int orders[];
	private boolean labeled=false;
	private boolean ordered=false;
	
	
	public DependencyInstance(String[] forms, String[] poss){
		this.forms=forms;
		this.poss=poss;
	}
	public DependencyInstance(String[] forms, String[] poss, int[] heads, String[] deprels){
		this(forms,poss);
		this.heads=heads;
		this.deprels=deprels;
		this.labeled=true;
	}
	public DependencyInstance(String[] forms, String[] poss, int[] heads, String[] deprels,int[] orders){
		this(forms, poss,heads,deprels);
		this.orders=orders;
		this.ordered=true;
	}
}
