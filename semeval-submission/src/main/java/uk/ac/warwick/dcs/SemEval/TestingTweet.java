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
	
	public TestingTweet(String text, long id1, int id2,
			List<Pair<Integer, Integer>> interestingSections) {
		this(text, id1, id2);
		for (Pair<Integer, Integer> section: interestingSections) {
			this.addInterestingSection(section.first, section.second);
		}
	} 

	public void addInterestingSection(int startOffset, int endOffset) {
		this.interestingSections.add(
				new Pair<Integer, Integer>(startOffset, endOffset)
			);
	}
	
	public List<Pair<Integer, Integer>> getInterestingSections() {
		return this.interestingSections;
	}
	
	public boolean inInterestingSection(int i) {
		for (Pair<Integer, Integer> p : this.interestingSections) {
			if (i < p.first) continue;
			if (i > p.second) continue;
			return true;
		}
		return false;
	}

}
