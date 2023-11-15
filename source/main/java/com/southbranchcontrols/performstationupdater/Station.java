package com.southbranchcontrols.performstationupdater;

import java.util.List;

public record Station(String name, String address) {

	public static Station of( List<String> values ) {
		return new Station(values.get(0), values.get(1));
	}

}
