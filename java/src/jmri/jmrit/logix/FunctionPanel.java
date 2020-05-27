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
    private final JPanel mainPanel;

    private FunctionButton functionButton[];
    private final javax.swing.JToggleButton alt1Button;
    private final javax.swing.JToggleButton alt2Button;

    /**
     * Constructor.
     *
     * @param rosterEntry the associated roster entry
     * @param learnFrame  the window in which this window is embedded
     */
    public FunctionPanel(RosterEntry rosterEntry, LearnThrottleFrame learnFrame) {
        super("Functions");
        mainPanel = new JPanel();
        _rosterEntry = rosterEntry;
        _throttleFrame = learnFrame;
        
        alt1Button = new javax.swing.JToggleButton();
        alt2Button = new javax.swing.JToggleButton();
        
        initGUI();
    }

    /**
     * Get notification that a throttle has been found as we requested.
     *
     * @param t An instantiation of the DccThrottle with the address requested.
     */
    public void notifyThrottleFound(DccThrottle t) {
        log.debug("Throttle found");
        _throttle = t;
        for (int i = 0; i < FunctionPanel.NUM_FUNCTION_BUTTONS; i++) {
            int functionNumber = functionButton[i].getIdentity();
            functionButton[i].setState(_throttle.getFunction(functionNumber));
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
        }
        this.setEnabled(true);
        _throttle.addPropertyChangeListener(this);
    }

    /**
     * Remove PCL from Throttle.
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        if (_throttle != null) {
            _throttle.removePropertyChangeListener(this);
            _throttle = null;
        }
        super.dispose();
    }

    /**
     * Get notification that a function has changed state.
     * Sends to Throttle to action.
     *
     * @param functionNumber The function that has changed (0-9).
     * @param isSet          True if the function is now active (or set).
     */
    @Override
    public void notifyFunctionStateChanged(int functionNumber, boolean isSet) {
        log.debug("notifyFunctionStateChanged: functionNumber={} isSet={}"
            ,functionNumber, isSet);
       _throttle.setFunction(functionNumber, isSet);
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
        if (_throttle != null) {
            // throttle can be null when loading throttle layout
            _throttle.setFunctionMomentary(functionNumber, !isLockable);
        }
    }

    /**
     * Enable or disable all the buttons.
     *
     * @param isEnabled true to enable; false to disable.
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
            functionButton[i].addFunctionListener(this);
            functionButton[i].setButtonLabel( i<3 ?
                Bundle.getMessage(Throttle.getFunctionString(i))
                : Throttle.getFunctionString(i) );
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
     * Make sure that all function buttons are being displayed.
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
    private class FunctionButtonKeyListener extends KeyAdapter {

        private boolean keyReleased = true;

        /**
         * Description of the Method
         *
         * @param e Description of the Parameter
         */
        @Override
        public void keyPressed(KeyEvent e) {
            log.debug("keyPressed: KeyCode= {}", e.getKeyCode());
            if (keyReleased) {
                log.debug("Pressed");
                for (int i = 0; i < NUM_FUNCTION_BUTTONS; i++) {
                    if (functionButton[i].checkKeyCode(e.getKeyCode())) {
                        functionButton[i].setSelected(!functionButton[i].isSelected());
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
            log.debug("keyReleased: KeyCode= {}", e.getKeyCode());
            for (int i = 0; i < NUM_FUNCTION_BUTTONS; i++) {
                if (functionButton[i].checkKeyCode(e.getKeyCode())) {
                    if (!functionButton[i].getIsLockable()) {
                        functionButton[i].setSelected(!functionButton[i].isSelected());
                    }
                }
            }
            keyReleased = true;
        }
    }

    // update the state of this panel if any of the properties change
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        String functionName = e.getPropertyName();
        if (!functionName.startsWith("F")) {
            return;
        }
        boolean isSet = ((Boolean) e.getNewValue());
        
        for ( int i = 0; i< 29; i++ ) {
            if (functionName.equals(Throttle.getFunctionString(i))) {
                functionButton[i].setState(isSet);
                break;
            }
            if (functionName.equals(Throttle.getFunctionMomentaryString(i))) {
                functionName = "Lock" + Throttle.getFunctionString(i);
                functionButton[0].setIsLockable(isSet);
                _throttleFrame.setFunctionLock(functionName, isSet);
                return;
            }
        }
        _throttleFrame.setFunctionState(functionName, isSet);
    }

    private final static Logger log = LoggerFactory.getLogger(FunctionPanel.class);
}
