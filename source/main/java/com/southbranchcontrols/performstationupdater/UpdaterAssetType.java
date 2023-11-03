package com.southbranchcontrols.performstationupdater;

import com.avereon.product.Rb;
import com.avereon.xenon.RbKey;
import com.avereon.xenon.Xenon;
import com.avereon.xenon.XenonProgramProduct;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.AssetType;
import com.avereon.xenon.asset.Codec;
import com.avereon.xenon.asset.PlaceholderCodec;
import com.avereon.xenon.asset.exception.AssetException;

public class UpdaterAssetType extends AssetType {

	private static final String uriPattern = "perform:updater";

	public static final java.net.URI URI = java.net.URI.create( uriPattern );

	public UpdaterAssetType( XenonProgramProduct product ) {
		super( product, "updater" );

		PlaceholderCodec codec = new PlaceholderCodec();
		codec.addSupported( Codec.Pattern.URI, uriPattern );
		setDefaultCodec( codec );
	}

	@Override
	public String getKey() {
		return uriPattern;
	}

	@Override
	public boolean assetOpen( Xenon program, Asset asset ) throws AssetException {
		asset.setUri( URI );
		asset.setName( Rb.text( RbKey.ASSET, "updater-name" ) );
		asset.setModified( false );
		return true;
	}

}
