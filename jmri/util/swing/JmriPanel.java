// JmriPanel.java

package jmri.util.swing;

/**
 * JPanel extension to handle automatic creation
 * of window title and help reference.
 * <p>
 * For use with {@link JmriAbstractAction}
 * or preferably {@link JmriNamedPaneAction}.
 * <p>
 * The expected initialization sequence is:
 *<ol>
 *<li>The constructor, which can initialize internal
 *    variables, but shouldn't expose the object by
 *    installing any listeners, etc.
 *<li>initComponents() is called, which initializes
 *    Swing components and can make other external references.
 *<li>initContext(Object context) is called, which can
 *    make outside connections
 *<li>Optionally, other usage-specific initialization
 *    methods can be called to e.g. connect to 
 *    protocol handlers.
 *</ol>
 *<p>
 * A {@link WindowInterface} property is provided for use
 * when the JmriPanel's controller logic wants to 
 * pop a subwindow.
 *
 * @author Bob Jacobsen  Copyright 2010
 * @since 2.9.4
 * @version $Revision: 1.5 $
 */

public class JmriPanel extends javax.swing.JPanel {

    /**
     * Provide a help target string which an enclosing
     * frame can provide as a help reference.
     */
    public String getHelpTarget() { return null; }

    /**
     * Provide a recommended title for an enclosing frame.
     */
    public String getTitle() { return null; }
    
    public WindowInterface getWindowInterface() {
        return wi;
    }
    private WindowInterface wi = null;
    public void setWindowInterface(WindowInterface w) {
        wi = w;
    }
    
    /**
     * 2nd stage of initialization, invoked after
     * the constuctor is complete.
     */
    public void initComponents() throws Exception {}
    
    /**
     * 3rd stage of initialization, invoked after
     * Swing components exist.
     */
    public void initContext(Object context) throws Exception {}
    
    public void dispose() {}
}