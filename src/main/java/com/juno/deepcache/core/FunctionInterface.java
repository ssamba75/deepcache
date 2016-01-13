package com.juno.deepcache.core;

import java.util.List;
import java.util.Map;

public interface FunctionInterface<K, V> {

	public <K, V> Map<K, V> getResult(List<K> keys);
}
