import com.southbranchcontrols.performstationupdater.UpdaterMod;

module com.southbranchcontrols.performstationupdater {

	// Compile-time only
	requires static lombok;

	// Both compile-time and run-time
	requires com.avereon.xenon;
	requires com.opencsv;
	requires java.logging;
	requires java.management;
	requires jsch;

	opens com.southbranchcontrols.performstationupdater;
	opens com.southbranchcontrols.performstationupdater.bundles;
	opens com.southbranchcontrols.performstationupdater.settings;

	exports com.southbranchcontrols.performstationupdater to com.avereon.xenon, com.avereon.zarra;

	provides com.avereon.xenon.Mod with UpdaterMod;

}
