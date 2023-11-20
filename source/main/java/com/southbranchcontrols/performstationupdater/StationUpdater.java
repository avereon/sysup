package com.southbranchcontrols.performstationupdater;

import com.avereon.util.IoUtil;
import com.avereon.util.ThreadUtil;
import com.avereon.xenon.task.Task;
import com.avereon.zarra.javafx.Fx;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import lombok.CustomLog;
import lombok.Getter;

import java.io.*;
import java.net.*;
import java.util.concurrent.TimeoutException;

@Getter
@CustomLog
public class StationUpdater {

	private static final String PERFORM = "perform";

	private static final int CONNECT_TIMEOUT = 5000;

	private static final int RESTART_TIMEOUT = 120000;

	private final JSch jsch;

	private StationUpdateManager manager;

	private StationStatus station;

	private StationUpdater() {
		jsch = new JSch();
	}

	public static StationUpdater of( StationUpdateManager manager, StationStatus station ) {
		StationUpdater updater = new StationUpdater();
		updater.manager = manager;
		updater.station = station;
		return updater;
	}

	public void next() {
		setup( station );
	}

	private JSch jsch() throws JSchException {
		jsch.addIdentity( System.getProperty( "user.home" ) + "/.ssh/id_rsa" );
		jsch.setKnownHosts( System.getProperty( "user.home" ) + "/.ssh/known_hosts" );
		return jsch;
	}

	private void setup( StationStatus station ) {
		if( station.getSetupStatus().state() != StepStatus.State.WAITING ) return;

		getManager().getProgram().getTaskManager().submit( Task.of( "Perform station " + station.getAddress() + " setup", () -> {
			Fx.run( () -> station.setSetupStatus( StepStatus.of( StepStatus.State.RUNNING ) ) );
			try {
				InputStream resourceInput = getClass().getResourceAsStream( "station-update" );
				ByteArrayOutputStream resourceOutput = new ByteArrayOutputStream();
				IoUtil.copy( resourceInput, resourceOutput );
				if( resourceInput != null ) resourceInput.close();

				byte[] content = resourceOutput.toByteArray();
				long filesize = content.length;

				run( station.getAddress(), "sudo apt -y autoremove" );
				//				// Make sure there is an Updates folder
				//				run( station.getAddress(), "mkdir -p /home/perform/Updates" );
				//				scpPut( "station-update", filesize, new ByteArrayInputStream( content ), station.getAddress(), "/home/perform/Updates/station-update" );
				//				run( station.getAddress(), "chmod a+x /home/perform/Updates/station-update" );

				// Assuming all of that worked, update the step status
				Fx.run( () -> station.setSetupStatus( StepStatus.of( StepStatus.State.SUCCESS ) ) );
				update( station );
			} catch( IOException exception ) {
				Fx.run( () -> station.setSetupStatus( StepStatus.of( StepStatus.State.FAILURE ) ) );
				throw new RuntimeException( exception );
			}
		} ) );
	}

	private void update( StationStatus station ) {
		if( station.getUpdateStatus().state() != StepStatus.State.WAITING ) return;

		getManager().getProgram().getTaskManager().submit( Task.of( "Perform station " + station.getAddress() + " update", () -> {
			Fx.run( () -> station.setUpdateStatus( StepStatus.of( StepStatus.State.RUNNING ) ) );
			try {
				run( station.getAddress(), "sudo apt -y update" );

				// Assuming all of that worked, update the step status
				Fx.run( () -> station.setUpdateStatus( StepStatus.of( StepStatus.State.SUCCESS ) ) );
				upgrade( station );
			} catch( IOException exception ) {
				Fx.run( () -> station.setUpdateStatus( StepStatus.of( StepStatus.State.FAILURE ) ) );
				throw new RuntimeException( exception );
			}
		} ) );
	}

	private void upgrade( StationStatus station ) {
		if( station.getUpgradeStatus().state() != StepStatus.State.WAITING ) return;

		getManager().getProgram().getTaskManager().submit( Task.of( "Perform station " + station.getAddress() + " upgrade", () -> {
			Fx.run( () -> station.setUpgradeStatus( StepStatus.of( StepStatus.State.RUNNING ) ) );
			try {
				run( station.getAddress(), "sudo apt -y dist-upgrade" );

				// Assuming all of that worked, update the step status
				Fx.run( () -> station.setUpgradeStatus( StepStatus.of( StepStatus.State.SUCCESS ) ) );
				restart( station );
			} catch( IOException exception ) {
				Fx.run( () -> station.setUpgradeStatus( StepStatus.of( StepStatus.State.FAILURE ) ) );
				throw new RuntimeException( exception );
			}
		} ) );
	}

