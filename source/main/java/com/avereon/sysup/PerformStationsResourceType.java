package com.avereon.sysup;

import com.avereon.xenon.XenonProgramProduct;
import com.avereon.xenon.resource.ResourceType;
import lombok.CustomLog;

@CustomLog
public class PerformStationsResourceType extends ResourceType {

	public static final String KEY = "updater";

	public PerformStationsResourceType( XenonProgramProduct product ) {
		super( product, KEY );
		setDefaultCodec( new PerformStationCodec( product ) );
	}

	@Override
	public String getKey() {
		return KEY;
	}

}
