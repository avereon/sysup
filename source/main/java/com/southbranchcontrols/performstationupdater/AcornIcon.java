package com.southbranchcontrols.performstationupdater;

import com.avereon.zarra.color.Colors;
import com.avereon.zarra.image.RenderedIcon;
import javafx.scene.paint.Color;

public class AcornIcon extends RenderedIcon {

	private final double centerLine = 16;

	private final double stemRadius = 2;

	private final double stemTop = 0;

	private final double capRadius = 12;

	private final double capTop = 6;

	private final double capBase = 12;

	private final double nutRadius = 10;

	private final double nutBottom = 32;

	public AcornIcon() {
		Color lightPaint = Color.SADDLEBROWN.deriveColor( 0, 0.5, 0.8, 1 );
		Color darkPaint = lightPaint.deriveColor( 0, 1.2, 1.5, 1 );
		setStyle( "-fx-stroke: ladder( -fx-text-background-color, " + Colors.toString( lightPaint ) + " 50%, " + Colors.toString( darkPaint ) + " 50% );" );
	}

	public static void main( String[] commands ) {
		proof( new AcornIcon() );
	}

	@Override
	protected void render() {
		drawStem();
		fill();

		drawNut();
		fill();

		drawCap();
		fill();
	}

	private void drawStem() {
		double r = 2;
		startPath( g( centerLine - stemRadius ), g( capTop - 2 ) );
		addArc( g( centerLine - stemRadius + r ), g( stemTop + r ), g( r ), g( r ), 180, -90 );
		addArc( g( centerLine + stemRadius - r ), g( stemTop + r ), g( r ), g( r ), 90, -90 );
		lineTo( g( centerLine + stemRadius ), g( capTop - 2 ) );
		closePath();
	}

	private void drawCap() {
		startPath( g( centerLine + capRadius - 1 ), g( capBase ) );
		addArc( g( centerLine + capRadius - 1 ), g( capBase - 1 ), g( 1 ), g( 1 ), 270, 90 );
		addArc( g( centerLine ), g( capBase - 1 ), g( capRadius ), g( capBase - capTop - 1 ), 0, 180 );
		addArc( g( centerLine - capRadius + 1 ), g( capBase - 1 ), g( 1 ), g( 1 ), 180, 90 );
		closePath();
	}

	private void drawNut() {
		double nutTop = capBase + 2;

		double a = nutTop + 19;
		double b = nutBottom - 4;
		double c = centerLine - nutRadius;
		double d = centerLine + nutRadius;

		double r = 1;

		startPath( g( c ), g( nutTop ) );
		curveTo( g( c ), g( a ), g( centerLine - r ), g( b ), g( centerLine - r ), g( nutBottom - r ) );
		addArc( g( centerLine ), g( nutBottom - r ), g( r ), g( r ), 180, 180 );
		curveTo( g( centerLine + r ), g( b ), g( d ), g( a ), g( d ), g( nutTop ) );
		closePath();
	}

}
