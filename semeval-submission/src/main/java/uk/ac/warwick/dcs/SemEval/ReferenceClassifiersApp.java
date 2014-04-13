package uk.ac.warwick.dcs.SemEval;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import uk.ac.warwick.dcs.SemEval.io.NebraskaReader;
import uk.ac.warwick.dcs.SemEval.io.NebraskaReader.NebraskaDomain;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;

public class ReferenceClassifiersApp {

	public static List<NebraskaDomain> getDomains() {
		List<NebraskaDomain> ret = new ArrayList<NebraskaDomain>();
		ret.add(NebraskaDomain.Finance);
		ret.add(NebraskaDomain.Politics);
		ret.add(NebraskaDomain.Tech);
		return ret;
	}
	
	public static void main(String[] args) throws Exception {
		for (NebraskaDomain d : ReferenceClassifiersApp.getDomains()) {
			ReferenceClassifiersApp.produceDomainSpecificClassifiers(d);
		}
	}
	
	private static NebraskaReader getReader(NebraskaDomain d) {
		return new NebraskaReader("amt.sqlite", d);
	}
	
	private static void saveClassifier(AbstractClassifier cl, String path) throws FileNotFoundException, IOException {
		ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(path));
		oos.writeObject(cl);
		oos.flush();
		oos.close();
	}
	
	private static String buildClassifierName(String s, NebraskaDomain d) {
		return String.format("%s.%s.model", d.toString(), s);
	}
	
	private static void evaluateClassifier(AbstractClassifier cls, SentimentApp a) throws Exception {
		Instances i = a.createInstances();
		Evaluation e = new Evaluation(i);
		e.crossValidateModel(cls, i, 4, new Random());
		System.out.println(e.toSummaryString());
	}
	
	private static AbstractClassifier createAndSaveSubjectivityClassifier(NebraskaDomain d) throws Exception {
		System.out.printf("Building subjectivity classifier for %s...\n", d.toString());
		NebraskaReader nr = getReader(d);
		SubjectivityApp sa = new SubjectivityApp(nr);
		sa.readTweets();
		sa.posTagTweets();
		AbstractClassifier subjectivity = sa.buildClassifier();
		saveClassifier(subjectivity, buildClassifierName("subj", d));
		evaluateClassifier(subjectivity, sa);
		return subjectivity;
	}
	
	private static AbstractClassifier createAndSaveWordLevelClassifier(NebraskaDomain d) throws Exception {
		System.out.printf("Building word-level classifier for %s...\n", d.toString());
		NebraskaReader nr = getReader(d);
		WordSentimentApp wa = new WordSentimentApp(nr);
		wa.readTweets();
		wa.posTagTweets();
		AbstractClassifier wcls = wa.buildClassifier();
		saveClassifier(wcls, buildClassifierName("word", d));
		evaluateClassifier(wcls, wa);
		return wcls;
	}
	
	private static AbstractClassifier createAndSaveDocumentClassifier(NebraskaDomain d) throws Exception {
		System.out.printf("Building document-level classifier for %s...\n", d.toString());
		NebraskaReader nr = getReader(d);
		PolarityApp pa = new PolarityApp(nr);
		pa.readTweets();
		pa.posTagTweets();
		AbstractClassifier wcls = pa.buildClassifier();
		saveClassifier(wcls, buildClassifierName("tweet", d));
		evaluateClassifier(wcls, pa);
		return wcls;
	}

	private static void produceDomainSpecificClassifiers(NebraskaDomain d) throws Exception {
		createAndSaveSubjectivityClassifier(d);
		createAndSaveWordLevelClassifier(d);
		createAndSaveDocumentClassifier(d);
	}

}
