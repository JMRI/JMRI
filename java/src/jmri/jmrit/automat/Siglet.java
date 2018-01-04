package jmri.jmrit.automat;

import java.beans.PropertyChangeEvent;
import java.util.Arrays;
import jmri.NamedBean;
import jmri.util.PropertyChangeEventQueue;
import jmri.util.ThreadingUtil;

/**
 * A Siglet is a "an embedded signal automation", like an "applet" an embedded
 * application.
 * <P>
 * Subclasses must load the inputs and outputs arrays during the defineIO
 * method. When any of these change, the Siglet must then recompute and apply
 * the output signal settings via their implementation of the {@link #setOutput}
 * method.
 * <P>
 * Siglets may not run in their own thread; they should not use wait() in any of
 * it's various forms.
 * <P>
 * Siglet was separated from AbstractAutomaton in JMRI 4.9.2
 * <P>
 * Do not have any overlap between the items in the input and output lists; this
 * will cause a recursive invocation when the output changes.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2017
 */
abstract public class Siglet {

    public Siglet() {
        this.name = "";
    }

    public Siglet(String name) {
        this.name = name;
    }

    public NamedBean[] inputs;      // public for Jython subclass access
    public NamedBean[] outputs;     // public for Jython subclass access

    /**
     * User-provided routine to define the input and output objects to be
     * handled. Invoked during the Siglet {@link #start()} call.
     */
    abstract public void defineIO();

    /**
     * User-provided routine to compute new output state and apply it.
     */
    abstract public void setOutput();

    final public String getName() {
        return name;
    }
    private String name;

    final public void setName(String name) {
        this.name = name;
    }

    public void start() {
        Thread previousThread = thread;
        try {
            if (previousThread != null) {
                previousThread.join();
            }
        } catch (InterruptedException e) {
            log.warn("Aborted start() due to interrupt");
        }
        if (thread != null) {
            log.error("Found thread != null, which is an internal synchronization error for {}", name);
        }

        defineIO(); // user method that will load inputs
        if (inputs == null || inputs.length <= 0) {
            log.error("Siglet start invoked {}, but no inputs provided", ((name!=null && !name.isEmpty()) ? "for \""+name+"\"" : "(without a name)") );
            return;
        }

        pq = new PropertyChangeEventQueue(inputs);
        setOutput();

        // run one cycle at start
        thread = new Thread(() -> {
            while (true) {
                try {
                    PropertyChangeEvent pe = pq.take();
                    // _any_ event drives output
                    log.trace("driving setOutput from {}", pe);
                    ThreadingUtil.runOnLayout(() -> {
                        setOutput();
                    });
                } catch (InterruptedException e) {
                    log.trace("InterruptedException");
                    thread.interrupt();
                }
                if (thread.isInterrupted()) {
                    log.trace("isInterrupted()");
                    // done
                    pq.dispose();
                    thread = null; // flag that this won't execute again
                    return;
                }
            }
        });
        thread.setDaemon(true);
        thread.setName(getName());
        thread.start();
    }

    /**
     * Stop execution of the logic.
     * <p>
     * Note: completion not guaranteed when this returns, as the internal
     * operation may proceed for a short time. It's safe to call "start" again
     * without worrying about that.
     */
    public void stop() {
        if (thread != null) {
            thread.interrupt();
        }
    }

    public boolean isRunning() {
        return thread != null;
    }
    /**
     * Set inputs to the items in in.
     *
     * @param in the inputs to set
     */
    public void setInputs(NamedBean[] in) {
        inputs = Arrays.copyOf(in, in.length);
    }

    protected PropertyChangeEventQueue pq;
    protected Thread thread;
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Siglet.class);

}
