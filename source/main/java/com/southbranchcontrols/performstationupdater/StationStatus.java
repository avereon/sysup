package com.southbranchcontrols.performstationupdater;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import lombok.CustomLog;
import lombok.Getter;

import java.util.Date;

@CustomLog
public class StationStatus {

	@Getter
	private final String name;

	@Getter
	private final String address;

	private final SimpleObjectProperty<StepStatus> setupStatusProperty;

	private final SimpleObjectProperty<StepStatus> updateStatusProperty;

	private final SimpleObjectProperty<StepStatus> upgradeStatusProperty;

	private final SimpleObjectProperty<StepStatus> restartStatusProperty;

	private final SimpleObjectProperty<StepStatus> aliveStatusProperty;

	public StationStatus( Station station ) {
		this.name = station.name();
		this.address = station.address();
		this.setupStatusProperty = new SimpleObjectProperty<>( new StepStatus( StepStatus.State.WAITING, new Date() ) );
		this.updateStatusProperty = new SimpleObjectProperty<>( new StepStatus( StepStatus.State.WAITING, new Date() ) );
		this.upgradeStatusProperty = new SimpleObjectProperty<>( new StepStatus( StepStatus.State.WAITING, new Date() ) );
		this.restartStatusProperty = new SimpleObjectProperty<>( new StepStatus( StepStatus.State.WAITING, new Date() ) );
		this.aliveStatusProperty = new SimpleObjectProperty<>( new StepStatus( StepStatus.State.WAITING, new Date() ) );
	}

	public ReadOnlyObjectProperty<StepStatus> setupStatusProperty() {
		return setupStatusProperty;
	}

	public StepStatus getSetupStatus() {
		return setupStatusProperty.get();
	}

	public void setSetupStatus( StepStatus status) {
		setupStatusProperty.set( status );
	}

	public ReadOnlyObjectProperty<StepStatus> updateStatusProperty() {
		return updateStatusProperty;
	}

	public StepStatus getUpdateStatus() {
		return updateStatusProperty.get();
	}

	public void setUpdateStatus( StepStatus status) {
		updateStatusProperty.set( status );
	}

	public ReadOnlyObjectProperty<StepStatus> upgradeStatusProperty() {
		return upgradeStatusProperty;
	}

	public StepStatus getUpgradeStatus() {
		return upgradeStatusProperty.get();
	}

	public void setUpgradeStatus( StepStatus status) {
		upgradeStatusProperty.set( status );
	}

	public ReadOnlyObjectProperty<StepStatus> restartStatusProperty() {
		return restartStatusProperty;
	}

	public StepStatus getRestartStatus() {
		return restartStatusProperty.get();
	}

	public void setRestartStatus( StepStatus status) {
		restartStatusProperty.set( status );
	}

	public ReadOnlyObjectProperty<StepStatus> aliveStatusProperty() {
		return aliveStatusProperty;
	}

	public StepStatus getAliveStatus() {
		return aliveStatusProperty.get();
	}

	public void setAliveStatus( StepStatus status) {
		aliveStatusProperty.set( status );
	}

}
