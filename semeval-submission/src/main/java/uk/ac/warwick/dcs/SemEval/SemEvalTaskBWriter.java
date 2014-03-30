package uk.ac.warwick.dcs.SemEval;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import uk.ac.warwick.dcs.SemEval.AnnotationType.AnnotationKind;

public class SemEvalTaskBWriter {
	private BufferedWriter bw;
	
	public SemEvalTaskBWriter(String path) throws IOException {
		this.bw = new BufferedWriter(new FileWriter(path));
	}
	
	public String outputAnnotation(AnnotationKind ret) {
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
	
	public void writeTweet(TestingBTweet t) throws IOException {
		if (t.getId1() == 0) {
			this.bw.write("NA");
		}
		else {
			this.bw.write(Long.toString(t.getId1()));
		}
		this.bw.write('\t');
		this.bw.write(Integer.toString(t.getId2()));
		this.bw.write('\t');
		this.bw.write(outputAnnotation(t.getAnnotation().getKind()));
		this.bw.write('\t');
		this.bw.write(t.getText());
		this.bw.write('\n');
	}
	
	public void finish() throws IOException {
		this.bw.close();
	}
}
