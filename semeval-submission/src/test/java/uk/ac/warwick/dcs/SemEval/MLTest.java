package uk.ac.warwick.dcs.SemEval;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import uk.ac.warwick.dcs.SemEval.AnnotationType.AnnotationKind;
import uk.ac.warwick.dcs.SemEval.exceptions.WordRangeMapException;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SimpleLogistic;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import cmu.arktweetnlp.RawTagger;
import edu.stanford.nlp.util.Pair;

public class MLTest {
	
	private SubjectivityMap s;
	private RawTagger       tagger;
	
	
	@Before
	public void setup() throws IOException {
		this.s = new SubjectivityMap();
		this.tagger = new RawTagger();
		this.tagger.loadModel("model.20120919");
	}

	@Test
	public void testMatchingUp() throws Exception {
		Tweet t1 = new Tweet("In case you weren't invited . . .: ..it 'may' have been because Andy was hiding a deep,dark secret Personali... http://bit.ly/WdxawR ");
		t1.addAnnotation(new AnnotationSpan(AnnotationKind.Negative, 3, 4));
		t1.addAnnotation(new AnnotationSpan(AnnotationKind.Negative, 17, 20));

		Tweet t2 = new Tweet("Barclays Profit Driven by Investment Banking: LONDON--Barclays PLC (BCS) Wednesday continued to reap the rewards... http://bit.ly/W5t0au ");
		t2.addAnnotation(new AnnotationSpan(AnnotationKind.Positive, 10, 14));

		POSTaggedTweet p1 = new POSTaggedTweet(t1, this.tagger);
		POSTaggedTweet p2 = new POSTaggedTweet(t2, this.tagger);
		this.s.updateFromTweet(p1);
		this.s.updateFromTweet(p2);
		
		MLSubjectiveTweet m1 = new MLSubjectiveTweet(p1);
		MLSubjectiveTweet m2 = new MLSubjectiveTweet(p2);
		
		List<String> nominalVals = AnnotationType.getNominalSubjList();
		ArrayList<Attribute> attrs = new ArrayList<Attribute>();
		attrs.add(new Attribute("conf1"));
		attrs.add(new Attribute("conf2"));
		attrs.add(new Attribute("conf3"));
		Attribute classAttr = new Attribute("subjective", nominalVals);
		attrs.add(classAttr);
		
		Instances setInstances = new Instances("subj", attrs, 0);
		setInstances.setClassIndex(3);
		
		Instance instanceTemplate = new DenseInstance(4);
		instanceTemplate.setDataset(setInstances);
		
		for (Instance dat : m1.addInstanceData(instanceTemplate, setInstances, this.s)) {
			assertTrue(setInstances.checkInstance(dat));
			setInstances.add(dat);
		}
		for (Instance dat : m2.addInstanceData(instanceTemplate, setInstances, this.s)) {
			assertTrue(setInstances.checkInstance(dat));
			setInstances.add(dat);
		}
		
		AbstractClassifier clf = new SimpleLogistic();
		clf.buildClassifier(setInstances);
		Evaluation elv = new Evaluation(setInstances);
		//elv.crossValidateModel(clf, setInstances, 1, new Random());
		elv.evaluateModel(clf, setInstances);
		System.out.println(elv.toClassDetailsString());
		System.out.println(elv.toSummaryString());
		System.out.println(elv.toClassDetailsString());
		System.out.println(elv.toMatrixString());
		System.out.println(clf);
		
		for (Pair<Integer, Instance> dat : m1.getPredictionInstances(instanceTemplate, setInstances, this.s)) {
			double prediction = clf.classifyInstance(dat.second);
			String predictionStr = classAttr.value((int) prediction);
			AnnotationType a = new AnnotationType(predictionStr);
			m1.getWrappedTweet().applyDerivedAnnotation(dat.first, a);
		}
		
		for (Pair<Integer, Instance> dat : m2.getPredictionInstances(instanceTemplate, setInstances, this.s)) {
			double prediction = clf.classifyInstance(dat.second);
			String predictionStr = classAttr.value((int) prediction);
			AnnotationType a = new AnnotationType(predictionStr);
			m2.getWrappedTweet().applyDerivedAnnotation(dat.first, a);
		}
		
		assertTrue(m1.getWrappedTweet().getParent().subjEqual(t1));
		assertTrue(m2.getWrappedTweet().getParent().subjEqual(t2));
	}
	
	@Test
	public void testML() throws Exception {
		
		Tweet t1 = new Tweet("In case you weren't invited . . .: ..it 'may' have been because Andy was hiding a deep,dark secret Personali... http://bit.ly/WdxawR ");
		t1.addAnnotation(new AnnotationSpan(AnnotationKind.Negative, 3, 4));
		t1.addAnnotation(new AnnotationSpan(AnnotationKind.Negative, 17, 20));

		Tweet t2 = new Tweet("Barclays Profit Driven by Investment Banking: LONDON--Barclays PLC (BCS) Wednesday continued to reap the rewards... http://bit.ly/W5t0au ");
		t2.addAnnotation(new AnnotationSpan(AnnotationKind.Positive, 10, 14));

		POSTaggedTweet p1 = new POSTaggedTweet(t1, this.tagger);
		POSTaggedTweet p2 = new POSTaggedTweet(t2, this.tagger);
		this.s.updateFromTweet(p1);
		this.s.updateFromTweet(p2);
		
		MLSubjectiveTweet m1 = new MLSubjectiveTweet(p1);
		MLSubjectiveTweet m2 = new MLSubjectiveTweet(p2);
		
		List<String> nominalVals = AnnotationType.getNominalSubjList();
		ArrayList<Attribute> attrs = new ArrayList<Attribute>();
		attrs.add(new Attribute("conf1"));
		attrs.add(new Attribute("conf2"));
		attrs.add(new Attribute("conf3"));
		attrs.add(new Attribute("subjective", nominalVals));
		
		Instances setInstances = new Instances("subj", attrs, 0);
		setInstances.setClassIndex(3);
		
		Instance instanceTemplate = new DenseInstance(4);
		instanceTemplate.setDataset(setInstances);
		
		for (Instance dat : m1.addInstanceData(instanceTemplate, setInstances, this.s)) {
			assertTrue(setInstances.checkInstance(dat));
			setInstances.add(dat);
		}
		for (Instance dat : m2.addInstanceData(instanceTemplate, setInstances, this.s)) {
			assertTrue(setInstances.checkInstance(dat));
			setInstances.add(dat);
		}
		
		AbstractClassifier clf = new SimpleLogistic();
		clf.buildClassifier(setInstances);
		Evaluation elv = new Evaluation(setInstances);
		//elv.crossValidateModel(clf, setInstances, 1, new Random());
		elv.evaluateModel(clf, setInstances);
		System.out.println(elv.toClassDetailsString());
		System.out.println(elv.toSummaryString());
		System.out.println(elv.toClassDetailsString());
		System.out.println(elv.toMatrixString());
		System.out.println(clf);
		
		
	}

}
