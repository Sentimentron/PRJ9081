package uk.ac.warwick.dcs.SemEval.models;

import java.util.List;

public interface ITweetReader {

	public abstract List<Tweet> readTweets() throws Exception;

}