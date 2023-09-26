package com.southbranchcontrols.performstationupdater;

import com.avereon.xenon.ProgramTool;
import com.avereon.xenon.XenonProgramProduct;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.OpenAssetRequest;
import com.avereon.xenon.workpane.ToolException;
import lombok.CustomLog;

@CustomLog
public class UpdaterTool extends ProgramTool {

	public UpdaterTool( XenonProgramProduct product, Asset asset ) {
		super( product, asset );
		addStylesheet( UpdaterMod.STYLESHEET );
		getStyleClass().addAll( "updater-tool" );
		setIcon( "updater" );

	}

	@Override
	protected void allocate() throws ToolException {
	}

	@Override
	protected void ready( OpenAssetRequest request ) throws ToolException {
		super.ready( request );
		setTitle( request.getAsset().getName() );
	}

	@Override
	protected void deallocate() throws ToolException {
	}

}
