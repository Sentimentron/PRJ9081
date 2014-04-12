package uk.ac.warwick.dcs.SemEval;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import uk.ac.warwick.dcs.SemEval.io.SemEvalTaskBReader;
import uk.ac.warwick.dcs.SemEval.models.ITweetReader;
import uk.ac.warwick.dcs.SemEval.models.Tweet;
import uk.ac.warwick.dcs.SemEval.models.Tweet;
import uk.ac.warwick.dcs.SemEval.models.AnnotationType.AnnotationKind;

public class TaskBReaderTest {

	@Test
	public void testReadTweets() throws Exception {
		ITweetReader taskBReader = new SemEvalTaskBReader("task-B-input.tsv.sample");
		
		List<Tweet> allTweets = taskBReader.readTweets();
		assertTrue(allTweets.size() == 5);
		Tweet read; 
		
		read = (Tweet) allTweets.get(0);
		assertTrue(read.getId1() == 260097528899452929L);
		assertTrue(read.getId2() == 595739778);
		assertTrue(read.getAnnotation().getKind() == AnnotationKind.Neutral);
		assertTrue(read.getText().equals("Won the match #getin . Plus, tomorrow is a very busy day, with Awareness Day's and debates. Gulp. Debates..."));
		
		read = (Tweet) allTweets.get(1);
		assertTrue(read.getId1() == 263791921753882624L);
		assertTrue(read.getId2() == 83619901);
		assertTrue(read.getAnnotation().getKind() == AnnotationKind.Neutral);
		assertTrue(read.getText().equals("Some areas of New England could see the first flakes of the season Tuesday."));

		read = (Tweet) allTweets.get(2);
		assertTrue(read.getId1() == 260486470828171265L);
		assertTrue(read.getId2() == 44399968);
		assertTrue(read.getAnnotation().getKind() == AnnotationKind.Neutral);
		assertTrue(read.getText().equals("Tina Fey &amp; Amy Poehler are hosting the Golden Globe awards on January 13. What do you think?"));
		
		read = (Tweet) allTweets.get(3);
		assertTrue(read.getId1() == 262968617233162240L);
		assertTrue(read.getId2() == 757209662);
		assertTrue(read.getAnnotation().getKind() == AnnotationKind.Positive);
		assertTrue(read.getText().equals("Lunch from my new Lil spot ...THE COTTON BOWL ....pretty good#1st#time#will be going back# http://t.co/Dbbj8xLZ"));
		
		read = (Tweet) allTweets.get(4);
		assertTrue(read.getId1() == 263790847424880641L);
		assertTrue(read.getId2() == 420538325);
		assertTrue(read.getAnnotation().getKind() == AnnotationKind.Positive);
		assertTrue(read.getText().equals("SNC Halloween Pr. Pumped. Let's work it for Sunday....Packers vs....who knows or caresn. #SNC #cheerpracticeonhalloween"));
	}

}
