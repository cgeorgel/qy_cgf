package com.baoyun.subsystems.cgf.handler.processor.cdr;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class JavaUtilMapStorageBackend<T> implements StorageBackend<T> {

	private Map<String, T> storage = new HashMap<String, T>();

	@Override
	public boolean contains(String key) {

		return storage.containsKey(key);
	}

	@Override
	public void put(String key, T value) {

		storage.put(key, value);
	}

	@Override
	public T get(String key) {

		return storage.get(key);
	}

	@Override
	public void delete(String key) {

		storage.remove(key);
	}

	@Override
	public Iterator<T> iterator() {

		return storage.values().iterator();
	}

	@Override
	public Set<String> keySet() {

		return storage.keySet();
	}
}
