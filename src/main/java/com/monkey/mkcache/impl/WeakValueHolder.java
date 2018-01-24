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

import java.lang.ref.WeakReference;

import com.monkey.mkcache.ValueHolder;

/**
 * @author: feiweiwei
 * @description: 弱引用value数据类型
 * @created Date: 14:53 18/1/24.
 * @modify by:
 */
public class WeakValueHolder<V> implements ValueHolder<V> {

	public WeakValueHolder(V value) {
		super();
		if (null == value) {
			return;
		}
		this.v = new WeakReference<V>(value);
	}

	private WeakReference<V> v;

	@Override
	public V value() {
		return this.v.get();
	}

}
