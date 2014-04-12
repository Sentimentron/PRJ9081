package uk.ac.warwick.dcs.SemEval.models;

import java.util.ArrayList;
import java.util.List;

public class AnnotationType {

	public enum AnnotationKind {
		Positive, // NB, there are all subjective
		Negative,
		Neutral,
		Objective,
		Subjective, // This means we don't know whether is positive, negative, neutral
		Unknown,
	}
	
	AnnotationKind type;
	
	public static AnnotationKind computeConsensus(List<AnnotationKind> kList) {
		int objectiveFound = 0;
		int negativeFound  = 0;
		int neutralFound   = 0;
		int positiveFound  = 0;
		
		for (AnnotationKind k : kList) {
			switch(k) {
			case Objective:
				objectiveFound++;
				break;
			case Negative:
				negativeFound++;
				break;
			case Neutral:
				neutralFound++;
				break;
			case Positive:
				positiveFound++;
				break;
			default:
				System.err.println("outputConsensusAnnotation: should only be getting p, q, n, e.");
			}
		}
		
		int mostPopular = objectiveFound;
		AnnotationKind ret = AnnotationKind.Objective;
		
		if (negativeFound >= mostPopular) {
			mostPopular = negativeFound;
			ret = AnnotationKind.Negative;
		}
		if (positiveFound >= mostPopular) {
			mostPopular = positiveFound;
			ret = AnnotationKind.Positive;
		}
		if (neutralFound >= mostPopular) {
			mostPopular = neutralFound;
			ret = AnnotationKind.Neutral;
		}
		
		return ret;
	}
	
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
	
	public static AnnotationKind fromChar(char s) {
		
		switch(s) {
			case 'p':
				return AnnotationKind.Positive;
			case 'n':
				return AnnotationKind.Negative;
			case 'e':
				return AnnotationKind.Neutral;
			case 's':
				return AnnotationKind.Subjective;
			default:
				return AnnotationKind.Objective;
		}
	}
	
	public AnnotationType clone() {
		return new AnnotationType(this.getKind());
	}
	
	public static AnnotationType fromSemEvalString(String s) throws Exception {
		if(s.equals("negative")) return new AnnotationType(AnnotationKind.Negative);
		else if (s.equals("positive")) return new AnnotationType(AnnotationKind.Positive);
		else if(s.equals("neutral")) return new AnnotationType(AnnotationKind.Neutral);
		else if(s.equals("objective")) return new AnnotationType(AnnotationKind.Objective);
		else if(s.equals("objective-OR-neutral")) return new AnnotationType(AnnotationKind.Neutral);
		else if(s.equals("unknwn")) return new AnnotationType(AnnotationKind.Unknown);
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
		ret.add("s");
		return ret;
	}
	
}
