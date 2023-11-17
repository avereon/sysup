package com.southbranchcontrols.performstationupdater;

import com.avereon.xenon.Mod;
import com.avereon.xenon.ToolRegistration;
import com.avereon.zenna.icon.PerformIcon;
import com.avereon.zenna.icon.SouthBranchIcon;
import lombok.CustomLog;
import lombok.Getter;

@CustomLog
public class UpdaterMod extends Mod {

	public static final String STYLESHEET = "mod.css";

	private UpdaterAssetType updaterAssetType;

	@Getter
	private StationUpdateManager stationUpdateManager;

	public UpdaterMod() {
		super();
	}

	@Override
	public void startup() throws Exception {
		super.startup();
		registerIcon( "sbc", new SouthBranchIcon() );
		registerIcon( "perform", new PerformIcon() );
		registerIcon( "updater", new PerformIcon() );

		getProgram().getAssetManager().addScheme( new PerformScheme( getProgram() ) );

		registerAssetType( updaterAssetType = new UpdaterAssetType( this ) );
		ToolRegistration design2dEditorRegistration = new ToolRegistration( this, UpdaterTool.class );
		design2dEditorRegistration.setName( "Perform Station Updater Tool" );
		registerTool( updaterAssetType, design2dEditorRegistration );

		stationUpdateManager = new StationUpdateManager( getProgram() ).start();
	}

	@Override
	public void shutdown() throws Exception {
		stationUpdateManager.stop();

		unregisterTool( updaterAssetType, UpdaterTool.class );
		unregisterAssetType( updaterAssetType );

		getProgram().getAssetManager().removeScheme( PerformScheme.ID );

		unregisterIcon( getCard().getArtifact(), new SouthBranchIcon() );
		super.shutdown();
	}

}
