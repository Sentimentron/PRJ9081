
package uk.ac.warwick.dcs.SemEval.utils;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class Counter<K> implements Map<K, Integer> {
	
	Map<K, Integer> map = new TreeMap<K, Integer>();

	@Override
	public void clear() {
		this.map.clear();
	}

	@Override
	public boolean containsKey(Object key) {
		return this.map.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return false; // Doesn't make sense!
	}

	@Override
	public Set<java.util.Map.Entry<K, Integer>> entrySet() {
		return this.map.entrySet();
	}

	@Override
	public Integer get(Object key) {
		
		if (this.map.containsKey(key)) {
			return this.map.get(key);
		}
		return 0;
	}

	@Override
	public boolean isEmpty() {
		return this.map.isEmpty();
	}

	@Override
	public Set<K> keySet() {
		return this.map.keySet();
	}

	@Override
	public Integer put(K key, Integer value) {
		if (this.map.containsKey(key)) {
			int c = this.map.get(key);
			c += value;
			this.map.put(key, c);
		}
		else {
			this.map.put(key, 1);
		}
		return value;
	}
	
	public Integer put(K key) {
		if (this.map.containsKey(key)) {
			int c = this.map.get(key);
			this.map.put(key, ++c);
			return c; 
		}
		this.map.put(key, 1);
		return 1;
	}

	@Override
	public void putAll(Map<? extends K, ? extends Integer> m) {
		for (Entry<? extends K, ? extends Integer> e : m.entrySet()) {
			if (this.map.containsKey(e.getKey())) {
				int c = this.map.get(e.getKey());
				c += e.getValue();
				this.map.put(e.getKey(), c);
			}
			else {
				this.map.put(e.getKey(), e.getValue());
			}
		}
		
	}

	@Override
	public Integer remove(Object keyObj) {
		K key = (K)keyObj;
		if (this.map.containsKey(key)) {
			int c = this.map.get(key);
			this.map.put(key, --c);
			return c;
		}
		return null;
	}

	@Override
	public int size() {
		return this.map.size();
	}

	@Override
	public Collection<Integer> values() {
		return null; // Senseless!
	}

}
