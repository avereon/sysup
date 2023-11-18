package com.southbranchcontrols.performstationupdater;

import com.avereon.util.IoUtil;
import com.avereon.xenon.task.Task;
import com.avereon.zarra.javafx.Fx;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import lombok.CustomLog;
import lombok.Getter;

import java.io.*;

@Getter
@CustomLog
public class StationUpdater {

	private static final String PERFORM = "perform";

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
		if( station.getSetup().state() != StepStatus.State.WAITING ) return;

		getManager().getProgram().getTaskManager().submit( Task.of( () -> {
			Fx.run( () -> station.setSetup( StepStatus.of( StepStatus.State.RUNNING ) ) );
			try {
				InputStream resourceInput = getClass().getResourceAsStream( "station-update" );
				ByteArrayOutputStream resourceOutput = new ByteArrayOutputStream();
				IoUtil.copy( resourceInput, resourceOutput );
				if( resourceInput != null ) resourceInput.close();

				byte[] content = resourceOutput.toByteArray();
				long filesize = content.length;

				// Make sure there is an Updates folder
				run( station.getAddress(), "mkdir -p /home/perform/Updates" );
				scpPut( "station-update", filesize, new ByteArrayInputStream( content ), station.getAddress(), "/home/perform/Updates/station-update" );
				run( station.getAddress(), "chmod a+x /home/perform/Updates/station-update" );

				// Assuming all of that worked, update the step status
				Fx.run( () -> station.setSetup( StepStatus.of( StepStatus.State.SUCCESS ) ) );
			} catch( IOException exception ) {
				Fx.run( () -> station.setSetup( StepStatus.of( StepStatus.State.FAILURE ) ) );
				throw new RuntimeException( exception );
			}
		} ) );
	}

	private void run( String address, String command ) throws IOException {
		try {
			// Create the shell session
			Session session = jsch().getSession( PERFORM, address );
			session.connect( 3000 );

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
			session.connect( 3000 );

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

}
