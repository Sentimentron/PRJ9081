package uk.ac.warwick.dcs.SemEval;

import java.util.ArrayList;
import java.util.List;

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
	
	public boolean equals(AnnotationType t) {
		return this.getKind() == t.getKind();
	}
	
	public String toNominalSubj() {
		if (this.isSubjective()) return "s";
		return "q";
	}
	
	public static List<String> getNominalSubjList() {
		List<String> ret = new ArrayList<String>();
		ret.add("s");
		ret.add("q");
		return ret;
	}
}
