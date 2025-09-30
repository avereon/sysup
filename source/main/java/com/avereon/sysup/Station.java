package com.avereon.sysup;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Station {

	private Type type = Type.PI;

	private String name;

	private String address;

	private String user = "perform";

	public static Station of( List<String> values ) {
		int size = values.size();

		Station station = new Station();
		if( size > 0 ) station.setName( values.get( 0 ).trim() );
		if( size > 1 ) station.setAddress( values.get( 1 ).trim() );
		if( size > 2 ) station.setUser( values.get( 2 ).trim() );
		if( size > 3 ) station.setType( Type.of( values.get( 3 ) ) );

		return station;
	}

	@Override
	public String toString() {
		return name;
	}

}
