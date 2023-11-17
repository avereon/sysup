package com.southbranchcontrols.performstationupdater;

import com.avereon.skill.Controllable;
import com.avereon.xenon.Xenon;
import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class StationUpdateManager implements Controllable<StationUpdateManager> {

	@Getter
	private final Xenon program;

	private final Map<StationStatus, StationUpdater> updaters;

	public StationUpdateManager( Xenon program ) {
		this.program = program;
		this.updaters = new ConcurrentHashMap<>();
	}

	@Override
	public StationUpdateManager start() {
		return this;
	}

	@Override
	public boolean isRunning() {
		return true;
	}

	@Override
	public StationUpdateManager stop() {
		// TODO Shutdown all the updaters
		return this;
	}

	public StationUpdater getUpdater( StationStatus status ) {
		return updaters.computeIfAbsent( status, k -> StationUpdater.of( this, k ) );
	}

}
