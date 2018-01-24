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

package com.monkey.mkcache.impl;

import java.util.concurrent.ConcurrentHashMap;

import com.monkey.mkcache.DataStore;
import com.monkey.mkcache.MkCacheException;
import com.monkey.mkcache.ValueHolder;

/**
 * @author: feiweiwei
 * @description: 缓存数据存储基础类，基本缓存存储类型没有特殊算法
 * @created Date: 13:42 18/1/24.
 * @modify by:
 */
public class BasicDataStore<K, V> implements DataStore<K, V> {

	ConcurrentHashMap<K, ValueHolder<V>> map = new ConcurrentHashMap<K, ValueHolder<V>>();

	@Override
	public ValueHolder<V> get(K key) throws MkCacheException {
		return map.get(key);
	}

	@Override
	public PutStatus put(K key, V value) throws MkCacheException {
		ValueHolder<V> v = new BasicValueHolder<V>(value);
		map.put(key, v);
		return PutStatus.PUT;
	}

	@Override
	public ValueHolder<V> remove(K key) throws MkCacheException {
		return map.remove(key);
	}

	@Override
	public void clear() throws MkCacheException {
		map.clear();
	}

}
