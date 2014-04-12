package uk.ac.warwick.dcs.SemEval;

import io.SemEvalTaskAReader;

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
		
		SubjectivityApp subjectivitySource = new SubjectivityApp(trainSrc);
		subjectivitySource.readTweets();
		subjectivitySource.posTagTweets();
		subjectivitySource.updateSubjectivityMap();
		
		SubjectivityApp subjectivityTarget = new SubjectivityApp(testSrc);
		subjectivityTarget.readTweets();
		subjectivityTarget.posTagTweets();
		subjectivityTarget.setSubjectivityMap(subjectivitySource.getSubjectivityMap());
		
		Instances subjectiveInstances = subjectivityTarget.createInstances();
		BufferedWriter writer = new BufferedWriter(new FileWriter("subjectiveTarget.arff"));
		writer.write(subjectiveInstances.toString());
		writer.flush();
		writer.close();
		
		subjectiveInstances = subjectivitySource.createInstances();
		writer = new BufferedWriter(new FileWriter("subjectiveSource.arff"));
		writer.write(subjectiveInstances.toString());
		writer.flush();
		writer.close();
	}

}
