package uk.ac.warwick.dcs.SemEval.subjectivity;

import uk.ac.warwick.dcs.SemEval.models.POSToken;

public class LowerCaseSubjectivityMap extends SubjectivityMap {

	@Override
	public String getMapRepresentation(POSToken p) {
		String key = super.getMapRepresentation(p);
		return key.toLowerCase();
	}
	
}
