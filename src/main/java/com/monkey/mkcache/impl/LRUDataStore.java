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
 * @description: 缓存数据存储LRU类，基本缓存存储类型缓存所有操作采用LRU算法
 * @created Date: 13:49 18/1/24.
 * @modify by:
 */
public class LRUDataStore<K, V> implements DataStore<K, V> {

	public LRUDataStore(int maxSize) {
		super();
		this.maxSize = maxSize;
	}

	/**
	 * 构造函数定义LRU缓存数据存储的链表最大长度
	 */
	private final int maxSize;

	/**
	 * 定义一个ConcurrentHashMap，用于存储LRUEntry，存储数据结构为HashMap，逻辑数据结构为一个链表
	 */
	ConcurrentHashMap<K, LRUEntry<K, ValueHolder<?>>> map = new ConcurrentHashMap<K, LRUEntry<K, ValueHolder<?>>>();

	/**
	 * LRU链表的首节点first
	 */
	private LRUEntry<K, ValueHolder<?>> first;
	/**
	 * LRU链表的末节点last
	 */
	private LRUEntry<K, ValueHolder<?>> last;

	@SuppressWarnings("unchecked")
	@Override
	public ValueHolder<V> get(K key) throws MkCacheException {
		LRUEntry<K, ValueHolder<?>> entry = (LRUEntry<K, ValueHolder<?>>) getEntry(key);
		if (entry == null) {
			return null;
		}
		//获取到相应LRU节点后，将该节点移动到链表头
		moveToFirst(entry);
		return (ValueHolder<V>) entry.getValue();

	}

	@Override
	public PutStatus put(K key, V value) throws MkCacheException {
		LRUEntry<K, ValueHolder<?>> entry = (LRUEntry<K, ValueHolder<?>>) getEntry(key);
		PutStatus status = PutStatus.DROP;
		//如果LRU缓存中没有该key，则加入链表
		if (entry == null) {
			//如果LRU链表长度大于最大值则删除掉最后一个节点
			if (map.size() >= maxSize) {
				//map删除最后一个节点
				map.remove(last.getKey());
				//LRU链表末节点前移一个节点
				removeLast();
			}
			entry = new LRUEntry<K, ValueHolder<?>>(key, new BasicValueHolder<V>(value));
			status = PutStatus.PUT;
		} else {
			entry.setValue(new BasicValueHolder<V>(value));
			status = PutStatus.UPDATE;
		}
		//根据LRU算法将该节点移动到首节点
		moveToFirst(entry);
		map.put(key, entry);
		return status;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ValueHolder<V> remove(K key) throws MkCacheException {
		LRUEntry<K, ValueHolder<?>> entry = getEntry(key);
		//如果能查到该entry则进行删除操作
		if (entry != null) {
			if (entry.getPre() != null) {
				entry.getPre().setNext(entry.getNext());
			}
			if (entry.getNext() != null) {
				entry.getNext().setPre(entry.getPre());
			}
			if (entry == first) {
				first = entry.getNext();
			}
			if (entry == last) {
				last = entry.getPre();
			}
		}
		LRUEntry<K, ValueHolder<?>> oldValue = map.remove(key);
		return (ValueHolder<V>) oldValue.getValue();
	}

	@Override
	public void clear() throws MkCacheException {
		this.map.clear();
		this.first = this.last = null;
	}

	private void moveToFirst(LRUEntry<K, ValueHolder<?>> entry) {
		if (entry == first) {
			return;
		}
		if (entry.getPre() != null) {
			entry.getPre().setNext(entry.getNext());
		}
		if (entry.getNext() != null) {
			entry.getNext().setPre(entry.getPre());
		}
		if (entry == last) {
			last = last.getPre();
		}
		if (first == null || last == null) {
			first = last = entry;
			return;
		}

		entry.setNext(first);
		first.setPre(entry);
		first = entry;
		entry.setPre(null);
	}

	private void removeLast() {
		if (last != null) {
			last = last.getPre();
			if (last == null)
				first = null;
			else
				last.next = null;
		}
	}

	private LRUEntry<K, ValueHolder<?>> getEntry(K key) {
		return map.get(key);
	}

}
