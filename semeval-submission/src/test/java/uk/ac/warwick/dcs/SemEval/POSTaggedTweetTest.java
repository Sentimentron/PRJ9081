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
	public void testConstructionAndPOSTagging1() throws WordRangeMapException {
		Tweet t = new Tweet("Barclays Profit Driven by Investment Banking: LONDON--Barclays PLC (BCS) Wednesday continued to reap the rewards... http://bit.ly/W5t0au ");
		t.addAnnotation(new AnnotationSpan(AnnotationKind.Positive, 10, 14));
		// Positive span should extend to word offset 16 after pos tagging
		POSTaggedTweet pt = new POSTaggedTweet(t, this.tagger);
		AnnotationMap mp = pt.getAnnotations();
		assert(t.getAnnotations().get(14).getKind() == AnnotationKind.Positive);
		assert(mp.get(16).getKind() == AnnotationKind.Positive);
		assert(mp.get(10).getKind() == AnnotationKind.Positive);
	}

	// 0  1    2   3       4       5 6 7  8    9     10   11   12      13   14  15     16 17  18   19     20           21
	// In case you weren't invited . . .: ..it 'may' have been because Andy was hiding a deep,dark secret Personali... http://bit.ly/WdxawR |
	// 0  1    2   3       4       5 6 7  8    9 10  11 12  13   14      15   16  17     18 19  20   21     22           23
	// In case you weren't invited . . .: ..it ' may ' have been because Andy was hiding a deep,dark secret Personali... http://bit.ly/WdxawR 
	// || |  | | | |     | |     | | | || |   || | | |||  | |  | |     | |  | | | |    |   |  ||   | |    | |           ||                  |
	@Test
	public void testConstructionAndPosTagging2() throws WordRangeMapException {
		Tweet t = new Tweet("In case you weren't invited . . .: ..it 'may' have been because Andy was hiding a deep,dark secret Personali... http://bit.ly/WdxawR ");
		t.addAnnotation(new AnnotationSpan(AnnotationKind.Negative, 3, 4));
		t.addAnnotation(new AnnotationSpan(AnnotationKind.Negative, 17, 20));
		POSTaggedTweet pt = new POSTaggedTweet(t, this.tagger);
		AnnotationMap mp  = pt.getAnnotations();
		assert(mp.get(3).getKind() == AnnotationKind.Negative);
		assert(mp.get(4).getKind() == AnnotationKind.Negative);
		assert(mp.get(17).getKind() == AnnotationKind.Objective);
		assert(mp.get(18).getKind() == AnnotationKind.Objective);
		assert(mp.get(19).getKind() == AnnotationKind.Negative);
		assert(mp.get(20).getKind() == AnnotationKind.Negative);
		assert(mp.get(21).getKind() == AnnotationKind.Negative);
		assert(mp.get(22).getKind() == AnnotationKind.Negative);
	}
	
}
