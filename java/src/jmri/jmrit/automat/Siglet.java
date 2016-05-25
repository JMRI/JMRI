package jmri.jmrit.automat;

import jmri.NamedBean;

/**
 * A Siglet is a "an embedded signal automation", like an "applet" an embedded
 * application.
 * <P>
 * Subclasses must load the inputs and outputs arrays during the defineIO
 * method. When any of these change, the Siglet must then recompute and apply
 * the output signal settings.
 * <P>
 * You can't assume that Siglets run in their own thread; they should not use
 * wait() in any of it's various forms.
 * <P>
 * Do not assume that Siglets will always inherit from AbstractAutomaton; that
 * may be an implementation artifact.
 * <P>
 * Do not have any overlap between the items in the input and output lists; this
 * will cause a recursive invocation when the output changes.
 *
 * @author	Bob Jacobsen Copyright (C) 2003
 */
public class Siglet extends AbstractAutomaton {

    public Siglet() {
        super();
    }

    public Siglet(String name) {
        super(name);
    }

    public NamedBean[] inputs;      // public for Jython subclass access
    public NamedBean[] outputs;     // public for Jython subclass access

    /**
     * User-provided routine to define the input and output objects to be
     * handled.
     */
    public void defineIO() {
    }

    /**
     * User-provided routine to compute new output state and apply it.
     */
    public void setOutput() {
    }

    /**
     * Implements AbstractAutomaton method to initialise connections to the
     * layout.
     */
    protected void init() {
        defineIO();
    }

    /**
     * Implements AbstractAutomaton method to wait for state changes and
     * respond.
     */
    protected boolean handle() {
        // update the result
        setOutput();
        // wait for changes
        waitChange(inputs);
        // and repeat
        return true;
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "EI_EXPOSE_REP2") // OK until Java 1.6 allows cheap array copy
    public void setInputs(NamedBean[] in) {
        inputs = in;
    }
}
