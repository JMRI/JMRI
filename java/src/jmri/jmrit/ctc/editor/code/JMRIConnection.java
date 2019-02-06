package code;

import gui.FrmMainForm;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import jmri.jmrit.ctcserialdata.CodeButtonHandlerData;

/**
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019
 * 
 * This object establishes and maintains a connection to the JMRI Simple Server.
 * https://www.developer.com/java/data/understanding-asynchronous-socket-channels-in-java.html
 * https://stackoverflow.com/questions/7872846/how-to-read-from-standard-input-non-blocking
 * https://stackoverflow.com/questions/30149214/how-to-make-multiple-threads-use-the-same-socket-to-read-and-write
 * 
 * Setting "socket.setKeepAlive(true);" is WORTHLESS, since the period of checking
 * is not programmable, and presently probably is set to something like 2 hours
 * at the Operating System level.
 * So instead, I implement a "keep alive ping" myself, about every second,
 * to see if the connection is still available.
 * 
 * Also the thread is created as a Daemon thread, so that it is low priority,
 * and will automatically go away after all of the main (non Daemon) thread(s) go away,
 * in case I have a "bug" somewhere in here that prevents it from going away on it's own
 * after "close()" is called.
 */
public class JMRIConnection {
    private static final int TIMEOUT_IN_MILLISECONDS = 500;
    private static final long KEEP_ALIVE_REPEAT_TIME_IN_MILLISECONDS = 1000;   // One second
    public enum ConnectionStatus { UNKNOWN, NOT_CONNECTED, CONNECTED, CONNECTED_WRONG_VERSION, NOT_RUNNING, UNINITIALIZED }

//  By definition, these are volatile, since they all are initialized in the constructor or now:    
    private final FrmMainForm _mFrmMainForm;
    private final SynchronousQueue<String> _mTransmitSynchronousQueue = new SynchronousQueue<>();
    private final SynchronousQueue<Boolean> _mReceiveSynchronousQueue = new SynchronousQueue<>();
    
//  These are all "shared" between GUI thread and Daemon:    
    private volatile int _mSequenceNummber;
    private volatile Thread _mThread = null;
    private volatile ConnectionStatus _mLastConnectionStatus = ConnectionStatus.UNINITIALIZED;
    private volatile boolean _mRunning;
    private volatile String _mHost;
    private volatile int _mPort;
    private volatile boolean _mAfterSocketReturns = false;
    
    private class RunnableClass implements Runnable {
        public void run() {
            _mRunning = true;
            try {
                Socket socket;
                Scanner inputScanner;
                DataOutputStream  dataOutputStream;
                String stringToTransmit;
                String stringReceived;
                Instant lastSentMsgTime;
                Instant nowTime;
                boolean sendKeepAlive;
                boolean firstTimeSendKeepAlive; // To find out if this server supports us!
                while (_mRunning) {
                    setConnectionStatus(ConnectionStatus.NOT_CONNECTED);
                    try {
                        socket = new Socket(_mHost, _mPort);
                        dataOutputStream = new DataOutputStream(socket.getOutputStream());
                        inputScanner = new Scanner(new InputStreamReader(socket.getInputStream(), "UTF-8"));                    
                        stringReceived = inputScanner.nextLine();  // Skip (ignore)
                        stringReceived = inputScanner.nextLine();  // "Welcome"
                        stringReceived = inputScanner.nextLine();  // lines from server.
                        setConnectionStatus(ConnectionStatus.CONNECTED);
                        boolean connected = true;   // Assume for now
                        lastSentMsgTime = Instant.now();
                        firstTimeSendKeepAlive = true;
                        _mAfterSocketReturns = true;    // Indicate to "start()" that we're running and ready (in a few instructions) to process requests.
                        while (connected && _mRunning) {
//  First, see if it's time for a keep alive:
//  Normally, these two concepts (KEEP_ALIVE and everything else) are in different
//  layers of the protocol.  I mix layers here for convienence:
                            nowTime = Instant.now();
                            sendKeepAlive = firstTimeSendKeepAlive | Duration.between(lastSentMsgTime, nowTime).toMillis() >= KEEP_ALIVE_REPEAT_TIME_IN_MILLISECONDS;
                            if (sendKeepAlive) { // Time for a keep alive:
                                stringToTransmit = createStringToTransmit(CodeButtonHandlerData.objectTypeToCheck.KEEP_ALIVE, "X");
                                firstTimeSendKeepAlive = false;
                            } else {
                                stringToTransmit = _mTransmitSynchronousQueue.poll(TIMEOUT_IN_MILLISECONDS, TimeUnit.MILLISECONDS);
                            }
                            if (stringToTransmit != null) { // Something to send:
                                String[] args = stringToTransmit.split(" ");
                                String sequenceNumberString = args.length >=2 ? args[1] : "X";
                                dataOutputStream.writeBytes(stringToTransmit + "\n");
                                while(true) {
                                    stringReceived = inputScanner.nextLine();
                                    if (stringReceived.compareTo(sequenceNumberString + " YES") == 0 || stringReceived.compareTo(sequenceNumberString + " NO") == 0) break;
                                    if (stringReceived.startsWith("Unknown")) {
                                        setConnectionStatus(ConnectionStatus.CONNECTED_WRONG_VERSION);
                                        continue;               // Try again.
                                    }
                                }
                                if (!sendKeepAlive) { // Queue the results to the caller:
                                    _mReceiveSynchronousQueue.offer(convertResponseStringToBoolean(stringReceived), TIMEOUT_IN_MILLISECONDS, TimeUnit.MILLISECONDS);
                                }
                                lastSentMsgTime = Instant.now();
                            } else { // Timed out:
                                if (!socket.isConnected()) { // Socket went away:
                                    socket.close();
                                    socket = null;          // Not that it matters, but principle
                                    connected = false;
                                    continue;               // Try again.
                                }
                            }
                        }
                    }
                    catch (IOException | NoSuchElementException ex)
                    {
                        _mAfterSocketReturns = true;    // Yes, socket returned an error indicating a problem with the connection.
                    }  // socket, readLine, inputScanner.nextLine()
                    catch (InterruptedException ex) {  // _mThread.interrupt() due to call of "close()".
                        _mRunning = false;
                        setConnectionStatus(ConnectionStatus.NOT_RUNNING); 
                        return;
                    }
                }
            } catch (Exception ex) {
                setConnectionStatus(ConnectionStatus.NOT_RUNNING);
            }
        }
    }

