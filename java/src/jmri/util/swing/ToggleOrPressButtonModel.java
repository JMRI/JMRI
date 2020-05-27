package jmri.util.swing;

import java.awt.event.ActionEvent;
import javax.swing.DefaultButtonModel;
import javax.swing.JToggleButton;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * ToggleOrPressButtonModel handles the storage and maintenance of the 
 * state of the button.  
 * <p>
 * Changes the state of the function 
 * depending on the locking state of the button.
 * <p>
 * Modified from http://developer.classpath.org/doc/javax/swing/JToggleButton-source.html
 * <p>
 * Updates the button state depending if is lockable.
 * @since 4.19.6
 * @author Steve Young
 * 
 */
public class ToggleOrPressButtonModel extends DefaultButtonModel {

    private final JToggleButton _button;
    private boolean _isLockable;

    /**
     * Create a new ToggleOrPressButtonModel.
     * @param button the button being controlled.
     * @param startLockable true to start as a toggle button, 
     *                      false to start as a click on / release off button.
     */
    public ToggleOrPressButtonModel(JToggleButton button, boolean startLockable){
        super();
        _button = button;
        _isLockable = startLockable;
    }
    
    /**
     * Set button lockable state.
     * <p>
     * Lockable on - Normal Toggle button.
     * Lockable off - push on, release off.
     * <p>
     * If button is set unlocked when pressed, is de-pressed.
     * @param lockable true for lockable, else false.
     */
    public void setLockable(boolean lockable) {
        _isLockable = lockable;
        if (!_isLockable && isSelected()){
            stateMask = ( stateMask | PRESSED ); // mark button pressed without creating event
            setPressed(false); // depress button creating event.
        }
    }
    
    /**
     * Get if Button is Lockable.
     * @return true if normal toggle button, false if push on release off.
     */
    public boolean getLockable() {
        return _isLockable;
    }
    
    /**
     * An external change has happened so we update.
     * @param p new Selected state.
     */
    public void updateSelected(boolean p){
        setSelected(p);
    }
    
    /**
     * Sets the pressed state of the button.
     * <p>
     * The selected state
     * of the button also changes following the button being pressed.
     *
     * @param p true if the button is pressed down.
     */
    @Override
    public void setPressed(boolean p) {
        // cannot change PRESSED state unless button is enabled
        if (! isEnabled())
          return;

        // if this call does not represent a CHANGE in state, then return
        if ((p && isPressed()) || (!p && !isPressed()))
          return;
        
        stateMask = ( p ? ( stateMask | PRESSED) : (stateMask & (~PRESSED)));
        
        // The JDK first fires events in the following order:
        // 1. ChangeEvent for selected
        // 2. ChangeEvent for pressed
        // 3. ActionEvent
        
        if (_isLockable) { // if we were armed, we flip the selected state.
            if (!p ) { // only change state on button release
                setSelected (! isSelected()); // flip the selected state.
                _button.setSelected(isSelected());
            }
        }
        else {
            setSelected(p);
            _button.setSelected(isSelected());
        }
        
        // notify interested ChangeListeners
        fireStateChanged();        
        fireActionPerformed(new ActionEvent(this,
            ActionEvent.ACTION_PERFORMED, actionCommand));

    }

    // private final static Logger log = LoggerFactory.getLogger(ToggleOrPressButtonModel.class);
    
}
