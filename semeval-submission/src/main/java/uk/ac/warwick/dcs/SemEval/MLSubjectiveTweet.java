package uk.ac.warwick.dcs.SemEval;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.stanford.nlp.util.Pair;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class MLSubjectiveTweet {
	
	private POSTaggedTweet wrappedTweet;
	
	public MLSubjectiveTweet(POSTaggedTweet t) {
		this.wrappedTweet = t;
	}
	
	public POSTaggedTweet getWrappedTweet() {
		return this.wrappedTweet;
	}
		
	public List<Instance> addInstanceData(Instance template, Instances dataset, SubjectivityMap s) {
	
		List<Instance> ret = new ArrayList<Instance>();
		
		double prev1 = 0.0;
		double prev2 = 0.0;
		
		for (POSToken t : this.wrappedTweet.getPOSTokens()) {
			Instance cur = new DenseInstance(template);
			cur.setDataset(dataset);
			float subj = s.get(t);
			cur.setValue(0, prev1);
			cur.setValue(1, prev2);
			cur.setValue(2, subj);
			cur.setValue(3, t.getAnnotation().toNominalSubj());
			prev1 = prev2;
			prev2 = subj;
			ret.add(cur);
		}
		
		return ret;
	}

	public Iterable<Pair<Integer,Instance>> getPredictionInstances(Instance template, Instances dataset, SubjectivityMap s) {
		ArrayList<Pair<Integer, Instance>> ret = new ArrayList<Pair<Integer, Instance>>();
		double prev1 = 0.0;
		double prev2 = 0.0;
		
		for (POSToken t : this.wrappedTweet.getPOSTokens()) {
			Instance cur = new DenseInstance(template);
			cur.setDataset(dataset);
			float subj = s.get(t);
			cur.setValue(0, prev1);
			cur.setValue(1, prev2);
			cur.setValue(2, subj);
			prev1 = prev2;
			prev2 = subj;
			ret.add(new Pair<Integer, Instance>(t.endCharOffset, cur));
		}
		
		return ret;
	}
	
}
