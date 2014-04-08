package uk.ac.warwick.dcs.SemEval;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import uk.ac.warwick.dcs.SemEval.AnnotationType.AnnotationKind;

public class SemEvalTaskAWriter {

	private BufferedWriter bw;
	
	public SemEvalTaskAWriter(String path) throws IOException {
		this.bw = new BufferedWriter(new FileWriter(path));
	}
	
	private String outputConsensusAnnotation(Tweet t, int offsetStart, int offsetEnd) {
		
		
		List<AnnotationKind> kList = new ArrayList<AnnotationKind>();
		
		for (int i = offsetStart; i <= offsetEnd; i++) {
			AnnotationType k = t.getAnnotations().get(i);
			if (k == null) continue;
			kList.add(k.getKind());
		}
		
		AnnotationKind ret = AnnotationType.computeConsensus(kList);
		
		switch(ret) {
		case Neutral:
			return "neutral";
		case Positive:
			return "positive";
		case Negative:
			return "negative";
		default:
			return "neutral";
		}
		
	}
	
	public void writeTweet(Tweet t, int offsetStart, int offsetEnd) throws IOException {
		if (t.getId1() == 0) {
			this.bw.write("NA");
		}
		else {
			this.bw.write(Long.toString(t.getId1()));
		}
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
