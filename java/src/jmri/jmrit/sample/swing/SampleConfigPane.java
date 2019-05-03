package jmri.jmrit.sample.swing;

import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerRepository;

/**
 * Sample config pane; all it does is show a label.
 * <p>
 * This works with the Start Up pane in Preferences
 * to add this "Open Sample Pane" via Perform action and 
 * Add button to main window options.
 *
 * @author Bob Jacobsen Copyright 2018
 * @since 4.13.4
 */
public class SampleConfigPane extends jmri.util.swing.JmriPanel {

    /**
     * Provide a recommended title for an enclosing frame.
     */
    @Override
    public String getTitle() {
        return "Sample Pane"; // for I18N, use e.g. Bundle.getMessage("MenuItemSample");
    }

    /**
     * Provide menu items
     */
    //@Override
    //public List<JMenu> getMenus() { return null; }
    
    public SampleConfigPane() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    /**
     * 2nd stage of initialization, invoked after the constructor is complete.
     */
    @Override
    public void initComponents() {
        JLabel label = new JLabel("Some GUI elements ...");
        this.add(label);
    }

    /**
     * 3rd stage of initialization, invoked after Swing components exist.
     */
    @Override
    public void initContext(Object context) {
    }

    /** {@inheritDoc} */
    @Override
    public void dispose() {
    }

    /**
     * Nested class to create one of these using old-style defaults.
     */
    static public class Default extends jmri.util.swing.JmriNamedPaneAction {

        public Default() {
            super("Open Sample Pane",  // eventually Bundle.getMessage("MenuItemSampleConfig"), for I18N
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    SampleConfigPane.class.getName());
        }
    }
}
