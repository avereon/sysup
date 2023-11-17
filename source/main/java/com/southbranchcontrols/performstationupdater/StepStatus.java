package com.southbranchcontrols.performstationupdater;

import javafx.scene.paint.Color;

import java.util.Date;

public record StepStatus(State state, Date when) {

	public enum State {
		WAITING( Color.TRANSPARENT ),
		RUNNING( Color.BLUE ),
		SUCCESS( Color.GREEN ),
		FAILURE( Color.RED );

		private final Color color;

		State( Color color ) {
			this.color = color;
		}

		public Color color() {
			return color;
		}
	}

	public static StepStatus of( State state ) {
		return new StepStatus( state, new Date() );
	}

	@Override
	public String toString() {
		return state.name();
	}

}
