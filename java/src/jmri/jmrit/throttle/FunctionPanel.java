package jmri.jmrit.throttle;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.WindowConstants;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.LocoAddress;
import jmri.Throttle;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A JInternalFrame that contains buttons for each decoder function.
 */
public class FunctionPanel extends JInternalFrame implements FunctionListener, java.beans.PropertyChangeListener, AddressListener {

    public static final int NUM_FUNCTION_BUTTONS = 29;
    public static final int NUM_FUNC_BUTTONS_INIT = 16; //only show 16 function buttons at start
    private DccThrottle mThrottle;

    private JPanel mainPanel;
    private FunctionButton functionButton[];
    private final JToggleButton alt1Button;
    private final JToggleButton alt2Button;

    private AddressPanel addressPanel = null; // to access roster infos

    /**
     * Constructor
     */
    public FunctionPanel() {
        alt1Button = new JToggleButton();
        alt2Button = new JToggleButton();
        initGUI();
    }

    public void destroy() {
        if (addressPanel != null) {
            addressPanel.removeAddressListener(this);
        }
        if (mThrottle != null) {
            mThrottle.removePropertyChangeListener(this);
            mThrottle = null;
        }
    }

    public FunctionButton[] getFunctionButtons() {
        return Arrays.copyOf(functionButton, functionButton.length);
    }

    /**
     * Get notification that a function has changed state.
     *
     * @param functionNumber The function that has changed.
     * @param isSet          True if the function is now active (or set).
     */
    @Override
    public void notifyFunctionStateChanged(int functionNumber, boolean isSet) {
        if (mThrottle != null) {
            mThrottle.setFunction(functionNumber, isSet);
        }
    }

    /**
     * Get notification that a function's lockable status has changed.
     *
     * @param functionNumber The function that has changed (0-28).
     * @param isLockable     True if the function is now Lockable (continuously
     *                       active).
     */
    @Override
    public void notifyFunctionLockableChanged(int functionNumber, boolean isLockable) {
        log.debug("notifyFnLockableChanged: fNumber={} isLockable={} " ,functionNumber, isLockable);
        if (mThrottle != null) {
            // throttle can be null when loading throttle layout
            mThrottle.setFunctionMomentary(functionNumber, !isLockable);
        }
    }

    /**
     * Enable or disable all the buttons.
     * @param isEnabled true to enable, false to disable.
     */
    @Override
    public void setEnabled(boolean isEnabled) {
        //super.setEnabled(isEnabled);
        for (int i = 0; i < NUM_FUNCTION_BUTTONS; i++) {
            functionButton[i].setEnabled(isEnabled);
        }
        alt1Button.setEnabled(isEnabled);
        alt2Button.setEnabled(isEnabled);
    }

    public void setEnabled() {
        setEnabled(mThrottle != null);
    }

    public void setAddressPanel(AddressPanel addressPanel) {
        this.addressPanel = addressPanel;
    }

    public void saveFunctionButtonsToRoster(RosterEntry rosterEntry) {
        log.debug("saveFunctionButtonsToRoster");
        if (rosterEntry == null) {
            return;
        }
        for (int i = 0; i < NUM_FUNCTION_BUTTONS; i++) {
            int functionNumber = functionButton[i].getIdentity();
            String text = functionButton[i].getButtonLabel();
            boolean lockable = functionButton[i].getIsLockable();
            String imagePath = functionButton[i].getIconPath();
            String imageSelectedPath = functionButton[i].getSelectedIconPath();
            if (functionButton[i].isDirty() && !text.equals(rosterEntry.getFunctionLabel(functionNumber))) {
                functionButton[i].setDirty(false);
                if (text.isEmpty()) {
                    text = null;  // reset button text to default
                }
                rosterEntry.setFunctionLabel(functionNumber, text);
            }
            if (rosterEntry.getFunctionLabel(functionNumber) != null ) {
                if( lockable != rosterEntry.getFunctionLockable(functionNumber)) {
                   rosterEntry.setFunctionLockable(functionNumber, lockable);
                }
                if ( imagePath.compareTo(rosterEntry.getFunctionImage(functionNumber)) != 0) {
                   rosterEntry.setFunctionImage(functionNumber, imagePath);
                }
                if ( imageSelectedPath.compareTo(rosterEntry.getFunctionSelectedImage(functionNumber)) != 0) {
                   rosterEntry.setFunctionSelectedImage(functionNumber, imageSelectedPath);
                }
            }
        }
        Roster.getDefault().writeRoster();
    }

