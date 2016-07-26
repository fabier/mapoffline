package fr.fabier.mapoffline.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class ListenableFutureTask<V> extends FutureTask<V> {

	private ResultHandler<V> resultHandler;

	public ListenableFutureTask(Callable<V> callable, ResultHandler<V> resultHandler) {
		super(callable);
		this.resultHandler = resultHandler;
	}

	@Override
	protected void done() {
		resultHandler.taskCompleted(this);
	}
}