	private void restart( StationStatus station ) {
		if( station.getRestartStatus().state() != StepStatus.State.WAITING ) return;

		getManager().getProgram().getTaskManager().submit( Task.of( "Perform station " + station.getAddress() + " restart", () -> {
			Fx.run( () -> station.setRestartStatus( StepStatus.of( StepStatus.State.RUNNING ) ) );
			try {
				run( station.getAddress(), "sudo reboot; exit;" );

				// Wait a few seconds for the station to shut down before continuing
				// Otherwise, it is immediately reachable, which is not what we want
				ThreadUtil.pause( 10000 );

				// Assuming all of that worked, update the step status
				Fx.run( () -> station.setRestartStatus( StepStatus.of( StepStatus.State.SUCCESS ) ) );
				isAlive( station );
			} catch( IOException exception ) {
				Fx.run( () -> station.setRestartStatus( StepStatus.of( StepStatus.State.FAILURE ) ) );
				throw new RuntimeException( exception );
			}
		} ) );
	}

	private void isAlive( StationStatus station ) {
		if( station.getAliveStatus().state() != StepStatus.State.WAITING ) return;

		getManager().getProgram().getTaskManager().submit( Task.of( "Perform station " + station.getAddress() + " respond", () -> {
			Fx.run( () -> station.setAliveStatus( StepStatus.of( StepStatus.State.RUNNING ) ) );
			try {
				long threshold = System.currentTimeMillis() + RESTART_TIMEOUT;
				boolean isTimeout = System.currentTimeMillis() > threshold;
				while( !isTimeout ) {
					if( isReachable( station.getAddress(), 22 ) ) break;
					ThreadUtil.pause( 1000 );
					isTimeout = System.currentTimeMillis() > threshold;
				}

				if( isTimeout ) throw new TimeoutException();

				// Assuming all of that worked, update the step status
				Fx.run( () -> station.setAliveStatus( StepStatus.of( StepStatus.State.SUCCESS ) ) );
				//checkThatItCameBack( station );
			} catch( IOException | TimeoutException exception ) {
				Fx.run( () -> station.setAliveStatus( StepStatus.of( StepStatus.State.FAILURE ) ) );
				throw new RuntimeException( exception );
			}
		} ) );
	}

	private void run( String address, String command ) throws IOException {
		try {
			// Create the shell session
			Session session = jsch().getSession( PERFORM, address );
			session.connect( CONNECT_TIMEOUT );

			// Create the execution channel
			ChannelExec channel = (ChannelExec)session.openChannel( "exec" );
			channel.setCommand( command );
			//channel.setInputStream( null );
			//channel.setOutputStream( System.out );
			//channel.setErrStream( System.err );

			log.atConfig().log( "Connecting to {0} ...", address );
			channel.connect();
			log.atInfo().log( "Connected to {0}", address );

			// Read the input buffer
			byte[] buffer = new byte[ 1024 ];
			InputStream input = channel.getInputStream();
			while( !channel.isClosed() ) {
				while( input.available() > 0 ) {
					int i = input.read( buffer );
					if( i < 0 ) break;
				}
			}
			log.atDebug().log( "exit-status: {0}", channel.getExitStatus() );

			log.atConfig().log( "Disconnecting from {0} ...", address );
			channel.disconnect();
			session.disconnect();
			log.atInfo().log( "Disconnected from {0}", address );
		} catch( JSchException exception ) {
			throw new IOException( exception );
		}
	}

	private void scpPut( String sourcePath, String address, String targetPath ) throws IOException {
		File sourceFile = new File( sourcePath );
		try( FileInputStream fis = new FileInputStream( sourceFile ) ) {
			scpPut( sourceFile.getName(), sourceFile.length(), fis, address, targetPath );
		}
	}

	private void scpPut( String name, long filesize, InputStream sourceInput, String address, String targetPath ) throws IOException {
		try {
			// Create the shell session
			Session session = jsch().getSession( PERFORM, address );
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

			log.atConfig().log( "Connecting to {0} ...", address );
			channel.connect();
			log.atInfo().log( "Connected to {0}", address );

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

			log.atDebug().log( "exit-status: {0}", channel.getExitStatus() );
			log.atConfig().log( "Disconnecting from {0} ...", address );
			channel.disconnect();
			session.disconnect();
			log.atInfo().log( "Disconnected from {0}", address );
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

}
