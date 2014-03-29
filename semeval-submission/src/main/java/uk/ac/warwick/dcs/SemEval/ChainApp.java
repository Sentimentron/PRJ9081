package uk.ac.warwick.dcs.SemEval;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import weka.classifiers.AbstractClassifier;
import weka.core.Instances;

public class ChainApp {

	public static void main(String[] args) throws Exception {

		SubjectivityApp subjectivitySource = new SubjectivityApp(new NebraskaReader("amt.sqlite"));
		subjectivitySource.readTweets();
		subjectivitySource.posTagTweets();
		subjectivitySource.updateSubjectivityMap();
		AbstractClassifier clfSubjective = subjectivitySource.buildClassifier();
		
		SubjectivityApp subjectivityTarget = new SubjectivityApp(new SemEvalTaskAReader("twitter-test-gold-A.tsv"));
		subjectivityTarget.readTweets();
		subjectivityTarget.posTagTweets();
		subjectivityTarget.updateSubjectivityMap();
		subjectivityTarget.applyPredictions(clfSubjective, subjectivitySource.getSubjectivityMap());
		
		WordSentimentApp wordAnnotationSource = new WordSentimentApp(new NebraskaReader("amt.sqlite"));
		wordAnnotationSource.readTweets();
		wordAnnotationSource.posTagTweets();
		wordAnnotationSource.updateSubjectivityMap();
		AbstractClassifier clfWords = wordAnnotationSource.buildClassifier();
		
		WordSentimentApp wordAnnotationTarget = new WordSentimentApp(new SemEvalTaskAReader("twitter-test-gold-A.tsv"));
		wordAnnotationTarget.readTweets();
		wordAnnotationTarget.posTagTweets();
		wordAnnotationTarget.evaluateOn(clfWords, wordAnnotationSource.generateModifierWords());
		/*
		wordAnnotationTarget.applyPredictions(clfWords, wordAnnotationSource.getSubjectiveTokens());
		
		SemEvalTaskAWriter sw = new SemEvalTaskAWriter("amt-on-gold.pred");
		for (Tweet t : wordAnnotationTarget )
		
		
		WordSentimentApp wa = new WordSentimentApp(subjectivityTarget.getTweets());
		wa.posTagTweets();
		wa.createAttr();
		
		WordSentimentApp wb = new WordSentimentApp();
		wb.readTweets();
		wb.posTagTweets();
		wb.setAttributeMap(wa.getAttributeMap());
		
		Instances toExport = wb.createInstances();
		BufferedWriter writer = new BufferedWriter(new FileWriter("sentimentWithDerivedSubjectivity.arff"));
		writer.write(toExport.toString());
		writer.flush();
		writer.close();*/
	}

}
