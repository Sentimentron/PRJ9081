package uk.ac.warwick.dcs.SemEval;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.stanford.nlp.util.ArrayMap;
import edu.stanford.nlp.util.Pair;
import uk.ac.warwick.dcs.SemEval.AnnotationType.AnnotationKind;
import uk.ac.warwick.dcs.SemEval.exceptions.InvalidAnnotationSpanException;

public class AnnotationMap implements Map<Integer, AnnotationType> {

	private Map<Integer, AnnotationType> impl;
	private List<AnnotationSpan> spanList;
	
	public enum DuplicationStrategy {
		Replace
	};
	
	private DuplicationStrategy duplicateStrategy; 
	
	AnnotationMap(DuplicationStrategy s) {
		this.duplicateStrategy = s;
		this.spanList = new ArrayList<AnnotationSpan>();
		this.impl = new HashMap<Integer, AnnotationType>();
	}
	
	AnnotationMap(DuplicationStrategy s, Iterable<AnnotationSpan> types) {
		this(s);
		for (AnnotationSpan p : types) {
			this.spanList.add(p);
		}
	}
	
	public boolean equal(AnnotationMap o) {
		Set<Map.Entry<Integer,AnnotationType>> s1 = this.entrySet();
		Set<Map.Entry<Integer,AnnotationType>> s2 = o.entrySet();
		if (s1.containsAll(s2)) {
			if (s2.containsAll(s1)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean subjEqual(AnnotationMap o) {
		Set<Map.Entry<Integer,AnnotationType>> s1 = this.entrySet();
		Set<Map.Entry<Integer,AnnotationType>> s2 = o.entrySet();
		for (Map.Entry<Integer, AnnotationType> e1 : s1) {
			for (Map.Entry<Integer, AnnotationType> e2: s2) {
				if (e1.getKey() != e2.getKey()) continue;
				if (!(e1.getValue().isSubjective() && e1.getValue().isSubjective())) {
					return false;
				}
			}
		}
		return true;
	}
	
	public AnnotationMap clone() {
		List<AnnotationSpan> spanClones = new ArrayList<AnnotationSpan>();
		for (AnnotationSpan s : this.spanList) {
			spanClones.add((AnnotationSpan)s.clone());
		}
		return new AnnotationMap(this.duplicateStrategy, spanClones);
	}
	
	public void regenerate() {
		this.impl.clear();
		for (AnnotationSpan p : this.spanList) {
			int start = p.getStart();
			int end   = p.getEnd();
			for (int i = start; i <= end; i++) {
				if (this.impl.containsKey(i)) {
					// Use the duplicate strategy to decide what to do
					if (this.duplicateStrategy == DuplicationStrategy.Replace) {
						this.impl.put(i, new AnnotationSpan(p.getKind(), i, i));
					}
					else {
						throw new RuntimeException("Unknown duplication strategy.");
					}
				}
				else {
					this.impl.put(i, new AnnotationSpan(p.getKind(), i, i));
				}
			}
		}
	}
	
	@Override
	public void clear() {
		this.spanList.clear();
		this.regenerate();
	}

	@Override
	public boolean containsKey(Object arg0) {
		this.regenerate();
		return this.impl.containsKey(arg0);
	}

	@Override
	public boolean containsValue(Object arg0) {
		this.regenerate();
		return this.impl.containsValue(arg0);
	}

	@Override
	public Set<Entry<Integer, AnnotationType>> entrySet() {
		this.regenerate();
		return this.impl.entrySet();
	}

	@Override
	public AnnotationType get(Object arg0) {
		this.regenerate();
		int offset = (int)arg0;
		if (this.impl.containsKey(offset)) {
			return this.impl.get(offset);
		}
		return null;
	}

	@Override
	public boolean isEmpty() {
		return this.spanList.isEmpty();
	}

	@Override
	public Set<Integer> keySet() {
		this.regenerate();
		return this.impl.keySet();
	}

	public AnnotationSpan put(AnnotationSpan s) {
		
		if (s.getKind() == AnnotationKind.Objective) {
			return s; // Ignored 
		}
		
		this.spanList.add(s);
		return s;
	}
	
	@Override
	public AnnotationType put(Integer offset, AnnotationType span) {
		AnnotationSpan s;
		try {
			s = new AnnotationSpan(span.getKind(), offset, offset);
		}
		catch(InvalidAnnotationSpanException ex) {
			throw new RuntimeException(ex);
		}
		this.put(s);	
		return span;
	}

	@Override
	public void putAll(Map<? extends Integer, ? extends AnnotationType> of) {
		for (java.util.Map.Entry<? extends Integer, ? extends AnnotationType> e : of.entrySet()) {
			this.put(e.getKey(), e.getValue());
		}
	}

	@Override
	public AnnotationType remove(Object what) {
		return null;
	}
	
	public AnnotationSpan remove(AnnotationSpan s) {
		this.spanList.remove(s);
		return s;
	}

	@Override
	public int size() {
		this.regenerate();
		return this.impl.size();
	}

	@Override
	public Collection<AnnotationType> values() {
		return this.impl.values();
	}

	public void removeSliceShiftRightToLeft(int offset) {
		// Decrement all start offsets which appear above offset
		// (except if they appear on the offset)
		// Decrement all end offset which are above the offset
		List<AnnotationSpan> spans = new ArrayList<AnnotationSpan>();
		for (AnnotationSpan s : this.spanList) {
			int startOffset = s.getStart();
			int endOffset   = s.getEnd();
			if (startOffset > offset) startOffset--;
			else if (startOffset == offset) startOffset++; 
			if (endOffset >= offset) endOffset--;
			if (AnnotationSpan.isValid(startOffset, endOffset)) {
				AnnotationSpan r = new AnnotationSpan(s.getKind(), startOffset, endOffset);
				spans.add(r);
			}
		}
		this.spanList = spans;
		this.regenerate();
	}
	
	public void insertSlice(int offset) {
		// Increment all end offsets which occur above offset
		// Increment all start offsets which occur above offset 
		List<AnnotationSpan> spans = new ArrayList<AnnotationSpan>();
		for (AnnotationSpan s : this.spanList) {
			int startOffset = s.getStart();
			int endOffset   = s.getEnd();
			if (startOffset > offset) startOffset++; // If we're on the offset, keep the start here
			if (endOffset >= offset) endOffset++;
			if (AnnotationSpan.isValid(startOffset, endOffset)) {
				AnnotationSpan r = new AnnotationSpan(s.getKind(), startOffset, endOffset);
				spans.add(r);
			}
		}
		this.spanList = spans;
		this.regenerate();
	}
	
	public void removeSliceRange(int startOffset, int endOffset) {
		int slicesToRemove = endOffset - startOffset + 1;
		while(slicesToRemove > 0) {
			this.removeSliceShiftRightToLeft(startOffset);
			slicesToRemove--;
		}
	}
	
}
