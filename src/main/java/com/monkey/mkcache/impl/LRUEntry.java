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

import java.util.Map.Entry;

import com.monkey.mkcache.ValueHolder;

/**
 * @author: feiweiwei
 * @description: LRU链表节点
 * @created Date: 14:43 18/1/24.
 * @modify by:
 */
public class LRUEntry<K, V extends ValueHolder<?>> implements Entry<K, ValueHolder<?>> {

	final K key; // non-null
	ValueHolder<?> v; // non-null

	LRUEntry<K, ValueHolder<?>> pre;
	LRUEntry<K, ValueHolder<?>> next;

	public LRUEntry<K, ValueHolder<?>> getPre() {
		return pre;
	}

	public void setPre(LRUEntry<K, ValueHolder<?>> pre) {
		this.pre = pre;
	}

	public LRUEntry<K, ValueHolder<?>> getNext() {
		return next;
	}

	public void setNext(LRUEntry<K, ValueHolder<?>> next) {
		this.next = next;
	}

	public LRUEntry(K key, V value) {
		super();

		this.key = key;
		this.v = value;
	}

	@Override
	public K getKey() {
		return key;
	}

	@Override
	public ValueHolder<?> getValue() {
		return this.v;
	}

	@Override
	public ValueHolder<?> setValue(ValueHolder<?> value) {
		ValueHolder<?> oldValue = this.v;
		this.v = value;
		return oldValue;
	}

}
