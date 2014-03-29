package uk.ac.warwick.dcs.SemEval;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import uk.ac.warwick.dcs.SemEval.AnnotationType.AnnotationKind;

public class WriterTest {

	List<Tweet> tweetsToWrite;
	List<Integer> offsetStartsToWrite;
	List<Integer> offsetEndsToWrite;
	
	@Before
	public void setup() {
		
		this.tweetsToWrite = new ArrayList<Tweet>();
		this.offsetEndsToWrite = new ArrayList<Integer>();
		this.offsetStartsToWrite = new ArrayList<Integer>();
		
		Tweet t;
		int start;
		int end;
		AnnotationKind k;
		
		t = new Tweet("That's great!", 418381654813081601L, 15115101);
		start = 0;
		end = 1;
		k = AnnotationKind.Negative;
		t.addAnnotation(new AnnotationSpan(k, start, end));
		this.tweetsToWrite.add(t);
		this.offsetEndsToWrite.add(end);
		this.offsetStartsToWrite.add(start);
		
		t = new Tweet("Going to Delaware on Sat.", 418381654813081602L, 15115101);
		start = 0;
		end = 1;
		k = AnnotationKind.Positive;
		t.addAnnotation(new AnnotationSpan(k, start, end));
		this.tweetsToWrite.add(t);
		this.offsetEndsToWrite.add(end);
		this.offsetStartsToWrite.add(start);
		
		t = new Tweet("who's up for some enchiladas tonite?", 418381654813081603L, 15115101);
		start = 1;
		end = 2;
		k = AnnotationKind.Neutral;
		t.addAnnotation(new AnnotationSpan(k, start, end));
		this.tweetsToWrite.add(t);
		this.offsetEndsToWrite.add(end);
		this.offsetStartsToWrite.add(start);
		
	}
	
	@Test
	public void testWriter() throws IOException {
		
		SemEvalTaskAWriter wt = new SemEvalTaskAWriter("taskA.pred.sample.output");
		
		for (int i = 0; i < this.tweetsToWrite.size(); i++) {
			wt.writeTweet(this.tweetsToWrite.get(i), 
					this.offsetStartsToWrite.get(i),
					this.offsetEndsToWrite.get(i)
				);
		}
		
		wt.finish();
		
		File outputFile = new File("taskA.pred.sample.output");
		File referenceFile = new File("taskA.pred.sample");
		
		assertTrue(FileUtils.contentEquals(outputFile, referenceFile));
		
	}

}
