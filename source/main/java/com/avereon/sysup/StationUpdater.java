package com.avereon.sysup;

import com.avereon.util.ThreadUtil;
import com.avereon.xenon.notice.Notice;
import com.avereon.xenon.task.Task;
import com.avereon.zerra.javafx.Fx;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import lombok.CustomLog;
import lombok.Getter;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

@Getter
@CustomLog
public class StationUpdater {

	private static final int CONNECT_TIMEOUT = 10000;

	private static final int CHANNEL_TIMEOUT = 3600000;

	private static final int RESTART_PAUSE = 12000;

	private static final int RESTART_TIMEOUT = 180000;

	//private final JSch jsch;

	private StationUpdateManager manager;

	private StationStatus station;

	private StationUpdater() {
		//jsch = new JSch();
	}

	public static StationUpdater of( StationUpdateManager manager, StationStatus station ) {
		StationUpdater updater = new StationUpdater();
		updater.manager = manager;
		updater.station = station;
		return updater;
	}

	public void run() {
		if( isRunning() ) return;
		station.reset();

		// setup
		// update
		// upgrade
		// restart
		// isAlive
		// verify

		setup( station );
	}

	private boolean isRunning() {
		for( StepStatus step : station.getSteps() ) {
			if( step.state() == StepStatus.State.RUNNING ) return true;
		}
		return false;
	}

	private JSch jsch() throws JSchException {
		JSch.setConfig( "StrictHostKeyChecking", "no" );
		JSch.setLogger( new JschLogger() );

		JSch jsch = new JSch();
		jsch.addIdentity( System.getProperty( "user.home" ) + "/.ssh/id_rsa" );
		jsch.setKnownHosts( getManager().getProduct().getDataFolder() + "/known_hosts" );

		return jsch;
	}

	private void setup( StationStatus status ) {
		if( status.getSetupStatus().state() != StepStatus.State.WAITING ) return;
		getManager().getProgram().getTaskManager().submit( setupTask( status ) );
	}

	private Task<?> setupTask( StationStatus status ) {
		return Task.of(
			"Perform station " + status.getStation().getAddress() + " setup", () -> {
				Fx.run( () -> status.setSetupStatus( StepStatus.of( StepStatus.State.RUNNING ) ) );
				try {
					run( status.getStation(), "sudo apt autoremove -y; mkdir -p Downloads;" );

					//					InputStream resourceInput = getClass().getResourceAsStream( "station-update" );
					//					ByteArrayOutputStream resourceOutput = new ByteArrayOutputStream();
					//					if( resourceInput != null ) {
					//						IoUtil.copy( resourceInput, resourceOutput );
					//						resourceInput.close();
					//					}
					//
					//					byte[] content = resourceOutput.toByteArray();
					//					long filesize = content.length;
					//				scpPut( "station-update", filesize, new ByteArrayInputStream( content ), station.getAddress(), "/home/perform/Updates/station-update" );
					//				run( station.getAddress(), "chmod a+x /home/perform/Updates/station-update" );

					// Update the VPN client config
					// TODO This should be moved to a module setting
					Path vpnClientFolder = Paths.get( System.getProperty( "user.home" ) + "/Data/sod/vpn/client" );
					String vpnClientConfig = status.getStation().getAddress().replace( ".", "-" ) + ".conf";
					Path vpnClientPath = vpnClientFolder.resolve( vpnClientConfig );
					if( Files.exists( vpnClientPath ) ) {
						scpPut( status.getStation(), vpnClientPath, "Downloads/" + vpnClientConfig );
					}
					run( status.getStation(), "sudo cp Downloads/" + vpnClientConfig + " /etc/openvpn/" + vpnClientConfig + ";" );

					// Assuming all of that worked, update the step status
					Fx.run( () -> status.setSetupStatus( StepStatus.of( StepStatus.State.SUCCESS ) ) );
					update( status );
				} catch( IOException exception ) {
					Fx.run( () -> status.setSetupStatus( StepStatus.of( StepStatus.State.FAILURE ) ) );
					stationUpdateFailure( exception );
					throw new RuntimeException( exception );
				}
			}
		);
	}

	private void update( StationStatus status ) {
		if( status.getUpdateStatus().state() != StepStatus.State.WAITING ) return;
		getManager().getProgram().getTaskManager().submit( updateTask( status ) );
	}

