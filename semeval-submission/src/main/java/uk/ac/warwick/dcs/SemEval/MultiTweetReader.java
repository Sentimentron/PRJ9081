package uk.ac.warwick.dcs.SemEval;

import java.util.ArrayList;
import java.util.List;

public class MultiTweetReader implements ITweetReader {

	private List<ITweetReader> readers;
	
	public MultiTweetReader() {
		this.readers = new ArrayList<ITweetReader>();
	}
	
	public void addReader(ITweetReader r) {
		this.readers.add(r);
	}
	
	@Override
	public List<Tweet> readTweets() throws Exception {
		
		List<Tweet> ret = new ArrayList<Tweet>();
		for (ITweetReader r : this.readers) {
			ret.addAll(r.readTweets());
		}
		
		return ret;
	}

}
