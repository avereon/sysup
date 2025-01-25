import com.avereon.sysup.UpdaterMod;
import com.avereon.xenon.Module;

module com.avereon.sysup {

	// Compile-time only
	requires static lombok;

	// Both compile-time and run-time
	requires com.avereon.xenon;
	requires com.opencsv;
	requires java.logging;
	requires java.management;
	requires com.jcraft.jsch;
	requires org.apache.commons.lang3;

	opens com.avereon.sysup;
	opens com.avereon.sysup.bundles;
	opens com.avereon.sysup.settings;

	exports com.avereon.sysup to com.avereon.xenon, com.avereon.zarra;

	provides Module with UpdaterMod;

}
