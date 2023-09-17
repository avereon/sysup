import com.southbranchcontrols.performstationupdater.UpdaterMod;

module com.southbranchcontrols.perform.station.updater {

	// Compile-time only
	requires static lombok;

	// Both compile-time and run-time
	requires com.avereon.xenon;
	requires java.management;

	opens com.southbranchcontrols.performstationupdater.bundles;
	opens com.southbranchcontrols.performstationupdater.settings;

	exports com.southbranchcontrols.performstationupdater to com.avereon.xenon, com.avereon.zarra;

	provides com.avereon.xenon.Mod with UpdaterMod;

}
