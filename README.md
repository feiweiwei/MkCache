# 轻量级开源缓存MkCache

## 前言

之前在很多项目中都用到了缓存，常用的有memcache、redis、ehcache等，这些都是目前主流的缓存中间件，今天我给大家介绍下自己写的一个轻量级的缓存框架MkCache，这个轻量级框架能够满足一般项目的使用，目前的0.1版本还比较简单只能实现线程安全的内存缓存，支持标准hash缓存、基于LRU算法的hash缓存、弱引用的hash缓存，后续会增加分布式集群支持，数据库一致性支持等功能。下面就简单介绍下如何使用和MkCache的架构和关键类。

github地址：https://github.com/feiweiwei/MkCache

## 如何使用MkCache

MkCache的使用非常简单，可以直接将源码放到项目的目录中，也可以引用jar包，后续会发布到OSS中这样直接在pom里就可以引用了。

普通的java对象缓存

```java
MkCache<String, String> cache = new MkCache<String, String>(new BasicDataStore<String, String>());
String key = "Hello";
cache.put(key, "World!");
Assert.assertEquals("World!", cache.get(key));
```

基于LRU算法的对象缓存

```java
MkCache<String, User> cache = new MkCache<String, User>(new LRUDataStore<String, User>(2));
String key = "fei";
User user = new User();
user.setName("fei");

String key1 = "du";
User user1 = new User();
user1.setName("du");

String key2 = "wang";
User user2 = new User();
user2.setName("wang");

cache.put(key, user);
cache.put(key1, user1);
cache.get(key);
cache.put(key2, user2);

if (cache.get(key) != null) {
  Assert.assertEquals("fei", cache.get(key).getName());		
}
if (cache.get(key1) != null) {
  Assert.assertEquals("du", cache.get(key1).getName());		
}
if (cache.get(key2) != null) {
  Assert.assertEquals("wang", cache.get(key2).getName());		
}
}
```

使用起来是不是很简单。

## MkCache架构

![Screenshot 2018-01-25 15.12.00](https://raw.githubusercontent.com/feiweiwei/MkCache/master/class-diagram.png)

整个MkCache缓存框架的核心类主要有这么几个，每一层都是面向接口编程这样可以方便的进行后续扩展。

| 类名                 | 功能                                       |
| ------------------ | ---------------------------------------- |
| ValueHolder        | 缓存value类型接口，所有缓存中Value类型都是实现该接口          |
| BasicValueHolder   | 缓存基础Vaule类，基础范型类，常用的缓存对象都可以使用该类          |
| WeakValueHolder    | 弱引用value数据类型，对于在缓存中需要弱引用的类型可以使用该类        |
| DataStore          | 缓存数据存储接口，所有缓存数据类型都实现该接口                  |
| BasicDataStore     | 缓存数据存储基础类，基本缓存存储类型没有特殊算法，没有特殊要求的缓存都可以使用该类 |
| WeakValueDataStore | 弱引用数据类型缓存数据类型                            |
| LRUDataStore       | 缓存数据存储LRU类，基本缓存存储类型缓存所有操作采用LRU算法         |
| LRUEntry           | LRU链表节点，里面定义了链表节点的前后节点的数据结构              |
| MkCache            | MkCache缓存核心调用类，都是缓存的接口都是通过这个接口对外暴露       |
|                    |                                          |



## MkCache关键类实现

MkCache类

```java
public class MkCache<K, V> {
	private final DataStore<K, V> store;

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
```



BasicDataStore类

```java
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
```

LRUDataStore类是相对复杂的一个基于LRU算法实现的数据缓存类型，类里维护了一个LRU链表，所有的get、put操作都基于LRU算法实现。

```java
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
```

LRUEntry类，LRU链表节点类，其中定义了一个单向链表的节点数据结构。

```java
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
```



## MkCache规划

MkCache目前0.1版本是比较轻量级的一个版本，该版本还不支持集群模式，只支持基本的分布式线性部署，而且也没有自动失效机制，后续会增加分布式集群支持，数据库一致性支持等功能，还要解决缓存穿透和缓存同时失效等缓存常见问题。




