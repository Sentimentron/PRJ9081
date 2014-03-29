package uk.ac.warwick.dcs.SemEval;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import edu.stanford.nlp.util.Pair;

public class SemEvalTaskATestReader implements ITweetReader {

	private String path;
	
	public SemEvalTaskATestReader(String path) {
		this.path = path;
	}
	
	private Map<Integer, TestingTweet> readFromFile() throws Exception {
		
		BufferedReader br;
		String line;
		Map<Integer, TestingTweet> ret;
		
		ret = new TreeMap<Integer, TestingTweet>();
		br = new BufferedReader(new FileReader(this.path));
		
		while ((line = br.readLine()) != null) {
			String[] fields = line.split("\t");
			// First identifier is missing in test data
			int identifier = Integer.parseInt(fields[1]);
			int start = Integer.parseInt(fields[2]);
			int end   = Integer.parseInt(fields[3]);
			String text = fields[5];
			
			TestingTweet out;
			if (ret.containsKey(identifier)) {
				out = ret.get(identifier);
			}
			else {
				out = new TestingTweet(text, 0, identifier);
				ret.put(identifier, out);
			}
			
			out.addInterestingSection(start, end);
		}
		
		br.close();
		return ret;
	}
	
	
	@Override
	public List<Tweet> readTweets() throws Exception {
		List<Tweet> ret = new ArrayList<Tweet>();
		Map<Integer, TestingTweet> tweets = this.readFromFile();
		for (TestingTweet t : tweets.values()) {
			ret.add(t);
		}
		return ret;
	}

}
