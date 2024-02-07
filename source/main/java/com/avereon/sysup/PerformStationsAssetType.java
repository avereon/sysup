package com.avereon.sysup;

import com.avereon.xenon.XenonProgramProduct;
import com.avereon.xenon.asset.AssetType;
import lombok.CustomLog;

@CustomLog
public class PerformStationsAssetType extends AssetType {

	public static final String KEY = "updater";

	public PerformStationsAssetType( XenonProgramProduct product ) {
		super( product, KEY );
		setDefaultCodec( new PerformStationCodec( product ) );
	}

	@Override
	public String getKey() {
		return KEY;
	}

}
