// AbstractAutomaton.java

package jmri.jmrit.automat;

import java.awt.*;
import javax.swing.*;

import jmri.*;

/**
 * Abstract base for user automaton classes, which provide
 * individual bits of automation.
 * <P>
 * Each individual automaton runs in a separate thread, so they
 * can operate independently.  This class handles thread
 * creation and scheduling, and provides a number of services
 * for the user code.
 * <P>
 * Subclasses provide a "handle()" function, which does the needed
 * work, and optionally a "init()" function.
 * These can use any JMRI resources for input and output.  It should
 * not spin on a condition without explicit wait requests; it is more efficient
 * to use the explicit wait services when waiting for some specific
 * condition.
 * <P>
 * handle() is executed repeatedly until either the Automate object is
 * halted(), or it returns "false".  Returning "true" will just cause
 * handle() to be invoked again, so you can cleanly restart the Automaton
 * by returning from multiple points in the function.
 * <P>
 * Since handle() executes outside the GUI thread, it is important that
 * access to GUI (AWT, Swing) objects be scheduled through the
 * various service routines.
 * <P>
 * Services are provided by public member functions, described below.
 * They must only be invoked from the init and handle methods, as they
 * must be used in a delayable thread.  If invoked from the GUI thread,
 * for example, the program will appear to hang. To help ensure this,
 * a warning will be logged if they are used before the thread starts.
 *
 * @author	Bob Jacobsen    Copyright (C) 2003
 * @version     $Revision: 1.13 $
 */
abstract public class AbstractAutomaton implements Runnable {

    public AbstractAutomaton() {}

    public void start() {
        Thread t = new Thread(this);
        t.start();
    }

    public void run() {
        inThread = true;
        init();
        while (handle()) {}
    }

    /**
     * User-provided initialization routine
     */
    abstract protected void init();

    /**
     * User-provided main routine. This is run repeatedly until
     * it signals the end by returning false.  Many automata
     * are intended to run forever, and will always return true.
     *
     * @return false to terminate the automaton, for example due to an error.
     */
    abstract protected boolean handle();

    /**
     * Control optional debugging prompt.  If this is set true,
     * each call to wait() will prompt the user whether to continue.
     */
    protected boolean promptOnWait = false;

    /**
     * Wait for an interval, in a simple form.
     * <P>
     * This handles exceptions internally,
     * so they needn't clutter up the code.  Note that the current
     * implementation doesn't guarantee the time, either high or low.
     * @param milliseconds
     */
    protected synchronized void wait(int milliseconds){
        if (!inThread) log.warn("wait invoked from invalid context");
        try {
            super.wait(milliseconds);
        } catch (InterruptedException e) {
            // do nothing for now
        }
        if (promptOnWait) debuggingWait();
    }

    /**
     * Flag used to ensure that service routines
     * are only invoked in the automaton thread.
     */
    private boolean inThread = false;

    private AbstractAutomaton self = this;

