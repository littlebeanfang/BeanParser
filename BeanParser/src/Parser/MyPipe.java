package Parser;

import DataStructure.DependencyInstance;
import DataStructure.Feature;
import DataStructure.FeatureVector;
import DataStructure.Parameters;
import DataStructure.ParseAgenda;
import DataStructure.ParserOptions;
import IO.CONLLReader;
import gnu.trove.TIntArrayList;
import gnu.trove.TIntIntHashMap;
import gnu.trove.TIntIntIterator;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.ListIterator;

import javax.lang.model.type.TypeVisitor;

public class MyPipe extends DependencyPipe {
	//TODO: change this to decide whether predict label
	boolean parsewithrelation=true;

	// -----------------------------Initialize---------------------------------------------
	public MyPipe(ParserOptions options) throws IOException {
		super(options);
		// TODO Auto-generated constructor stub
	}

	// -----------------------------Functions for
	// Parsing-------------------------------------
	public void extractFeatures(DependencyInstance instance, int childindex,
			int parentindex, ParseAgenda pa, FeatureVector fv) { // given an
		// unparsed
		// instance,extract
		// features
		// from it
		// boolean labeled;
		boolean leftToRight = (childindex > parentindex);
		int small = leftToRight ? parentindex : childindex;
		int large = leftToRight ? childindex : parentindex;
		addCoreFeatures(instance, small, large, leftToRight, fv);

		addTwoOrderFeatures(instance, parentindex, childindex, pa, fv);
		// addLabeledFeatures(instance, childindex, type, attR, childFeatures,
		// fv);
	}
	
	public Object[] extractParseFeatures(DependencyInstance instance, int childindex,
			int parentindex, ParseAgenda pa, Parameters param) { // given an
		// unparsed
		// instance,extract
		// features
		// from it
		// boolean labeled;
		FeatureVector fv=new FeatureVector();
		boolean leftToRight = (childindex > parentindex);
		int small = leftToRight ? parentindex : childindex;
		int large = leftToRight ? childindex : parentindex;
		addCoreFeatures(instance, small, large, leftToRight, fv);

		addTwoOrderFeatures(instance, parentindex, childindex, pa, fv);
		//System.out.println("before addlabeledfeature:"+fv.size());
		// addLabeledFeatures(instance, childindex, type, attR, childFeatures,
		// fv);
		//TODO add labeled feature here
		Object[] featret=AddMyParseLabeledFeatures(instance, childindex, parentindex,  param);
		FeatureVector labelfv=(FeatureVector)featret[0];
		fv=fv.cat(labelfv);
		Object[] ret=new Object[3];
		ret[0]=fv;
		ret[1]=labelfv;
		ret[2]=featret[1];
		return ret;
	}
	
