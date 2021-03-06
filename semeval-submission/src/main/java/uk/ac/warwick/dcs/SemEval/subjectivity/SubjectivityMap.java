package uk.ac.warwick.dcs.SemEval.subjectivity;

import java.io.Serializable;

import uk.ac.warwick.dcs.SemEval.models.POSTaggedTweet;
import uk.ac.warwick.dcs.SemEval.models.POSToken;
import uk.ac.warwick.dcs.SemEval.utils.Counter;

public class SubjectivityMap implements Serializable {

	private static final long serialVersionUID = 7996978875904940291L;
	private Counter<String> subj_map = new Counter<String>();
	private Counter<String> freq_map = new Counter<String>();
	
	public String getMapRepresentation(POSToken p) {
		return String.format("%s", p.token);
	}
	
	public void clear() {
		// TODO Auto-generated method stub
		this.subj_map.clear();
		this.freq_map.clear();
	}

	public boolean containsKey(Object key) {
		return this.freq_map.containsKey(
				this.getMapRepresentation((POSToken)key)
		);
	}

	public Float get(POSToken p) {
		String key = this.getMapRepresentation(p);
		if (this.freq_map.containsKey(key)) {
			int val1 = this.freq_map.get(key);
			int val2 = this.subj_map.get(key);
			return (float)val2/val1;
		}
		return 0.0f; // No evidence
	}

	public boolean isEmpty() {
		return this.freq_map.isEmpty();
	}

	public void put(POSToken keyObj) {
		String key = this.getMapRepresentation(keyObj);
		if (key == null) return;
		this.freq_map.put(key);
		if (keyObj.getAnnotation().isSubjective()) {
			this.subj_map.put(key);
		}
	}

	public void remove(POSToken p) {
		String key = this.getMapRepresentation(p);
		if (p.getAnnotation().isSubjective()) {
			this.subj_map.remove(key);
		}
		this.freq_map.remove(key);
	}
	
	public void updateFromTweet(POSTaggedTweet p) {
		for (POSToken t : p.getPOSTokens()) {
			this.put(t);
		}
	}
}
