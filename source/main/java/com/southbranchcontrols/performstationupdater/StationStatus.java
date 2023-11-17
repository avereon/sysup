package com.southbranchcontrols.performstationupdater;

import lombok.CustomLog;
import lombok.Data;

import java.util.Date;

@Data
@CustomLog
public class StationStatus {

	private final String name;

	private final String address;

	private StepStatus setup = new StepStatus( StepStatus.State.WAITING, new Date() );

	private StepStatus update = new StepStatus( StepStatus.State.WAITING, new Date() );

	private StepStatus upgrade = new StepStatus( StepStatus.State.WAITING, new Date() );

	private StepStatus restart = new StepStatus( StepStatus.State.WAITING, new Date() );

	public StationStatus( Station station ) {
		this.name = station.name();
		this.address = station.address();
//		try {
//			this.address = InetAddress.getByName( station.address() );
//		} catch( UnknownHostException exception ) {
//			log.atWarn().log( "Unable to resolve address {0}", station.address() );
//		}
	}

}
