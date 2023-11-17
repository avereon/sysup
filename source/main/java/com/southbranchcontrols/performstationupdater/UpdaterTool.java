package com.southbranchcontrols.performstationupdater;

import com.avereon.xenon.ProgramTool;
import com.avereon.xenon.XenonProgramProduct;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.OpenAssetRequest;
import com.avereon.xenon.workpane.ToolException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import lombok.CustomLog;
import lombok.Data;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;

@CustomLog
public class UpdaterTool extends ProgramTool {

	private final TableView<StationStatus> table;

	public UpdaterTool( XenonProgramProduct product, Asset asset ) {
		super( product, asset );
		addStylesheet( UpdaterMod.STYLESHEET );
		getStyleClass().addAll( "updater-tool" );
		setIcon( "updater" );

		// Initial design thoughts:
		// - Show a table with each of the stations and the "known" update state
		// - Might need to store the last known state because the state can not need to be queried easily

		table = new TableView<>();
		ScrollPane scroller = new ScrollPane( table );
		scroller.setFitToWidth( true );
		getChildren().addAll( scroller );
	}

	@Override
	protected void ready( OpenAssetRequest request ) throws ToolException {
		super.ready( request );
		//setTitle( request.getAsset().getName() );
		setTitle( "Station Updater" );

		// The stations model from the asset
		Asset asset = request.getAsset();
		List<Station> stations = asset.getModel();
		ObservableList<StationStatus> status = FXCollections.observableArrayList( stations.stream().map( StationStatus::new ).toList() );
		asset.setValue( "status", status );

		table.setItems( status );
		table.setPlaceholder( new Label( "No stations loaded" ) );

		TableColumn<StationStatus, String> action = new TableColumn<>( "Action" );
		//action.setCellValueFactory( new PropertyValueFactory<>( "action" ) );
		action.setCellFactory( new StationButtonCellFactory() );
		TableColumn<StationStatus, String> name = new TableColumn<>( "Name" );
		name.setCellValueFactory( new PropertyValueFactory<>( "name" ) );
		TableColumn<StationStatus, StepStatus> setup = new TableColumn<>( "Setup" );
		setup.setCellValueFactory( new PropertyValueFactory<>( "setup" ) );
		TableColumn<StationStatus, StepStatus> update = new TableColumn<>( "Update" );
		update.setCellValueFactory( new PropertyValueFactory<>( "update" ) );
		TableColumn<StationStatus, StepStatus> upgrade = new TableColumn<>( "Upgrade" );
		upgrade.setCellValueFactory( new PropertyValueFactory<>( "upgrade" ) );

		TableColumn<StationStatus, StepStatus> restart = new TableColumn<>( "Restart" );
		restart.setCellValueFactory( new PropertyValueFactory<>( "restart" ) );
		restart.setCellFactory( new StepStatusCellFactory() );

		table.getColumns().addAll( List.of( action, name, setup, update, upgrade, restart ) );
	}

	static class StationButtonCellFactory implements Callback<TableColumn<StationStatus, String>, TableCell<StationStatus, String>> {

		@Override
		public TableCell<StationStatus, String> call( TableColumn<StationStatus, String> param ) {
			return new TableCell<>() {

				final Button btn = new Button( "Start" );

				@Override
				protected void updateItem( String item, boolean empty ) {
					super.updateItem( item, empty );
					if( empty ) {
						setGraphic( null );
						setText( null );
					} else {
						setGraphic( btn );
						setText( null );
						btn.setOnAction( event -> {
							StationStatus status = getTableView().getItems().get( getIndex() );
							System.out.println( status.getName() );
						} );
					}
				}
			};
		}

	}

	static class StepStatusCellFactory implements Callback<TableColumn<StationStatus, StepStatus>, TableCell<StationStatus, StepStatus>> {

		@Override
		public TableCell<StationStatus, StepStatus> call( TableColumn<StationStatus, StepStatus> param ) {
			return new TableCell<>() {

				@Override
				protected void updateItem( StepStatus item, boolean empty ) {
					super.updateItem( item, empty );

					if( item == null || empty ) {
						setText( null );
						setBackground( Background.EMPTY );
					} else {
						// Format date.
						setText( item.toString() );
						setBackground( Background.fill( item.state().color() ) );
					}
				}
			};

		}

	}

	@Data
	public static class StationStatus {

		private final String name;

		private InetAddress address;

		private StepStatus setup = new StepStatus( StepStatus.State.WAITING, new Date() );

		private StepStatus update = new StepStatus( StepStatus.State.WAITING, new Date() );

		private StepStatus upgrade = new StepStatus( StepStatus.State.WAITING, new Date() );

		private StepStatus restart = new StepStatus( StepStatus.State.WAITING, new Date() );

		public StationStatus( Station station ) {
			this.name = station.name();
			try {
				this.address = InetAddress.getByName( station.address() );
			} catch( UnknownHostException exception ) {
				log.atWarn().log( "Unable to resolve address {}", station.address() );
			}
		}

	}

	public record StepStatus(State state, Date when) {

		public enum State {
			WAITING( Color.GRAY ),
			RUNNING( Color.BLUE ),
			SUCCESS( Color.GREEN ),
			FAILURE( Color.RED );

			private final Color color;

			State( Color color ) {
				this.color = color;
			}

			public Color color() {
				return color;
			}
		}

		@Override
		public String toString() {
			return state.name();
		}

	}

}
