package Parser;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import gnu.trove.TIntIntHashMap;
import DataStructure.DependencyInstance;
import DataStructure.FeatureVector;
import DataStructure.Parameters;
import DataStructure.ParseAgenda;
import IO.CONLLReader;
import IO.CONLLWriter;
import IO.WriteInstanceWithOrder;
import mstparser.Alphabet;

/**
 * this class is used to train a order predictor, using online training method
 * MIRA
 * 
 * @author Bean
 *
 */
public class MIRAOrderPredictor {
	public Alphabet orderfeat;
	public Parameters param;

	public final void add(String feat, FeatureVector fv, double value) {
		int num = orderfeat.lookupIndex(feat);
		if (num >= 0)
			fv.add(num, value);
	}

	public void loadmodel(String modelfile) throws IOException,
			ClassNotFoundException {
		ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(
				new FileInputStream(modelfile)));
		double[] parameters = (double[]) in.readObject();
		param = new Parameters(parameters);
		orderfeat = (Alphabet) in.readObject();
		System.out.println("Model loaded!");
		in.close();
		orderfeat.stopGrowth();
	}

	public void savemodel(String modelfile) throws IOException {
		ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(
				modelfile)));
		out.writeObject(param.parameters);
		out.writeObject(orderfeat);
		System.out.println("Model saved!");
		out.close();
	}

	public Object[] PredictOrderByCurrentParam(DependencyInstance di) {
		/*
		 * predict order by current param and fill the feature vector 1. select
		 * delete node
		 */
		Object[] ret = new Object[2];
		FeatureVector fvforsent = new FeatureVector();
		TIntIntHashMap order_child = new TIntIntHashMap();

		int nodelength = di.length()-1;// valid length
		int node[] = new int[di.length() + 2];// in order to get position
												// i-1,i+1 and i+2
		node[0] = -1;
		node[nodelength + 1] = -1;
		node[nodelength + 2] = -1;
		for (int i = 1; i <= nodelength; i++) {
			node[i] = i;
		}
		
		while (nodelength > 0) {
			// predict one node from node[1]~node[length]
			FeatureVector fvmax = new FeatureVector();
			int context[] = GetRelatedIndexs(node, 1);
//			System.out.println("indexL:"+context[0]+",index:"+context[1]+"indexR:"+context[2]+"indexRR:"+context[3]);
			ExtractFeature(di.postags, di.forms, context, fvmax, nodelength,1);
			double scoremax = fvmax.getScore(this.param.parameters);
			int selectindex = node[1];// index in sentence
			int selecti = 1;// index in node
			for (int i = 2; i <= nodelength; i++) {
				FeatureVector fvtemp = new FeatureVector();
				context = GetRelatedIndexs(node, i);
				ExtractFeature(di.postags, di.forms, context, fvtemp,
						nodelength,i);
				double scoretemp = fvtemp.getScore(param.parameters);
				if (scoretemp > scoremax) {
					scoremax = scoretemp;
					fvmax = fvtemp;
					selectindex = node[i];
					selecti = i;
				}
			}

			// store best fv and index in fvforsent and order_child
//			System.out.println(fvmax.size());
			fvforsent = fvforsent.cat(fvmax);
//			System.out.println("order:"+(di.length() - nodelength)+",predict child:"+selectindex);
			order_child.put(di.length() - nodelength, selectindex);
			// delete the node in node[] and subtract node length
			for (int i = selecti; i <= nodelength; i++) {
				node[i] = node[i + 1];
			}
			nodelength--;
		}

		ret[0] = fvforsent;
//		System.out.println(fvforsent.size());
		ret[1] = order_child;
		return ret;
	}

	public int[] GetRelatedIndexs(int[] node, int indexinnode) {
		// return: context in node, and index in sentence
		// positions: i-1,i,i+1,i+2
		int[] ret = new int[4];
		ret[0] = node[indexinnode - 1];
		ret[1] = node[indexinnode];
		ret[2] = node[indexinnode + 1];
		ret[3] = node[indexinnode + 2];
		return ret;
	}
	public void tranverseTIntIntMap(TIntIntHashMap map){
		System.out.println("len:"+map.size());
		for(int i=1;i<=map.size();i++){
			System.out.println("order:"+i+",child:"+map.get(i));
		}
	}
	public void ExtractFeatureVectorByOrder(DependencyInstance di,
			TIntIntHashMap order_child, FeatureVector fv) {
		/*
		 * according to the order loop: +get the feature vector for current
		 * processing index +delete the process index
		 */
//		tranverseTIntIntMap(order_child);
		int nodelength = di.length()-1;// valid length
		int node[] = new int[nodelength + 3];// in order to get position
												// i-1,i+1 and i+2
		node[0] = -1;
		node[nodelength + 1] = -1;
		node[nodelength + 2] = -1;
		for (int i = 1; i <= nodelength; i++) {
			node[i] = i;
		}
		while (nodelength > 0) {
			// extract feature of one node
			int childindexinsentence = order_child.get(di.length() 
					- nodelength);
//			System.out.println("child index:"+childindexinsentence+",order:"+(di.length()
//					- nodelength));
			int[] context = GetRelatedIndexByValue(node, childindexinsentence);
			ExtractFeature(di.postags, di.forms, context, fv, nodelength,context[4]);
			// delete the node in node[] and subtract node length
			for (int i = context[4]; i <= nodelength; i++) {
				node[i] = node[i + 1];
			}
			nodelength--;
		}
	}

	public int[] GetRelatedIndexByValue(int node[], int childindex) {
		int[] ret = new int[5];
		boolean find = false;
		for (int i = 1; i < node.length && !find; i++) {
			if (node[i] == childindex) {
				// find child in node
				ret[0] = node[i - 1];
				ret[1] = node[i];
				ret[2] = node[i + 1];
				ret[3] = node[i + 2];
				ret[4] = i;// additional return: delete node index in node[]
				find = true;
			}
		}
		if (!find) {
			System.out.println("Bug: not find child !");
		}
		return ret;
	}

	public void ExtractFeature(String[] poss, String[] forms, int context[],
			FeatureVector fv, int nodelength, int positioninnode) {
		/*
		 * 1. find index in node 2. extracts linear feature 3. operate node[]
		 * context[] store context in node(index in sentence): context[0]:i-1,
		 * context[1]:i, context[2]:i+1, context[3]:i+2
		 * fv.value=(double)nodelength/(pos.length-1)
		 */
		int indexL = context[0];
		int index = context[1];
		int indexR = context[2];
		int indexRR = context[3];
//		System.out.println("indexL:"+indexL+", index:"+index+", indexR:"+indexR+",indexRR:"+indexRR);
//		System.out.println("index:"+index+",form:"+forms[index]+",positioninnode:"+positioninnode);
		AddPositionFeature(positioninnode, fv,forms.length-1);
		// feat: i
		if (index != -1) {
			AddSelfFeature(forms, poss, index, fv, nodelength);
		} else {
			System.out.println("Bug: index cann't be -1.");
		}
		// feat: i, i-1
		if (index != -1 && indexL != -1) {
			AddLSFeature(forms, poss, index, indexL, fv, nodelength);
		}
		//feat: i,i+1
		if(index!=-1&&indexR!=-1){
			
			AddSRFeature(forms, poss, index, indexR, fv, nodelength);
		}
		if(index!=-1&&indexR!=-1&&indexRR!=-1){
			AddSRRRFeature(forms, poss, index, indexR, indexRR, fv, nodelength);
		}
	}
	
	private void AddPositionFeature(int positioninnode,FeatureVector fv,int sentlength){
		//position is important, so set value as 1.0
		add("Order_Position_"+positioninnode, fv, 1.0);
	}
	// Form[index], Pos[index]
	private void AddSelfFeature(String[] forms, String[] poss, int index,
			FeatureVector fv, int nodelength) {
		StringBuffer prefix = new StringBuffer("Order_Self");
		double value = (double) nodelength / (forms.length - 1);
		String formfeat = prefix.append("_form_").append(forms[index])
				.toString();
		add(formfeat, fv, value);
		String posfeat = prefix.append("_pos_").append(poss[index]).toString();
		add(posfeat, fv, value);
	}

	// Form[index-1]Form[index], Pos[index-1]Pos[index]
	private void AddLSFeature(String[] forms, String[] poss, int index,
			int indexL, FeatureVector fv, int nodelength) {
		StringBuffer prefix = new StringBuffer("Order_LS");
		double value = (double) nodelength / (forms.length - 1);
		String formfeat = prefix.append("_form1_").append(forms[indexL])
				.append("_form2_").append(forms[index]).toString();
		add(formfeat, fv, value);
		String posfeat = prefix.append("_pos1_").append(forms[indexL])
				.append("_pos2_").append(forms[index]).toString();
		add(posfeat, fv, value);
	}

	// Form[index]Form[index+1], Pos[index]Pos[index+1]
	private void AddSRFeature(String[] forms, String[] poss, int index,
			int indexR, FeatureVector fv, int nodelength) {
		StringBuffer prefix = new StringBuffer("Order_SR");
		double value = (double) nodelength / (forms.length - 1);
		String formfeat = prefix.append("_form1_").append(forms[index])
				.append("_form2_").append(forms[indexR]).toString();
		add(formfeat, fv, value);
		String posfeat = prefix.append("_pos1_").append(forms[index])
				.append("_pos2_").append(forms[indexR]).toString();
		add(posfeat, fv, value);
	}

	// Form[index]Form[index+1]Form[index+2], Pos[index]Pos[index+1]Pos[index+2]
	private void AddSRRRFeature(String[] forms, String[] poss, int index,
			int indexR, int indexRR, FeatureVector fv, int nodelength) {
		StringBuffer prefix = new StringBuffer("Order_SRRR");
		double value = (double) nodelength / (forms.length - 1);
		String formfeat = prefix.append("_form1_").append(forms[index])
				.append("_form2_").append(forms[indexR]).append("_form3_")
				.append(forms[indexRR]).toString();
//		add(formfeat, fv, value);
		String posfeat = prefix.append("_pos1_").append(forms[index])
				.append("_pos2_").append(forms[indexR]).append("_pos3_")
				.append(forms[indexRR]).toString();
		add(posfeat, fv, value);
	}

	public int CountReverse(TIntIntHashMap hm1, TIntIntHashMap hm2) {
		// reverse pair number as error function
		// get mapping array
//		System.out.println("hm1");
//		tranverseTIntIntMap(hm1);
//		System.out.println("hm2");
//		tranverseTIntIntMap(hm2);
		int array[] = new int[hm1.size()];
		TIntIntHashMap hm2_child_order = new TIntIntHashMap();
		for (int i = 1; i <= hm2.size(); i++) {
			hm2_child_order.put(hm2.get(i), i);
		}
		for (int i = 1; i <= hm2.size(); i++) {
			array[i - 1] = hm1.get(hm2_child_order.get(i));
		}
//		System.out.print("array:");
//		for(int i=0;i<hm1.size();i++){
//			System.out.print(array[i]+"\t");
//		}
//		System.out.println();
		// count reverse
		BinaryMergeSort sort = new BinaryMergeSort();
		sort.mergesort(array, 0, array.length - 1);
		return sort.nixuNum;
	}
	public int CountOrderSquareDist(TIntIntHashMap hm1, TIntIntHashMap hm2) {
		// reverse pair number as error function
		// get mapping array
//		System.out.println("hm1");
//		tranverseTIntIntMap(hm1);
//		System.out.println("hm2");
//		tranverseTIntIntMap(hm2);
		int count=0;
		TIntIntHashMap hm1_child_order = new TIntIntHashMap();
		for (int i = 1; i <= hm1.size(); i++) {
			hm1_child_order.put(hm1.get(i), i);
		}
		TIntIntHashMap hm2_child_order = new TIntIntHashMap();
		for (int i = 1; i <= hm2.size(); i++) {
			hm2_child_order.put(hm2.get(i), i);
		}
		for(int i=1;i<=hm1.size();i++){
			int dist=hm2_child_order.get(i)-hm1_child_order.get(i);
			count+=dist*dist;
		}
		return count;
	}
	
	public int ViolationCount(DependencyInstance di,TIntIntHashMap order_child) throws IOException{
		//check di.order[]
		
		int violationcount=0;
		
		TIntIntHashMap child_order=new TIntIntHashMap();
		for(int i=1;i<=order_child.size();i++){
			child_order.put(order_child.get(i), i);
		}
		
		HashMap<String,String> parent_childs=new HashMap<String,String>();
		for(int i=1;i<di.length();i++){
			String head=di.heads[i]+"";
			if(parent_childs.containsKey(head)){
				parent_childs.put(head, parent_childs.get(head)+"\t"+i);
			}else{
				parent_childs.put(head, i+"");
			}
		}
		Iterator iterator=parent_childs.entrySet().iterator();
		while(iterator.hasNext()){
			Map.Entry<String, String> entry=(java.util.Map.Entry<String, String>) iterator.next();
			int head=Integer.parseInt(entry.getKey());
			String childs[]=entry.getValue().split("\t");
			for(int i=0;i<childs.length;i++){
				//check violation: di.order[head] < di.child[child]
				int child=Integer.parseInt(childs[i]);
				if(child_order.get(head)<child_order.get(child)&&head!=0){
					violationcount++;
//					System.out.println("head:"+head+",child:"+child);
				}
			}
		}
		return violationcount;
	}

	public int CreatingAlphabet(String orderedconll) throws IOException {
		/*
		 * according to the gold order, to generate fv for every sentence 1.read
		 * every instance from conll 2.extract feature vector of instance
		 * 3.close alphabet, stop growth 4.init param
		 */
		System.out.println("Creating alphabet start...");
		long start=System.currentTimeMillis();
		int numofinstance = 0;
		CONLLReader reader = new CONLLReader();
		reader.startReading(orderedconll);
		DependencyInstance di = reader.getNext();
		while (di != null) {
			numofinstance++;
			ExtractFeatureVectorByOrder(di, di.orders, new FeatureVector());
			di = reader.getNext();
		}
		long end=System.currentTimeMillis();
		System.out.println("Done. num of feat:"+orderfeat.size()+". Time:"+(end-start)/1000+" seconds.");
		param = new Parameters(orderfeat.size());
		orderfeat.stopGrowth();
		return numofinstance;
	}

	public void Train(String orderedconll, int iter, String modelfile)
			throws IOException {
		/*
		 * 1. creating alphabet and init param 2. as setting iter nummber, call
		 * train iter
		 */
		System.out.println("Train iter:"+iter);
		this.orderfeat=new Alphabet();
		int numofinst = CreatingAlphabet(orderedconll);
		for (int i = 1; i <= iter; i++) {
			TrainIter(numofinst, orderedconll, i, iter);
		}
		param.averageParams(iter * numofinst);
		savemodel(modelfile);
	}

	public void Predict(String orderconll, String modelfile, String writefile)
			throws ClassNotFoundException, IOException {
		long start=System.currentTimeMillis();
		MIRAOrderPredictor predictor = new MIRAOrderPredictor();
		predictor.loadmodel(modelfile);
		CONLLReader reader = new CONLLReader();
		reader.startReading(orderconll);
		WriteInstanceWithOrder writer = new WriteInstanceWithOrder();
		writer.startWriting(writefile);
		DependencyInstance di = reader.getNext();
		int instcount = 0;
		int reversecount=0;
		int CN2count=0;//for normalization
		double percentageaccumulatation=0.0;
		while (di != null) {
			instcount++;
			TIntIntHashMap predictorder = (TIntIntHashMap) predictor
					.PredictOrderByCurrentParam(di)[1];
			int reversetemp=this.CountReverse(di.orders, predictorder);
			reversecount+=reversetemp;
			int CN2temp=(di.length()-1)*(di.length()-2)/2;
			CN2count+=CN2temp;
			percentageaccumulatation+=(double)reversetemp/CN2temp;
			/*
			System.out.println("sent " + instcount + ":"
					+ this.CountReverse(di.orders, predictorder)
					+ " reverse orders.");
					*/
			// write childs !
			/*
			writer.write(new DependencyInstance(RemoveRoot(di.forms),
					RemoveRoot(di.lemmas), RemoveRoot(di.cpostags),
					RemoveRoot(di.postags), di.feats, RemoveRoot(di.deprels),
					RemoveRoot(di.heads), predictorder));
					*/
//			System.out.println("predict order");
//			tranverseTIntIntMap(predictorder);
//			System.out.println("===========");
			di.orders=predictorder;
			writer.write(di);
			di = reader.getNext();
		}
		writer.finishWriting();
		long end=System.currentTimeMillis();
		System.out.println("Predict Done.\nTime:"+(end-start)/1000+".\nReversecount:"+reversecount+".\nCN2count:"+CN2count+"\npercentage:"+percentageaccumulatation/instcount);
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

	public void TrainIter(int numInstances, String trainfile, int curiter,
			int iter) throws IOException {
		/*
		 * 1. process every instance 2. call hildreth 3. fv.update param
		 */
		System.out.println("Iter: "+curiter);
		long start=System.currentTimeMillis();
		int numUpd = 0;
		CONLLReader reader = new CONLLReader();
		reader.startReading(trainfile);

		DependencyInstance inst;
		int currentInstance = 0;
		inst = reader.getNext();
		while (inst != null) {
			currentInstance++;
			if (currentInstance % 500 == 0) {
				System.out.print(currentInstance + ",");
			}
			int lastpunish=1;
			int punish=2;
			while(punish>0&&punish!=lastpunish){
//				System.out.println("punish="+punish);
				FeatureVector fv = new FeatureVector();
				ExtractFeatureVectorByOrder(inst, inst.orders, fv);
				inst.setFeatureVector(fv);
	
				double upd = (double) (iter * numInstances
						- (numInstances * (curiter - 1) + currentInstance) + 1);
	
				Object[] ret = PredictOrderByCurrentParam(inst);
				FeatureVector fvpredict = (FeatureVector) ret[0];
				TIntIntHashMap order_predict = (TIntIntHashMap) ret[1];
				int K = 1;
				FeatureVector[] dist = new FeatureVector[K];
				dist[0] = fv.getDistVector(fvpredict);
				double[] b = new double[K];
				lastpunish=punish;
				punish=CountReverse(order_predict, inst.orders)+ViolationCount(inst, order_predict)+CountOrderSquareDist(order_predict, inst.orders);
				b[0] = (double) punish/inst.length()
						- (fv.getScore(param.parameters) - fvpredict
								.getScore(param.parameters));
				double[] alpha = hildreth(dist, b);
				for (int k = 0; k < K; k++) {
					dist[k].update(param.parameters, param.total, alpha[k], upd);
				}
			}
			inst = reader.getNext();
		}
		long end=System.currentTimeMillis();
		System.out.println("=========================Time:"+(end-start)/1000);
	}

	private double[] hildreth(FeatureVector[] a, double[] b) {

		int i;
		int max_iter = 10000;
		double eps = 0.00000001;
		double zero = 0.000000000001;

		double[] alpha = new double[b.length];

		double[] F = new double[b.length];
		double[] kkt = new double[b.length];
		double max_kkt = Double.NEGATIVE_INFINITY;

		int K = a.length;

		double[][] A = new double[K][K];
		boolean[] is_computed = new boolean[K];
		for (i = 0; i < K; i++) {
			A[i][i] = a[i].dotProduct(a[i]);
			is_computed[i] = false;
		}

		int max_kkt_i = -1;

		for (i = 0; i < F.length; i++) {
			F[i] = b[i];
			kkt[i] = F[i];
			if (kkt[i] > max_kkt) {
				max_kkt = kkt[i];
				max_kkt_i = i;
			}
		}

		int iter = 0;
		double diff_alpha;
		double try_alpha;
		double add_alpha;

		while (max_kkt >= eps && iter < max_iter) {

			diff_alpha = A[max_kkt_i][max_kkt_i] <= zero ? 0.0 : F[max_kkt_i]
					/ A[max_kkt_i][max_kkt_i];
			try_alpha = alpha[max_kkt_i] + diff_alpha;
			add_alpha = 0.0;

			if (try_alpha < 0.0)
				add_alpha = -1.0 * alpha[max_kkt_i];
			else
				add_alpha = diff_alpha;

			alpha[max_kkt_i] = alpha[max_kkt_i] + add_alpha;

			if (!is_computed[max_kkt_i]) {
				for (i = 0; i < K; i++) {
					A[i][max_kkt_i] = a[i].dotProduct(a[max_kkt_i]); // for
																		// version
																		// 1
					is_computed[max_kkt_i] = true;
				}
			}

			for (i = 0; i < F.length; i++) {
				F[i] -= add_alpha * A[i][max_kkt_i];
				kkt[i] = F[i];
				if (alpha[i] > zero)
					kkt[i] = Math.abs(F[i]);
			}

			max_kkt = Double.NEGATIVE_INFINITY;
			max_kkt_i = -1;
			for (i = 0; i < F.length; i++)
				if (kkt[i] > max_kkt) {
					max_kkt = kkt[i];
					max_kkt_i = i;
				}

			iter++;
		}

		return alpha;
	}
	
	public void BatRun(String args[]) throws ClassNotFoundException, IOException{
		//args: train-file, iter-num, model-name, test-file, order-file
		System.out.println("train-file:"+args[0]);
		System.out.println("iter-num:"+args[1]);
		System.out.println("model-name£º"+args[2]);
		System.out.println("test-file:"+args[3]);
		System.out.println("order-file:"+args[4]);
		MIRAOrderPredictor test=new MIRAOrderPredictor();
		test.Train(args[0], Integer.parseInt(args[1]), args[2]);
		test.Predict(args[3], args[2], args[4]);
	}
	public void BatTest(String args[]) throws ClassNotFoundException, IOException{
		System.out.println("model-name£º"+args[0]);
		System.out.println("test-file:"+args[1]);
		System.out.println("order-file:"+args[2]);
		MIRAOrderPredictor test=new MIRAOrderPredictor();
		test.Predict(args[1], args[0], args[2]);
		
	}
	public static void main(String args[]) throws IOException, ClassNotFoundException{
		MIRAOrderPredictor test=new MIRAOrderPredictor();
//		test.Train("1sen.txt", 1, "1senorderpredict.model");
//		test.Predict("1sen.txt", "1senorderpredict.model", "1senorderpredict.order");
		
		System.out.println("change punish function");
		test.Train("wsj_2-21_malt_processindex.txt", 4, "wsj_2-21_malt_orderpredictor_chgcost4.model");
		test.Predict("wsj_00-01_malt_processindex.txt", "wsj_2-21_malt_orderpredictor_chgcost4.model", "wsj_00-01_malt_orderpredictor_chgcost4.order");
		
//		test.BatRun(args);
//		test.BatTest(args);
	}
	
}