    /**
     * Place and initialize all the buttons.
     */
    private void initGUI() {
        mainPanel = new JPanel();
        mainPanel.removeAll();
        setContentPane(mainPanel);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        mainPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        functionButton = new FunctionButton[NUM_FUNCTION_BUTTONS];
        for (int i = 0; i < NUM_FUNCTION_BUTTONS; i++) {
            functionButton[i] = new FunctionButton();
            // place function button 0 at the button of the panel
            if (i > 0) {
                mainPanel.add(functionButton[i]);
                if (i >= NUM_FUNC_BUTTONS_INIT) {
                    functionButton[i].setVisible(false);
                }
            }
        }
        alt1Button.setText("*");
        alt1Button.setPreferredSize(new Dimension(FunctionButton.BUT_WDTH, FunctionButton.BUT_HGHT));
        alt1Button.setToolTipText(java.util.ResourceBundle.getBundle("jmri/jmrit/throttle/ThrottleBundle").getString("Push_for_alternate_set_of_function_keys"));
        alt1Button.addActionListener((java.awt.event.ActionEvent e) -> {
            buttonActionCmdPerformed();
        });
        mainPanel.add(alt1Button);

        mainPanel.add(functionButton[0]);

        alt2Button.setText("#");
        alt2Button.setPreferredSize(new Dimension(FunctionButton.BUT_WDTH, FunctionButton.BUT_HGHT));
        alt2Button.setToolTipText(java.util.ResourceBundle.getBundle("jmri/jmrit/throttle/ThrottleBundle").getString("currently_not_used"));
        mainPanel.add(alt2Button);

        resetFnButtons();
        KeyListenerInstaller.installKeyListenerOnAllComponents(new FunctionButtonKeyListener(), this);
    }

    /**
     * Activated when alt1Button is pressed or released.
     * Swap f3 through f15 with f16 through f28.
     */
    public void buttonActionCmdPerformed() {
        for (int i = 3; i < NUM_FUNCTION_BUTTONS; i++) {
            if (alt1Button.isSelected()) {
                if (i < NUM_FUNC_BUTTONS_INIT) {
                    functionButton[i].setVisible(false);
                } else {
                    functionButton[i].setVisible(functionButton[i].getDisplay());
                }

            } else {
                if (i < NUM_FUNC_BUTTONS_INIT) {
                    functionButton[i].setVisible(functionButton[i].getDisplay());
                } else {
                    functionButton[i].setVisible(false);
                }
            }
        }
    }

