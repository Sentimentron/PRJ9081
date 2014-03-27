package uk.ac.warwick.dcs.SemEval;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import cmu.arktweetnlp.RawTagger;
import uk.ac.warwick.dcs.SemEval.AnnotationType.AnnotationKind;
import uk.ac.warwick.dcs.SemEval.exceptions.WordRangeMapException;

public class POSTaggedTweetTest {
	
	RawTagger tagger;
	
	@Before
	public void setup() throws IOException {
		System.out.println("Working Directory = " +
	              System.getProperty("user.dir"));
		this.tagger = new RawTagger();
		this.tagger.loadModel("model.20120919");
	}

	@Test
	// Barclays Profit Driven by Investment Banking: LONDON--Barclays PLC ( BCS ) Wednesday continued to reap the rewards... http://bit.ly/W5t0au
	public void testConstructionAndPOSTagging() throws WordRangeMapException {
		Tweet t = new Tweet("Barclays Profit Driven by Investment Banking: LONDON--Barclays PLC (BCS) Wednesday continued to reap the rewards... http://bit.ly/W5t0au ");
		t.addAnnotation(new AnnotationSpan(AnnotationKind.Positive, 10, 14));
		// Positive span should extend to word offset 16 after pos tagging
		POSTaggedTweet pt = new POSTaggedTweet(t, this.tagger);
		AnnotationMap mp = pt.getAnnotations();
		assert(t.getAnnotations().get(14).getKind() == AnnotationKind.Positive);
		assert(mp.get(16).getKind() == AnnotationKind.Positive);
		assert(mp.get(10).getKind() == AnnotationKind.Positive);
	}

}
