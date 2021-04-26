package jmri.util.swing;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Provides a standard "search bar" for addition to 
 * other panels.  Actual search is via call-back.
 *
 * @author Bob Jacobsen
 */
public class SearchBar extends javax.swing.JPanel {

    Runnable forward;
    Runnable backward;
    Runnable done;
    
    JTextField textField = new JTextField();
    JButton rightButton = new JButton(">");
    JButton leftButton = new JButton("<");
    JButton doneButton = new JButton(Bundle.getMessage("ButtonDone"));
    
    public SearchBar(Runnable forward, Runnable backward, Runnable done) {
        super();
        this.forward = forward;
        this.backward = backward;
        this.done = done;
        
        // create GUI
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(textField);
        add(Box.createHorizontalGlue());
        add(leftButton);
        add(rightButton);
        add(doneButton);
        
        leftButton.setToolTipText("Search backwards");
        rightButton.setToolTipText("Search forwards");
        doneButton.setToolTipText("Close search bar");
        
        // add actions
        doneButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (done != null) {
                    log.debug("firing done");
                    done.run();
                } else {
                    log.warn("no search done defined, setting invisible");
                    SearchBar.this.setVisible(false);
                }
            }
        });
        
        leftButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (backward != null) {
                    log.debug("firing backward");
                    backward.run();
                } else {
                    log.warn("no backward search defined");
                }
            }
        });
        
        rightButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (forward != null) {
                    log.debug("firing forward");
                    forward.run();
                } else {
                    log.warn("no forward search defined");
                }
            }
        });

        // Enter in the text field does a forward search
        // and then leaves forward search selected for the next one
        textField.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (forward != null) {
                    log.debug("firing forward");
                    forward.run();
                } else {
                    log.warn("no forward search defined");
                }
                rightButton.requestFocusInWindow();
            }
        });
        
    }

    public String getSearchString() {
        return textField.getText();
    }
    
    /**
     * A service routine to connect the SearchBar to 
     * the usual modifier keys
     * @param frame JFrame containing this search bar; used to set key maps
     */
    public void configureKeyModifiers(JFrame frame) {
        JRootPane rootPane = frame.getRootPane();
        
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "openSearch");
        
        rootPane.getActionMap().put("openSearch", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
            
                // don't retain last text?
                textField.setText("");
                
                // alternate visible
                SearchBar.this.setVisible(! SearchBar.this.isVisible());
                
                // if visible, move focus
                if (SearchBar.this.isVisible()) {
                    SearchBar.this.textField.requestFocusInWindow();
                }
            }
        });

        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_G, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()|java.awt.event.InputEvent.SHIFT_DOWN_MASK), "forwardSearch");
        
        rootPane.getActionMap().put("forwardSearch", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
            
                // same as button
                leftButton.doClick();
                }
        });

        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_G, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "backwardSearch");
        
        rootPane.getActionMap().put("backwardSearch", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
            
                // same as button
                rightButton.doClick();
                }
        });
    }
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SearchBar.class);
}
