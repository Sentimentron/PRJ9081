package uk.ac.warwick.dcs.SemEval;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import weka.core.Attribute;
import cmu.arktweetnlp.RawTagger;

public class SentimentApp {
	
	protected SemEvalTaskAReader r;
	protected SubjectivityMap sm;
	protected RawTagger posTagger;
	protected List<Tweet> tweets;
	protected List<POSTaggedTweet> taggedTweets;
	protected Set<String> modifierWords;
	protected Map<String, Attribute> attrMap;
	
	public SentimentApp() throws IOException {
		this.r = new SemEvalTaskAReader("tweeter-dev-full-A-tweets.tsv");				
		this.sm = new SubjectivityMap();
		this.posTagger = new RawTagger();
		posTagger.loadModel("model.20120919");
	}
	
	protected void readTweets() throws Exception {
		this.tweets = this.r.readTweets();
	}
	
	protected void posTagTweets() throws Exception {
		this.taggedTweets = new ArrayList<POSTaggedTweet>();
		for (Tweet t : tweets) {
			POSTaggedTweet p = new POSTaggedTweet(t, posTagger);
			taggedTweets.add(p);
		}
	}
	
	protected void updateSubjectivityMap() {
		for (POSTaggedTweet t : this.taggedTweets) {
			this.sm.updateFromTweet(t);
		}
	}
}
