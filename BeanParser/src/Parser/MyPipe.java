package Parser;

import gnu.trove.TIntIntIterator;

import java.io.IOException;

import DataStructure.DependencyInstance;
import DataStructure.FeatureVector;
import DataStructure.ParseAgenda;
import DataStructure.ParserOptions;

public class MyPipe extends DependencyPipe {
	public MyPipe(ParserOptions options) throws IOException {
		super(options);
		// TODO Auto-generated constructor stub
	}

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

	}

	private final void addTwoOrderFeatures(DependencyInstance instance,
			int parentindex, int childindex, ParseAgenda pa, FeatureVector fv) {
		//System.out.println(childindex + "\t" + parentindex);
		
		if (pa.tii.containsKey(parentindex)) { // this shows that the parent
											// candidate already has head,so we
											// can add grandparent-parent-child
											// feature

		}

		if (pa.tii.containsValue(childindex)) { // this shows that the child
											// candidate is already used as
											// another word's parent

		}
		
        //for (TIntIntIterator iter = pa.tii.iterator(); iter.hasNext();){
        //	iter.advance();
        //	System.out.println(iter.key() + "\t" + iter.value());
        //}
        
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
	};

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
	public void AddNewFeature(DependencyInstance inst, int childindex,
			int parentindex, ParseAgenda pa, FeatureVector fv) {

	}
	*/
}
