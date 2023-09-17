package com.southbranchcontrols.performstationupdater;

import com.avereon.util.TextUtil;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;

public class AcornScore extends Label {

	private final BooleanProperty left;

	private final long score;

	public AcornScore( boolean left, long score ) {
		this( left, score, null );
	}

	public AcornScore( boolean left, long score, String text ) {
		this( left, score, text, null );
	}

	public AcornScore( boolean left, long score, String text, Node graphic ) {
		super( getText( left, score, text ), graphic );
		this.score = score;
		this.left = new SimpleBooleanProperty( left );
	}

	private static String getText( boolean left, long score, String text ) {
		if( TextUtil.isEmpty( text ) ) return String.valueOf( score );
		return left ? text + " - " + score : score + " - " + text;
	}

	public BooleanProperty leftProperty() {
		return left;
	}

	public long getScore() {
		return score;
	}

}
