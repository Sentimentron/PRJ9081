package uk.ac.warwick.dcs.SemEval;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import uk.ac.warwick.dcs.SemEval.io.SemEvalTaskATestReader;
import uk.ac.warwick.dcs.SemEval.models.TestingATweet;
import uk.ac.warwick.dcs.SemEval.models.Tweet;
import edu.stanford.nlp.util.Pair;

public class TestReaderTest {

	@Test
	public void testReadTweets() throws Exception {
		SemEvalTaskATestReader tr = new SemEvalTaskATestReader("test.sample");
		List<Tweet> tweets = tr.readTweets();
		assertTrue(tweets.size() == 3);
		List<Pair<Integer, Integer>> interestingSections;
		Pair<Integer, Integer> section;
		TestingATweet t;
		
		t = (TestingATweet)tweets.get(0);
		assertTrue(t.getId2() == 1);
		assertTrue(t.getText().equals("@GMA My daughter @jordynking17 will be 14 on December 17th! We r going to see Buddy @ The Kirby Center in WB on December 14th. So excited!"));
		interestingSections = t.getInterestingSections();
		assertTrue(interestingSections.size() == 1);
		section = interestingSections.get(0);
		assertTrue(section.first == 25);
		assertTrue(section.second == 26);
		
		t = (TestingATweet)tweets.get(1);
		assertTrue(t.getId2() == 2);
		assertTrue(t.getText().equals("@moanajkidd The massacre on March 11 in Karm al-Zaitoun was similar: 25 children killed with knives\\u002c 20 women: https://t.co/hxc5Kexv"));
		interestingSections = t.getInterestingSections();
		assertTrue(interestingSections.size() == 1);
		section = interestingSections.get(0);
		assertTrue(section.first == 13);
		assertTrue(section.second == 13);
		
		t = (TestingATweet)tweets.get(2);
		assertTrue(t.getId2() == 3);
		assertTrue(t.getText().equals("@BarackObama\\u002c Clinton\\u002c Panetta\\u002c Petraeus we will not #StandDown on Nov 6 or Nov 7 or Nov 8th. Do the right thing now. #WeWillNotLetThisGo"));
		interestingSections = t.getInterestingSections();
		assertTrue(interestingSections.size() == 1);
		section = interestingSections.get(0);
		assertTrue(section.first == 7);
		assertTrue(section.second == 7);
	}

}
