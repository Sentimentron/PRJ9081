package uk.ac.warwick.dcs.SemEval;

public class StemmingSubjectivityMap extends SubjectivityMap {

	private PorterStemmer stemmer;
	
	public StemmingSubjectivityMap() {
		super();
		this.stemmer = new PorterStemmer();
	}
	
	@Override
	public String getMapRepresentation(POSToken p) {
		String key = p.token;
		key = stemmer.stem(key);
		return key;
	}
	
}
