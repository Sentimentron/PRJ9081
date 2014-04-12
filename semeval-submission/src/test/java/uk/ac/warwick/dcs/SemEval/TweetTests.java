package uk.ac.warwick.dcs.SemEval;

import static org.junit.Assert.*;
import io.SemEvalTaskAReader;

import java.util.List;

import org.junit.Test;

import uk.ac.warwick.dcs.SemEval.AnnotationType.AnnotationKind;

public class TweetTests {

	@Test
	public void testEquality() {
		Tweet t1 = new Tweet("Barclays Profit Driven by Investment Banking: LONDON--Barclays PLC (BCS) Wednesday continued to reap the rewards... http://bit.ly/W5t0au ", 0, 0);
		Tweet t2 = new Tweet("Barclays Profit Driven by Investment Banking: LONDON--Barclays PLC (BCS) Wednesday continued to reap the rewards... http://bit.ly/W5t0au ", 0, 0);

		assertTrue(t1.equal(t2));
		
		t1.addAnnotation(new AnnotationSpan(AnnotationKind.Negative, 1, 3));
		assertFalse(t1.equal(t2));
		
		t2.addAnnotation(new AnnotationSpan(AnnotationKind.Negative, 1, 1));
		t2.addAnnotation(new AnnotationSpan(AnnotationKind.Negative, 2, 2));
		t2.addAnnotation(new AnnotationSpan(AnnotationKind.Negative, 3, 3));
		assertTrue(t1.equal(t2));
		
		t1.addAnnotation(new AnnotationSpan(AnnotationKind.Negative, 4, 4));
		t2.addAnnotation(new AnnotationSpan(AnnotationKind.Positive, 4, 4));
		assertFalse(t1.equal(t2));
		assertTrue(t1.subjEqual(t2));
	}
	
	@Test
	public void testReading() throws Exception {
		ITweetReader r = new SemEvalTaskAReader("tweeter-dev-full-A-tweets-sample.tsv");
		List<Tweet> tweets = r.readTweets();
		
		Tweet t1r = tweets.get(0);
		Tweet t1f = new Tweet("Won the match #getin . Plus, tomorrow is a very busy day, with Awareness Day's and debates. Gulp. Debates...", 0, 0);
		t1f.addAnnotation(new AnnotationSpan(AnnotationKind.Neutral, 10, 10));
		t1f.addAnnotation(new AnnotationSpan(AnnotationKind.Negative, 17, 17));
		assertTrue(t1r.equal(t1f));
		assertTrue(t1f.equal(t1r));
		
		Tweet t2r = tweets.get(1);
		Tweet t2f = new Tweet("Lunch from my new Lil spot ...THE COTTON BOWL ....pretty good#1st#time#will be going back# http://instagr.am/p/RX9939CIv8/\240", 0, 0);
		t2f.addAnnotation(new AnnotationSpan(AnnotationKind.Positive, 9, 13));
		assertTrue(t2f.equal(t2r));
		
		assertTrue(tweets.size() == 5);
		
	}

}
