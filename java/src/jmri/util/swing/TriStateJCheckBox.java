package jmri.util.swing;

import java.awt.*;
import java.awt.event.ActionEvent;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.plaf.UIResource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
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
public class TriStateJCheckBox extends JCheckBox implements Icon, UIResource {
   
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
        super();
        Icon ico = UIManager.getIcon("CheckBox.icon"); // NOI18N
        if (ico != null){
            icon=ico;
        } else {
            icon = new javax.swing.plaf.metal.MetalCheckBoxIcon();
        }
        initProperties();
    }
    
    /**
     * Creates a check box with text.
     * Defaults to unchecked state.
     * 
     * @param text the text of the check box.
     */
    public TriStateJCheckBox (String text) {
        this();
        super.setText(text);
    }
    
    private void initProperties(){
        setModel(new TriStateModel(State.UNCHECKED));
        setIcon(this);
        setSelectedIcon(icon);
        setRolloverEnabled( false );
        setOpaque(true);
        log.debug("started TriStateJCheckBox with Icon: {}",icon);
    }
 
    /**
     * Set the new state to either CHECKED, PARTIAL or UNCHECKED.
     * @param state enum of new state.
     */
    public void setState(@Nonnull State state) {
        ((TriStateModel) model).setState(state);
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
            ((TriStateModel) model).setState(State.UNCHECKED);
        } else if (arrayDoesNotContainsTrueOrFalse(booleanArray,false)){
            ((TriStateModel) model).setState(State.CHECKED);
        } else {
            ((TriStateModel) model).setState(State.PARTIAL);
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
        return ((TriStateModel) model).getState();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSelected(boolean selected) {
        ((TriStateModel) model).setSelected(selected);    
    }
    
    private final Icon icon;
    
    /**
     * Paint the Icon dependant on state.
     * {@inheritDoc}
     */
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        
        icon.paintIcon(c, g, x, y);
        
        if ((((TriStateModel) model).getState() != TriStateJCheckBox.State.PARTIAL)
            || c == null || g == null){
            return;
        }
        
        int w = getIconWidth();
        int h = getIconHeight();
        g.setColor(c.isEnabled() ? new Color(51, 51, 51) : new Color(122, 138, 153));
        g.fillRect(x+4, y+4, w-8, h-8);

        if (!c.isEnabled()) return;
        g.setColor(new Color(81, 81, 81));
        g.drawRect(x+4, y+4, w-9, h-9);
        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getIconWidth() {
        return (icon.getIconWidth());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getIconHeight() {
        return (icon.getIconHeight());
    }
    
    
    /** 
     * Model for checkbox
     */
    private static class TriStateModel extends JToggleButton.ToggleButtonModel {      
    
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
            if (pressed) {
                state = ( state==State.UNCHECKED ? State.CHECKED : State.UNCHECKED );
                fireStateChanged();
                fireActionPerformed(
                    new ActionEvent(this, ActionEvent.ACTION_PERFORMED,getActionCommand()));
            }
        }
    
        /**
         * {@inheritDoc}
         */
        @Override
        public void setSelected(boolean selected) {       
            this.state = ( selected ? State.CHECKED : State.UNCHECKED );    
        }
    }
    
    private final static Logger log = LoggerFactory.getLogger(TriStateJCheckBox.class);

}
