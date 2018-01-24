/*
 * Copyright (c) 2008-2018  monkey01 All Rights Reserved.
 *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 */

package com.monkey.mkcache;

/**
 * @author: feiweiwei
 * @description: MkCache缓存工具类
 * @created Date: 14:56 18/1/24.
 * @modify by:
 */
public class MkCache<K, V> {
	private final DataStore<K, V> store;
//	private static Logger logger = LoggerFactory.getLogger(MkCache.class);

	public MkCache(final DataStore<K, V> dataStore) {
		store = dataStore;
	}

	public V get(final K key) {
		try {
			ValueHolder<V> value = store.get(key);
			if (null == value) {
				return null;
			}
			return value.value();
		} catch (MkCacheException e) {
			System.out.println(e.getStackTrace().toString());
			return null;
		}
	}

	public void put(final K key, final V value) {
		try {
			store.put(key, value);
		} catch (MkCacheException e) {
			System.out.println(e.getStackTrace().toString());

		}
	}

	public V remove(K key) {
		try {
			ValueHolder<V> value = store.remove(key);
			return value.value();
		} catch (MkCacheException e) {
			System.out.println(e.getStackTrace().toString());

			return null;
		}
	}

	public void clear() {
		try {
			store.clear();
		} catch (MkCacheException e) {
			System.out.println(e.getStackTrace().toString());

		}
	}
}