    /**
     * Make sure that all function buttons are being displayed if buttons label
     * loaded from a roster entry, update buttons accordingly
     */
    public void resetFnButtons() {
        final ThrottlesPreferences preferences = InstanceManager.getDefault(ThrottleFrameManager.class).getThrottlesPreferences();
        // Buttons names, ids,
        for (int i = 0; i < NUM_FUNCTION_BUTTONS; i++) {
            functionButton[i].setThrottle(mThrottle);
            functionButton[i].setIdentity(i);
            functionButton[i].addFunctionListener(this);
            functionButton[i].setButtonLabel( i<3 ?
                Bundle.getMessage(Throttle.getFunctionString(i))
                : Throttle.getFunctionString(i) );
            functionButton[i].setDisplay(true);
            if ((i < 3) && preferences.isUsingIcons()) {
                switch (i) {
                    case 0:
                        functionButton[i].setIconPath("resources/icons/throttles/Light.png");
                        functionButton[i].setSelectedIconPath("resources/icons/throttles/LightOn.png");
                        break;
                    case 1:
                        functionButton[i].setIconPath("resources/icons/throttles/Bell.png");
                        functionButton[i].setSelectedIconPath("resources/icons/throttles/BellOn.png");
                        break;
                    case 2:
                        functionButton[i].setIconPath("resources/icons/throttles/Horn.png");
                        functionButton[i].setSelectedIconPath("resources/icons/throttles/HornOn.png");
                        break;
                    default:
                        break;
                }
            } else {
                functionButton[i].setIconPath(null);
                functionButton[i].setSelectedIconPath(null);
            }
            functionButton[i].updateLnF();

            // always display f0, F1 and F2
            if (i < 3) {
                functionButton[i].setVisible(true);
            }
        }
        functionButton[0].setKeyCode(KeyEvent.VK_NUMPAD0);
        functionButton[1].setKeyCode(KeyEvent.VK_NUMPAD1);
        functionButton[2].setKeyCode(KeyEvent.VK_NUMPAD2);
        functionButton[3].setKeyCode(KeyEvent.VK_NUMPAD3);
        functionButton[4].setKeyCode(KeyEvent.VK_NUMPAD4);
        functionButton[5].setKeyCode(KeyEvent.VK_NUMPAD5);
        functionButton[6].setKeyCode(KeyEvent.VK_NUMPAD6);
        functionButton[7].setKeyCode(KeyEvent.VK_NUMPAD7);
        functionButton[8].setKeyCode(KeyEvent.VK_NUMPAD8);
        functionButton[9].setKeyCode(KeyEvent.VK_NUMPAD9);
        functionButton[10].setKeyCode(110); // numpad decimal (f10 button causes problems)
        functionButton[11].setKeyCode(KeyEvent.VK_F11);
        functionButton[12].setKeyCode(KeyEvent.VK_F12);
        functionButton[13].setKeyCode(KeyEvent.VK_F13);
        functionButton[14].setKeyCode(KeyEvent.VK_F14);
        functionButton[15].setKeyCode(KeyEvent.VK_F15);
        functionButton[16].setKeyCode(KeyEvent.VK_F16);
        functionButton[17].setKeyCode(KeyEvent.VK_F17);
        functionButton[18].setKeyCode(KeyEvent.VK_F18);
        functionButton[19].setKeyCode(KeyEvent.VK_F19);
        functionButton[20].setKeyCode(KeyEvent.VK_F20);
        functionButton[21].setKeyCode(KeyEvent.VK_F21);
        functionButton[22].setKeyCode(KeyEvent.VK_F22);
        functionButton[23].setKeyCode(KeyEvent.VK_F23);
        functionButton[24].setKeyCode(KeyEvent.VK_F24);
        functionButton[25].setKeyCode(0xF00C);   // keycodes 25 - 28 don't exist in KeyEvent
        functionButton[26].setKeyCode(0xF00D);
        functionButton[27].setKeyCode(0xF00E);
        functionButton[28].setKeyCode(0xF00F);

        alt1Button.setVisible(true);
        alt2Button.setVisible(true);
        buttonActionCmdPerformed();
        setFnButtons();
    }

    // Update buttons value from slot + load buttons definition from roster if any
    private void setFnButtons() {
        final ThrottlesPreferences preferences = InstanceManager.getDefault(ThrottleFrameManager.class).getThrottlesPreferences();
        if (mThrottle != null) {
            if (addressPanel == null) {
                return;
            }
            RosterEntry rosterEntry = addressPanel.getRosterEntry();
            if (rosterEntry != null) {
                log.debug("RosterEntry found: {}", rosterEntry.getId());
            }
            int maxi = 0; // the number of function buttons defined for this entry
            for (int i = 0; i < FunctionPanel.NUM_FUNCTION_BUTTONS; i++) {
                functionButton[i].setIdentity(i); // full reset of function
                functionButton[i].setThrottle(mThrottle);
                functionButton[i].setState(mThrottle.getFunction(i)); // reset button state
                if (rosterEntry != null) { // from here, update button text with roster data
                    String text = rosterEntry.getFunctionLabel(i);
                    if (text != null) {
                        functionButton[i].setDisplay(true);
                        functionButton[i].setButtonLabel(text);
                        if (preferences.isUsingIcons()) {
                            functionButton[i].setIconPath(rosterEntry.getFunctionImage(i));
                            functionButton[i].setSelectedIconPath(rosterEntry.getFunctionSelectedImage(i));
                        } else {
                            functionButton[i].setIconPath(null);
                            functionButton[i].setSelectedIconPath(null);
                        }
                        functionButton[i].setIsLockable(rosterEntry.getFunctionLockable(i));
                        functionButton[i].updateLnF();
                        if (maxi < NUM_FUNC_BUTTONS_INIT) {
                            functionButton[i].setVisible(true);
                        }
                        maxi++; // bump number of buttons shown
                    } else if (preferences.isUsingExThrottle()
                            && preferences.isHidingUndefinedFuncButt()) {
                        functionButton[i].setDisplay(false);
                        functionButton[i].setVisible(false);
                    }
                }
            }
            // hide alt buttons if not required
            if ((rosterEntry != null) && (maxi < NUM_FUNC_BUTTONS_INIT
                    && preferences.isUsingExThrottle()
                    && preferences.isHidingUndefinedFuncButt())) {
                alt1Button.setVisible(false);
                alt2Button.setVisible(false);
            }
        }
    }

