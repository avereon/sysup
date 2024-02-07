package com.avereon.sysup;

import com.avereon.xenon.Mod;
import com.avereon.xenon.ToolRegistration;
import com.avereon.zenna.icon.AvereonIcon;
import com.avereon.zenna.icon.PerformIcon;
import com.avereon.zenna.icon.SouthBranchIcon;
import lombok.CustomLog;
import lombok.Getter;

@CustomLog
public class UpdaterMod extends Mod {

	public static final String STYLESHEET = "mod.css";

	private PerformStationsAssetType performStationsAssetType;

	@Getter
	private StationUpdateManager stationUpdateManager;

	public UpdaterMod() {
		super();
	}

	@Override
	public void startup() throws Exception {
		super.startup();
		registerIcon( "avereon", new AvereonIcon() );
		registerIcon( "perform", new PerformIcon() );
		registerIcon( "updater", new PerformIcon() );

		getProgram().getAssetManager().addScheme( new PerformScheme( getProgram() ) );

		// Register the asset type
		performStationsAssetType = new PerformStationsAssetType( this );
		registerAssetType( performStationsAssetType );

		// Register the updater tool
		ToolRegistration design2dEditorRegistration = new ToolRegistration( this, UpdaterTool.class );
		design2dEditorRegistration.setName( "Perform Station Updater Tool" );
		registerTool( performStationsAssetType, design2dEditorRegistration );

		stationUpdateManager = new StationUpdateManager( this ).start();
	}

	@Override
	public void shutdown() throws Exception {
		stationUpdateManager.stop();

		unregisterTool( performStationsAssetType, UpdaterTool.class );
		unregisterAssetType( performStationsAssetType );

		getProgram().getAssetManager().removeScheme( PerformScheme.ID );

		unregisterIcon( getCard().getArtifact(), new SouthBranchIcon() );
		super.shutdown();
	}

}
