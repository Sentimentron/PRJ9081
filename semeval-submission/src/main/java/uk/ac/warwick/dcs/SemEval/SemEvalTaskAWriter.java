package uk.ac.warwick.dcs.SemEval;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import uk.ac.warwick.dcs.SemEval.AnnotationType.AnnotationKind;

public class SemEvalTaskAWriter {

	private BufferedWriter bw;
	
	public SemEvalTaskAWriter(String path) throws IOException {
		this.bw = new BufferedWriter(new FileWriter(path));
	}
	
	private String outputConsensusAnnotation(Tweet t, int offsetStart, int offsetEnd) {
		
		int objectiveFound = 0;
		int negativeFound  = 0;
		int neutralFound   = 0;
		int positiveFound  = 0;
		
		for (int i = offsetStart; i <= offsetEnd; i++) {
			AnnotationType k = t.getAnnotations().get(i);
			if (k == null) continue;
			switch(k.getKind()) {
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
		
		switch(ret) {
		case Objective:
			return "objective";
		case Positive:
			return "positive";
		case Neutral:
			return "neutral";
		case Negative:
			return "negative";
		default:
			System.err.printf("ouputConsensusAnnotation: Warning: should't have %s here!\n", ret);
			return "unknwn";
		}
		
	}
	
	public void writeTweet(Tweet t, int offsetStart, int offsetEnd) throws IOException {
		this.bw.write(Long.toString(t.getId1()));
		this.bw.write('\t');
		this.bw.write(Integer.toString(t.getId2()));
		this.bw.write('\t');
		this.bw.write(Integer.toString(offsetStart));
		this.bw.write('\t');
		this.bw.write(Integer.toString(offsetEnd));
		this.bw.write('\t');
		this.bw.write(this.outputConsensusAnnotation(t, offsetStart, offsetEnd));
		this.bw.write('\t');
		this.bw.write(t.getText());
		this.bw.write('\n');
	}
	
	public void finish() throws IOException {
		this.bw.write('\n'); // Always seems to have two blank lines at the end
		this.bw.close();
	}
	
}
