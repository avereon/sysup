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
import lombok.CustomLog;
import lombok.Getter;
import lombok.Setter;

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
		TableColumn<StationStatus, String> name = new TableColumn<>( "Name" );
		name.setCellValueFactory( new PropertyValueFactory<>( "name" ) );
		TableColumn<StationStatus, String> setup = new TableColumn<>( "Setup" );
		setup.setCellValueFactory( new PropertyValueFactory<>( "setup" ) );
		TableColumn<StationStatus, String> update = new TableColumn<>( "Update" );
		update.setCellValueFactory( new PropertyValueFactory<>( "update" ) );
		TableColumn<StationStatus, String> upgrade = new TableColumn<>( "Upgrade" );
		upgrade.setCellValueFactory( new PropertyValueFactory<>( "upgrade" ) );
		TableColumn<StationStatus, String> restart = new TableColumn<>( "Restart" );
		restart.setCellValueFactory( new PropertyValueFactory<>( "restart" ) );

		table.getColumns().addAll( action, name, setup, update, upgrade, restart );

		action.setCellFactory( c -> new TableCell<>() {

			final Button btn = new Button( "Start" );

			@Override
			public void updateItem( String item, boolean empty ) {
				super.updateItem( item, empty );
				if( empty ) {
					setGraphic( null );
					setText( null );
				} else {
					btn.setOnAction( event -> {
						StationStatus status = getTableView().getItems().get( getIndex() );
						System.out.println( status.getName() );
					} );
					setGraphic( btn );
					setText( null );
				}
			}
		} );
	}

	private static class StationTableCell extends TableCell<StationStatus, String> {

	}

	@Getter
	@Setter
	public static class StationStatus {

		protected static final Date EMPTY = new Date( 0 );

		private final String name;

		private InetAddress address;

		private Date setup = EMPTY;

		private Date update = EMPTY;

		private Date upgrade = EMPTY;

		private Date restart = EMPTY;

		public StationStatus( Station station ) {
			this.name = station.name();
			try {
				this.address = InetAddress.getByName( station.address() );
			} catch( UnknownHostException exception ) {
				log.atWarn().log("Unable to resolve address {}", station.address());
			}
		}

	}

}
