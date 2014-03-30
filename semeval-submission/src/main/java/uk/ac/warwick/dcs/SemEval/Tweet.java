package uk.ac.warwick.dcs.SemEval;

import uk.ac.warwick.dcs.SemEval.AnnotationType.AnnotationKind;
import uk.ac.warwick.dcs.SemEval.exceptions.InvalidAnnotationSpanException;

public class Tweet {

	protected AnnotationMap annotations;
	protected String text;
	private int maxLength;
	
	private AnnotationType annotation;
	
	private long id1;
	private int id2;
	
	public long getId1() {
		return this.id1;
	}
	
	public int getId2() {
		return this.id2;
	}
	
	public Tweet(String text, long id1, int id2) {
		String[] arr;
		
		this.id1 = id1;
		this.id2 = id2;
		
		this.text = text;
		arr = this.text.split("[ ,]"); //TODO should roll this into WordRangeMap
		this.annotations = new AnnotationMap(AnnotationMap.DuplicationStrategy.Replace);
		this.maxLength = arr.length;
	}
	
	public Tweet(String text, AnnotationMap m, long id1, int id2) {
		this(text, id1, id2);
		this.annotations = m;
	}
	
	public void addAnnotation(AnnotationSpan s) throws InvalidAnnotationSpanException {
		if (s.getEnd() > this.maxLength) {
			throw new InvalidAnnotationSpanException("Longer than the tweet", this.maxLength, s.getEnd());
		}
		this.annotations.put(s);
	}
	
	public String getText() {
		return this.text;
	}
	
	public AnnotationMap getAnnotations() {
		return this.annotations.clone();
	}
	
	public void clearAnnotations() {
		this.annotations.clear();
	}
	
	public boolean equal(Tweet other) {
		if (this.getText().equals(other.getText())) {
			if (this.annotations.equal(other.getAnnotations())) {
				return true;
			}
		}
		return false;
	}
	
	public boolean subjEqual(Tweet other) {
		if (this.getText().equals(other.getText())) {
			if (this.annotations.subjEqual(other.getAnnotations())) {
				return true;
			}
		}
		return false;
	}
}
