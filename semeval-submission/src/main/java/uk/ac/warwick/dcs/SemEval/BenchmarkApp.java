package uk.ac.warwick.dcs.SemEval;

import java.io.IOException;

import edu.stanford.nlp.util.Pair;
import weka.classifiers.AbstractClassifier;

public class BenchmarkApp {

	public static void main(String[] args) throws Exception {

		SemEvalTaskAWriter testWriter = new SemEvalTaskAWriter("output.pred");
		SemEvalTaskATestReader testReader = new SemEvalTaskATestReader("tweeter-dev-full-A-tweets-sample.tsv");				//new SemEvalTaskATestReader("twitter-test-gold-A.tsv");
		ITweetReader trainSrc = new SemEvalTaskAReader("tweeter-dev-full-A-tweets.tsv");	//new NebraskaReader("amt.sqlite");
		
		SubjectivityApp subjectivitySource = new SubjectivityApp(trainSrc);
		subjectivitySource.readTweets();
		subjectivitySource.posTagTweets();
		subjectivitySource.updateSubjectivityMap();
		AbstractClassifier clfSubjective = subjectivitySource.buildClassifier();
		
		SubjectivityApp subjectivityTarget = new SubjectivityApp(testReader);
		subjectivityTarget.readTweets();
		subjectivityTarget.posTagTweets();
		subjectivityTarget.updateSubjectivityMap();
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
			TestingTweet fmtOutput = (TestingTweet)outputTweet;
			for (Pair<Integer, Integer> section : fmtOutput.getInterestingSections()) {
				testWriter.writeTweet(outputTweet, section.first, section.second);
			}
		}
		testWriter.finish();
		
	}

}