    public JMRIConnection(FrmMainForm frmMainForm) {
        _mFrmMainForm = frmMainForm;
        _mSequenceNummber = 0;
        setConnectionStatus(ConnectionStatus.UNKNOWN);
    }
    
//  GUI thread calls us here to start us up:    
//  NOTE:
//  Since this is called as part of the initialization of the GUI system,
//  we have time here to wait for the "socket" command above to 
//  either fail or succeed.  Otherwise, the connection isn't present when later
//  code starts checking all of the data for validity against JMRI, and it
//  passes as valid entries in error.  After all, JMRI takes FOREVER to start
//  itself up before it becomes responsive!
    public void start(String host, int port) {
        _mHost = host;  // Before we start it up,
        _mPort = port;  // let these be "stable".
        _mThread = new Thread(new RunnableClass());
        _mThread.setDaemon(true);
        _mThread.start();
        while (!_mAfterSocketReturns) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException ex) {}
        }
    }
    
//  GUI thread calls us here to shut us down:    
//  Since the thread IS a daemon thread, it is low priority, and will automatically go away when the main thread(s) terminates, in case
//  this thread for some reason doesn't go away in a timely fashion.
    public void close() {
        privateClose(false);
    }
    
//  Here we don't wait for "_mAfterSocketReturns", since this is part of a
//  GUI request to reconfigure us.  The main line code won't be doing any
//  inquiry(s) until the user presses a button once the connection is MADE.
    public void reconnect(String host, int port) {
        privateClose(true);
        start(host, port);
    }
    
    private void privateClose(boolean waitForCloseToComplete) {
        if (_mThread != null) {
            _mRunning = false;
            _mThread.interrupt();
            if (waitForCloseToComplete) {
                while (_mThread.getState() != Thread.State.TERMINATED) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) { // Who interrupted us?  Done in THAT case!
                        break;
                    }
                }
            }
            _mThread = null;
        }
    }

//  This is called by the GUI thread.  DO NOT SPEND TIME IN HERE, otherwise GUI will be unresponsive.    
//  Returns true if server not available!  If itemTypetoCheck = KEEP_ALIVE, then "jmriObjectToCheck" is ignored and can (for example) be null.
    public boolean JMRIQueryAndResponse(CodeButtonHandlerData.objectTypeToCheck itemTypetoCheck, String jmriObjectToCheck) {
        if (!ableToCommunicate()) return true; // Quickly!
        String stringToReturn = null;
        try {
            if (_mTransmitSynchronousQueue.offer(createStringToTransmit(itemTypetoCheck, jmriObjectToCheck), TIMEOUT_IN_MILLISECONDS, TimeUnit.MILLISECONDS)) {  // Worked:
                Boolean blah = _mReceiveSynchronousQueue.poll(TIMEOUT_IN_MILLISECONDS, TimeUnit.MILLISECONDS);
                return blah;
            }
        } catch (InterruptedException ex) {}
        return true;
    }

    private boolean ableToCommunicate() { return _mLastConnectionStatus == ConnectionStatus.CONNECTED; }
    
    private String createStringToTransmit(CodeButtonHandlerData.objectTypeToCheck itemTypetoCheck, String jmriObjectToCheck) {
        return  CodeButtonHandlerData.COMMAND_PREFIX + " "
                + Integer.toString(++_mSequenceNummber) + " "
                + itemTypetoCheck.toString() + " "
                + ((itemTypetoCheck == CodeButtonHandlerData.objectTypeToCheck.KEEP_ALIVE) ? "X" : jmriObjectToCheck);
//  The extra "()" in the above line was required due to a compiler bug.  Go figure!
    }
    
//  Report to the GUI Object the connection status, let it deal with multi-threading issues:
    private void setConnectionStatus(ConnectionStatus connectionStatus) {
        if (_mLastConnectionStatus != connectionStatus) {
            _mLastConnectionStatus = connectionStatus;
            _mFrmMainForm.updateJMRIStatus(connectionStatus);
        }
    }

//  If it doesn't make sense, it returns "false".  The sequence number is the first part of the string.
    private Boolean convertResponseStringToBoolean(String aString) {
        int spaceIndex = aString.indexOf(" ");  // Find start of YES or NO:
        if (spaceIndex < 0) return new Boolean(false);
        String yesOrNo = aString.substring(spaceIndex + 1);
        return new Boolean(yesOrNo.equals("YES"));
    }
}
