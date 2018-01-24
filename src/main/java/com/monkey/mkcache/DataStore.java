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
 * @description: 缓存数据存储接口
 * @created Date: 14:54 18/1/24.
 * @modify by:
 */
public interface DataStore<K, V> {
	ValueHolder<V> get(K key) throws MkCacheException;

	PutStatus put(K key, V value) throws MkCacheException;

	ValueHolder<V> remove(K key) throws MkCacheException;

	void clear() throws MkCacheException;

	enum PutStatus {
		/**
		 * 新的数据插入
		 */
		PUT,
		/**
		 * 缓存中存在数据，进行缓存更新
		 */
		UPDATE,
		/**
		 * 缓存数据被删除
		 */
		DROP
	}
}
