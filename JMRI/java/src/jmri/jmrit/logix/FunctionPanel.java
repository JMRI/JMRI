package jmri.jmrit.logix;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import jmri.DccThrottle;
import jmri.Throttle;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.throttle.FunctionButton;
import jmri.jmrit.throttle.FunctionListener;
import jmri.jmrit.throttle.KeyListenerInstaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A JInternalFrame that contains buttons for each decoder function.
 */
public class FunctionPanel extends JInternalFrame implements FunctionListener, java.beans.PropertyChangeListener {

    public static final int NUM_FUNCTION_BUTTONS = 29;
    public static final int NUM_FUNC_BUTTONS_INIT = 16; //only show 16 function buttons at start
    private DccThrottle _throttle;
    private final RosterEntry _rosterEntry;
    private final LearnThrottleFrame _throttleFrame;

    private FunctionButton functionButton[];
    javax.swing.JToggleButton alt1Button = new javax.swing.JToggleButton();
    javax.swing.JToggleButton alt2Button = new javax.swing.JToggleButton();

    /**
     * Constructor.
     *
     * @param rosterEntry the associated roster entry
     * @param learnFrame  the window in which this window is embedded
     */
    public FunctionPanel(RosterEntry rosterEntry, LearnThrottleFrame learnFrame) {
        super("Functions");
        _rosterEntry = rosterEntry;
        _throttleFrame = learnFrame;
        initGUI();
    }

    /**
     * Get notification that a throttle has been found as we requested. Use
     * reflection to find the proper getF? method for each button.
     *
     * @param t An instantiation of the DccThrottle with the address requested.
     */
    public void notifyThrottleFound(DccThrottle t) {
        if (log.isDebugEnabled()) {
            log.debug("Throttle found");
        }
        _throttle = t;
        for (int i = 0; i < FunctionPanel.NUM_FUNCTION_BUTTONS; i++) {
            try {
                int functionNumber = functionButton[i].getIdentity();
                java.lang.reflect.Method getter
                        = _throttle.getClass().getMethod("getF" + functionNumber, (Class[]) null);
                Boolean state = (Boolean) getter.invoke(_throttle, (Object[]) null);
                functionButton[i].setState(state);
                if (_rosterEntry != null) {
                    String text = _rosterEntry.getFunctionLabel(functionNumber);
                    if (text != null) {
                        functionButton[i].setText(text);
                        // adjust button width for text
                        int butWidth = functionButton[i].getFontMetrics(functionButton[i].getFont()).stringWidth(text);
                        butWidth = butWidth + 20; // pad out the width a bit
                        if (butWidth < FunctionButton.getButtonWidth()) {
                            butWidth = FunctionButton.getButtonWidth();
                        }
                        functionButton[i].setPreferredSize(new Dimension(butWidth, FunctionButton.getButtonHeight()));
                        functionButton[i].setIsLockable(_rosterEntry.getFunctionLockable(functionNumber));
                    }
                }
            } catch (java.lang.NoSuchMethodException | java.lang.IllegalAccessException | java.lang.reflect.InvocationTargetException ex) {
                log.warn("Exception in notifyThrottleFound: {}", ex);
            }
        }
        this.setEnabled(true);
        _throttle.addPropertyChangeListener(this);
    }

