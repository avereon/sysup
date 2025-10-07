package com.avereon.sysup;

import com.avereon.xenon.Xenon;
import com.avereon.xenon.asset.Resource;
import com.avereon.xenon.asset.exception.ResourceException;
import com.avereon.xenon.scheme.ProductScheme;

public class PerformScheme extends ProductScheme {

	public static final String ID = "perform";

	public PerformScheme( Xenon program ) {
		super( program, ID );
	}

	@Override
	public boolean exists( Resource resource ) {
		return resource.getUri().getSchemeSpecificPart().equals( "updater" );
	}

	@Override
	public boolean canLoad( Resource resource ) throws ResourceException {
		return true;
	}

}
