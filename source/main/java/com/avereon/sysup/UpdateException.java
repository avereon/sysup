package com.avereon.sysup;

public class UpdateException extends RuntimeException {

	public UpdateException() {
		super();
	}

	public UpdateException( String message ) {
		super( message );
	}

	public UpdateException( Throwable cause ) {
		super( cause );
	}

	public UpdateException( String message, Throwable cause ) {
		super( message, cause );
	}

}
