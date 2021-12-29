package com.github.javlock.lstr.v2.gui.api;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.DefaultListModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javlock.lstr.data.Message;
import com.github.javlock.lstr.v2.data.AppInfo;
import com.github.javlock.lstr.v2.data.Data;

public class DefaultListModelApi<E> extends DefaultListModel<E> {
	private static final long serialVersionUID = -2216686696758956440L;
	private static final transient Logger LOGGER = LoggerFactory.getLogger("");

	public final ConcurrentHashMap<Object, Serializable> connectionInfoMap = new ConcurrentHashMap<>();

	@Override
	public void addElement(E element) {

		if (element instanceof Message) {
			Message data = ((Message) element);
			String key = data.getId();
			connectionInfoMap.put(key, data);
		} else if (element instanceof AppInfo) {
			AppInfo data = ((AppInfo) element);
			Object key = data.getId();
			connectionInfoMap.put(key, data);
		} else {
			throw new UnsupportedOperationException(
					String.format("impl get key in (addElement) for %s", element.getClass().getSimpleName()));
		}

		LOGGER.info("connectionInfoMap:{}", connectionInfoMap);

		super.addElement(element);
	}

	@Override
	public boolean contains(Object elem) {
		Object key = null;
		if (elem instanceof Message) {
			key = ((Message) elem).getId();
		} else if (elem instanceof AppInfo) {
			key = ((AppInfo) elem).getId();
		} else {
			throw new UnsupportedOperationException(
					String.format("impl get key in (contains) for %s", elem.getClass().getSimpleName()));
		}

		if (connectionInfoMap.contains(key)) {
			return true;
		}
		return super.contains(elem);
	}

	@Override
	public boolean removeElement(Object obj) {
		Data data = (Data) obj;
		Object key = data.getId();
		connectionInfoMap.remove(key, data);

		return super.removeElement(obj);
	}
}