	public FeatureVector[] extractTrainFeatures(DependencyInstance instance, int childindex,
			int parentindex, ParseAgenda pa) { // given an
		// unparsed
		// instance,extract
		// features
		// from it
		// boolean labeled;
		FeatureVector fv=new FeatureVector();
		boolean leftToRight = (childindex > parentindex);
		int small = leftToRight ? parentindex : childindex;
		int large = leftToRight ? childindex : parentindex;
		addCoreFeatures(instance, small, large, leftToRight, fv);

		addTwoOrderFeatures(instance, parentindex, childindex, pa, fv);
		// addLabeledFeatures(instance, childindex, type, attR, childFeatures,
		// fv);
		//TODO add labeled feature here
//		System.out.println("Before extract labeled train feat:"+fv.size());
		FeatureVector labelfv=AddMyTrainLabeledFeatures(instance, childindex, parentindex, instance.deprels[childindex]);
//		System.out.println("After extract labeled train feat:"+fv.size());
		FeatureVector ret[]=new FeatureVector[2];
		ret[0]=fv.cat(labelfv);
		ret[1]=labelfv;
		return ret;
	}
	
	
	public Object[] AddMyParseLabeledFeatures(DependencyInstance instance, int child, int parent, Parameters param){
		/**
		 * select the label with highest score
		 */
		//System.out.println("inside add feat:"+fv.size());
		FeatureVector bestlabelfv=new FeatureVector();
		double bestlabelscore=Double.NEGATIVE_INFINITY;
		String besttypeString="";
		//step 1: generate fv and score array, length is the type length
		int typenum=this.typeAlphabet.size();
//		FeatureVector fvarray[]=new FeatureVector[typenum];
//		double scorearray[]=new double[typenum];
		//step 2: fill fv and score array
		for(int i=0;i<typenum;i++){
			FeatureVector fvtemp=new FeatureVector();
			double scoretemp;
			String type=types[i];
			//System.out.println("type["+i+"]:"+type);
			boolean attR=child<parent?false:true;
//			System.out.println("type:"+type);//for debugging
			this.addLabeledFeatures(instance, child, type, attR, true, fvtemp);
			this.addLabeledFeatures(instance, parent, type, attR, false, fvtemp);
			//System.out.println("fvtemp size:"+fvtemp.size());
			//scoretemp=param.getScore(new FeatureVector(fvtemp2));
			scoretemp=fvtemp.getScore(param.parameters);
//			System.out.println("+++print fv&param");
//			this.PrintFVScore(fvtemp2, param.parameters);
//			System.out.println("+++end");
			//System.out.println("fvtemp size:"+fvtemp.size());
//			System.out.println("fvtemp2 size:"+fvtemp2.size());
//			System.out.println("score temp:"+scoretemp+",param size:"+param.parameters.length);
			//System.out.println("score:"+scoretemp+",rel:"+type+",child index:"+child+"parent index:"+parent);
			if(scoretemp>=bestlabelscore){
				bestlabelscore=scoretemp;
				bestlabelfv=new FeatureVector(fvtemp.keys());
//				System.out.println("fvtemp size:"+fvtemp.size());
				//System.out.println("Bestlabelfv size:"+bestlabelfv.size());
				besttypeString=types[i];
//				System.out.println("test:"+besttypeString);
			}
		}
		//step 3: get the highest fv and change in param fv
		//!!!no use: cannot change fv at all
//		System.out.println("fv before cat:"+fv.size());
		//System.out.println("bestfv:"+bestlabelfv.size()+",bestscore:"+bestlabelscore);
//		System.out.println("fv after cat:"+fv.size());
		
		//step 4: fill label in instance 
		//instance.deprels[child]=besttypeString;  //! need not assign here!!
		//System.out.println("child:"+child+"parent:"+parent+",Best relation:"+besttypeString);
		//System.out.println("instance in addmyparselabeledfeature:"+instance.deprels[child]+"child:"+child+",parent:"+parent);
		//return fv;
		Object[] ret=new Object[2];
		ret[0]=bestlabelfv;
		ret[1]=besttypeString;
		return ret;
	}
	public void PrintFVScore(FeatureVector fv, double[] parameters){
		if (null != fv.subfv1) {
		   PrintFVScore(fv.subfv1,parameters);

		    if (null != fv.subfv2) {
			
		    	PrintFVScore(fv.subfv2,parameters);
		    }
		}

		ListIterator it = fv.listIterator();

		    while (it.hasNext()) {
			Feature f = (Feature)it.next();
			System.out.println("FV index:"+f.index+",param:"+parameters[f.index]);
		    }
	}
	
	public FeatureVector AddMyTrainLabeledFeatures(DependencyInstance instance, int child, int parent,String relation){
		/**
		 * generate vector with labeled data
		 */
		
		FeatureVector fvtemp=new FeatureVector();
		boolean attR=child<parent?false:true;
		this.addLabeledFeatures(instance, child, relation, attR, true, fvtemp);
		this.addLabeledFeatures(instance, parent, relation, attR, false, fvtemp);
		//System.out.println("train fvtemp:"+fvtemp.size());
//		System.out.println("fv before add temp:"+fv.size());
		//fv=fv.cat(fvtemp);
//		System.out.println("fv after add temp:"+fv.size());
		//System.out.println("With the relation:"+relation);
		return fvtemp;
	}
	public void addLabeledFeatures(DependencyInstance instance, int word,
			String type, boolean attR, boolean childFeatures, FeatureVector fv) {
//		System.out.println("labeled in addLabeledFeatures:"+labeled);
//		System.out.println("addLabeledFeatures start:"+fv.size());
		String[] forms = instance.forms;
		String[] pos = instance.postags;

		String att = "";
		if (attR)
			att = "RA";
		else
			att = "LA";

		att += "&" + childFeatures;

		String w = forms[word];
		String wP = pos[word];

		String wPm1 = word > 0 ? pos[word - 1] : "STR";
		String wPp1 = word < pos.length - 1 ? pos[word + 1] : "END";

		add("NTS1=" + type + "&" + att, fv);
//		System.out.println("test label feat num:"+dataAlphabet.lookupIndex("NTS1=" + type + "&" + att)+",word:"+word+",type:"+type);
		add("ANTS1=" + type, fv);
//		System.out.println("addLabeledFeatures 2:"+fv.size()+" keys:"+Arrays.toString(fv.keys()));
		for (int i = 0; i < 2; i++) {
			String suff = i < 1 ? "&" + att : "";
			suff = "&" + type + suff;

			add("NTH=" + w + " " + wP + suff, fv);
//			System.out.println("addLabeledFeatures 3:"+fv.size()+" keys:"+Arrays.toString(fv.keys()));
			add("NTI=" + wP + suff, fv);
//			System.out.println("addLabeledFeatures 4:"+fv.size()+" keys:"+Arrays.toString(fv.keys()));
			add("NTIA=" + wPm1 + " " + wP + suff, fv);
//			System.out.println("addLabeledFeatures 5:"+fv.size()+" keys:"+Arrays.toString(fv.keys()));
			add("NTIB=" + wP + " " + wPp1 + suff, fv);
//			System.out.println("addLabeledFeatures 6:"+fv.size()+" keys:"+Arrays.toString(fv.keys()));
			add("NTIC=" + wPm1 + " " + wP + " " + wPp1 + suff, fv);
//			System.out.println("addLabeledFeatures 7:"+fv.size()+" keys:"+Arrays.toString(fv.keys()));
			add("NTJ=" + w + suff, fv); // this
//			System.out.println("addLabeledFeatures 8:"+fv.size()+" keys:"+Arrays.toString(fv.keys()));
		}
//		System.out.println("addLabeledFeatures end:"+fv.size());
	}

