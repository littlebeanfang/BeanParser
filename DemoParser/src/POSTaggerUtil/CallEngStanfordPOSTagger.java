package POSTaggerUtil;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;

import java.io.IOException;


public class CallEngStanfordPOSTagger {
	public MaxentTagger tagger;

	public CallEngStanfordPOSTagger(String model){
		try {
			tagger = new MaxentTagger(model);
		}catch(Exception e){
			System.err.println(e);
		}
	}

	public String Call(String input) throws ClassNotFoundException, IOException{
		// Initialize the tagger

		// The sample string
		String sample = input ;//"This is a sample text";
		// The tagged string
		String tagged = tagger.tagString(sample);
		// Output the result
//		System.out.println(tagged);
		return tagged;
	}

	public static void main(String args[]) throws ClassNotFoundException, IOException{
		CallEngStanfordPOSTagger test=new CallEngStanfordPOSTagger("models/wsj-0-18-bidirectional-distsim.tagger");

		String retString=test.Call("This is a sample text.");
		System.out.println(retString);
		ConllConverter conllConverter=new ConllConverter("_");
		String sb=conllConverter.toConll(retString);
		System.out.print(sb);
	}
}
