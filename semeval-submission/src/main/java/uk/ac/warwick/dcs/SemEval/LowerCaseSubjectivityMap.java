package uk.ac.warwick.dcs.SemEval;

public class LowerCaseSubjectivityMap extends SubjectivityMap {

	@Override
	public String getMapRepresentation(POSToken p) {
		String key = super.getMapRepresentation(p);
		return key.toLowerCase();
	}
	
}
