package jmri.jmrit.throttle;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import jmri.DccThrottle;
import jmri.ThrottleManager;
import jmri.ThrottleListener;
import jmri.InstanceManager;

/**
 * A JFrame to contain throttle elements such as speed control, address chooser,
 * function panel, and maybe others.
 * <p>
 * This class creates a DccThrottle and calls methods in that object as
 * directed by the interface.
 *
 * @author			Glen Oberhauser
 * @version
 */
public class ThrottleFrame extends JFrame
        implements ControlPanelListener, FunctionListener, AddressListener,
        ThrottleListener
{
    private final Integer PANEL_LAYER = new Integer(1);
    private DccThrottle throttle;
    private ThrottleManager throttleManager;
    private ControlPanel controlPanel;
    private AddressPanel addressPanel;
    private FunctionPanel functionPanel;

    /**
     * Default constructor
     */
    public ThrottleFrame()
    {
        initGUI();
    }

    private void initGUI()
    {
        setTitle("Throttle");
        JDesktopPane desktop = new JDesktopPane();
        this.setContentPane(desktop);

        controlPanel = new ControlPanel(0, 1, 128, true);
        controlPanel.setControlPanelListener(this);
        controlPanel.setResizable(true);
        controlPanel.setClosable(true);
        controlPanel.setIconifiable(true);
        controlPanel.setTitle("Control Panel");
        controlPanel.setSize(100,320);
        controlPanel.setVisible(true);
        controlPanel.setEnabled(false);

        functionPanel = new FunctionPanel();
        functionPanel.setFunctionListener(this);
        functionPanel.setResizable(true);
        functionPanel.setClosable(true);
        functionPanel.setIconifiable(true);
        functionPanel.setTitle("Function Panel");
        functionPanel.setSize(200,200);
        functionPanel.setLocation(100, 0);
        functionPanel.setVisible(true);
        functionPanel.setEnabled(false);

        addressPanel = new AddressPanel();
        addressPanel.setAddressListener(this);
        addressPanel.setResizable(true);
        addressPanel.setClosable(true);
        addressPanel.setIconifiable(true);
        addressPanel.setTitle("Address Panel");
        addressPanel.setSize(200,120);
        addressPanel.setLocation(100, 200);
        addressPanel.setVisible(true);

        desktop.add(controlPanel, PANEL_LAYER);
        desktop.add(functionPanel, PANEL_LAYER);
        desktop.add(addressPanel, PANEL_LAYER);

        desktop.setPreferredSize(new Dimension(300, 340));

        try
        {
            addressPanel.setSelected(true);
        }
        catch (java.beans.PropertyVetoException ex)
        {
            System.out.println(ex.getMessage());
        }
    }


    /**
     * Get notification that a throttle has been found as we requested.
     * @param t An instantiation of the DccThrottle with the address requested.
     */
    public void notifyThrottleFound(DccThrottle t)
    {
        this.throttle = t;
        controlPanel.setEnabled(true);
        functionPanel.setEnabled(true);
    }

    /**
     * Get notification that the speed control has been adjusted.
     * @param speed The new speed setting.
     */
    public void notifySpeedChanged(int speed)
    {
        throttle.setSpeedSetting(speed);
    }

    /**
     * Get notification that the direction has changed.
     * @param isForward True if the setting is now set to forward.
     */
    public void notifyDirectionChanged(boolean isForward)
    {
        throttle.setIsForward(isForward);
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
     * Get notification that the decoder address value has changed.
     * @param newAddress The new address.
     */
    public void notifyAddressChanged(int newAddress)
    {
        if (throttleManager == null)
        {
            throttleManager = InstanceManager.throttleManagerInstance();
        }
        throttleManager.requestThrottle(newAddress, this);
        controlPanel.setEnabled(false);
        functionPanel.setEnabled(false);
    }

}