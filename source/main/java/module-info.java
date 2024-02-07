import com.avereon.sysup.UpdaterMod;

module com.avereon.sysup {

	// Compile-time only
	requires static lombok;

	// Both compile-time and run-time
	requires com.avereon.xenon;
	requires com.opencsv;
	requires java.logging;
	requires java.management;
	requires com.jcraft.jsch;

	opens com.avereon.sysup;
	opens com.avereon.sysup.bundles;
	opens com.avereon.sysup.settings;

	exports com.avereon.sysup to com.avereon.xenon, com.avereon.zarra;

	provides com.avereon.xenon.Mod with UpdaterMod;

}
