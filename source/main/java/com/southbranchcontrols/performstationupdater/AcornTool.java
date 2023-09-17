package com.southbranchcontrols.performstationupdater;

import com.avereon.xenon.ProgramTool;
import com.avereon.xenon.XenonProgramProduct;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.OpenAssetRequest;
import com.avereon.xenon.workpane.ToolException;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import lombok.CustomLog;

import java.util.Timer;
import java.util.function.Consumer;

@CustomLog
public class AcornTool extends ProgramTool {

	private static final Timer timer = new Timer( true );

	private final Consumer<Double> cpuLoadListener;

	private final ScoreGraph scoreGraph;

	private SystemCpuLoadCheck cpuLoadCheck;

	public AcornTool( XenonProgramProduct product, Asset asset ) {
		super( product, asset );
		addStylesheet( UpdaterMod.STYLESHEET );
		getStyleClass().addAll( "updater-tool" );
		setIcon( "updater" );

		cpuLoadListener = d -> log.atFine().log( "cpu=%s", d );

		scoreGraph = new ScoreGraph();

		int threads = Runtime.getRuntime().availableProcessors();

		AcornTest allThreadsTest = new AcornTest( this, "All Threads", threads );
		AcornTest oneThreadTest = new AcornTest( this, "One Thread", 1 );
		VBox box = new VBox( allThreadsTest, oneThreadTest );
		box.getStyleClass().addAll( "layout" );
		HBox.setHgrow( box, Priority.ALWAYS );

		HBox parts = new HBox( box, scoreGraph );
		parts.getStyleClass().addAll( "left" );

		getChildren().add( parts );
	}

	@Override
	protected void allocate() throws ToolException {
		cpuLoadCheck = new SystemCpuLoadCheck();
		cpuLoadCheck.addListener( cpuLoadListener );
		timer.schedule( cpuLoadCheck, 0, 1000 );
	}

	@Override
	protected void ready( OpenAssetRequest request ) throws ToolException {
		super.ready( request );
		setTitle( request.getAsset().getName() );
	}

	@Override
	protected void deallocate() throws ToolException {
		cpuLoadCheck.cancel();
		cpuLoadCheck.removeListener( cpuLoadListener );
	}

	ScoreGraph getScoreGraph() {
		return scoreGraph;
	}

}
