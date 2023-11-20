package com.southbranchcontrols.performstationupdater;

import javafx.scene.paint.Color;

import java.util.Date;

public record StepStatus(State state, Date when) {

	public enum State {
		WAITING( "", null ),
		RUNNING( "Running", Color.BLUE ),
		SUCCESS( "Success", Color.GREEN ),
		FAILURE( "Failure", Color.RED );

		private final String text;

		private final Color color;

		State( String name, Color color ) {
			this.text = name;
			this.color = color;
		}

		public Color color() {
			return color;
		}

		@Override
		public String toString() {
			return text;
		}
	}

	public static StepStatus of( State state ) {
		return new StepStatus( state, new Date() );
	}

	@Override
	public String toString() {
		return state.toString();
	}

}
