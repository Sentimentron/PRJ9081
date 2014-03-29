package uk.ac.warwick.dcs.SemEval;

public class BetaEstimatingSubjectivityMap extends SubjectivityMap {

	private int alpha;
	private int beta;
	private PorterStemmer stemmer;
	
	public BetaEstimatingSubjectivityMap() {
		this(2, 2);
	}
	
	public BetaEstimatingSubjectivityMap(int alpha, int beta) {
		this.alpha = alpha;
		this.beta = beta;
		this.stemmer = new PorterStemmer();
	}
	
	@Override
	public String getMapRepresentation(POSToken p) {
		String key = p.token;
		key = stemmer.stem(key);
		key = key.toLowerCase();
		key = key.replaceAll("[^a-z]","");
		key = key.trim();
		key = String.format("%s/%s", p.tag, key);
		return key;
	}
	
	@Override
	public Float get(POSToken p) {
		String key = this.getMapRepresentation(p);
		if (this.freq_map.containsKey(key)) {
			int total = this.freq_map.get(key);
			int successes = this.subj_map.get(key);
			double alpha = this.alpha + successes-1;
			double beta  = this.beta + (total-successes);
			
			return (float)((alpha)/(alpha + beta));
		}
		else {
			return (float)((this.alpha)/(this.alpha + this.beta));
		}
	}
}