	private Task<?> updateTask( StationStatus status ) {
		return Task.of(
			"Perform station " + status.getStation().getAddress() + " update", () -> {
				Fx.run( () -> status.setUpdateStatus( StepStatus.of( StepStatus.State.RUNNING ) ) );
				try {
					run( status.getStation(), "sudo apt update -y" );

					// Assuming all of that worked, update the step status
					Fx.run( () -> status.setUpdateStatus( StepStatus.of( StepStatus.State.SUCCESS ) ) );
					upgrade( status );
				} catch( IOException exception ) {
					Fx.run( () -> status.setUpdateStatus( StepStatus.of( StepStatus.State.FAILURE ) ) );
					stationUpdateFailure( exception );
					throw new RuntimeException( exception );
				}
			}
		);
	}

	private void upgrade( StationStatus status ) {
		if( status.getUpgradeStatus().state() != StepStatus.State.WAITING ) return;
		getManager().getProgram().getTaskManager().submit( upgradeTask( status ) );
	}

	private Task<?> upgradeTask( StationStatus status ) {
		return Task.of(
			"Perform station " + status.getStation().getAddress() + " upgrade", () -> {
				Fx.run( () -> status.setUpgradeStatus( StepStatus.of( StepStatus.State.RUNNING ) ) );
				try {
					run( status.getStation(), "sudo apt dist-upgrade -y" );

					// Assuming all of that worked, update the step status
					Fx.run( () -> status.setUpgradeStatus( StepStatus.of( StepStatus.State.SUCCESS ) ) );
					restart( status );
				} catch( IOException exception ) {
					Fx.run( () -> status.setUpgradeStatus( StepStatus.of( StepStatus.State.FAILURE ) ) );
					stationUpdateFailure( exception );
					throw new RuntimeException( exception );
				}
			}
		);
	}

	private void restart( StationStatus status ) {
		if( status.getRestartStatus().state() != StepStatus.State.WAITING ) return;
		getManager().getProgram().getTaskManager().submit( restartTask( status ) );
	}

	private Task<?> restartTask( StationStatus status ) {
		return Task.of(
			"Perform station " + status.getStation().getAddress() + " restart", () -> {
				Fx.run( () -> status.setRestartStatus( StepStatus.of( StepStatus.State.RUNNING ) ) );
				try {
					run( status.getStation(), "sudo reboot; exit;", Set.of( -1, 0 ) );
					ThreadUtil.pause( 2000 );

					// Assuming all of that worked, update the step status
					Fx.run( () -> status.setRestartStatus( StepStatus.of( StepStatus.State.SUCCESS ) ) );
					isAlive( status );
				} catch( IOException exception ) {
					Fx.run( () -> status.setRestartStatus( StepStatus.of( StepStatus.State.FAILURE ) ) );
					stationUpdateFailure( exception );
					throw new RuntimeException( exception );
				}
			}
		);
	}

	private void isAlive( StationStatus status ) {
		if( status.getAliveStatus().state() != StepStatus.State.WAITING ) return;
		getManager().getProgram().getTaskManager().submit( isAliveTask( status ) );
	}

	private Task<?> isAliveTask( StationStatus status ) {
		return Task.of(
			"Perform station " + status.getStation().getAddress() + " alive", () -> {
				Fx.run( () -> status.setAliveStatus( StepStatus.of( StepStatus.State.RUNNING ) ) );
				try {
					// Wait a few seconds for the station to shut down before continuing
					// Otherwise, it is immediately reachable, which is not what we want
					ThreadUtil.pause( RESTART_PAUSE );

					long threshold = System.currentTimeMillis() + RESTART_TIMEOUT;
					boolean isTimeout = System.currentTimeMillis() > threshold;
					while( !isTimeout ) {
						if( isReachable( status.getStation().getAddress(), 22 ) ) break;
						ThreadUtil.pause( 1000 );
						isTimeout = System.currentTimeMillis() > threshold;
					}

					if( isTimeout ) throw new TimeoutException();

					// Assuming all of that worked, update the step status
					Fx.run( () -> status.setAliveStatus( StepStatus.of( StepStatus.State.SUCCESS ) ) );
					verify( status );
				} catch( IOException | TimeoutException exception ) {
					Fx.run( () -> status.setAliveStatus( StepStatus.of( StepStatus.State.FAILURE ) ) );
					stationUpdateFailure( exception );
					throw new RuntimeException( exception );
				}
			}
		);
	}

