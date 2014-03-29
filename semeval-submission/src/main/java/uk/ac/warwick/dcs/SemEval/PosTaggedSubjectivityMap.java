package uk.ac.warwick.dcs.SemEval;

public class PosTaggedSubjectivityMap extends SubjectivityMap {
	@Override
	public String getMapRepresentation(POSToken p) {
		String fmt = "%s/%s";
		String key = String.format("%s/%s", p.token, p.tag);
		return key;
	}
}
