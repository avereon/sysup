package com.avereon.sysup;

public enum Type {

	PI,
	POTATO,
	MINTBOX,
	DEBIAN;

	public static Type of( String name ) {
		try {
			return Type.valueOf( name.trim().toUpperCase() );
		} catch( Exception exception ) {
			return PI;
		}
	}

}
