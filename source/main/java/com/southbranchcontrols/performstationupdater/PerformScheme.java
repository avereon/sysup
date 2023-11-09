package com.southbranchcontrols.performstationupdater;

import com.avereon.xenon.Xenon;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.exception.AssetException;
import com.avereon.xenon.scheme.BaseScheme;

public class PerformScheme extends BaseScheme {

	public static final String ID = "perform";

	public PerformScheme( Xenon program ) {
		super( program, ID );
	}

	@Override
	public boolean exists( Asset asset ) {
		return asset.getUri().getSchemeSpecificPart().equals( "updater" );
	}

	@Override
	public boolean canLoad( Asset asset ) throws AssetException {
		return true;
	}

}
