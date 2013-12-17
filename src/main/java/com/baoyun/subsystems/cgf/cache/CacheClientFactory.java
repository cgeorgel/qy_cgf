package com.baoyun.subsystems.cgf.cache;

import java.io.IOException;

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.MemcachedClientBuilder;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.utils.AddrUtil;

/**
 * <p>
 *
 * </p>
 *
 * TODO: 部署/运行时, MemcachedClient实例, 与MemcachedClientBuilder#getCacheClient()的返回值的关系;
 * 以及Configuration的具体信息.
 *
 * @author George
 *
 */
public class CacheClientFactory {
    private static MemcachedClient client = null;

    private static final MemcachedClientBuilder builder = null/*new XMemcachedClientBuilder(AddrUtil.getAddresses("localhost:11211"))*/;

    public static MemcachedClient getCacheClient() {
//        try {
//            if (client == null) {
//                client = builder.build();
//            }
//            return client;
//        } catch (IOException e) {
//            e.printStackTrace();
//            return null;
//		}
    	return null;
	}

}
