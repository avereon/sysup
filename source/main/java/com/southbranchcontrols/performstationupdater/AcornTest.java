package com.southbranchcontrols.performstationupdater;

import com.avereon.product.Rb;
import com.avereon.xenon.RbKey;
import com.avereon.xenon.Xenon;
import com.avereon.xenon.task.TaskEvent;
import com.avereon.zarra.javafx.Fx;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import lombok.CustomLog;

import java.util.concurrent.ExecutionException;

@CustomLog
public class AcornTest extends HBox {

	private final AcornTool tool;

	private final String title;

	private final int threads;

	private final Button button;

	private final ProgressIndicator progress;

	private final Label result;

	private final Label message;

	private AcornScore score;

	private AcornTask checker;

	public AcornTest( AcornTool tool, String title, int threads ) {
		this.tool = tool;
		this.title = title;
		this.threads = threads;

		getStyleClass().addAll( "layout" );

		String waitingText = Rb.text( "message", "waiting-to-start" );

		Label titleLabel = new Label( title );
		titleLabel.getStyleClass().addAll( "icon" );
		result = new Label( "----" );
		result.getStyleClass().addAll( "result" );
		button = new Button();
		//button.getStyleClass().addAll( "button" );
		progress = new ProgressBar( 0 );
		progress.getStyleClass().addAll( "progress" );
		message = new Label( waitingText );
		message.getStyleClass().addAll( "message" );

		button.setOnAction( e -> toggle() );
		updateButtonState();

		getChildren().addAll( button, titleLabel, progress, result );
	}

	private Xenon getProgram() {
		return tool.getProgram();
	}

	private boolean isRunning() {
		return !(checker == null || checker.isDone());
	}

	private void toggle() {
		if( isRunning() ) {
			checker.cancel( true );
		} else {
			start();
		}
	}

	private void updateButtonState() {
		String startText = Rb.text( RbKey.LABEL, "start" );
		String cancelText = Rb.text( RbKey.LABEL, "cancel" );
		Node pauseIcon = getProgram().getIconLibrary().getIcon( "pause" );
		Node playIcon = getProgram().getIconLibrary().getIcon( "play" );
		Fx.run( () -> {
			button.setGraphic( isRunning() ? pauseIcon : playIcon );
			//button.setText( isRunning() ? cancelText : startText );
		} );
	}

	private void start() {
		checker = new AcornTask();
		checker.register( TaskEvent.SUBMITTED, e -> {
			Fx.run( () -> progress.setProgress( 0 ) );
			updateButtonState();
		} );
		checker.register( TaskEvent.PROGRESS, e -> Fx.run( () -> progress.setProgress( e.getTask().getPercent() ) ) );
		checker.register( TaskEvent.SUCCESS, e -> Fx.run( () -> {
			try {
				setScore( checker.get() );
			} catch( InterruptedException | ExecutionException exception ) {
				log.atWarning().withCause( exception ).log( "Error computing acorn count" );
			}
		} ) );
		checker.register( TaskEvent.CANCEL, e -> Fx.run( () -> progress.setProgress( 0 ) ) );
		checker.register( TaskEvent.FINISH, e -> updateButtonState() );

		getProgram().getTaskManager().submit( checker );
	}

	private void setScore( long score ) {
		Fx.run( () -> {
			tool.getScoreGraph().removeScore( this.score );
			result.setText( String.valueOf( score ) );
			tool.getScoreGraph().addScore( this.score = new AcornScore( true, score, "Your Computer " + title ) );
		} );
	}

}
