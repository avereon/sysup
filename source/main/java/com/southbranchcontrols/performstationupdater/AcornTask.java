package com.southbranchcontrols.performstationupdater;

import com.avereon.xenon.task.Task;

public class AcornTask extends Task<Long> {

	//	private final AcornCounter counter;
	//
	//	public AcornTask( int threads ) {
	//		counter = new AcornMonitor( threads );
	//		setTotal( counter.getTotal() );
	//		counter.addListener( this::setProgress );
	//	}

	@Override
	public Long call() throws Exception {
		//		counter.start();
		//		counter.join();
		//		return counter.getScore();
		return 0L;
	}

}