	private final void addTwoOrderFeatures(DependencyInstance instance,
			int parentindex, int childindex, ParseAgenda pa, FeatureVector fv) {
		// System.out.println(childindex + "\t" + parentindex);

		if (pa.tii.containsKey(parentindex)) { // this shows that the parent
			// candidate already has head,so we
			// can add grandparent-parent-child
			// feature

		}

		if (pa.tii.containsValue(childindex)) { // this shows that the child
			// candidate is already used as
			// another word's parent

		}

		// for (TIntIntIterator iter = pa.tii.iterator(); iter.hasNext();){
		// iter.advance();
		// System.out.println(iter.key() + "\t" + iter.value());
		// }

		if (pa.tii.containsValue(parentindex)) { // this shows that the parent
			// candidate has already been
			// another word's parent
			for (TIntIntIterator iter = pa.tii.iterator(); iter.hasNext();) {
				iter.advance();
				int existing_child = iter.key();
				int parent = iter.value();
				if (parent == parentindex) {
					addTripFeatures(instance, childindex, existing_child,
							parentindex, fv); // parent can be in any place
					addSiblingFeatures(instance, childindex, existing_child,
							false, fv);
					addSiblingFeatures(instance, childindex, existing_child,
							true, fv);
				}
			}
		}
	}

	;

	private final void addSiblingFeatures(DependencyInstance instance, int ch1,
			int ch2, boolean isST, FeatureVector fv) {

		String[] forms = instance.forms;
		String[] pos = instance.postags;

		// ch1 is always the closes to par
		String dir = ch1 > ch2 ? "RA" : "LA";

		String ch1_pos = isST ? "STPOS" : pos[ch1];
		String ch2_pos = pos[ch2];
		String ch1_word = isST ? "STWRD" : forms[ch1];
		String ch2_word = forms[ch2];

		add("CH_PAIR=" + ch1_pos + "_" + ch2_pos + "_" + dir, 1.0, fv);
		add("CH_WPAIR=" + ch1_word + "_" + ch2_word + "_" + dir, 1.0, fv);
		add("CH_WPAIRA=" + ch1_word + "_" + ch2_pos + "_" + dir, 1.0, fv);
		add("CH_WPAIRB=" + ch1_pos + "_" + ch2_word + "_" + dir, 1.0, fv);
		add("ACH_PAIR=" + ch1_pos + "_" + ch2_pos, 1.0, fv);
		add("ACH_WPAIR=" + ch1_word + "_" + ch2_word, 1.0, fv);
		add("ACH_WPAIRA=" + ch1_word + "_" + ch2_pos, 1.0, fv);
		add("ACH_WPAIRB=" + ch1_pos + "_" + ch2_word, 1.0, fv);

		int dist = Math.max(ch1, ch2) - Math.min(ch1, ch2);
		String distBool = "0";
		if (dist > 1)
			distBool = "1";
		if (dist > 2)
			distBool = "2";
		if (dist > 3)
			distBool = "3";
		if (dist > 4)
			distBool = "4";
		if (dist > 5)
			distBool = "5";
		if (dist > 10)
			distBool = "10";
		add("SIB_PAIR_DIST=" + distBool + "_" + dir, 1.0, fv);
		add("ASIB_PAIR_DIST=" + distBool, 1.0, fv);
		add("CH_PAIR_DIST=" + ch1_pos + "_" + ch2_pos + "_" + distBool + "_"
				+ dir, 1.0, fv);
		add("ACH_PAIR_DIST=" + ch1_pos + "_" + ch2_pos + "_" + distBool, 1.0,
				fv);
	}

