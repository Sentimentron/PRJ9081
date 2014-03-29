package uk.ac.warwick.dcs.SemEval;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import uk.ac.warwick.dcs.SemEval.AnnotationType.AnnotationKind;

public class MultiAnnotationMap extends AnnotationMap {

	MultiAnnotationMap(DuplicationStrategy s) {
		super(s);
	}
	
	public MultiAnnotationMap() {
		super(DuplicationStrategy.Replace);
	}

	@Override
	public int regenerate() {
		
		Map<Integer, List<AnnotationKind>> tmp = new TreeMap<Integer, List<AnnotationKind>>();
		
		int ret = 0;
		for (AnnotationSpan p : this.spanList) {
			int start = p.getStart();
			int end   = p.getEnd();
			for (int i = start; i <= end; i++) {
				if (i > ret) ret = i;
				if (tmp.containsKey(i)) {
					List<AnnotationKind> cur = tmp.get(i);
					cur.add(p.getKind());
				}
				else {
					List<AnnotationKind> cur = new ArrayList<AnnotationKind>();
					cur.add(p.getKind());
					tmp.put(i, cur);
				}
			}
		}
		
		this.impl.clear();
		
		for (Entry<Integer, List<AnnotationKind>> e : tmp.entrySet()) {
			List<AnnotationKind> kList = e.getValue();
			int position = e.getKey();
			AnnotationKind consensus = AnnotationType.computeConsensus(kList);
			this.impl.put(position, new AnnotationSpan(consensus, position, position));
		}
		
		return ret;
	}
	
	public void addAll(List<AnnotationSpan> spans) {
		for (AnnotationSpan s : spans) {
			this.spanList.add(s);
		}
		this.regenerate();
	}
	
}
