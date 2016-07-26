package fr.fabier.mapoffline.concurrent;

import java.util.concurrent.Future;

public interface ResultHandler<T> {
	public void taskCompleted(Future<T> result);
}