    /**
     * Wait for a sensor to change state.
     * <P>
     * The current (OK) state of the Sensor is passed to avoid
     * a possible race condition. The new state is returned
     * for a similar reason.
     * <P>
     * This works by registering a listener, which is likely to
     * run in another thread.  That listener then interrupts the automaton's
     * thread, who confirms the change.
     *
     * @param mState Current state of the sensor
     * @param mSensor Sensor to watch
     * @return newly detected Sensor state
     */
    protected synchronized int waitSensorChange(int mState, Sensor mSensor){
        if (!inThread) log.warn("waitSensorChange invoked from invalid context");
        if (log.isDebugEnabled()) log.debug("waitSensorChange starts: "+mSensor.getSystemName());
        // register a listener
        java.beans.PropertyChangeListener l;
        mSensor.addPropertyChangeListener(l = new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                synchronized (self) {
                    self.notify();
                }
            }
        });

        int now;
        while (mState == (now = mSensor.getKnownState())) {
            try {
                super.wait(10000);
            } catch (InterruptedException e) {
                log.warn("waitSensorChange interrupted; this is unexpected");
            }
        }

        // remove the listener & report new state
        mSensor.removePropertyChangeListener(l);

        return now;
    }

    /**
     * Wait for a sensor to become active.
     * <P>
     * This works by registering a listener, which is likely to
     * run in another thread.  That listener then interrupts the automaton's
     * thread, who confirms the change.
     *
     * @param mSensor Sensor to watch
     */
    protected synchronized void waitSensorActive(Sensor mSensor){
        if (!inThread) log.warn("waitSensorActive invoked from invalid context");
        if (mSensor.getKnownState() == Sensor.ACTIVE) return;
        if (log.isDebugEnabled()) log.debug("waitSensorActive starts: "+mSensor.getSystemName());
        // register a listener
        java.beans.PropertyChangeListener l;
        mSensor.addPropertyChangeListener(l = new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                synchronized (self) {
                    self.notify();
                }
            }
        });

        while (Sensor.ACTIVE != mSensor.getKnownState()) {
            try {
                super.wait(10000);
            } catch (InterruptedException e) {
                log.warn("waitSensorActive interrupted; this is unexpected");
            }
        }

        // remove the listener & report new state
        mSensor.removePropertyChangeListener(l);

        return;
    }

    /**
     * Wait for a sensor to become inactive.
     * <P>
     * This works by registering a listener, which is likely to
     * run in another thread.  That listener then interrupts the automaton's
     * thread, who confirms the change.
     *
     * @param mSensor Sensor to watch
     */
    protected synchronized void waitSensorInactive(Sensor mSensor){
        if (!inThread) log.warn("waitSensorInactive invoked from invalid context");
        if (mSensor.getKnownState() == Sensor.INACTIVE) return;
        if (log.isDebugEnabled()) log.debug("waitSensorInactive starts: "+mSensor.getSystemName());
        // register a listener
        java.beans.PropertyChangeListener l;
        mSensor.addPropertyChangeListener(l = new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                synchronized (self) {
                    self.notify();
                }
            }
        });

        while (Sensor.INACTIVE != mSensor.getKnownState()) {
            try {
                super.wait(10000);
            } catch (InterruptedException e) {
                log.warn("waitSensorInactive interrupted; this is unexpected");
            }
        }

        // remove the listener & report new state
        mSensor.removePropertyChangeListener(l);

        return;
    }

    /**
     * Wait for one of a list of sensors to be active.
     * <P>
     * This works by registering a listener, which is likely to
     * run in another thread.  That listener then interrupts the automaton's
     * thread, who confirms the change.
     *
     * @param mSensors Array of sensors to watch
     */
    protected synchronized void waitSensorActive(Sensor[] mSensors){
        if (!inThread) log.warn("waitSensorActive invoked from invalid context");
        if (log.isDebugEnabled()) log.debug("waitSensorActive[] starts");

        // do a quick check first, just in case
        if (checkForActive(mSensors)) {
            log.debug("returns immediately");
            return;
        }
        // register listeners
        int i;
        java.beans.PropertyChangeListener[] listeners =
                new java.beans.PropertyChangeListener[mSensors.length];
        for (i=0; i<mSensors.length; i++) {

            mSensors[i].addPropertyChangeListener(listeners[i] = new java.beans.PropertyChangeListener() {
                public void propertyChange(java.beans.PropertyChangeEvent e) {
                    synchronized (self) {
                        log.debug("notify waitSensorActive[] of property change");
                        self.notify();
                    }
                }
            });

        }

        while (!checkForActive(mSensors)) {
            try {
                super.wait(10000);
            } catch (InterruptedException e) {
                log.warn("waitSensorChange interrupted; this is unexpected");
            }
        }

        // remove the listeners
        for (i=0; i<mSensors.length; i++) {
            mSensors[i].removePropertyChangeListener(listeners[i]);
        }

        return;
    }

    /**
     * Wait for one of a list of NamedBeans (sensors, signal heads and/or turnouts) to change.
     * <P>
     * This works by registering a listener, which is likely to
     * run in another thread.  That listener then interrupts the automaton's
     * thread, who confirms the change.
     *
     * @param mInputs Array of NamedBeans to watch
     */
    protected synchronized void waitChange(NamedBean[] mInputs){
        if (!inThread) log.warn("waitChange invoked from invalid context");
        if (log.isDebugEnabled()) log.debug("waitChange[] starts");

        // register listeners
        int i;
        java.beans.PropertyChangeListener[] listeners =
                new java.beans.PropertyChangeListener[mInputs.length];
        for (i=0; i<mInputs.length; i++) {

            mInputs[i].addPropertyChangeListener(listeners[i] = new java.beans.PropertyChangeListener() {
                public void propertyChange(java.beans.PropertyChangeEvent e) {
                    synchronized (self) {
                        log.debug("notify waitChange[] of property change");
                        self.notify();
                    }
                }
            });

        }

        // wait for notify
        try {
            super.wait();
        } catch (InterruptedException e) {
            log.warn("waitChange interrupted; this is unexpected");
        }

        // remove the listeners
        for (i=0; i<mInputs.length; i++) {
            mInputs[i].removePropertyChangeListener(listeners[i]);
        }

        return;
    }

    /**
     * Wait for one of an array of sensors to change.
     * <P>
     * This is an older method, now superceded by waitChange, which can wait
     * for any NamedBean.
     *
     * @param mSensors Array of sensors to watch
     */
    protected synchronized void waitSensorChange(Sensor[] mSensors){
        waitChange(mSensors);
       return;
    }

    /**
     * Check an array of sensors to see if any are active
     * @param mSensors Array to check
     * @return true if any are ACTIVE
     */
    private boolean checkForActive(Sensor[] mSensors) {
        for (int i=0; i<mSensors.length; i++) {
            if (mSensors[i].getKnownState() == Sensor.ACTIVE) return true;
        }
        return false;
    }

    private DccThrottle throttle;
    /**
     * Obtains a DCC throttle, including waiting for the command station response.
     * @param address
     * @param longAddress true if this is a long address, false for a short address
     * @return A usable throttle, or null if error
     */
    protected DccThrottle getThrottle(int address, boolean longAddress) {
        if (!inThread) log.warn("getThrottle invoked from invalid context");
        throttle = null;
        InstanceManager.throttleManagerInstance()
                .requestThrottle(address,new ThrottleListener() {
                    public void notifyThrottleFound(DccThrottle t) {
                        throttle = t;
                        synchronized (self) {
                            self.notify();
                        }
                    }
                });
        // now wait for reply from identified throttle
        while (throttle == null) {
            log.debug("waiting for throttle");
            try {
                synchronized (self) {
                    super.wait(10000);
                }
            } catch (InterruptedException e) {
                log.warn("getThrottle got unexpected interrupt");
            }
            if (throttle == null) log.warn("Still waiting for throttle!");
        }
        return throttle;
    }

    /**
     * Write a CV on the service track, including waiting for completion.
     * @param CV Number 1 through 512
     * @param value
     * @return true if completed OK
     */
    protected boolean writeServiceModeCV(int CV, int value) {
        // get service mode programmer
        Programmer programmer = InstanceManager.programmerManagerInstance()
                        .getServiceModeProgrammer();

        // do the write, response will wake the thread
        try {
            programmer.writeCV(CV, value, new ProgListener() {
                public void programmingOpReply(int value, int status) {
                    synchronized (self) { self.notify(); }
                }
            });
        } catch (ProgrammerException e) {
            log.warn("Exception during writeServiceModeCV: "+e);
            return false;
        }
        // wait for the result
        try {
            synchronized (self) {
                super.wait(30000);
            }
        } catch (InterruptedException e) {
            log.warn("writeServiceModeCV got unexpected interrupt");
        }
        return true;
    }

    private volatile int cvReturnValue;
    private volatile int cvReturnStatus;

    /**
     * Read a CV on the service track, including waiting for completion.
     * @param CV Number 1 through 512
     * @return -1 if error, else value
     */
    protected int readServiceModeCV(int CV) {
        // get service mode programmer
        Programmer programmer = InstanceManager.programmerManagerInstance()
                        .getServiceModeProgrammer();

        // do the write, response will wake the thread
        cvReturnValue = -1;
        try {
            programmer.readCV(CV, new ProgListener() {
                public void programmingOpReply(int value, int status) {
                    cvReturnValue = value;
                    cvReturnStatus = status;
                    synchronized (self) { self.notify(); }
                }
            });
        } catch (ProgrammerException e) {
            log.warn("Exception during writeServiceModeCV: "+e);
            return -1;
        }
        // wait for the result
        try {
            synchronized (self) {
                super.wait(30000);
            }
        } catch (InterruptedException e) {
            log.warn("writeServiceModeCV got unexpected interrupt");
        }
        return cvReturnValue;
    }

    /**
     * Write a CV in ops mode, including waiting for completion.
     * @param CV Number 1 through 512
     * @param value
     * @param loco   Locomotive decoder address
     * @param longAddress true is the locomotive is using a long address
     * @return true if completed OK
     */
    protected boolean writeOpsModeCV(int CV, int value, boolean longAddress, int loco) {
        // get service mode programmer
        Programmer programmer = InstanceManager.programmerManagerInstance()
                        .getOpsModeProgrammer(longAddress, loco);

        // do the write, response will wake the thread
        try {
            programmer.writeCV(CV, value, new ProgListener() {
                public void programmingOpReply(int value, int status) {
                    synchronized (self) { self.notify(); }
                }
            });
        } catch (ProgrammerException e) {
            log.warn("Exception during writeServiceModeCV: "+e);
            return false;
        }
        // wait for the result
        try {
            synchronized (self) {
                super.wait(30000);
            }
        } catch (InterruptedException e) {
            log.warn("writeServiceModeCV got unexpected interrupt");
        }
        return true;
    }

    JFrame messageFrame = null;
    String message = null;

    /**
     * Internal class to show a Frame
     */
    public class MsgFrame implements Runnable {
        String mMessage;
        boolean mPause;
        boolean mShow;
        JFrame mFrame = null;
        JButton mButton;
        JTextArea mArea;

        public void hide() {
            mShow = false;
            // invoke the operation
            javax.swing.SwingUtilities.invokeLater(this);
        }

        /**
         * Show a message, and optionally wait for the user to acknowledge
         */
        public void show(String pMessage, boolean pPause) {
            mMessage = pMessage;
            mPause = pPause;
            mShow = true;

            // invoke the operation
            javax.swing.SwingUtilities.invokeLater(this);
            // wait to proceed?
            if (mPause) {
                synchronized(self) {
                    try {
                        self.wait();
                    }  catch (InterruptedException e) {
                        log.warn("Interrupted during pause, not expected");
                    }
                }
            }
        }
        public void run() {
            // create the frame if it doesn't exist
            if (mFrame==null) {
                mFrame = new JFrame("");
                mArea = new JTextArea();
                mArea.setEditable(false);
                mArea.setLineWrap(false);
                mArea.setWrapStyleWord(true);
                mButton = new JButton("Continue");
                mFrame.getContentPane().setLayout(new BorderLayout());
                mFrame.getContentPane().add(mArea, BorderLayout.CENTER );
                mFrame.getContentPane().add(mButton, BorderLayout.SOUTH );
                mButton.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        synchronized (self) {
                            self.notify();
                        }
                        mFrame.hide();
                    }
                });
                mFrame.pack();
            }
            if (mShow) {
                // update message, show button if paused
                mArea.setText(mMessage);
                if (mPause) {
                    mButton.setVisible(true);
                } else {
                    mButton.setVisible(false);
                }
                // do optional formatting
                format();
                // center the frame
                mFrame.pack();
                Dimension screen = mFrame.getContentPane().getToolkit().getScreenSize();
                Dimension size = mFrame.getSize();
                mFrame.setLocation((screen.width-size.width)/2,(screen.height-size.height)/2);
                // and show it to the user
                mFrame.show();
            }
            else mFrame.hide();
        }

        /**
         * Abstract method to handle formatting of the text on a show
         */
        protected void format() {}
    }

    JFrame debugWaitFrame = null;

    /**
     * Wait for the user to OK moving forward. This is complicated
     * by not running in the GUI thread, and by not wanting to use
     * a modal dialog.
     */
    private void debuggingWait() {
        // post an event to the GUI pane
        Runnable r = new Runnable() {
            public void run() {
                // create a prompting frame
                if (debugWaitFrame==null) {
                    debugWaitFrame = new JFrame("Automaton paused");
                    JButton b = new JButton("Continue");
                    debugWaitFrame.getContentPane().add(b);
                    b.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            synchronized (self) {
                                self.notify();
                            }
                            debugWaitFrame.hide();
                        }
                    });
                    debugWaitFrame.pack();
                }
                debugWaitFrame.show();
            }
        };
        javax.swing.SwingUtilities.invokeLater(r);
        // wait to proceed
        try {
            super.wait();
        }  catch (InterruptedException e) {
            log.warn("Interrupted during debugging wait, not expected");
        }
    }
    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AbstractAutomaton.class.getName());
}

/* @(#)AbstractAutomaton.java */
