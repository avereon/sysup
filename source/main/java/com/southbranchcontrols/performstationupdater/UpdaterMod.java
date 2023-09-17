package com.southbranchcontrols.performstationupdater;

import com.avereon.xenon.Mod;
import com.avereon.xenon.ToolRegistration;
import lombok.CustomLog;

@CustomLog
public class UpdaterMod extends Mod {

	public static final String STYLESHEET = "mod.css";

	private UpdaterAssetType updaterAssetType;

	public UpdaterMod() {
		super();
	}

	@Override
	public void startup() throws Exception {
		super.startup();
		registerIcon( "updater", new AcornIcon() );

		getProgram().getAssetManager().addScheme( new UpdaterScheme( getProgram() ) );

		registerAssetType( updaterAssetType = new UpdaterAssetType( this ) );
		ToolRegistration design2dEditorRegistration = new ToolRegistration( this, AcornTool.class );
		design2dEditorRegistration.setName( "Acorn Counting Tool" );
		registerTool( updaterAssetType, design2dEditorRegistration );
	}

	@Override
	public void shutdown() throws Exception {
		unregisterTool( updaterAssetType, AcornTool.class );
		unregisterAssetType( updaterAssetType );

		getProgram().getAssetManager().removeScheme( UpdaterScheme.ID );

		unregisterIcon( getCard().getArtifact(), new AcornIcon() );
		super.shutdown();
	}

}
