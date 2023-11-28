package com.southbranchcontrols.performstationupdater;

import com.avereon.product.Rb;
import com.avereon.xenon.RbKey;
import com.avereon.xenon.Xenon;
import com.avereon.xenon.XenonProgramProduct;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.AssetType;
import com.avereon.xenon.asset.Codec;
import com.avereon.xenon.asset.PlaceholderCodec;
import com.avereon.xenon.asset.exception.AssetException;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import lombok.CustomLog;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@CustomLog
public class UpdaterAssetType extends AssetType {

	private static final String URI_PATTERN = "perform:updater";

	public static final java.net.URI URI = java.net.URI.create( URI_PATTERN );

	public UpdaterAssetType( XenonProgramProduct product ) {
		super( product, "updater" );

		PlaceholderCodec codec = new PlaceholderCodec();
		codec.addSupported( Codec.Pattern.URI, URI_PATTERN );
		setDefaultCodec( codec );
	}

	@Override
	public String getKey() {
		return URI_PATTERN;
	}

	@Override
	public boolean assetOpen( Xenon program, Asset asset ) throws AssetException {
		asset.setUri( URI );
		asset.setName( Rb.text( RbKey.ASSET, "updater-name" ) );

		// Setting the scheme when the asset is opened solves a bunch of "new" asset problems
		asset.setScheme( program.getAssetManager().getScheme( URI.getScheme() ) );

		// Load the station data
		loadData( asset );

		asset.setModified( false );
		return true;
	}

	private void loadData( Asset asset ) throws AssetException {
		try( InputStream input = Objects.requireNonNull( getClass().getResourceAsStream( "stations.csv" ) ) ) {

			List<List<String>> stations = new ArrayList<>();
			try( CSVReader csvReader = new CSVReader( new InputStreamReader( input, StandardCharsets.UTF_8 ) ) ) {
				String[] values;
				while( (values = csvReader.readNext()) != null ) {
					stations.add( Arrays.asList( values ) );
				}
			}

			asset.setModel( stations.stream().filter( l -> l.size() > 1 ).map( Station::of ).toList() );
		} catch( CsvValidationException | IOException | NullPointerException exception ) {
			throw new AssetException( asset, "Error loading station data", exception );
		}
	}

}
