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
import javafx.scene.control.cell.TextFieldTableCell;
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

	public static class StepStatusCellFactory<S, T> implements Callback<TableColumn<S, T>, TableCell<S, T>> {

		@Override
		public TableCell<S, T> call( TableColumn<S, T> param ) {
			TextFieldTableCell<S, T> cell = new TextFieldTableCell<>();

			StepStatus status = (StepStatus)param.getCellData( 0 );
			cell.setBackground( Background.fill( status.color() ) );

			return cell;
		}

	}

	//	public class StepStatusCell<S,T> implements Callback<TableColumn.CellDataFeatures<S,T>, ObservableValue<T>> {
	//
	//		@Override
	//		public ObservableValue<T> call( TableColumn.CellDataFeatures<S, T> param ) {
	//			return new ReadOnlyObjectWrapper<>( (T)param.getValue() );
	//		}
	//
	//	}
	//
	//	private static class StationTableCell extends TableCell<StationStatus, String> {
	//
	//	}

	@Data
	public static class StationStatus {

		private final String name;

		private InetAddress address;

		private StepStatus setup = new StepStatus();

		private StepStatus update = new StepStatus();

		private StepStatus upgrade = new StepStatus();

		private StepStatus restart = new StepStatus();

		public StationStatus( Station station ) {
			this.name = station.name();
			try {
				this.address = InetAddress.getByName( station.address() );
			} catch( UnknownHostException exception ) {
				log.atWarn().log( "Unable to resolve address {}", station.address() );
			}
		}

	}

	@Data
	public static class StepStatus {

		public enum State {
			WAITING,
			RUNNING,
			SUCCESS,
			FAILURE
		}

		private State state = State.WAITING;

		private Date when = new Date();

		public Color color() {
			return Color.GREY;
		}

		@Override
		public String toString() {
			return state.name();
		}

	}

}