    @Override
    public void dispose() {
        if (_throttle != null) {
            _throttle.removePropertyChangeListener(this);
            _throttle = null;
        }
        super.dispose();
    }

//    public FunctionButton[] getFunctionButtons() { return functionButton; }
    /**
     * Get notification that a function has changed state
     *
     * @param functionNumber The function that has changed (0-9).
     * @param isSet          True if the function is now active (or set).
     */
    @Override
    public void notifyFunctionStateChanged(int functionNumber, boolean isSet) {
        if (log.isDebugEnabled()) {
            log.debug("notifyFunctionStateChanged: functionNumber= "
                    + functionNumber + " isSet= " + isSet);
        }
        switch (functionNumber) {
            case 0:
                _throttle.setF0(isSet);
                break;
            case 1:
                _throttle.setF1(isSet);
                break;
            case 2:
                _throttle.setF2(isSet);
                break;
            case 3:
                _throttle.setF3(isSet);
                break;
            case 4:
                _throttle.setF4(isSet);
                break;
            case 5:
                _throttle.setF5(isSet);
                break;
            case 6:
                _throttle.setF6(isSet);
                break;
            case 7:
                _throttle.setF7(isSet);
                break;
            case 8:
                _throttle.setF8(isSet);
                break;
            case 9:
                _throttle.setF9(isSet);
                break;
            case 10:
                _throttle.setF10(isSet);
                break;
            case 11:
                _throttle.setF11(isSet);
                break;
            case 12:
                _throttle.setF12(isSet);
                break;
            case 13:
                _throttle.setF13(isSet);
                break;
            case 14:
                _throttle.setF14(isSet);
                break;
            case 15:
                _throttle.setF15(isSet);
                break;
            case 16:
                _throttle.setF16(isSet);
                break;
            case 17:
                _throttle.setF17(isSet);
                break;
            case 18:
                _throttle.setF18(isSet);
                break;
            case 19:
                _throttle.setF19(isSet);
                break;
            case 20:
                _throttle.setF20(isSet);
                break;
            case 21:
                _throttle.setF21(isSet);
                break;
            case 22:
                _throttle.setF22(isSet);
                break;
            case 23:
                _throttle.setF23(isSet);
                break;
            case 24:
                _throttle.setF24(isSet);
                break;
            case 25:
                _throttle.setF25(isSet);
                break;
            case 26:
                _throttle.setF26(isSet);
                break;
            case 27:
                _throttle.setF27(isSet);
                break;
            case 28:
                _throttle.setF28(isSet);
                break;
            default:
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
        if (log.isDebugEnabled()) {
            log.debug("notifyFunctionLockableChanged: functionNumber= "
                    + functionNumber + " isLockable= " + isLockable);
        }
        if (_throttle == null) {
            // throttle can be null when loading throttle layout
            return;
        }

        switch (functionNumber) {
            case 0:
                _throttle.setF0Momentary(!isLockable);
                break;
            case 1:
                _throttle.setF1Momentary(!isLockable);
                break;
            case 2:
                _throttle.setF2Momentary(!isLockable);
                break;
            case 3:
                _throttle.setF3Momentary(!isLockable);
                break;
            case 4:
                _throttle.setF4Momentary(!isLockable);
                break;
            case 5:
                _throttle.setF5Momentary(!isLockable);
                break;
            case 6:
                _throttle.setF6Momentary(!isLockable);
                break;
            case 7:
                _throttle.setF7Momentary(!isLockable);
                break;
            case 8:
                _throttle.setF8Momentary(!isLockable);
                break;
            case 9:
                _throttle.setF9Momentary(!isLockable);
                break;
            case 10:
                _throttle.setF10Momentary(!isLockable);
                break;
            case 11:
                _throttle.setF11Momentary(!isLockable);
                break;
            case 12:
                _throttle.setF12Momentary(!isLockable);
                break;
            case 13:
                _throttle.setF13Momentary(!isLockable);
                break;
            case 14:
                _throttle.setF14Momentary(!isLockable);
                break;
            case 15:
                _throttle.setF15Momentary(!isLockable);
                break;
            case 16:
                _throttle.setF16Momentary(!isLockable);
                break;
            case 17:
                _throttle.setF17Momentary(!isLockable);
                break;
            case 18:
                _throttle.setF18Momentary(!isLockable);
                break;
            case 19:
                _throttle.setF19Momentary(!isLockable);
                break;
            case 20:
                _throttle.setF20Momentary(!isLockable);
                break;
            case 21:
                _throttle.setF21Momentary(!isLockable);
                break;
            case 22:
                _throttle.setF22Momentary(!isLockable);
                break;
            case 23:
                _throttle.setF23Momentary(!isLockable);
                break;
            case 24:
                _throttle.setF24Momentary(!isLockable);
                break;
            case 25:
                _throttle.setF25Momentary(!isLockable);
                break;
            case 26:
                _throttle.setF26Momentary(!isLockable);
                break;
            case 27:
                _throttle.setF27Momentary(!isLockable);
                break;
            case 28:
                _throttle.setF28Momentary(!isLockable);
                break;
            default:
        }
    }

    /**
     * Enable or disable all the buttons.
     *
     * @param isEnabled true to enable; false to disable
     */
    @Override
    public void setEnabled(boolean isEnabled) {
        super.setEnabled(isEnabled);
        for (int i = 0; i < NUM_FUNCTION_BUTTONS; i++) {
            functionButton[i].setEnabled(isEnabled);
        }
        alt1Button.setEnabled(isEnabled);
        alt2Button.setEnabled(isEnabled);
    }

    /*   protected void resetFuncButtons(){
     functionButton = null;
     initGUI();
     setEnabled(true);
     } */
    JPanel mainPanel = new JPanel();

    /**
     * Place and initialize all the buttons.
     */
    public void initGUI() {
        mainPanel.removeAll();
        this.setContentPane(mainPanel);
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        mainPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        functionButton = new FunctionButton[NUM_FUNCTION_BUTTONS];
        for (int i = 0; i < NUM_FUNCTION_BUTTONS; i++) {
            functionButton[i] = new FunctionButton();
            functionButton[i].setIdentity(i);
            functionButton[i].setFunctionListener(this);
            if (i < 3) {
                functionButton[i].setText(Bundle.getMessage("F" + String.valueOf(i)));
            } else {
                functionButton[i].setText("F" + String.valueOf(i));
            }
            if (i > 0) {
                mainPanel.add(functionButton[i]);
                if (i >= NUM_FUNC_BUTTONS_INIT) {
                    functionButton[i].setVisible(false);
                }
            }
        }
        alt1Button.setText("Alt");
        alt1Button.setPreferredSize(new Dimension(FunctionButton.getButtonWidth(), FunctionButton.getButtonHeight()));
        alt1Button.setToolTipText(java.util.ResourceBundle.getBundle("jmri/jmrit/throttle/ThrottleBundle").getString("Push_for_alternate_set_of_function_keys"));
        alt1Button.addActionListener((java.awt.event.ActionEvent e) -> {
            buttonActionCmdPerformed();
        });
        mainPanel.add(alt1Button);

        mainPanel.add(functionButton[0]);

        alt2Button.setText("#");
        alt2Button.setPreferredSize(new Dimension(FunctionButton.getButtonWidth(), FunctionButton.getButtonHeight()));
        alt2Button.setToolTipText(java.util.ResourceBundle.getBundle("jmri/jmrit/throttle/ThrottleBundle").getString("currently_not_used"));
        mainPanel.add(alt2Button);

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
        KeyListenerInstaller.installKeyListenerOnAllComponents(new FunctionButtonKeyListener(), this);
        // Make F2 (Horn) momentary
        functionButton[2].setIsLockable(false);
    }

    // activated when alt1Button is pressed or released
    public void buttonActionCmdPerformed() {
        // swap f3 through f15 with f16 through f28
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
     * Make sure that all function buttons are being displayed
     */
    public void showAllFnButtons() {
        // should show all, or just the initial ones?
        for (int i = 0; i < NUM_FUNCTION_BUTTONS; i++) {
            functionButton[i].setDisplay(true);
            if (i < 3) {
                functionButton[i].setVisible(true);
            }
        }
        alt1Button.setVisible(true);
        alt2Button.setVisible(true);
        buttonActionCmdPerformed();
    }

    /**
     * A KeyAdapter that listens for the keys that work the function buttons
     *
     * @author glen
     */
    class FunctionButtonKeyListener extends KeyAdapter {

        private boolean keyReleased = true;

        /**
         * Description of the Method
         *
         * @param e Description of the Parameter
         */
        @Override
        public void keyPressed(KeyEvent e) {
            if (log.isDebugEnabled()) {
                log.debug("keyPressed: KeyCode= " + e.getKeyCode());
            }
            if (keyReleased) {
                if (log.isDebugEnabled()) {
                    log.debug("Pressed");
                }
                for (int i = 0; i < NUM_FUNCTION_BUTTONS; i++) {
                    if (functionButton[i].checkKeyCode(e.getKeyCode())) {
                        functionButton[i].changeState(!functionButton[i].isSelected());
                    }
                }
            }
            keyReleased = false;
        }

        @Override
        public void keyTyped(KeyEvent e) {
            //if (log.isDebugEnabled())log.debug("Typed");
        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (log.isDebugEnabled()) {
                log.debug("keyReleased: KeyCode= " + e.getKeyCode());
            }
            for (int i = 0; i < NUM_FUNCTION_BUTTONS; i++) {
                if (functionButton[i].checkKeyCode(e.getKeyCode())) {
                    if (!functionButton[i].getIsLockable()) {
                        functionButton[i].changeState(!functionButton[i].isSelected());
                    }
                }
            }
            keyReleased = true;
        }
    }

    // update the state of this panel if any of the properties change
    // did not add f13 - f28 dboudreau, maybe I should have?
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        String functionName = e.getPropertyName();
        if (!functionName.startsWith("F")) {
            return;
        }
        boolean isSet = ((Boolean) e.getNewValue());
        boolean lockable = false;
        switch (functionName) {
            case Throttle.F0:
                functionButton[0].setState(isSet);
                break;
            case Throttle.F1:
                functionButton[1].setState(isSet);
                break;
            case Throttle.F2:
                functionButton[2].setState(isSet);
                break;
            case Throttle.F3:
                functionButton[3].setState(isSet);
                break;
            case Throttle.F4:
                functionButton[4].setState(isSet);
                break;
            case Throttle.F5:
                functionButton[5].setState(isSet);
                break;
            case Throttle.F6:
                functionButton[6].setState(isSet);
                break;
            case Throttle.F7:
                functionButton[7].setState(isSet);
                break;
            case Throttle.F8:
                functionButton[8].setState(isSet);
                break;
            case Throttle.F9:
                functionButton[9].setState(isSet);
                break;
            case Throttle.F10:
                functionButton[10].setState(isSet);
                break;
            case Throttle.F11:
                functionButton[11].setState(isSet);
                break;
            case Throttle.F12:
                functionButton[12].setState(isSet);
                break;
            case Throttle.F0Momentary:
                lockable = true;
                functionName = "LockF0";
                functionButton[0].setIsLockable(isSet);
                break;
            case Throttle.F1Momentary:
                functionName = "LockF1";
                lockable = true;
                functionButton[1].setIsLockable(isSet);
                break;
            case Throttle.F2Momentary:
                functionName = "LockF2";
                lockable = true;
                functionButton[2].setIsLockable(isSet);
                break;
            case Throttle.F3Momentary:
                functionName = "LockF3";
                lockable = true;
                functionButton[3].setIsLockable(isSet);
                break;
            case Throttle.F4Momentary:
                functionName = "LockF4";
                lockable = true;
                functionButton[4].setIsLockable(isSet);
                break;
            case Throttle.F5Momentary:
                functionName = "LockF5";
                lockable = true;
                functionButton[5].setIsLockable(isSet);
                break;
            case Throttle.F6Momentary:
                functionName = "LockF6";
                lockable = true;
                functionButton[6].setIsLockable(isSet);
                break;
            case Throttle.F7Momentary:
                functionName = "LockF7";
                lockable = true;
                functionButton[7].setIsLockable(isSet);
                break;
            case Throttle.F8Momentary:
                functionName = "LockF8";
                lockable = true;
                functionButton[8].setIsLockable(isSet);
                break;
            case Throttle.F9Momentary:
                functionName = "LockF9";
                lockable = true;
                functionButton[9].setIsLockable(isSet);
                break;
            case Throttle.F10Momentary:
                functionName = "LockF10";
                lockable = true;
                functionButton[10].setIsLockable(isSet);
                break;
            case Throttle.F11Momentary:
                functionName = "LockF11";
                lockable = true;
                functionButton[11].setIsLockable(isSet);
                break;
            case Throttle.F12Momentary:
                functionName = "LockF12";
                lockable = true;
                functionButton[12].setIsLockable(isSet);
                break;
            default:
                return;
        }
        if (lockable) {
            _throttleFrame.setFunctionLock(functionName, isSet);
        } else {
            _throttleFrame.setFunctionState(functionName, isSet);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(FunctionPanel.class);
}
