package com.southbranchcontrols.performstationupdater;

import com.avereon.xenon.Xenon;
import com.avereon.xenon.scheme.BaseScheme;

public class UpdaterScheme extends BaseScheme {

	public static final String ID = "updater";

	public UpdaterScheme( Xenon program ) {
		super( program, ID );
	}

}
