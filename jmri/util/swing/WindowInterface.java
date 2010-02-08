// WindowInterface.java

package jmri.util.swing;

import javax.swing.*;

/**
 * Interface for an object that can arrange for a 
 * JComponent to be displayed.
 * <p>
 * Typically used by some component that wants to display
 * a pane (e.g. in a JmriJFrame) to do some more stuff.
 * Rather than have the component build it's own window, etc
 * it invokes one of these, so that the position and 
 * display of that component can controlled.
 * <p>
 * Any JmriAbstractActiont that uses the show() method
 * will have its dispose() invoked when the associated frame
 * goes away.  It should dispose() any cached panes at that time.
 *
 * @author Bob Jacobsen  Copyright 2010
 * @since 2.9.4
 * @version $Revision: 1.2 $
 */

public interface WindowInterface {

    /**
     * Show, in whatever way is appropriate, 
     * a specific JComponent
     * @param child new JComponent to show
     * @param action JmriAbstractAction making the request
     */
    public void show(jmri.util.swing.JmriPanel child, JmriAbstractAction action );
    
    /**
     * Show, in whatever way is appropriate, 
     * a specific JComponent
     * @param child new JComponent to show
     * @param action JmriAbstractAction making the request
     * @param hint suggestion on where to put the content
     */
    public void show(jmri.util.swing.JmriPanel child, JmriAbstractAction action, Hint hint );
    
    /**
     * Should 2nd and subsequent requests
     * for a panel (e.g. in an Action) create a
     * new instance, or provide the 1st one?
     *@return true if multiple instances should be provided,
     *         false if only one should be provided.
     */
    public boolean multipleInstances();
    
    public enum Hint {
        DEFAULT,   // let the interface pick
        REPLACE,   // replace the current content with new
        EXTEND     // place nearby
    }
}