	private void verify( StationStatus status ) {
		if( status.getVerifyStatus().state() != StepStatus.State.WAITING ) return;
		getManager().getProgram().getTaskManager().submit( verifyTask( status ) );
	}

	private Task<?> verifyTask( StationStatus status ) {
		return Task.of(
			"Perform station " + status.getStation().getAddress() + " verify", () -> {
				Fx.run( () -> status.setVerifyStatus( StepStatus.of( StepStatus.State.RUNNING ) ) );
				try {
					ByteArrayOutputStream output = new ByteArrayOutputStream();
					run( status.getStation(), "apt list -a --upgradable | grep -v ^List | grep -v -e '^$' | wc -l", Set.of( 0 ), null, output, null );

					// Check the result from the output
					String resultData = output.toString( StandardCharsets.UTF_8 ).trim();
					int upgradableCount = Integer.parseInt( resultData );
					if( upgradableCount > 0 ) throw new IOException( "Station still has " + upgradableCount + " upgradable packages" );

					// Assuming all of that worked, update the step status
					Fx.run( () -> status.setVerifyStatus( StepStatus.of( StepStatus.State.SUCCESS ) ) );
					log.atInfo().log( "Station {0} is up to date", status.getStation().getAddress() );
				} catch( IOException exception ) {
					Fx.run( () -> status.setVerifyStatus( StepStatus.of( StepStatus.State.FAILURE ) ) );
					stationUpdateFailure( exception );
					throw new RuntimeException( exception );
				}
			}
		);
	}

	private void run( Station station, String command ) throws IOException {
		run( station, command, Set.of( 0 ) );
	}

	private void run( Station station, String command, Set<Integer> expectedExitStatuses ) throws IOException {
		run( station, command, expectedExitStatuses, null, null, null );
	}

	private void run( Station station, String command, Set<Integer> expectedExitStatuses, InputStream in, OutputStream out, OutputStream err ) throws IOException {
		try {
			// Create the shell session
			Session session = jsch().getSession( station.getUser(), station.getAddress() );
			session.connect( CONNECT_TIMEOUT );

			// Create the execution channel
			ChannelExec channel = (ChannelExec)session.openChannel( "exec" );
			InputStream input = channel.getInputStream();
			channel.setCommand( command );
			channel.setInputStream( in );
			channel.setOutputStream( out, true );
			channel.setErrStream( err, true );

			log.atDebug().log( "Connecting to {0} ...", station.getAddress() );
			channel.connect( CHANNEL_TIMEOUT );
			log.atDebug().log( "Connected to {0}", station.getAddress() );

			// Read the input buffer
			byte[] buffer = new byte[ 1024 ];
			while( !channel.isClosed() ) {
				while( input.available() > 0 ) {
					int i = input.read( buffer );
					if( i < 0 ) break;
				}
			}
			int exitStatus = channel.getExitStatus();

			log.atDebug().log( "Disconnecting from {0} ...", station.getAddress() );
			channel.disconnect();
			session.disconnect();
			log.atDebug().log( "Disconnected from {0}", station.getAddress() );

			boolean isExpected = expectedExitStatuses.contains( exitStatus );
			if( !isExpected ) log.atDebug().log( "exit-status: {0} > {1}", exitStatus, command );
			if( !isExpected ) throw new IOException( "Unexpected exit status: " + exitStatus + " > " + command );
		} catch( JSchException exception ) {
			throw new IOException( exception );
		}
	}

	private void scpPut( Station station, Path sourcePath, String targetPath ) throws IOException {
		scpPut( station, sourcePath.toFile(), targetPath );
	}

	private void scpPut( Station station, String sourcePath, String targetPath ) throws IOException {
		scpPut( station, new File( sourcePath ), targetPath );
	}

	private void scpPut( Station station, File sourceFile, String targetPath ) throws IOException {
		try( FileInputStream fis = new FileInputStream( sourceFile ) ) {
			scpPut( sourceFile.getName(), sourceFile.length(), fis, station, targetPath );
		}
	}

