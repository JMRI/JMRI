package jmri.jmrit.throttle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import jmri.ThrottleManager;
import jmri.ThrottleListener;
import jmri.DccThrottle;
import jmri.InstanceManager;

import org.jdom.Element;

public class ControlPanel extends JInternalFrame
        implements AddressListener, ThrottleListener
{
    private ThrottleManager throttleManager;
    private DccThrottle throttle;
    private int requestedAddress;

    private JSlider speedSlider;
    private JRadioButton forwardButton, reverseButton;
    private JButton stopButton;
    private int speedIncrement;

    private static int MAX_SPEED = 127; //TODO: correct always?

    public ControlPanel()
    {
        speedSlider = new JSlider(0, MAX_SPEED);
        speedSlider.setValue(0);
        forwardButton = new JRadioButton("Forward");
        reverseButton = new JRadioButton("Reverse");

        initGUI();
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
        this.setIsForward(throttle.getIsForward());
        this.setSpeedValues((int)throttle.getSpeedIncrement(),
                            (int)throttle.getSpeedSetting());
        this.setEnabled(true);
    }


    public void setEnabled(boolean isEnabled)
    {
        super.setEnabled(isEnabled);
        forwardButton.setEnabled(isEnabled);
        reverseButton.setEnabled(isEnabled);
        stopButton.setEnabled(isEnabled);
        speedSlider.setEnabled(isEnabled);
    }

    public void setIsForward(boolean isForward)
    {
        forwardButton.setSelected(isForward);
        reverseButton.setSelected(!isForward);
    }

    public void setSpeedValues(int speedIncrement, int speed)
    {
        this.speedIncrement = speedIncrement;
        speedSlider.setValue(speed*speedIncrement);
    }

    private void initGUI()
    {
        JPanel mainPanel = new JPanel();
        this.setContentPane(mainPanel);
        mainPanel.setLayout(new GridBagLayout());
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.fill = GridBagConstraints.VERTICAL;
        constraints.gridheight = 1;
        constraints.gridwidth = 1;
        constraints.ipadx = 0;
        constraints.ipady = 0;
        Insets insets = new Insets(2, 2, 2, 2);
        constraints.insets = insets;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.gridx = 0;
        constraints.gridy = 0;

        this.getContentPane().add(speedSlider, constraints);
        speedSlider.setOrientation(JSlider.VERTICAL);
        speedSlider.addChangeListener(
                new ChangeListener()
                {
                   public void stateChanged(ChangeEvent e)
                   {
                       throttle.setSpeedSetting(speedSlider.getValue());
                   }
                });

        ButtonGroup directionButtons = new ButtonGroup();
        directionButtons.add(forwardButton);
        directionButtons.add(reverseButton);
        constraints.gridy = 1;
        this.getContentPane().add(forwardButton, constraints);
        constraints.gridy = 2;
        this.getContentPane().add(reverseButton, constraints);

        forwardButton.addActionListener(
                new ActionListener()
                {
                   public void actionPerformed(ActionEvent e)
                   {
                       throttle.setIsForward(true);
                   }
                });

        reverseButton.addActionListener(
                 new ActionListener()
                 {
                    public void actionPerformed(ActionEvent e)
                    {
                        throttle.setIsForward(false);
                    }
                 });

        stopButton = new JButton("STOP!");
        constraints.gridy = 3;
        this.getContentPane().add(stopButton, constraints);
        stopButton.addActionListener(
                 new ActionListener()
                 {
                    public void actionPerformed(ActionEvent e)
                    {
                        speedSlider.setValue(0);
                    }
                 });
     }

     public Element getXml()
     {
         Element me = new Element("ControlPanel");
         Element window = new Element("window");
         WindowPreferences wp = new WindowPreferences();
         com.sun.java.util.collections.ArrayList children =
                 new com.sun.java.util.collections.ArrayList(1);
         children.add(wp.getPreferences(this));
         me.setChildren(children);
         return me;
     }

     public void setXml(Element e)
     {
         Element window = e.getChild("window");
         WindowPreferences wp = new WindowPreferences();
         wp.setPreferences(this, window);
     }

}