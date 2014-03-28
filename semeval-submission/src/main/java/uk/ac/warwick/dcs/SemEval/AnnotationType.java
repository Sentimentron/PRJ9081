package uk.ac.warwick.dcs.SemEval;

import java.util.ArrayList;
import java.util.List;

public class AnnotationType {

	public enum AnnotationKind {
		Positive, // NB, there are all subjective
		Negative,
		Neutral,
		Objective,
		Subjective, // This means we don't know whether is positive, negative, neutral
	}
	
	AnnotationKind type;
	
	public AnnotationType(AnnotationKind k) {
		this.type = k;
	}
	
	public AnnotationType(String s) {
		if (s == "p") {
			this.type = AnnotationKind.Positive;
		}
		else if (s == "n") {
			this.type = AnnotationKind.Negative;
		}
		else if (s == "e") {
			this.type = AnnotationKind.Neutral;
		}
		else if (s == "s") {
			this.type = AnnotationKind.Subjective;
		}
		else {
			this.type = AnnotationKind.Objective;
		}
	}
	
	public static AnnotationType fromSemEvalString(String s) throws Exception {
		if(s.equals("negative")) return new AnnotationType(AnnotationKind.Negative);
		else if (s.equals("positive")) return new AnnotationType(AnnotationKind.Positive);
		else if(s.equals("neutral")) return new AnnotationType(AnnotationKind.Neutral);
		else if(s.equals("objective")) return new AnnotationType(AnnotationKind.Objective);
		else {
			throw new Exception(String.format("Can't interpret annotation string '%s'", s));
		}
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
	
	public String toNominalSentiment() {
		switch(this.getKind()){
		case Negative:
			return "n";
		case Neutral:
			return "e";
		case Positive:
			return "p";
		case Subjective:
			return "s";
		default:
			return "q";
			
		}
	}
	
	public static List<String> getNominalSubjList() {
		List<String> ret = new ArrayList<String>();
		ret.add("s");
		ret.add("q");
		return ret;
	}
	
	public static List<String> getNominalList() {
		List<String> ret = new ArrayList<String>();
		ret.add("n");
		ret.add("e");
		ret.add("p");
		ret.add("q");
		return ret;
	}
	
}
