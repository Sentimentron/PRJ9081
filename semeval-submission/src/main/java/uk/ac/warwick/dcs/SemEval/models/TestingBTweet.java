package uk.ac.warwick.dcs.SemEval.models;

import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.util.Pair;


public class TestingBTweet extends Tweet implements Comparable<TestingBTweet> {
	
	public TestingBTweet(String text, long id1, int id2, AnnotationType kind) {
		super(text, id1, id2);
		super.setAnnotation(kind);
	}
	
	public TestingBTweet(String text, long id1, int id2) {
		this(text, id1, id2, null);
	}

	@Override
	public int compareTo(TestingBTweet o) {
		int ret = this.getText().compareTo(o.getText());
		if (ret != 0) return ret; 
		if (this.getId1() > o.getId1()) {
			return 1;
		}
		else if (this.getId1() < o.getId1()) {
			return -1;
		}
		else if (this.getId2() > o.getId2()) {
			return 1;
		}
		else if (this.getId2() < o.getId2()) {
			return -1;
		}
		return 0;
	}
	
}
