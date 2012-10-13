// JsonServer.java
package jmri.jmris.json;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import java.util.Scanner;

import jmri.jmris.JmriConnection;
import jmri.jmris.JmriServer;

import org.apache.log4j.Logger;

/**
 * This is an implementation of a simple server for JMRI.
 * There is currently no handshaking in this server.  You may just start 
 * sending commands.
 * @author Paul Bender Copyright (C) 2010
 * @version $Revision: 21126 $
 *
 */
public class JsonServer extends JmriServer {

	private static JmriServer _instance = null;
	static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmris.json.JsonServer");
	static Logger log = Logger.getLogger(JsonServer.class.getName());

	public static JmriServer instance() {
		if (_instance == null) {
			int port = Integer.parseInt(rb.getString("JsonServerPort"));
			_instance = new JsonServer(port);
		}
		return _instance;
	}

	// Create a new server using the default port
	public JsonServer() {
		this(Integer.parseInt(rb.getString("JsonServerPort")));
	}

	public JsonServer(int port) {
		super(port);
		log.info("JMRI JsonServer started on port " + port);
	}

	// Handle communication to a client through inStream and outStream
	@Override
	public void handleClient(DataInputStream inStream, DataOutputStream outStream) throws IOException {
		Scanner scanner = new Scanner(new InputStreamReader(inStream));
		JsonClientHandler handler = new JsonClientHandler(new JmriConnection(outStream));

		// Start by sending a welcome message
		// TODO: use a settable constant
		handler.sendHello(10);

		// TODO: implement heartbeat-based timer
		while (true) {
			scanner.skip("[\r\n]*");// skip any stray end of line characters.
			// Read the command from the client
			try {
				handler.onMessage(scanner.nextLine());
			} catch (IOException e) {
				scanner.close();
				handler.onClose();
				throw e;
			} catch (NoSuchElementException nse) {
				// we get an NSE when we are finished with this client
				// so break out of the loop
				break;
			}
		}
		scanner.close();
		handler.onClose();
	}

}