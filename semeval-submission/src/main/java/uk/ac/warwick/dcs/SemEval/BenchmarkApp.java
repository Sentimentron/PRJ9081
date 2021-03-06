package uk.ac.warwick.dcs.SemEval;

import java.io.IOException;

import edu.stanford.nlp.util.Pair;
import uk.ac.warwick.dcs.SemEval.io.SemEvalTaskAReader;
import uk.ac.warwick.dcs.SemEval.io.SemEvalTaskATestReader;
import uk.ac.warwick.dcs.SemEval.io.SemEvalTaskAWriter;
import uk.ac.warwick.dcs.SemEval.models.TestingATweet;
import uk.ac.warwick.dcs.SemEval.models.Tweet;
import weka.classifiers.AbstractClassifier;

public class BenchmarkApp {

	public static void main(String[] args) throws Exception {

		SemEvalTaskAWriter testWriter = new SemEvalTaskAWriter("pred.A.constrained");
		SemEvalTaskATestReader testReader = new SemEvalTaskATestReader("twitter-test-gold-A.tsv");
		
		MultiTweetReader trainSrc = new MultiTweetReader();
		//trainSrc.addReader(new NebraskaReader("amt.sqlite"));
		trainSrc.addReader(new SemEvalTaskAReader("tweeter-dev-full-A-tweets.tsv"));
		trainSrc.addReader(new SemEvalTaskAReader("twitter-train-full-A.tsv"));
		
		SubjectivityApp subjectivitySource = new SubjectivityApp(trainSrc);
		subjectivitySource.readTweets();
		subjectivitySource.posTagTweets();
		subjectivitySource.updateSubjectivityMap();
		AbstractClassifier clfSubjective = subjectivitySource.buildClassifier();
		
		SubjectivityApp subjectivityTarget = new SubjectivityApp(testReader);
		subjectivityTarget.readTweets();
		subjectivityTarget.posTagTweets();
		subjectivityTarget.setSubjectivityMap(subjectivitySource.getSubjectivityMap());
		subjectivityTarget.applyPredictions(clfSubjective, subjectivitySource.getSubjectivityMap());
		
		WordSentimentApp wordAnnotationSource = new WordSentimentApp(trainSrc);
		wordAnnotationSource.readTweets();
		wordAnnotationSource.posTagTweets();
		wordAnnotationSource.updateSubjectivityMap();
		AbstractClassifier clfWords = wordAnnotationSource.buildClassifier();
		
		WordSentimentApp wordAnnotationTarget = new WordSentimentApp(subjectivityTarget.getTweets());
		wordAnnotationTarget.posTagTweets();
		wordAnnotationTarget.applyPredictions(clfWords, wordAnnotationSource.generateModifierWords());
		
		for (Tweet outputTweet : wordAnnotationTarget.getTweets()) {
			TestingATweet fmtOutput = (TestingATweet)outputTweet;
			for (Pair<Integer, Integer> section : fmtOutput.getInterestingSections()) {
				testWriter.writeTweet(outputTweet, section.first, section.second);
			}
		}
		testWriter.finish();
		
	}

}
