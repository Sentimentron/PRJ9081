package uk.ac.warwick.dcs.SemEval;

import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.util.Pair;

public class TestingTweet extends Tweet {

	List<Pair<Integer, Integer>> interestingSections;
	
	public TestingTweet(String text, long id1, int id2) {
		super(text, id1, id2);
		
		this.interestingSections = new ArrayList<Pair<Integer, Integer>>();
	}
	
	public void addInterestingSection(int startOffset, int endOffset) {
		this.interestingSections.add(
				new Pair<Integer, Integer>(startOffset, endOffset)
			);
	}
	
	public List<Pair<Integer, Integer>> getInterestingSections() {
		return this.interestingSections;
	}

}
