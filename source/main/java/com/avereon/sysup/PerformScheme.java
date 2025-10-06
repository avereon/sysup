package com.avereon.sysup;

import com.avereon.xenon.Xenon;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.exception.ResourceException;
import com.avereon.xenon.scheme.ProductScheme;

public class PerformScheme extends ProductScheme {

	public static final String ID = "perform";

	public PerformScheme( Xenon program ) {
		super( program, ID );
	}

	@Override
	public boolean exists( Asset asset ) {
		return asset.getUri().getSchemeSpecificPart().equals( "updater" );
	}

	@Override
	public boolean canLoad( Asset asset ) throws ResourceException {
		return true;
	}

}
