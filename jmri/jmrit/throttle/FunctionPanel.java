package jmri.jmrit.throttle;

import javax.swing.*;
import java.awt.*;
import jmri.DccThrottle;
import jmri.ThrottleListener;
import jmri.ThrottleManager;
import jmri.InstanceManager;


public class FunctionPanel extends JInternalFrame
        implements ThrottleListener, AddressListener, FunctionListener
{
    public static final int NUM_FUNCTION_BUTTONS = 10;

    private ThrottleManager throttleManager;
    private DccThrottle throttle;
    private int requestedAddress;

    private FunctionButton functionButton[];

    /**
     * Constructor
     */
    public FunctionPanel()
    {
        initGUI();
    }

    public void dispose()
    {
        if (throttleManager != null)
        {
            throttleManager.cancelThrottleRequest(requestedAddress, this);
        }
        super.dispose();
    }

    /**
     * Get notification that a throttle has been found as we requested.
     * @param t An instantiation of the DccThrottle with the address requested.
     */
    public void notifyThrottleFound(DccThrottle t)
    {
        this.throttle = t;
        functionButton[0].setState(throttle.getF0());
        functionButton[1].setState(throttle.getF1());
        functionButton[2].setState(throttle.getF2());
        functionButton[3].setState(throttle.getF3());
        functionButton[4].setState(throttle.getF4());
        functionButton[5].setState(throttle.getF5());
        functionButton[6].setState(throttle.getF6());
        functionButton[7].setState(throttle.getF7());
        functionButton[8].setState(throttle.getF8());
        functionButton[9].setState(false); // No F9?

        this.setEnabled(true);
    }

    /**
     * Get notification that the decoder address value has changed.
     * @param newAddress The new address.
     */
    public void notifyAddressChanged(int oldAddress, int newAddress)
    {
        if (throttleManager == null)
        {
            throttleManager = InstanceManager.throttleManagerInstance();
        }
        throttleManager.cancelThrottleRequest(oldAddress, this);
        throttleManager.requestThrottle(newAddress, this);
        requestedAddress = newAddress;
        this.setEnabled(false);
    }

    /**
     * Get notification that a function has changed state
     * @param functionNumber The function that has changed (0-9).
     * @param isSet True if the function is now active (or set).
     */
    public void notifyFunctionStateChanged(int functionNumber, boolean isSet)
    {
        switch (functionNumber)
        {
            case 0: throttle.setF0(isSet); break;
            case 1: throttle.setF1(isSet); break;
            case 2: throttle.setF2(isSet); break;
            case 3: throttle.setF3(isSet); break;
            case 4: throttle.setF4(isSet); break;
            case 5: throttle.setF5(isSet); break;
            case 6: throttle.setF6(isSet); break;
            case 7: throttle.setF7(isSet); break;
            case 8: throttle.setF8(isSet); break;
        }
    }

    /**
     * Enable or disable all the buttons
     */
    public void setEnabled(boolean isEnabled)
    {
        super.setEnabled(isEnabled);
        for (int i=0; i < NUM_FUNCTION_BUTTONS; i++)
        {
            functionButton[i].setEnabled(isEnabled);
        }
    }

    /**
     * Place and initialize all the buttons.
     */
    private void initGUI()
    {
        JPanel mainPanel = new JPanel();
        this.setContentPane(mainPanel);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        mainPanel.setLayout(new GridLayout(4, 3));
        functionButton = new FunctionButton[NUM_FUNCTION_BUTTONS];
        for (int i=0; i<NUM_FUNCTION_BUTTONS; i++)
        {
            functionButton[i] = new FunctionButton(i, false);
            functionButton[i].setFunctionListener(this);
            functionButton[i].setText("F"+String.valueOf(i));
            if (i > 0)
            {
                mainPanel.add(functionButton[i]);
            }
        }
        mainPanel.add(new JLabel(""));
        mainPanel.add(functionButton[0]);
    }

}