package jmri.util.swing;

import java.awt.event.ActionEvent;
import javax.swing.Icon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base for actions that will work with multiple JMRI GUIs.
 *
 * An opaque Object can be passed as a context, but null is also possible.
 *
 * <b>NOTE</b> Either
 * {@link jmri.util.swing.JmriAbstractAction#actionPerformed(java.awt.event.ActionEvent)}
 * or {@link jmri.util.swing.JmriAbstractAction#makePanel()} must be overridden
 * by extending classes.
 *
 * @author	Bob Jacobsen Copyright (C) 2010
 */
abstract public class JmriAbstractAction extends javax.swing.AbstractAction {

    protected WindowInterface.Hint hint = WindowInterface.Hint.DEFAULT;
    protected WindowInterface wi;
    protected Object context = null;
    private final static Logger log = LoggerFactory.getLogger(JmriAbstractAction.class);

    /**
     * Enhanced constructor for placing the pane in various GUIs.
     *
     * @param name the name for the action; a value of null is ignored
     * @param wi   the window interface controlling how this action is displayed
     */
    public JmriAbstractAction(String name, WindowInterface wi) {
        super(name);
        this.wi = wi;
        if (wi == null) {
            log.error("Cannot create action with null WindowInterface", new Exception());
        }
    }

    public JmriAbstractAction(String s, Icon i, WindowInterface wi) {
        super(s, i);
        this.wi = wi;
    }

    /**
     * Set the context for this action. The context can be any object that an
     * overriding class may need to complete an action. It is defined here to
     * provide a common API for passing these objects in.
     *
     * @param context the context object
     * @since 2.9.4
     */
    public void setContext(Object context) {
        this.context = context;
    }

    /**
     * Original constructor for compatibility with older menus. Assumes SDI GUI.
     *
     * @param name the name for the action; a value of null is ignored
     */
    public JmriAbstractAction(String name) {
        super(name);
        this.wi = new jmri.util.swing.sdi.JmriJFrameInterface();
    }

    public void setWindowInterface(WindowInterface wi) {
        this.wi = wi;
    }

    public void setName(String name) {
        putValue(javax.swing.Action.NAME, name);
    }

    @Override
    public String toString() {
        return (String) getValue(javax.swing.Action.NAME);
    }

    public JmriAbstractAction setHint(WindowInterface.Hint hint) {
        this.hint = hint;
        return this;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // we have to make a new panel if we don't have one yet
        // we don't make a new panel if the window interface is
        //      single instance (not multiple instance), 
        // or if the existing panel is single instance (not multiple instance)
        if (cache == null
                || (wi.multipleInstances() && cache.isMultipleInstances())) {
            try {
                cache = makePanel();
            } catch (Exception ex) {
                log.error("Exception creating panel: " + ex);
                return;
            }
            if (cache == null) {
                log.error("Unable to make panel");
                return;
            }
        }

        wi.show(cache, this, hint);  // no real context, this is new content
    }

    public void dispose() {
        if (cache != null) {
            cache.dispose();
            cache = null;
        }
    }
    JmriPanel cache = null;

    // A crude method to set a parameter in a given window when loading from the xml file
    public void setParameter(String parameter, String value) {
    }

    // A method to allow named parameters to be passed in
    // Note that if value is a String, setParameter(String, String) needs to be
    // implemented (for reasons I do not understand jmri.util.swing.GuiUtilBase
    // will not call this method with a String parameter for value)
    public void setParameter(String parameter, Object value) {
    }

    abstract public JmriPanel makePanel();
    /* {
        log.error("makePanel must be overridden", new Exception());
        return null;
    } */
}
