package com.southbranchcontrols.performstationupdater;

import lombok.CustomLog;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

@CustomLog
class SystemCpuLoadCheck extends TimerTask {

	private final OperatingSystemMXBean bean;

	private final Method method;

	private final Set<Consumer<Double>> listeners;

	public SystemCpuLoadCheck() {
		Method localMethod = null;
		ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors();
		this.bean = ManagementFactory.getOperatingSystemMXBean();
		for( Method method : bean.getClass().getMethods() ) {
			if( "getSystemCpuLoad".equals( method.getName() ) ) localMethod = method;
		}
		this.method = localMethod;
		this.listeners = new CopyOnWriteArraySet<>();
	}

	public void addListener( Consumer<Double> listener ) {
		listeners.add( listener );
	}

	public void removeListener( Consumer<Double> listener ) {
		listeners.remove( listener );
	}

	@Override
	public void run() {
		if( method == null ) return;
		try {
			Double result = (Double)method.invoke( bean );
			for( Consumer<Double> listener : listeners ) {
				try {
					listener.accept( result );
				} catch( Throwable throwable ) {
					log.atWarning().withCause( throwable ).log( "Error consuming system CPU load" );
				}
			}
		} catch( IllegalAccessException | InvocationTargetException exception ) {
			log.atSevere().withCause( exception ).log( "Error getting system CPU load" );
			cancel();
		}
	}

}