    /**
     * A KeyAdapter that listens for the keys that work the function buttons.
     *
     * @author glen
     */
    private class FunctionButtonKeyListener extends KeyAdapter {

        private boolean keyReleased = true;

        /**
         * {@inheritDoc}
         */
        @Override
        public void keyPressed(KeyEvent e) {
            if (keyReleased) {
                for (int i = 0; i < NUM_FUNCTION_BUTTONS; i++) {
                    if (functionButton[i].checkKeyCode(e.getKeyCode())) {
                        functionButton[i].setState(!functionButton[i].isSelected());
                    }
                }
            }
            keyReleased = false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void keyReleased(KeyEvent e) {
            for (int i = 0; i < NUM_FUNCTION_BUTTONS; i++) {
                if ((functionButton[i].checkKeyCode(e.getKeyCode())) && (!functionButton[i].getIsLockable())) {
                    functionButton[i].setState(!functionButton[i].isSelected());
                }
            }
            keyReleased = true;
        }
    }

    /**
     * Update the state of this panel if any of the functions change.
     * {@inheritDoc}
     */
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (mThrottle!=null){
            for (int i = 0; i < mThrottle.getFunctions().length; i++) {
                if (e.getPropertyName().equals(Throttle.getFunctionString(i))) {
                    setButtonByFuncNumber(i,false,(Boolean) e.getNewValue());
                } else if (e.getPropertyName().equals(Throttle.getFunctionMomentaryString(i))) {
                    setButtonByFuncNumber(i,true,!(Boolean) e.getNewValue());
                }
            }
        }
    }
    
    private void setButtonByFuncNumber(int function, boolean lockable, boolean newVal){
        for (FunctionButton button : functionButton) {
            if (button.getIdentity() == function) {
                if (lockable) {
                    button.setIsLockable(newVal);
                } else {
                    button.setState(newVal);
                }
            }
        }
    }

    /**
     * Collect the prefs of this object into XML Element.
     * <ul>
     * <li> Window prefs
     * <li> Each button has id, text, lock state.
     * </ul>
     *
     * @return the XML of this object.
     */
    public Element getXml() {
        Element me = new Element("FunctionPanel"); // NOI18N
        java.util.ArrayList<Element> children = new java.util.ArrayList<>(1 + FunctionPanel.NUM_FUNCTION_BUTTONS);
        children.add(WindowPreferences.getPreferences(this));
        for (int i = 0; i < FunctionPanel.NUM_FUNCTION_BUTTONS; i++) {
            children.add(functionButton[i].getXml());
        }
        me.setContent(children);
        return me;
    }

    /**
     * Set the preferences based on the XML Element.
     * <ul>
     * <li> Window prefs
     * <li> Each button has id, text, lock state.
     * </ul>
     *
     * @param e The Element for this object.
     */
    public void setXml(Element e) {
        Element window = e.getChild("window");
        WindowPreferences.setPreferences(this, window);

        java.util.List<Element> buttonElements
                = e.getChildren("FunctionButton");

        if (buttonElements != null && buttonElements.size() > 0) {
            int i = 0;
            for (Element buttonElement : buttonElements) {
                functionButton[i++].setXml(buttonElement);
            }
        }
    }

    /**
     * Get notification that a throttle has been found as we requested.
     *
     * @param t An instantiation of the DccThrottle with the address requested.
     */
    @Override
    public void notifyAddressThrottleFound(DccThrottle t) {
        log.debug("Throttle found");
        mThrottle = t;
        setEnabled(true);
        mThrottle.addPropertyChangeListener(this);
        setFnButtons();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyAddressReleased(LocoAddress la) {
        log.debug("Throttle released");
        setEnabled(false);
        if (mThrottle != null) {
            mThrottle.removePropertyChangeListener(this);
        }
        mThrottle = null;
    }

    /**
     * Ignored.
     * {@inheritDoc}
     */
    @Override
    public void notifyAddressChosen(LocoAddress l) {
    }

    /**
     * Ignored.
     * {@inheritDoc}
     */
    @Override
    public void notifyConsistAddressChosen(int newAddress, boolean isLong) {
    }

    /**
     * Ignored.
     * {@inheritDoc}
     */
    @Override
    public void notifyConsistAddressReleased(int address, boolean isLong) {
    }

    /**
     * Ignored.
     * {@inheritDoc}
     */
    @Override
    public void notifyConsistAddressThrottleFound(DccThrottle throttle) {
    }

    private final static Logger log = LoggerFactory.getLogger(FunctionPanel.class);
}