	private final void addTripFeatures(DependencyInstance instance, int par,
			int ch1, int ch2, FeatureVector fv) {

		String[] pos = instance.postags;

		// ch1 is always the closest to par
		String dir = par > ch2 ? "RA" : "LA";

		String par_pos = pos[par];
		String ch1_pos = ch1 == par ? "STPOS" : pos[ch1];
		String ch2_pos = pos[ch2];

		String pTrip = par_pos + "_" + ch1_pos + "_" + ch2_pos;
		add("POS_TRIP=" + pTrip + "_" + dir, 1.0, fv);
		add("APOS_TRIP=" + pTrip, 1.0, fv);

	}

	/*
	 * public void AddNewFeature(DependencyInstance inst, int childindex, int
	 * parentindex, ParseAgenda pa, FeatureVector fv) {
	 * 
	 * }
	 */
	// -----------------------------Functions for
	// Training-------------------------------------
	public int[] createInstances(String file) throws IOException {

		createAlphabet(file);

		System.out.println("Num Features: " + dataAlphabet.size());

		labeled = depReader.startReading(file); // There is a question why some
												// funcions are abstract??????

		TIntArrayList lengths = new TIntArrayList();

		/*
		 * ObjectOutputStream out = options.createForest ? new
		 * ObjectOutputStream(new FileOutputStream(featFileName)) : null;
		 */
		DependencyInstance instance = depReader.getNext();
		int num1 = 0;

		System.out.println("Creating Feature Vector Instances: ");
		while (instance != null) {
			System.out.print(num1 + " ");

			instance.setFeatureVector(createFeatureVector(instance));

			String[] labs = instance.deprels;
			int[] heads = instance.heads;

			StringBuffer spans = new StringBuffer(heads.length * 5);
			for (int i = 1; i < heads.length; i++) {
				spans.append(heads[i]).append("|").append(i).append(":")
						.append(typeAlphabet.lookupIndex(labs[i])).append(" ");
			}
			instance.actParseTree = spans.substring(0, spans.length() - 1);

			lengths.add(instance.length());

			/*
			 * if (options.createForest) writeInstance(instance, out);
			 */
			// instance = null;
			instance = depReader.getNext();

			num1++;
		}

		System.out.println();

		closeAlphabets();

		/*
		 * if (options.createForest) out.close();
		 */
		return lengths.toNativeArray();

	}

	public FeatureVector[] extractFeatureVector(DependencyInstance instance) {
		FeatureVector[] ret=new FeatureVector[2];
		FeatureVector labelfv=new FeatureVector();
		final int instanceLength = instance.length();

		String[] labs = instance.deprels;
		int[] heads = instance.heads;
		TIntIntHashMap ordermap = instance.orders;
		FeatureVector fv = new FeatureVector();
		ParseAgenda pa = new ParseAgenda();
		for (int orderindex = 1; orderindex < instance.length(); orderindex++) {
			// skip root node
			int parseindex = ordermap.get(orderindex);
			int parsehead = heads[parseindex];
			if(parsewithrelation){
				FeatureVector[] retfv=extractTrainFeatures(instance, parseindex, parsehead, pa);
				fv=fv.cat(retfv[0]);
				labelfv=labelfv.cat(retfv[1]);
			}else{
				extractFeatures(instance, parseindex, parsehead, pa, fv);
			}
			pa.AddArc(parseindex, parsehead);
		}
		pa.AddArc(0, -1);// add root
		ret[0]=fv;
		ret[1]=labelfv;
		return ret;
	}

	public final int createMyAlphabet(String file) throws IOException {

		System.out.print("Creating Alphabet ... ");
		CONLLReader reader = new CONLLReader();
		labeled = reader.startReading(System.getenv("CODEDATA")
				+ File.separator + file);//bean: here changed the label
		int numInstances = 0;
		DependencyInstance instance = reader.getNext();
		while (instance != null) {
			numInstances++;
			System.out.print("sent " + numInstances + ",");
			String[] labs = instance.deprels;
			for (int i = 0; i < labs.length; i++) {
				typeAlphabet.lookupIndex(labs[i]);
			}
			extractFeatureVector(instance);
			instance = reader.getNext();
		}
		closeAlphabets();//bean: here get the type
		System.out.println("Done.");
		return numInstances;
	}

}
