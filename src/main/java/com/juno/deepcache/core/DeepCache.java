package com.juno.deepcache.core;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DeepCache {

	public static final String PREFIX_DEEP_CACHE = "deepCache_";

	/**
	 * Cacheable map.
	 *
	 * @param <K>         the type parameter
	 * @param <V>         the type parameter
	 * @param cache       the cache
	 * @param elementName the element name
	 * @param keys        the keys
	 * @param apiMethod   the api method
	 * @return the map
	 */
	public static <K, V> Map<K, V> cacheable(Cache cache, String elementName, List<K> keys, Function<List<K>, Map<K, V>> apiMethod) {

		try {

			List<K> elementKeysIsNotCachedYet = getKeysIsNotCachedYet(cache, elementName, keys);

			if (!elementKeysIsNotCachedYet.isEmpty()) {

				Optional<Map<K, V>> apiResult = Optional.ofNullable(apiMethod.apply(elementKeysIsNotCachedYet));
				if (apiResult.isPresent() && !apiResult.get().isEmpty()) {
					apiResult.get().forEach((k, v) -> setElementOnEhCache(cache, elementName, k, v));
				}
			}

			Map<K, V> cachedElementMap = new HashMap<>();
			keys.stream().forEach(dealNo -> cachedElementMap.put(dealNo, (V) getElementFromEhCache(cache, elementName, dealNo).orElse(null)));
			return cachedElementMap;

		} catch (Exception e) {
			return Collections.emptyMap();
		}
	}

	private static <K> List<K> getKeysIsNotCachedYet(Cache cache, String elementName, List<K> keys) {

		return keys.stream()
				.filter(key -> getElementFromEhCache(cache, elementName, key).isPresent() == false)
				.collect(Collectors.toList());
	}

	/**
	 * Cacheable map.
	 *
	 * @param <K>         the type parameter
	 * @param <V>         the type parameter
	 * @param cache       the cache
	 * @param elementName the element name
	 * @param keys        the keys
	 * @param apiMethod   the api method
	 * @param keyMapper   the key mapper
	 * @return the map
	 */
	public static <K, V> List<V> cacheable(Cache cache, String elementName, List<K> keys, Function<List<K>, List<V>> apiMethod, Function<V, K> keyMapper) {

		try {

			List<K> elementKeysIsNotCachedYet = getKeysIsNotCachedYet(cache, elementName, keys);

			if (!elementKeysIsNotCachedYet.isEmpty()) {

				Optional<List<V>> apiResult = Optional.ofNullable(apiMethod.apply(elementKeysIsNotCachedYet));
				if (apiResult.isPresent()) {
					Map<K, V> mappedApiResult = apiResult.get().stream().collect(Collectors.toMap(keyMapper, Function.identity()));

					if (!mappedApiResult.isEmpty()) {
						mappedApiResult.forEach((k, v) -> setElementOnEhCache(cache, elementName, k, v));
					}
				}
			}

			return keys.stream()
					.map(key -> (V) getElementFromEhCache(cache, elementName, key).orElse(null))
					.filter(Objects::nonNull)
					.collect(Collectors.toList());

		} catch (Exception e) {
			return Collections.emptyList();
		}
	}

	private static <K, V> Optional<V> getElementFromEhCache(Cache cache, String elementName, K key) {

		Element cacheElement = cache.get(PREFIX_DEEP_CACHE + elementName + key.hashCode());
		if (Objects.nonNull(cacheElement)) {
			return Optional.ofNullable((V) cacheElement.getObjectValue());
		} else {
			return Optional.ofNullable(null);
		}
	}

	private static <K, V> void setElementOnEhCache(Cache cache, String elementName, K key, V value) {

		cache.put(new Element(PREFIX_DEEP_CACHE + elementName + key.hashCode(), value));
	}
}
