package uk.ac.warwick.dcs.SemEval;

import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.util.Pair;


public class TestingBTweet extends Tweet {

	private AnnotationType annotation;
	
	public TestingBTweet(String text, long id1, int id2, AnnotationType kind) {
		super(text, id1, id2);
		this.annotation = kind;
	}
	
	public TestingBTweet(String text, long id1, int id2) {
		this(text, id1, id2, null);
	}
	
	public AnnotationType getAnnotation() {
		return this.annotation;
	}
	
	public void setAnnotation(AnnotationType a) {
		this.annotation = a;
	}
	
}
