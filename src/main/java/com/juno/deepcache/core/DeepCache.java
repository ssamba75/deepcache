package com.juno.deepcache.core;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class DeepCache {

	public static final String PREFIX_DEEP_CACHE = "deepCache_";

	public static <K, V> Map<K, V> cacheable(Cache cache, String elementName, List<K> keys, FunctionInterface<List<K>, Map<K, V>> function) {

		try {

			List<K> elementKeysIsNotCachedYet = keys.stream()
					.map(dealNo -> {
						if (getElementFromEhCache(cache, elementName, dealNo) == null) {
							return dealNo;
						} else {
							return null;
						}
					})
					.filter(Objects::nonNull)
					.collect(Collectors.toList());

			if (!elementKeysIsNotCachedYet.isEmpty()) {

				Optional<Map<K, V>> apiResult = Optional.ofNullable(function.getResult(elementKeysIsNotCachedYet));
				if (apiResult.isPresent() == false) {
					return null;
				}

				if (!apiResult.get().isEmpty()) {
					apiResult.get().forEach((k, v) -> setElementOnEhCache(cache, elementName, k, v));
				}
			}

			Map<K, V> cachedElementMap = new HashMap<>();
			keys.stream().forEach(dealNo -> cachedElementMap.put(dealNo, (V) getElementFromEhCache(cache, elementName, dealNo).orElse(null)));
			return cachedElementMap;

		} catch (Exception e) {
			e.printStackTrace();
			return Collections.emptyMap();
		}

	}

	private static <K, V> Optional<V> getElementFromEhCache(Cache cache, String elementName, K key) {

		Optional<Element> cacheElement = Optional.ofNullable(cache.get(PREFIX_DEEP_CACHE + elementName + key.hashCode()));
		if (cacheElement.isPresent()) {
			return Optional.ofNullable((V) cacheElement.get().getObjectValue());
		} else {
			return null;
		}
	}

	private static <K, V> void setElementOnEhCache(Cache cache, String elementName, K key, V value) {
		cache.put(new Element(PREFIX_DEEP_CACHE + elementName + key.hashCode(), value));
	}

}