	private void scpPut( String name, long filesize, InputStream sourceInput, Station station, String targetPath ) throws IOException {
		try {
			// Create the shell session
			Session session = jsch().getSession( station.getUser(), station.getAddress() );
			session.connect( CONNECT_TIMEOUT );

			// Modify the target path for remote use
			targetPath = targetPath.replace( "'", "'\"'\"'" );
			targetPath = "'" + targetPath + "'";

			// Create the execution channel
			ChannelExec channel = (ChannelExec)session.openChannel( "exec" );
			channel.setCommand( "scp -t " + targetPath );

			// Get the remote I/O streams
			OutputStream out = channel.getOutputStream();
			InputStream in = channel.getInputStream();

			log.atDebug().log( "Connecting to {0} ...", station.getAddress() );
			channel.connect();
			log.atDebug().log( "Connected to {0}", station.getAddress() );

			// Check that the remote scp is ready
			targetScpCheck( in );

			// Build the C command
			String command = "C0644 " + filesize + " " + name + "\n";

			// Send the scp command
			out.write( command.getBytes() );
			out.flush();
			targetScpCheck( in );

			// Send the content of the source file
			byte[] buffer = new byte[ 1024 ];
			int length = sourceInput.read( buffer );
			while( length > -1 ) {
				out.write( buffer, 0, length );
				length = sourceInput.read( buffer );
			}
			out.write( 0 );
			out.close();
			targetScpCheck( in );

			int exitStatus = channel.getExitStatus();

			log.at( exitStatus <= 0 ? Level.FINE : Level.WARNING ).log( "exit-status: {0}", exitStatus );
			log.atDebug().log( "Disconnecting from {0} ...", station.getAddress() );
			channel.disconnect();
			session.disconnect();
			log.atDebug().log( "Disconnected from {0}", station.getAddress() );
		} catch( JSchException exception ) {
			throw new IOException( exception );
		}
	}

	private static int targetScpCheck( InputStream in ) throws IOException {
		// b may be
		//   -1 or 0 for success
		//   1 for error
		//   2 for fatal error
		int b = in.read();

		// Get the error message
		if( b >= 1 && b <= 2 ) {
			StringBuilder buffer = new StringBuilder();
			int c = in.read();
			while( c != -1 && c != '\n' ) {
				buffer.append( (char)c );
				c = in.read();
			}
			throw new IOException( buffer.toString() );
		}

		return b;
	}

	private boolean isReachable( String name, int... ports ) throws IOException {
		int timeout = 250;
		try {
			InetAddress address = InetAddress.getByName( name );

			if( ping( address, timeout, ports.length == 0 ) ) return true;

			for( int port : ports ) {
				if( reach( address, port, timeout ) ) return true;
			}
		} catch( UnknownHostException exception ) {
			log.atDebug().log( "%s unknown host", this );
		}

		return false;
	}

	private static boolean ping( InetAddress address, int timeout, boolean noPorts ) throws IOException {
		try {
			if( address.isReachable( timeout ) ) return true;
		} catch( IOException exception ) {
			if( noPorts ) throw exception;
		}
		return false;
	}

	private boolean reach( InetAddress address, int port, int timeout ) throws IOException {
		try( Socket socket = new Socket() ) {
			socket.connect( new InetSocketAddress( address, port ), timeout );
			return true;
		} catch( SocketTimeoutException exception ) {
			log.atDebug().log( "%s connection timeout", this );
		} catch( ConnectException exception ) {
			log.atWarn().log( "%s %s", this, exception.getMessage().toLowerCase() );
		}
		return false;
	}

	private void stationUpdateFailure( Exception exception ) {
		getManager().getProgram().getNoticeManager().addNotice( new Notice( "Station Update Failure", exception.getMessage() ).setType( Notice.Type.WARN ) );
	}

	private static class JschLogger implements com.jcraft.jsch.Logger {

		static Map<Integer, java.util.logging.Level> name = new HashMap<>();

		static {
			name.put( DEBUG, Level.FINEST );
			name.put( INFO, Level.FINER );
			name.put( WARN, Level.FINE );
			name.put( ERROR, Level.WARNING );
			name.put( FATAL, Level.SEVERE );
		}

		public boolean isEnabled( int level ) {
			return true;
		}

		public void log( int level, String message ) {
			log.at( name.get( level ) ).log( message );
		}

	}

}
