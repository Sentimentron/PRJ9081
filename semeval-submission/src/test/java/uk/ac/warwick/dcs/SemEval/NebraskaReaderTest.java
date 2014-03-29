package uk.ac.warwick.dcs.SemEval;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import uk.ac.warwick.dcs.SemEval.AnnotationType.AnnotationKind;

public class NebraskaReaderTest {

	@Before
	public void setUp() throws Exception {
	}
	
	@Test 
	public void testNebraskaReader() throws Exception {
		NebraskaReader nr = new NebraskaReader("amt.sqlite.sample");
		List<Tweet> tweets = nr.readTweets();
		assertTrue(tweets.size() == 2);
		
		Tweet t;
		t = tweets.get(0);
		assertTrue("; I could feel ima be in twitter jail soon lbs".equals(t.getText()));
		assertTrue(t.getId2() == 1);
		//012345678910
		//qqqqqqqnnqq (7-8)
		//qqqqnnnn    (4-7)
		//qqqqqqqqnqq (8)
		
		assertTrue(t.getAnnotations().get(4).getKind() == AnnotationKind.Negative);
		assertTrue(t.getAnnotations().get(5).getKind() == AnnotationKind.Negative);
		assertTrue(t.getAnnotations().get(6).getKind() == AnnotationKind.Negative);
		assertTrue(t.getAnnotations().get(7).getKind() == AnnotationKind.Negative);
		assertTrue(t.getAnnotations().get(8).getKind() == AnnotationKind.Negative);

		
		t = tweets.get(1);
		assertTrue("Accidentally deleted my Yahoo cricket app AGAIN :-S".equals(t.getText()));
		
		//01234567
		//qnqqqqnq|neutral  (1,6)                                                                                                                   
		//qnqqqqnq|negative (1,6)                                                                                                                   
		//nnqqqqnn|negative (0-1, 6-7)                                                                                                                   
		//nnqqqqqn|negative (0-1, 7)
		
		assertTrue(t.getAnnotations().get(1).getKind() == AnnotationKind.Negative);
		assertTrue(t.getAnnotations().get(6).getKind() == AnnotationKind.Negative);
		assertTrue(t.getAnnotations().get(0).getKind() == AnnotationKind.Negative);
		assertTrue(t.getAnnotations().get(1).getKind() == AnnotationKind.Negative);
		assertTrue(t.getAnnotations().get(7).getKind() == AnnotationKind.Negative);
		
	}

	@Test
	public void testConsensusAnnotation() {
		
		MultiAnnotationMap mam = new MultiAnnotationMap();
		
		mam.addAll(AnnotationSpan.createSpansFromString("qqqqq"));
		for (int i = 0; i < 5; i++) assertTrue(mam.get(i) == null);
		
		mam.addAll(AnnotationSpan.createSpansFromString("qpenq"));
		assertTrue(mam.get(0) == null);
		assertTrue(mam.get(1).getKind() == AnnotationKind.Positive);
		assertTrue(mam.get(2).getKind() == AnnotationKind.Neutral);
		assertTrue(mam.get(3).getKind() == AnnotationKind.Negative);
		assertTrue(mam.get(4) == null);
		
		mam.addAll(AnnotationSpan.createSpansFromString("nnnnn"));
		assertTrue(mam.get(0).getKind() == AnnotationKind.Negative);
		assertTrue(mam.get(1).getKind() == AnnotationKind.Positive);
		assertTrue(mam.get(2).getKind() == AnnotationKind.Neutral);
		assertTrue(mam.get(3).getKind() == AnnotationKind.Negative);
		assertTrue(mam.get(4).getKind() == AnnotationKind.Negative);
		
	}

}