// JmriPanel.java

package jmri.util.swing;

/**
 * JPanel extension to handle automatic creation
 * of window title and help reference.
 * <p>
 * For use with JmriAbstractAction, etc
 *
 * @author Bob Jacobsen  Copyright 2010
 * @since 2.9.4
 * @version $Revision: 1.2 $
 */

public class JmriPanel extends javax.swing.JPanel {

    public String getHelpTarget() { return null; }
    public String getTitle() { return null; }
    
    protected WindowInterface getWindowInterface() {
        return wi;
    }
    private WindowInterface wi = null;
    protected void setWindowInterface(WindowInterface w) {
        wi = w;
    }
    
    /**
     * 2nd stage of initialization, invoked after
     * the constuctor is complete.
     */
    public void initComponents() throws Exception {}
    
    public void dispose() {}
}