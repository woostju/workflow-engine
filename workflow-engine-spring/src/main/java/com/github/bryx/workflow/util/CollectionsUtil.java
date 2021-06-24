package com.github.bryx.workflow.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;

public class CollectionsUtil {
	
	public static <T> List<T> subList(List<T> list, int offset, int pageSize){
		if(list == null || list.size()==0) {
			return new ArrayList<>();
		}
		if (offset >= list.size()) {
			return new ArrayList<>();
		}
		if (offset + pageSize >= list.size()) {
			return list.subList(offset, list.size());
		} else {
			return list.subList(offset, offset + pageSize);
		}
	}
	
	public static boolean empty(Collection collection){
		return collection==null || collection.isEmpty();
	}

	public static boolean isNotEmpty(Collection collection){
		return !empty(collection);

	}
	
	public static boolean mapEmpty(Map map){
		return map==null || map.isEmpty();
	}

	public static boolean mapNotEmpty(Map map){
		return !mapEmpty(map);
	}

	public static <T> List<T> convertToArrayList(T[] array) {
		return new ArrayList<T>(Arrays.asList(array));
	}
	
	public static boolean containsAll(Collection<?> source, Collection<?> target) {
		HashSet<?> sourceSet = Sets.newHashSet(source);
		HashSet<?> targetSet = Sets.newHashSet(target);
		sourceSet.retainAll(targetSet);
		return sourceSet.size() == targetSet.size();
	}

	public static boolean containsAny(Collection<?> source, Collection<?> target) {
		HashSet<?> sourceSet = Sets.newHashSet(source);
		HashSet<?> targetSet = Sets.newHashSet(target);
		targetSet.retainAll(sourceSet);
		return !targetSet.isEmpty();
	}

	public static boolean equals(Collection<?> source, Collection<?> target) {
		Set<?> sourceSet = Sets.newHashSet(source);
		Set<?> targetSet = Sets.newHashSet(target);
		if (sourceSet.size()!=targetSet.size()) {
			return false;
		}
		sourceSet.removeAll(targetSet);
		return sourceSet.isEmpty();
	}
	
}
