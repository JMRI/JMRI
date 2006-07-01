/**
 * XNetPortControllerScaffold.java
 *
 * Description:	    test implementation of XNetPortController
 * @author			Bob Jacobsen
 * @version         $Revision: 1.2 $
 */

package jmri.jmrix.lenz;

import jmri.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

class XNetPortControllerScaffold extends XNetPortController {

            public java.util.Vector getPortNames() { return null; }
	    public String openPort(String portName, String appName) { return null; }
	    public void configure() {}
	    public String[] validBaudRates() { return null; }

		protected XNetPortControllerScaffold() throws Exception {
			PipedInputStream tempPipe;
			tempPipe = new PipedInputStream();
			tostream = new DataInputStream(tempPipe);
			ostream = new DataOutputStream(new PipedOutputStream(tempPipe));
			tempPipe = new PipedInputStream();
			istream = new DataInputStream(tempPipe);
			tistream = new DataOutputStream(new PipedOutputStream(tempPipe));
		}

		// returns the InputStream from the port
		public DataInputStream getInputStream() { return istream; }

		// returns the outputStream to the port
		public DataOutputStream getOutputStream() { return ostream; }

		// check that this object is ready to operate
		public boolean status() { return true; }
        public boolean okToSend() { return true; }

	    DataOutputStream ostream;  // Traffic controller writes to this
	    DataInputStream  tostream; // so we can read it from this

	    DataOutputStream tistream; // tests write to this
	    DataInputStream  istream;  // so the traffic controller can read from this

}