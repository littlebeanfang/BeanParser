package DataStructure;

import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;

import DataStructure.ParseAgenda;

public class Beam {
	private ParseAgenda[] curr;
	private ParseAgenda[] next;
	private int width;
	private int index;
	private PriorityQueue<partialResult> queue;
	
	private class partialResult {
		public double score;
		public int child;
		public int head;
		public FeatureVector fv;
		public int index;
		
		public partialResult(int index, double score, int child, int head, FeatureVector fv) {
			this.index = index;
			this.score = score;
			this.child = child;
			this.head = head;
			this.fv = fv;
		}
		
		@Override
		public String toString() {
			return "score=" + score + ", child=" + child
					+ ", head=" + head + ", index=" + index + "]";
		}
	}
	
	private class partialResultComparator implements Comparator<partialResult> {
		@Override
		public int compare(partialResult r1, partialResult r2) {
			if (r1.score < r2.score) return -1;
			if (r1.score > r2.score) return 1;
			return 0;
		}
	}
	
	public Beam(int width) {
		curr = new ParseAgenda[width];
		next = new ParseAgenda[width];
		this.width = width;
		index = 0;
		Comparator<partialResult> comparator = new partialResultComparator();
		queue = new PriorityQueue<partialResult>(width, comparator);
	}
	
	public void initialize(int length) {
		curr[0] = new ParseAgenda(length);
		for (int i = 1;i < width;i++) curr[i] = null;
		queue.clear();
		index = 0;
	}
	
	public ParseAgenda getNext() {
		if (index < width) return curr[index++];
		return null;
	}
	
	public void addAgenda(double score, int child, int head, FeatureVector fv) {
		if (queue.size() < width) 
			queue.add(new partialResult(index - 1, score, child, head, fv));
		else {
			if (score > queue.peek().score) {
				queue.poll();
				queue.add(new partialResult(index - 1, score, child, head, fv));
			}
		}
	}
	
	public void finishIteration() {
		Iterator<partialResult> iter = queue.iterator();
		partialResult pr;
		int i = 0;
		while (iter.hasNext()) {
			pr = iter.next();
			next[i] = curr[pr.index].clone();
			next[i].UpdateSet(pr.child, pr.head);
			next[i].AddArc(pr.child, pr.head);
			next[i].ChildProcess(pr.child, pr.head);
			next[i].addFeatureVector(pr.fv);
			next[i].setScore(pr.score);
			i++;
		}
		index = 0;
		queue.clear();
		ParseAgenda[] tmp;
		tmp = curr;
		curr = next;
		next = tmp;
	}
	
	public ParseAgenda findBest() {
		double max = Double.NEGATIVE_INFINITY;
		ParseAgenda pa = null;
		for (int i = 0;i < width;i++) {
			if (curr[i].getScore() > max) {
				pa = curr[i];
				max = curr[i].getScore();
			}
		}
		return pa;
	}
	
	public ParseAgenda[] getQueue() {
		return curr;
	}
}
