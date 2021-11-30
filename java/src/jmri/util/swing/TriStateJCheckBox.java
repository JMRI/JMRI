package jmri.util.swing;

import java.awt.*;
import java.awt.event.*;

import javax.annotation.Nonnull;
import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JPanel containing 
 * Extension of JCheckBox allowing a partial state to be displayed.
 * 
 * The partial state is in the form of a small square, a style similar to Google Earth.
 * 
 * If Checkbox is pressed when unchecked, state changes to checked.
 * If Checkbox is pressed when checked, state changes to unchecked.
 * If Checkbox is pressed when partial, state changes to unchecked.
 * 
 * User can only check / un-check the checkbox, 
 * the partial state is only available programatically.
 * 
 * State can be set by enum or an array of boolean values.
 * 
 * The enum of the actual state can be obtained,
 * isSelected() returns false when in a partial state.
 * 
 * <p>
 * Inspired by postings of
 * 843806 https://community.oracle.com/tech/developers/discussion/1354306/is-there-a-tri-state-check-box-in-swing
 * s1w_ https://stackoverflow.com/questions/1263323/tristate-checkboxes-in-java
 * 
 * 
 * @author Steve Young Copyright (c) 2021
 */
public class TriStateJCheckBox extends JPanel {
   
    private final JPanel contentContainer;
    private final JCheckBox checkBox;
    private final JLabel label;
    private TriStateModel model;
    private String text;
    
    /**
     * Enum of TriStateJCheckBox state values.
     */
    public static enum State {
        CHECKED, UNCHECKED, PARTIAL 
    }
  
    /**
     * Creates a check box.
     * Defaults to unchecked state.
     * 
     */
    public TriStateJCheckBox () {
        this(null);
    }
    
    /**
     * Creates a check box with text.
     * Defaults to unchecked state.
     * 
     * @param labelText the text of the check box.
     */
    public TriStateJCheckBox (String labelText) {
        super();
        text = labelText;
        
        contentContainer = new JPanel(new BorderLayout(0, 0));
        label = new JLabel();
        checkBox = new TriCheckBox();
        initProperties();
    }
    
    private void initProperties(){
        
        log.debug("started TriStateJCheckBox");
        
        setLayout(new GridBagLayout());
        
        model = new TriStateModel(State.UNCHECKED);
        checkBox.setModel(model);
        
        // INITIALIZE COMPONENTS
        checkBox.setOpaque(false);
        label.setOpaque(false);
        contentContainer.setOpaque(false);
        setOpaque(false);
        setText(text);
        
        label.setLabelFor(checkBox);

        // LAYOUT COMPONENTS
        contentContainer.add(checkBox, BorderLayout.WEST);
        contentContainer.add(label, BorderLayout.CENTER);
        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1;
        gbc.weighty = 1;
        this.add(contentContainer, gbc);

        final MouseListener labelAndPanelListener = new MouseAdapter() {

            @Override
            public void mousePressed(final MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    model.setPressed(true);
                }
            }

            @Override
            public void mouseReleased(final MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    model.setPressed(false);
                }
            }
            
            @Override
            public void mouseEntered(final MouseEvent e) {
                model.setRollover(true);
            }

