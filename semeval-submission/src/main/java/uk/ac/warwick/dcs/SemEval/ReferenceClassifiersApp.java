package uk.ac.warwick.dcs.SemEval;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import edu.stanford.nlp.util.Pair;
import uk.ac.warwick.dcs.SemEval.io.NebraskaReader;
import uk.ac.warwick.dcs.SemEval.io.NebraskaReader.NebraskaDomain;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Evaluation;
import weka.core.Attribute;
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
	
	public static String buildClassifierName(String s, NebraskaDomain d) {
		return String.format("%s.%s.model", d.toString(), s);
	}
	
	private static void evaluateClassifier(AbstractClassifier cls, SentimentApp a) throws Exception {
		Instances i = a.createInstances();
		Evaluation e = new Evaluation(i);
		e.crossValidateModel(cls, i, 4, new Random());
		System.out.println(e.toSummaryString());
		System.out.println(e.toClassDetailsString());
	}
	
	private static AbstractClassifier createAndSaveSubjectivityClassifier(NebraskaDomain d) throws Exception {
		System.out.printf("Building subjectivity classifier for %s...\n", d.toString());
		NebraskaReader nr = getReader(d);
		SubjectivityApp sa = new SubjectivityApp(nr);
		sa.readTweets();
		sa.posTagTweets();
		sa.updateSubjectivityMap();
		AbstractClassifier subjectivity = sa.buildClassifier();
		saveClassifier(subjectivity, buildClassifierName("subj", d));
		evaluateClassifier(subjectivity, sa);
		
		ObjectOutputStream oos = new ObjectOutputStream(
				new FileOutputStream(buildClassifierName("subjMap", d))
				);
		oos.writeObject(sa.getSubjectivityMap());
		oos.flush();
		oos.close();
		
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
		
		ObjectOutputStream oos = new ObjectOutputStream(
				new FileOutputStream(buildClassifierName("wordMod", d))
				);
		oos.writeObject(wa.generateModifierWords());
		oos.flush();
		oos.close();
		
		return wcls;
	}
	
	private static AbstractClassifier createAndSaveDocumentClassifier(NebraskaDomain d) throws Exception {
		System.out.printf("Building document-level classifier for %s...\n", d.toString());
		NebraskaReader nr = getReader(d);
		PolarityApp pa = new PolarityApp(nr);
		pa.readTweets();
		pa.posTagTweets();
		Set<Pair<String, String>> thresholdedBigrams = pa.createThresholdedBigrams();
		Map<Pair<String, String>, Attribute> polarityAttrMap = pa.createAttributeMap(thresholdedBigrams);
		AbstractClassifier clfPolarity = pa.buildClassifier(thresholdedBigrams, polarityAttrMap);
		saveClassifier(clfPolarity, buildClassifierName("tweet", d));
		evaluateClassifier(clfPolarity, pa);
		
		ObjectOutputStream oos = new ObjectOutputStream(
				new FileOutputStream(buildClassifierName("tweetModAnn", d))
				);
		oos.writeObject(thresholdedBigrams);
		oos.writeObject(polarityAttrMap);
		oos.close();
		return clfPolarity;
	}

	private static void produceDomainSpecificClassifiers(NebraskaDomain d) throws Exception {
		createAndSaveSubjectivityClassifier(d);
		createAndSaveWordLevelClassifier(d);
		createAndSaveDocumentClassifier(d);
	}

}
