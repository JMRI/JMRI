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

/**
 * A JInternalFrame that contains a JSlider to control loco speed,
 * and buttons for forward, reverse and STOP.
 * TODO: fix speed increments (14, 28)
 * TODO: fix EMERGENCY STOP = 1
 * TODO: match physical throttle.
 */
public class ControlPanel extends JInternalFrame
        implements AddressListener, ThrottleListener
{
    private ThrottleManager throttleManager;
    private DccThrottle throttle;
    private int requestedAddress;

    private JSlider speedSlider;
    private GridBagConstraints sliderConstraints;
    private JRadioButton forwardButton, reverseButton;
    private JButton stopButton;
	private JButton idleButton;
    private JPanel buttonPanel;
    private int speedIncrement;

    private static int MAX_SPEED = 128; //TODO: correct always?

    /**
     * Constructor.
     */
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
        requestedAddress = newAddress;
        this.setEnabled(false);
        // this has to be done last, as it might come back immediately
        throttleManager.requestThrottle(newAddress, this);
    }

    /**
     * Cancel throttle request with ThrottleManager.
     */
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

    /**
     * Enable/Disable all buttons and slider.
     * @param isEnabled True if the buttons/slider should be enabled,
     * false otherwise.
     */
    public void setEnabled(boolean isEnabled)
    {
        //super.setEnabled(isEnabled);
        forwardButton.setEnabled(isEnabled);
        reverseButton.setEnabled(isEnabled);
        stopButton.setEnabled(isEnabled);
		idleButton.setEnabled(isEnabled);
        speedSlider.setEnabled(isEnabled);
    }

    /**
     * Set the GUI to match that the loco is set to forward.
     * @param isForward True if the loco is set to forward,
     * false otherwise.
     */
    public void setIsForward(boolean isForward)
    {
        forwardButton.setSelected(isForward);
        reverseButton.setSelected(!isForward);
    }

    /**
     * Set the GUI to match that the loco speed.
     * @param speedIncrement : TODO
     * @param speed The speed value of the loco.
     */
    public void setSpeedValues(int speedIncrement, int speed)
    {
        this.speedIncrement = speedIncrement;
        speedSlider.setValue(speed*speedIncrement);
    }

    /**
     * Create, initialize and place GUI components.
     */
    private void initGUI()
    {
        JPanel mainPanel = new JPanel();
        this.setContentPane(mainPanel);
        mainPanel.setLayout(new BorderLayout());
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JPanel sliderPanel = new JPanel();
        sliderPanel.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.fill = GridBagConstraints.BOTH;
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

        sliderPanel.add(speedSlider, constraints);
        this.getContentPane().add(sliderPanel, BorderLayout.CENTER);
        speedSlider.setOrientation(JSlider.VERTICAL);
        speedSlider.setMajorTickSpacing(32);
        speedSlider.setMinorTickSpacing(8);
        java.util.Hashtable labelTable = new java.util.Hashtable();
        labelTable.put( new Integer( 31 ), new JLabel("25%") );
        labelTable.put( new Integer( 63 ), new JLabel("50%") );
        labelTable.put( new Integer( 95 ), new JLabel("75%") );
        labelTable.put( new Integer( 127 ), new JLabel("100%") );
        speedSlider.setLabelTable( labelTable );
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);
        speedSlider.addChangeListener(
                new ChangeListener()
                {
                   public void stateChanged(ChangeEvent e)
                   {
                       if (!speedSlider.getValueIsAdjusting())
                       {
                           throttle.setSpeedSetting(speedSlider.getValue()/127.0f);
                       }
                   }
                });

        buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout());
        this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        ButtonGroup directionButtons = new ButtonGroup();
        directionButtons.add(forwardButton);
        directionButtons.add(reverseButton);
        constraints.fill = GridBagConstraints.NONE;
        constraints.gridy = 1;
        buttonPanel.add(forwardButton, constraints);
        constraints.gridy = 2;
        buttonPanel.add(reverseButton, constraints);

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
		constraints.fill = GridBagConstraints.HORIZONTAL;
        buttonPanel.add(stopButton, constraints);
        stopButton.addActionListener(
                 new ActionListener()
                 {
                    public void actionPerformed(ActionEvent e)
                    {
                        speedSlider.setValue(-1);
                    }
                 });

        idleButton = new JButton("Idle");
        constraints.gridy = 4;
        buttonPanel.add(idleButton, constraints);
        idleButton.addActionListener(
                 new ActionListener()
                 {
                    public void actionPerformed(ActionEvent e)
                    {
                        speedSlider.setValue(0);
                    }
                 });
				 
		this.addComponentListener(
                new ComponentAdapter()
        {
            public void componentResized(ComponentEvent e)
            {
                changeOrientation();
            }
        });
    }

    /**
     * The user has resized the Frame. Possibly change from
     * Horizontal to Vertical layout.
     */
    private void changeOrientation()
    {
        if (this.getWidth() > this.getHeight())
        {
            speedSlider.setOrientation(JSlider.HORIZONTAL);
            this.remove(buttonPanel);
            this.getContentPane().add(buttonPanel, BorderLayout.EAST);
        }
        else
        {
            speedSlider.setOrientation(JSlider.VERTICAL);
            this.remove(buttonPanel);
            this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        }
    }

    /**
     * Collect the prefs of this object into XML Element
     * <ul>
     * <li> Window prefs
     * </ul>
     * @return the XML of this object.
     */
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

     /**
      * Set the preferences based on the XML Element.
      * <ul>
      * <li> Window prefs
      * </ul>
      * @param e The Element for this object.
      */
     public void setXml(Element e)
     {
         Element window = e.getChild("window");
         WindowPreferences wp = new WindowPreferences();
         wp.setPreferences(this, window);
     }

}