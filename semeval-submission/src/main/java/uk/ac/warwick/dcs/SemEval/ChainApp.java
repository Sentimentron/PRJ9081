package uk.ac.warwick.dcs.SemEval;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import weka.classifiers.AbstractClassifier;
import weka.core.Instances;

public class ChainApp {

	public static void main(String[] args) throws Exception {

		SubjectivityApp oracle = new SubjectivityApp();
		oracle.readTweets();
		oracle.posTagTweets();
    	oracle.updateSubjectivityMap();
    	
    	AbstractClassifier clf = oracle.buildClassifier();	
		SubjectivityApp stooge  = new SubjectivityApp();
		stooge.readTweets();
		stooge.posTagTweets();
		stooge.applyPredictions(clf, oracle.getSubjectivityMap());
		
		WordSentimentApp wa = new WordSentimentApp(stooge.getTweets());
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
		writer.close();
	}

}
