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
import javafx.util.Callback;
import lombok.CustomLog;

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
		setup.setCellValueFactory( new PropertyValueFactory<>( "setupStatus" ) );
		setup.setCellFactory( new StepStatusCellFactory() );
		TableColumn<StationStatus, StepStatus> update = new TableColumn<>( "Update" );
		update.setCellValueFactory( new PropertyValueFactory<>( "updateStatus" ) );
		update.setCellFactory( new StepStatusCellFactory() );
		TableColumn<StationStatus, StepStatus> upgrade = new TableColumn<>( "Upgrade" );
		upgrade.setCellValueFactory( new PropertyValueFactory<>( "upgradeStatus" ) );
		upgrade.setCellFactory( new StepStatusCellFactory() );
		TableColumn<StationStatus, StepStatus> restart = new TableColumn<>( "Restart" );
		restart.setCellValueFactory( new PropertyValueFactory<>( "restartStatus" ) );
		restart.setCellFactory( new StepStatusCellFactory() );

		table.getColumns().addAll( List.of( action, name, setup, update, upgrade, restart ) );
	}

	class StationButtonCellFactory implements Callback<TableColumn<StationStatus, String>, TableCell<StationStatus, String>> {

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
							StationStatus station = getTableView().getItems().get( getIndex() );
							//System.out.println( station.getName() );
							((UpdaterMod)getProduct()).getStationUpdateManager().getUpdater( station ).next();
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

}
