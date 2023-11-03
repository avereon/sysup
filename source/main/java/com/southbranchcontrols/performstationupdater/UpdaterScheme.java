package com.southbranchcontrols.performstationupdater;

import com.avereon.xenon.Xenon;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.scheme.BaseScheme;

public class UpdaterScheme extends BaseScheme {

	public static final String ID = "perform";

	public UpdaterScheme( Xenon program ) {
		super( program, ID );
	}

	@Override
	public boolean exists( Asset asset ) {
		return asset.getUri().getSchemeSpecificPart().equals( "updater" );
	}

}
