package uk.ac.warwick.dcs.SemEval;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import weka.core.Instances;

public class SubjectivityInvestigationApp {

	public static void main(String[] args) throws Exception {

		ITweetReader testSrc = new SemEvalTaskAReader("twitter-test-gold-A.tsv");
		
		MultiTweetReader trainSrc = new MultiTweetReader();
		trainSrc.addReader(new SemEvalTaskAReader("tweeter-dev-full-A-tweets.tsv"));
		trainSrc.addReader(new SemEvalTaskAReader("twitter-train-full-A.tsv"));
		
		SubjectivityApp subjectivityMapSource = new SubjectivityApp(trainSrc);
		subjectivityMapSource.readTweets();
		subjectivityMapSource.posTagTweets();
		subjectivityMapSource.updateSubjectivityMap();
		
		SubjectivityApp subjectivityInstanceSource = new SubjectivityApp(
				new SemEvalTaskAReader("tweeter-dev-full-A-tweets.tsv")
			);
		subjectivityInstanceSource.readTweets();
		subjectivityInstanceSource.posTagTweets();
		
		SubjectivityApp subjectivityTarget = new SubjectivityApp(testSrc);
		subjectivityTarget.readTweets();
		subjectivityTarget.posTagTweets();
		subjectivityTarget.setSubjectivityMap(subjectivityMapSource.getSubjectivityMap());
		
		Instances subjectiveInstances = subjectivityTarget.createInstances();
		BufferedWriter writer = new BufferedWriter(new FileWriter("subjectiveTarget.arff"));
		writer.write(subjectiveInstances.toString());
		writer.flush();
		writer.close();
		
		subjectivityInstanceSource.setSubjectivityMap(subjectivityMapSource.getSubjectivityMap());
		subjectiveInstances = subjectivityInstanceSource.createInstances();
		writer = new BufferedWriter(new FileWriter("subjectiveSource.arff"));
		writer.write(subjectiveInstances.toString());
		writer.flush();
		writer.close();
	}

}
