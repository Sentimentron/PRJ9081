package uk.ac.warwick.dcs.SemEval;

public class AnnotationType {

	public enum AnnotationKind {
		Positive,
		Negative,
		Neutral,
		Objective
	}
	
	AnnotationKind type;
	
	public AnnotationType(AnnotationKind k) {
		this.type = k;
	}
	
	public boolean isSubjective() {
		return this.type != AnnotationKind.Objective;
	}

	public AnnotationKind getKind() {
		return this.type;
	}
	
	public boolean equals(AnnotationKind k) {
		return this.getKind() == k;
	}
	
}
