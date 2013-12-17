package com.baoyun.subsystems.cgf.handler.processor.cdr;

import java.util.Iterator;
import java.util.Set;

public interface StorageBackend<T> {

	public boolean contains(String key);

	public void put(String key, T value);

	public T get(String key);

	public void delete(String key);

	public Set<String> keySet();

	public Iterator<T> iterator();
}
