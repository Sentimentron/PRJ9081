package uk.ac.warwick.dcs.SemEval.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import uk.ac.warwick.dcs.SemEval.models.ITweetReader;
import uk.ac.warwick.dcs.SemEval.models.TestingATweet;
import uk.ac.warwick.dcs.SemEval.models.Tweet;
import edu.stanford.nlp.util.Pair;

public class SemEvalTaskATestReader implements ITweetReader {

	private String path;
	
	public SemEvalTaskATestReader(String path) {
		this.path = path;
	}
	
	private Map<Integer, TestingATweet> readFromFile() throws Exception {
		
		BufferedReader br;
		String line;
		Map<Integer, TestingATweet> ret;
		
		ret = new TreeMap<Integer, TestingATweet>();
		br = new BufferedReader(new FileReader(this.path));
		
		while ((line = br.readLine()) != null) {
			String[] fields = line.split("\t");
			// First identifier is missing in test data in 2014
			String identifier1Str = fields[0];
			long identifier1 = 0;
			if (!identifier1Str.equals("NA")) {
				identifier1 = Long.parseLong(identifier1Str);
			}
			int identifier2 = Integer.parseInt(fields[1]);
			int start = Integer.parseInt(fields[2]);
			int end   = Integer.parseInt(fields[3]);
			String text = fields[5];
			
			TestingATweet out;
			if (ret.containsKey(identifier2)) {
				out = ret.get(identifier2);
			}
			else {
				out = new TestingATweet(text, identifier1, identifier2);
				ret.put(identifier2, out);
			}
			
			out.addInterestingSection(start, end);
		}
		
		br.close();
		return ret;
	}
	
	
	@Override
	public List<Tweet> readTweets() throws Exception {
		List<Tweet> ret = new ArrayList<Tweet>();
		Map<Integer, TestingATweet> tweets = this.readFromFile();
		for (TestingATweet t : tweets.values()) {
			ret.add(t);
		}
		return ret;
	}

}
