package com.southbranchcontrols.performstationupdater;

import com.avereon.product.Product;
import com.avereon.product.Rb;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.Codec;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PerformStationCodec extends Codec {

	public static final String MEDIA_TYPE = "application/vnd.southbranchcontrols.perform.stations";

	private static final String EXTENSION = "pfmstn";

	private final Product product;

	public PerformStationCodec( Product product ) {
		this.product = product;
		setDefaultExtension( EXTENSION );
		addSupported( Pattern.MEDIATYPE, MEDIA_TYPE );
	}

	@Override
	public String getKey() {
		return MEDIA_TYPE;
	}

	@Override
	public String getName() {
		return Rb.text( "asset", "codec-perform-station-name" );
	}

	@Override
	public boolean canLoad() {
		return true;
	}

	@Override
	public boolean canSave() {
		return false;
	}

	@Override
	public void load( Asset asset, InputStream input ) throws IOException {
		try {
			List<List<String>> stations = new ArrayList<>();
			try( CSVReader csvReader = new CSVReader( new InputStreamReader( input, StandardCharsets.UTF_8 ) ) ) {
				String[] values;
				while( (values = csvReader.readNext()) != null ) {
					stations.add( Arrays.asList( values ) );
				}
			}

			asset.setModel( stations.stream().filter( l -> l.size() > 1 ).map( Station::of ).toList() );
		} catch( CsvValidationException | IOException | NullPointerException exception ) {
			throw new IOException( "Error loading station data", exception );
		}
	}

	@Override
	public void save( Asset asset, OutputStream output ) throws IOException {
		// This codec does not save data yet
	}

}
