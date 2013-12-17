package com.baoyun.subsystems.cgf.handler.processor.cdr;

import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.exception.MemcachedException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baoyun.subsystems.cgf.utils.MiscUtils;

public class MemcachedStorageBackend<T> implements StorageBackend<T> {

	private Logger log = LoggerFactory.getLogger(MemcachedStorageBackend.class);

	private Set<String> keySet = new HashSet<String>();

	private MemcachedClient storage;

	public MemcachedStorageBackend() {

	}

	// TODO: handle excpetions: TimeoutException, InterruptedException,
	// MemcachedException...

	@Override
	public boolean contains(String key) {

		try {
			return storage.get(key) != null;
		} catch (TimeoutException e) {

			log.error(MiscUtils.exceptionStackTrace2String(e));
		} catch (InterruptedException e) {

			log.error(MiscUtils.exceptionStackTrace2String(e));
		} catch (MemcachedException e) {

			log.error(MiscUtils.exceptionStackTrace2String(e));
		}
		return false;
	}

	@Override
	public void put(String key, T value) {

		keySet.add(key);
		try {
			storage.add(key, 0, value);
		} catch (TimeoutException e) {

			log.error(MiscUtils.exceptionStackTrace2String(e));
		} catch (InterruptedException e) {

			log.error(MiscUtils.exceptionStackTrace2String(e));
		} catch (MemcachedException e) {

			log.error(MiscUtils.exceptionStackTrace2String(e));
		}
	}

	@Override
	public T get(String key) {

		T value = null;
		try {
			value = storage.get(key);
		} catch (TimeoutException e) {

			log.error(MiscUtils.exceptionStackTrace2String(e));
		} catch (InterruptedException e) {

			log.error(MiscUtils.exceptionStackTrace2String(e));
		} catch (MemcachedException e) {

			log.error(MiscUtils.exceptionStackTrace2String(e));
		}

		return value;
	}

	@Override
	public void delete(String key) {

		keySet.remove(key);
		try {
			storage.delete(key);
		} catch (TimeoutException e) {

			log.error(MiscUtils.exceptionStackTrace2String(e));
		} catch (InterruptedException e) {

			log.error(MiscUtils.exceptionStackTrace2String(e));
		} catch (MemcachedException e) {

			log.error(MiscUtils.exceptionStackTrace2String(e));
		}
	}

	@Override
	public Set<String> keySet() {

		return keySet;
	}

	@Override
	public Iterator<T> iterator() {

		return new Iterator<T>() {

			String currKey;

			Iterator<String> keyIterator = keySet.iterator();

			@Override
			public boolean hasNext() {

				return keyIterator.hasNext();
			}

			@Override
			public T next() {

				currKey = keyIterator.next();
				T value = null;
				try {
					value = storage.get(currKey);
				} catch (TimeoutException e) {

					log.error(MiscUtils.exceptionStackTrace2String(e));
				} catch (InterruptedException e) {

					log.error(MiscUtils.exceptionStackTrace2String(e));
				} catch (MemcachedException e) {

					log.error(MiscUtils.exceptionStackTrace2String(e));
				}
				return value;
			}

			@Override
			public void remove() {

				try {
					storage.delete(currKey);
				} catch (TimeoutException e) {

					log.error(MiscUtils.exceptionStackTrace2String(e));
				} catch (InterruptedException e) {

					log.error(MiscUtils.exceptionStackTrace2String(e));
				} catch (MemcachedException e) {

					log.error(MiscUtils.exceptionStackTrace2String(e));
				}
			}
		};
	}
}
