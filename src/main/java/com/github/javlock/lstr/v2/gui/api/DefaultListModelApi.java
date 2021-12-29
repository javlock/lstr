package com.github.javlock.lstr.v2.gui.api;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import javax.swing.DefaultListModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultListModelApi<K, E> {

	private static final Logger LOGGER = LoggerFactory.getLogger("DefaultListModelApi");

	public final DefaultListModel<E> model = new DefaultListModel<>();
	public final ConcurrentHashMap<K, E> connectionInfoMap = new ConcurrentHashMap<>();

	public void addElement(K key, E element) {
		addElementIfAbsent(key, a -> element);
	}

	public E addElementIfAbsent(K key, Function<? super K, ? extends E> mappingFunction) {
		E element = connectionInfoMap.computeIfAbsent(key, mappingFunction);
		if (!model.contains(element)) {
			model.addElement(element);
		}
		return element;
	}

	public void clearGui() {
		model.clear();
	}

	public boolean contains(E element) {
		for (Object iterable_element : model.toArray()) {
			if (element == iterable_element || element.equals(iterable_element)) {
				return true;
			}
		}
		boolean modelExist = model.contains(element);
		boolean mapExist = connectionInfoMap.containsValue(element);
		return modelExist || mapExist;
	}

	public E getElement(K key) {
		return connectionInfoMap.get(key);
	}

	public void removeElement(K key, E element) {
		model.removeElement(element);
		connectionInfoMap.remove(key, element);
	}

}
