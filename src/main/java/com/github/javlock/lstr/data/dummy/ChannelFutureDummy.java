package com.github.javlock.lstr.data.dummy;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

public class ChannelFutureDummy implements ChannelFuture {

	@Override
	public ChannelFuture addListener(GenericFutureListener<? extends Future<? super Void>> listener) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ChannelFuture addListeners(
			@SuppressWarnings("unchecked") GenericFutureListener<? extends Future<? super Void>>... listeners) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ChannelFuture await() throws InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean await(long timeoutMillis) throws InterruptedException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ChannelFuture awaitUninterruptibly() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean awaitUninterruptibly(long timeoutMillis) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean awaitUninterruptibly(long timeout, TimeUnit unit) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Throwable cause() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Channel channel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void get() throws InterruptedException, ExecutionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Void getNow() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isCancellable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCancelled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDone() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSuccess() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isVoid() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ChannelFuture removeListener(GenericFutureListener<? extends Future<? super Void>> listener) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ChannelFuture removeListeners(
			@SuppressWarnings("unchecked") GenericFutureListener<? extends Future<? super Void>>... listeners) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ChannelFuture sync() throws InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ChannelFuture syncUninterruptibly() {
		// TODO Auto-generated method stub
		return null;
	}

}
