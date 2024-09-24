package com.avereon.sysup;

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

		table = new TableView<>();
		table.setColumnResizePolicy( TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN );

		ScrollPane scroller = new ScrollPane( table );
		scroller.setFitToWidth( true );
		scroller.setFitToHeight( true );
		getChildren().addAll( scroller );
	}

	@Override
	protected void ready( OpenAssetRequest request ) throws ToolException {
		super.ready( request );
		setTitle( request.getAsset().getName() );
		//setTitle( "Station Updater" );

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
		TableColumn<StationStatus, String> address = new TableColumn<>( "Address" );
		address.setCellValueFactory( new PropertyValueFactory<>( "address" ) );
		TableColumn<StationStatus, StepStatus> setup = new TableColumn<>( "Setup" );
		setup.setCellValueFactory( new PropertyValueFactory<>( "setupStatus" ) );
		setup.setCellFactory( new StepStatusCellFactory() );
		TableColumn<StationStatus, StepStatus> update = new TableColumn<>( "Refresh" );
		update.setCellValueFactory( new PropertyValueFactory<>( "updateStatus" ) );
		update.setCellFactory( new StepStatusCellFactory() );
		TableColumn<StationStatus, StepStatus> upgrade = new TableColumn<>( "Update" );
		upgrade.setCellValueFactory( new PropertyValueFactory<>( "upgradeStatus" ) );
		upgrade.setCellFactory( new StepStatusCellFactory() );
		TableColumn<StationStatus, StepStatus> restart = new TableColumn<>( "Restart" );
		restart.setCellValueFactory( new PropertyValueFactory<>( "restartStatus" ) );
		restart.setCellFactory( new StepStatusCellFactory() );
		TableColumn<StationStatus, StepStatus> alive = new TableColumn<>( "Restarted" );
		alive.setCellValueFactory( new PropertyValueFactory<>( "aliveStatus" ) );
		alive.setCellFactory( new StepStatusCellFactory() );
		TableColumn<StationStatus, StepStatus> verify = new TableColumn<>( "Verified" );
		verify.setCellValueFactory( new PropertyValueFactory<>( "verifyStatus" ) );
		verify.setCellFactory( new StepStatusCellFactory() );

		table.getColumns().addAll( List.of( action, name, address, setup, update, upgrade, restart, alive, verify ) );
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
							((UpdaterMod)getProduct()).getStationUpdateManager().getUpdater( station ).run();
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
