package com.avereon.sysup;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import lombok.CustomLog;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@CustomLog
public class StationStatus {

	@Getter
	private final Station station;

	private final SimpleObjectProperty<StepStatus> setupStatusProperty;

	private final SimpleObjectProperty<StepStatus> updateStatusProperty;

	private final SimpleObjectProperty<StepStatus> upgradeStatusProperty;

	private final SimpleObjectProperty<StepStatus> restartStatusProperty;

	private final SimpleObjectProperty<StepStatus> aliveStatusProperty;

	private final SimpleObjectProperty<StepStatus> verifyStatusProperty;

	public StationStatus( Station station ) {
		this.station = station;
		this.setupStatusProperty = new SimpleObjectProperty<>( new StepStatus( StepStatus.State.WAITING, new Date() ) );
		this.updateStatusProperty = new SimpleObjectProperty<>( new StepStatus( StepStatus.State.WAITING, new Date() ) );
		this.upgradeStatusProperty = new SimpleObjectProperty<>( new StepStatus( StepStatus.State.WAITING, new Date() ) );
		this.restartStatusProperty = new SimpleObjectProperty<>( new StepStatus( StepStatus.State.WAITING, new Date() ) );
		this.aliveStatusProperty = new SimpleObjectProperty<>( new StepStatus( StepStatus.State.WAITING, new Date() ) );
		this.verifyStatusProperty = new SimpleObjectProperty<>( new StepStatus( StepStatus.State.WAITING, new Date() ) );
	}

	public List<StepStatus> getSteps() {
		List<StepStatus> steps = new ArrayList<>();

		steps.add( setupStatusProperty.get() );
		steps.add( updateStatusProperty.get() );
		steps.add( upgradeStatusProperty.get() );
		steps.add( restartStatusProperty.get() );
		steps.add( aliveStatusProperty.get() );
		steps.add( verifyStatusProperty.get() );

		return steps;
	}

	public void reset() {
		setupStatusProperty.set(new StepStatus( StepStatus.State.WAITING, new Date()));
		updateStatusProperty.set(new StepStatus( StepStatus.State.WAITING, new Date()));
		upgradeStatusProperty.set(new StepStatus( StepStatus.State.WAITING, new Date()));
		restartStatusProperty.set(new StepStatus( StepStatus.State.WAITING, new Date()));
		aliveStatusProperty.set(new StepStatus( StepStatus.State.WAITING, new Date()));
		verifyStatusProperty.set(new StepStatus( StepStatus.State.WAITING, new Date()));
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

	public ReadOnlyObjectProperty<StepStatus> verifyStatusProperty() {
		return verifyStatusProperty;
	}

	public StepStatus getVerifyStatus() {
		return verifyStatusProperty.get();
	}

	public void setVerifyStatus( StepStatus status) {
		verifyStatusProperty.set( status );
	}

}