            @Override
            public void mouseExited(final MouseEvent e) {
                model.setRollover(false);
            }

        };

        label.addMouseListener(labelAndPanelListener);
        
    }
 
    /**
     * Set the new state to either CHECKED, PARTIAL or UNCHECKED.
     * @param state enum of new state.
     */
    public void setState(@Nonnull State state) {
        model.setState(state);
        checkBox.repaint();
    }
    
    /**
     * Set the new state using values within a boolean array.
     * 
     * boolean[]{false,false} = UNCHECKED
     * boolean[]{true,true} = CHECKED
     * boolean[]{true,false} = PARTIAL
     * 
     * @param booleanArray boolean values to compare
     */
    public void setState(boolean[] booleanArray){
        if (arrayDoesNotContainsTrueOrFalse(booleanArray,true)){
            setState(State.UNCHECKED);
        } else if (arrayDoesNotContainsTrueOrFalse(booleanArray,false)){
            setState(State.CHECKED);
        } else {
            setState(State.PARTIAL);
        }
    }
    
    private boolean arrayDoesNotContainsTrueOrFalse(boolean[] booleanArray, boolean condition){
        for(boolean value: booleanArray){
            if ( value == condition ) { 
                return false;
            }
        }
        return true;
    }
  
    /**
     * Return the current state, which is determined by the selection status of
     * the model.
     * @return enum of current state.
     */
    @Nonnull
    public State getState() {
        return model.getState();
    }

    /**
     * Set the CheckBox to Selected or Unselected.
     * @param selected true for selected, else false.
     */
    public void setSelected(boolean selected) {
        checkBox.setSelected(selected);
    }
    
    /**
     * Is the CheckBox currently fully selected?
     * @return true if CHECKED, false for UNCHECKED and PARTIAL.
     */
    public boolean isSelected(){
        return checkBox.isSelected();
    }
    
    /**
     * Set the CheckBox enabled or disabled.
     * @param enabled true for enabled, false disabled.
     */
    @Override
    public void setEnabled(final boolean enabled) {
        checkBox.setEnabled(enabled);
    }

    /**
     * Set the CheckBox Label Text if different from constructor.
     * @param newText New Text Label.
     */
    public void setText(final String newText) {
        if (newText == null || newText.trim().isEmpty()) {
            text = null;
        } else {
            text = newText;
        }
        label.setText(text);
    }
    
    /**
     * Add an ActionListener to the JCheckBox.
     * @param al the ActionListener to add.
     */
    public void addActionListener(ActionListener al) {
        checkBox.addActionListener(al);
    }
    
    @Override
    public void setToolTipText(String s){
        checkBox.setToolTipText(s);
        label.setToolTipText(s);
        contentContainer.setToolTipText(s);
        super.setToolTipText(s);
    }
    
    /** 
     * Model for checkbox
     */
    private class TriStateModel extends JToggleButton.ToggleButtonModel {      
    
        protected State state;
        
        public TriStateModel(State state) {
            this.state = state;
        }
   
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isSelected() {      
            return state == State.CHECKED;
        } 

        public State getState() {
            return state;
        }

        public void setState(State state) {
            this.state = state;
            fireStateChanged();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setPressed(boolean pressed) {
            log.debug("setPressed {}",pressed);
            if (pressed && isEnabled()) {
                state = ( state==State.UNCHECKED ? State.CHECKED : State.UNCHECKED );
                fireStateChanged();
                fireActionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED,getActionCommand()));
            }
        }
    
        /**
         * {@inheritDoc}
         */
        @Override
        public void setSelected(boolean selected) {
            this.state = ( selected ? State.CHECKED : State.UNCHECKED );
            super.setSelected(selected);
        }
    }

    private static class TriCheckBox extends JCheckBox {
    
        private static final int DOT_SIZE = 2;
        
        protected TriCheckBox() {
            super();
        }
        
        @Override
        protected void paintComponent(final Graphics g) {
            super.paintComponent(g);

            if ((((TriStateModel) model).getState() == TriStateJCheckBox.State.PARTIAL)){
                final int w = getWidth();
                final int h = getHeight();
                final int wOdd = w % 2;
                final int hOdd = h % 2;
                final int centerX = w / 2;
                final int centerY = h / 2;
                final int rw = DOT_SIZE * 2 + wOdd;
                final int rh = DOT_SIZE * 2 + hOdd;

                g.setColor(isEnabled() ? new Color(51, 51, 51) : new Color(122, 138, 153));
                g.fillRect(centerX - DOT_SIZE, centerY - DOT_SIZE, rw, rh);
            }
        }
    }
    
    private final static Logger log = LoggerFactory.getLogger(TriStateJCheckBox.class);

}
