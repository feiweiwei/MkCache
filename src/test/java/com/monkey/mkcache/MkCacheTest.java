package com.monkey.mkcache;


import com.monkey.mkcache.impl.BasicDataStore;
import com.monkey.mkcache.impl.LRUDataStore;
import com.monkey.mkcache.impl.WeakValueDataStore;
import org.junit.Assert;
import org.junit.Test;

public class MkCacheTest {
	@Test
	public void TestHelloWorld() {
		MkCache<String, String> cache = new MkCache<String, String>(new BasicDataStore<String, String>());
		String key = "Hello";
		cache.put(key, "World!");
		Assert.assertEquals("World!", cache.get(key));
	}

	@Test
	public void TestWeakValue() throws InterruptedException {
		MkCache<String, User> cache = new MkCache<String, User>(new WeakValueDataStore<String, User>());
		String key = "fei";
		User user = new User();
		user.setName("fei");
		cache.put(key, user);
		user = null;
		Assert.assertEquals("fei", cache.get(key).getName());

		System.gc();
		Thread.sleep(1000);
		System.out.println("Hello " + cache.get(key));
	}

	@Test
	public void TestLRU() {
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
			Assert.assertEquals("fei", cache.get(key).getName());		}
		if (cache.get(key1) != null) {
			Assert.assertEquals("du", cache.get(key1).getName());		}
		if (cache.get(key2) != null) {
			Assert.assertEquals("wang", cache.get(key2).getName());		}
		}

}
