package jmri.jmrit.throttle;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.*;
import java.util.ResourceBundle;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputAdapter;

import jmri.DccThrottle;
import jmri.util.MouseInputAdapterInstaller;

import org.jdom.Attribute;
import org.jdom.Element;

/**
 *  A JInternalFrame that contains a JSlider to control loco speed, and buttons
 *  for forward, reverse and STOP.
 *  <P>
 *  TODO: fix speed increments (14, 28)
 *
 * @author     glen   Copyright (C) 2002
 * @author Bob Jacobsen Copyright (C) 2007
 * @author Ken Cameron Copyright (C) 2008
 *
 * @version    $Revision: 1.91 $
 */
public class ControlPanel extends JInternalFrame implements java.beans.PropertyChangeListener, ActionListener, AddressListener 
{
    static final ResourceBundle rb = ThrottleBundle.bundle();
    
    private DccThrottle throttle;
    
    private JSlider speedSlider;
    private JSpinner speedSpinner;
    private SpinnerNumberModel speedSpinnerModel;
    private JRadioButton SpeedStep128Button;
    private JRadioButton SpeedStep28Button;
    private JRadioButton SpeedStep27Button;
    private JRadioButton SpeedStep14Button;
    //private GridBagConstraints sliderConstraints;
    private JRadioButton forwardButton, reverseButton;
    private JButton stopButton;
    private JButton idleButton;
    private JPanel buttonPanel;
    private boolean internalAdjust = false;
    
    private JPopupMenu propertiesPopup;
    private JPanel speedControlPanel;
    private	JPanel spinnerPanel;
    private	JPanel sliderPanel;

    private AddressPanel addressPanel; //for access to roster entry
    /* Constants for speed selection method */
    final public static int SLIDERDISPLAY = 0;
    final public static int STEPDISPLAY = 1;
    
    private int _displaySlider = SLIDERDISPLAY;
    
    /* real time tracking of speed slider - on iff trackSlider==true
     * Min interval for sending commands to the actual throttle can be configured
     * as part of the throttle config but is bounded
     */
    private JPanel mainPanel ;
    
    private boolean trackSlider = false;
    private boolean trackSliderDefault = false;
    private long trackSliderMinInterval = 200;                    // milliseconds
    private long trackSliderMinIntervalDefault = 200;       // milliseconds
    private long trackSliderMinIntervalMin = 50;       // milliseconds
    private long trackSliderMinIntervalMax = 1000;       // milliseconds
    private long lastTrackedSliderMovementTime = 0;
    
    public int accelerateKey = 107; // numpad +;
    public int decelerateKey = 109; // numpad -;
    public int accelerateKey1 = KeyEvent.VK_LEFT ; // Left Arrow
    public int decelerateKey1 = KeyEvent.VK_RIGHT ; // Left Arrow
    public int accelerateKey2 = KeyEvent.VK_PAGE_UP ; // Left Arrow
    public int decelerateKey2 = KeyEvent.VK_PAGE_DOWN ; // Left Arrow
    public int reverseKey = KeyEvent.VK_DOWN;
    public int forwardKey = KeyEvent.VK_UP;
    public int stopKey = 111; // numpad /
    public int idleKey = 106; // numpad *
    
    // LocoNet really only has 126 speed steps i.e. 0..127 - 1 for em stop
    private int intSpeedSteps = 126;
    
    private int maxSpeed = 126; //The maximum permissible speed
    
    // Save the speed step mode to aid in storage of the throttle.
    private int _speedStepMode = DccThrottle.SpeedStepMode128;
    
    // Save the speed step mode from the xml until the throttle is actually available
    private int _speedStepModeForLater = 0;
    
    /**
     *  Constructor.
     */
    public ControlPanel()
    {
        speedSlider = new JSlider(0, intSpeedSteps);
        speedSlider.setValue(0);
        speedSlider.setFocusable(false);
	
	    // add mouse-wheel support
        speedSlider.addMouseWheelListener(new MouseWheelListener() {
          public void mouseWheelMoved(MouseWheelEvent e) {
            if(e.getWheelRotation() > 0) 
            	/* Andrew Berridge added for loops */
            	for (int i = 0; i < e.getScrollAmount(); i++) decelerate1();
            else
            	for (int i = 0; i < e.getScrollAmount(); i++) accelerate1();
          }
        });
	    
        speedSpinner = new JSpinner();

        speedSpinnerModel = new SpinnerNumberModel(0, 0, intSpeedSteps, 1);
        speedSpinner.setModel(speedSpinnerModel);
        speedSpinner.setFocusable(false);

        SpeedStep128Button = new JRadioButton(rb.getString("Button128SS"));
        SpeedStep28Button = new JRadioButton(rb.getString("Button28SS"));
        SpeedStep27Button = new JRadioButton(rb.getString("Button27SS"));
        SpeedStep14Button= new JRadioButton(rb.getString("Button14SS"));
        
        forwardButton = new JRadioButton(rb.getString("ButtonForward"));
        reverseButton = new JRadioButton(rb.getString("ButtonReverse"));
        
        propertiesPopup = new JPopupMenu();
        initGUI();
    }
    
    public void setAddressPanel(AddressPanel addressPanel) {
		this.addressPanel = addressPanel;
	}

	public void destroy()
    {
        if (throttle != null)
            {
                throttle.setSpeedSetting(0);
            }
    }
	
    /**
     *  Enable/Disable all buttons and slider.
     *
     * @param  isEnabled  True if the buttons/slider should be enabled, false
     *      otherwise.
     */
    public void setEnabled(boolean isEnabled)
    {
        //super.setEnabled(isEnabled);
        forwardButton.setEnabled(isEnabled);
        reverseButton.setEnabled(isEnabled);
        SpeedStep128Button.setEnabled(isEnabled);
        SpeedStep28Button.setEnabled(isEnabled);
        SpeedStep27Button.setEnabled(isEnabled);
        SpeedStep14Button.setEnabled(isEnabled);
        if(isEnabled) configureAvailableSpeedStepModes();
        stopButton.setEnabled(isEnabled);
        idleButton.setEnabled(isEnabled);
        speedControllerEnable = isEnabled;
        switch(_displaySlider) {
        case STEPDISPLAY: {
            if(speedSpinner!=null)
                speedSpinner.setEnabled(isEnabled);
            speedSlider.setEnabled(false);
            break;
        }
        default:          {
            if(speedSpinner!=null)
                speedSpinner.setEnabled(false);
            speedSlider.setEnabled(isEnabled);
        }
        }
    }	
    
    /**
     *  Set the GUI to match that the loco is set to forward.
     *
     * @param  isForward  True if the loco is set to forward, false otherwise.
     */
    public void setIsForward(boolean isForward)
    {
        forwardButton.setSelected(isForward);
        reverseButton.setSelected(!isForward);
    }
    public boolean getIsForward() { return forwardButton.isSelected(); }
    
    /**
     * Set forward/reverse direction in both the 
     * GUI and on the layout
     */
    public void setForwardDirection(boolean isForward) {
        if (isForward) forwardButton.doClick();
        else reverseButton.doClick();
    }
    
    /**
     *  Set the GUI to match the speed steps of the current address.
     *  Initialises the speed slider and spinner - including setting their
     *  maximums based on the speed step setting and the max speed for the
     *  particular loco
     *
     * @param  speedStepMode Desired speed step mode. One of:
     * DccThrottle.SpeedStepMode128, DccThrottle.SpeedStepMode28,
     * DccThrottle.SpeedStepMode27, DccThrottle.SpeedStepMode14 
     * step mode
     */
    public void setSpeedStepsMode(int speedStepMode)
    {
       	int maxSpeedPCT = 100;
    	if (addressPanel.getRosterEntry() != null)
    		maxSpeedPCT = addressPanel.getRosterEntry().getMaxSpeedPCT();
   
        // Save the old speed as a float
        float oldSpeed = (speedSlider.getValue() / ( maxSpeed * 1.0f ) ) ;
        
        if(speedStepMode == DccThrottle.SpeedStepMode14) {
            SpeedStep14Button.setSelected(true);
            SpeedStep27Button.setSelected(false);
            SpeedStep28Button.setSelected(false);
            SpeedStep128Button.setSelected(false);
            intSpeedSteps=14;
        } else  if(speedStepMode == DccThrottle.SpeedStepMode27) {
            SpeedStep14Button.setSelected(false);
            SpeedStep27Button.setSelected(true);
            SpeedStep28Button.setSelected(false);
            SpeedStep128Button.setSelected(false);
            intSpeedSteps=27;
        } else  if(speedStepMode == DccThrottle.SpeedStepMode28) {
            SpeedStep14Button.setSelected(false);
            SpeedStep27Button.setSelected(false);
            SpeedStep28Button.setSelected(true);
            SpeedStep128Button.setSelected(false);
            intSpeedSteps=28;
        } else  {
            SpeedStep14Button.setSelected(false);
            SpeedStep27Button.setSelected(false);
            SpeedStep28Button.setSelected(false);
            SpeedStep128Button.setSelected(true);
            intSpeedSteps=126;
        }
        _speedStepMode=speedStepMode;
        /* Set maximum speed based on the max speed stored in the roster as a percentage of the maximum */
        maxSpeed = (int) ((float) intSpeedSteps*((float)maxSpeedPCT)/100);
        
        // rescale the speed slider to match the new speed step mode
        internalAdjust=true;
        
        this.speedSlider.setMaximum(maxSpeed);
		
        speedSlider.setValue((int)(oldSpeed * maxSpeed));
        speedSlider.setMajorTickSpacing(maxSpeed/2);
        java.util.Hashtable<Integer,JLabel> labelTable = new java.util.Hashtable<Integer,JLabel>();
        labelTable.put(Integer.valueOf(maxSpeed/2), new JLabel("50%"));
        labelTable.put(Integer.valueOf(maxSpeed), new JLabel("100%"));
        labelTable.put(Integer.valueOf(0), new JLabel(rb.getString("LabelStop")));
        speedSlider.setLabelTable(labelTable);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);
        
        speedSpinnerModel.setMaximum(Integer.valueOf(maxSpeed));
        speedSpinnerModel.setMinimum(Integer.valueOf(0));
        // rescale the speed value to match the new speed step mode
        internalAdjust=true;
        speedSpinnerModel.setValue(Integer.valueOf(speedSlider.getValue()));
    }
    
    /**
     *  Is this Speed Control selection method possible?
     *
     *  @param displaySlider integer value. possible values:
     *	SLIDERDISPLAY  = use speed slider display
     *      STEPDISPLAY = use speed step display
     */
    public boolean isSpeedControllerAvailable(int displaySlider) {
        switch(displaySlider) {
        case STEPDISPLAY: 
            return(speedSpinner!=null);
        case SLIDERDISPLAY:
            return(speedSlider!=null);
        default:
            return false;
        }
    }
    
    private boolean speedControllerEnable = false;
    /**
     *  Set the Speed Control selection method
     *
     *  @param displaySlider integer value. possible values:
     *	SLIDERDISPLAY  = use speed slider display
     *      STEPDISPLAY = use speed step display
     */
    @SuppressWarnings("fallthrough")
	public void setSpeedController(int displaySlider) {
        switch(displaySlider) {
        case STEPDISPLAY: {
            if (speedSpinner!=null) {
                sliderPanel.setVisible(false);
                speedSlider.setEnabled(false);
                spinnerPanel.setVisible(true);
                speedSpinner.setEnabled(speedControllerEnable);
                break;
            }
            // if speedSpinner == null, fall through to default case
        }
        default: {
            sliderPanel.setVisible(true);
            speedSlider.setEnabled(speedControllerEnable);
            spinnerPanel.setVisible(false);
            if (speedSpinner!=null) speedSpinner.setEnabled(false);
        }
        }
        _displaySlider=displaySlider;
    }
    
    /**
     *  Get the value indicating what speed input we're displaying
     *  
     */
    public int getDisplaySlider() {
        return _displaySlider;
    }
    
    /**
     * Set real-time tracking of speed slider, or not
     * 
     * @param track  boolean value, true to track, false to set speed on unclick
     */
    
    public void setTrackSlider(boolean track) {
        trackSlider = track;
    }
    
    /**
     * Get status of real-time speed slider tracking
     */
    
    public boolean getTrackSlider() {
        return trackSlider;
    }
    
    /**
     *  Set the GUI to match that the loco speed.
     *
     * @param  speedIncrement  The throttle back end's speed increment 
     *                         value - % increase for each speed step.
     * @param  speed           The speed value of the loco.
     */
    public void setSpeedValues(float speedIncrement, float speed)
    {
        //This is an internal speed adjustment
        internalAdjust=true;
    	//Translate the speed sent in to the max allowed by any set speed limit
    	speedSlider.setValue(java.lang.Math.round(speed/speedIncrement));
    			
        if (log.isDebugEnabled()) log.debug("SpeedSlider value: "+speedSlider.getValue());
        // Spinner Speed should be the raw integer speed value
        if(speedSpinner!=null)
            speedSpinnerModel.setValue(Integer.valueOf(speedSlider.getValue()));
    }
    
    public JSlider getSpeedSlider() { return speedSlider; }
    
    /**
     *  Create, initialize and place GUI components.
     */
    private void initGUI()
    {
        mainPanel = new JPanel();
        this.setContentPane(mainPanel);
        mainPanel.setLayout(new BorderLayout());
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        
        speedControlPanel = new JPanel();
        speedControlPanel.setLayout(new BoxLayout(speedControlPanel,BoxLayout.X_AXIS));
        mainPanel.add(speedControlPanel,BorderLayout.CENTER);
        sliderPanel = new JPanel();
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
        //this.getContentPane().add(sliderPanel,BorderLayout.CENTER);
        speedControlPanel.add(sliderPanel);
        speedSlider.setOrientation(JSlider.VERTICAL);
        speedSlider.setMajorTickSpacing(maxSpeed/2);
        java.util.Hashtable<Integer,JLabel> labelTable = new java.util.Hashtable<Integer,JLabel>();
        labelTable.put(Integer.valueOf(maxSpeed/2), new JLabel("50%"));
        labelTable.put(Integer.valueOf(maxSpeed), new JLabel("100%"));
        labelTable.put(Integer.valueOf(0), new JLabel(rb.getString("LabelStop")));
        speedSlider.setLabelTable(labelTable);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);
        // remove old actions
        speedSlider.addChangeListener(
                                      new ChangeListener()
                                      {
                                          public void stateChanged(ChangeEvent e) {
                                              if ( !internalAdjust) {
                                                  boolean doIt = false;
                                                  if (!speedSlider.getValueIsAdjusting()) {
                                                      doIt = true;
                                                      lastTrackedSliderMovementTime = System.currentTimeMillis() - trackSliderMinInterval;
                                                  } else if (trackSlider &&
                                                             System.currentTimeMillis() - lastTrackedSliderMovementTime >= trackSliderMinInterval) {
                                                      doIt = true;
                                                      lastTrackedSliderMovementTime = System.currentTimeMillis();
                                                  }
                                                  if (doIt) {
                                                      float newSpeed = (speedSlider.getValue() / ( intSpeedSteps * 1.0f ) ) ;
                                                      if (log.isDebugEnabled()) {log.debug( "stateChanged: slider pos: " + speedSlider.getValue() + " speed: " + newSpeed );}
                                                      if (sliderPanel.isVisible() && throttle != null) {
                                                          throttle.setSpeedSetting( newSpeed );
                                                      }
                                                      if(speedSpinner!=null)
                                                          speedSpinnerModel.setValue(Integer.valueOf(speedSlider.getValue()));
                                                  }
                                              } else {
                                                  internalAdjust=false;
                                              }
                                          }
                                      });
        
        spinnerPanel = new JPanel();
        spinnerPanel.setLayout(new GridBagLayout());
        
        if(speedSpinner!=null)
            spinnerPanel.add(speedSpinner, constraints);
        //this.getContentPane().add(spinnerPanel,BorderLayout.CENTER);
        speedControlPanel.add(spinnerPanel);
        // remove old actions
        if(speedSpinner!=null)
            speedSpinner.addChangeListener(
                                           new ChangeListener()
                                           {
                                               public void stateChanged(ChangeEvent e)
                                               {
                                                   if ( !internalAdjust) {
                                                       //if (!speedSpinner.getValueIsAdjusting())
                                                       //{
                                                       float newSpeed = ((Integer)speedSpinner.getValue()).floatValue() / ( intSpeedSteps * 1.0f );
                                                       if (log.isDebugEnabled()) {log.debug( "stateChanged: spinner pos: " + speedSpinner.getValue() + " speed: " + newSpeed );}
                                                       if (throttle != null) {                                                    	   
                                                           if (spinnerPanel.isVisible()) {
                                                               throttle.setSpeedSetting( newSpeed );
                                                           }
                                                           speedSlider.setValue(((Integer)speedSpinner.getValue()).intValue());
                                                       } else {
                                                           log.warn("no throttle object in stateChanged, ignoring change of speed to "+newSpeed);
                                                       }
                                                       //}
                                                   } else {
                                                       internalAdjust=false;
                                                   }
                                               }
                                           });
        
        ButtonGroup speedStepButtons = new ButtonGroup();
        speedStepButtons.add(SpeedStep128Button);
        speedStepButtons.add(SpeedStep28Button);
        speedStepButtons.add(SpeedStep27Button);
        speedStepButtons.add(SpeedStep14Button);
        constraints.fill = GridBagConstraints.NONE;
        constraints.gridy = 1;
        spinnerPanel.add(SpeedStep128Button, constraints);
        constraints.gridy = 2;
        spinnerPanel.add(SpeedStep28Button, constraints);
        constraints.gridy = 3;
        spinnerPanel.add(SpeedStep27Button, constraints);
        constraints.gridy = 4;
        spinnerPanel.add(SpeedStep14Button, constraints);
        
        SpeedStep14Button.addActionListener(
                                            new ActionListener()
                                            {
                                                public void actionPerformed(ActionEvent e)
                                                {
                                                    setSpeedStepsMode(DccThrottle.SpeedStepMode14);
                                                    throttle.setSpeedStepMode(DccThrottle.SpeedStepMode14);
                                                }
                                            });
        
        SpeedStep27Button.addActionListener(
                                            new ActionListener()
                                            {
                                                public void actionPerformed(ActionEvent e)
                                                {
                                                    setSpeedStepsMode(DccThrottle.SpeedStepMode27);
                                                    throttle.setSpeedStepMode(DccThrottle.SpeedStepMode27);
                                                }
                                            });
        
        SpeedStep28Button.addActionListener(
                                            new ActionListener()
                                            {
                                                public void actionPerformed(ActionEvent e)
                                                {
                                                    setSpeedStepsMode(DccThrottle.SpeedStepMode28);
                                                    throttle.setSpeedStepMode(DccThrottle.SpeedStepMode28);
                                                }
                                            });
        
        SpeedStep128Button.addActionListener(
                                             new ActionListener()
                                             {
                                                 public void actionPerformed(ActionEvent e)
                                                 {
                                                     setSpeedStepsMode(DccThrottle.SpeedStepMode128);
                                                     throttle.setSpeedStepMode(DccThrottle.SpeedStepMode128);
                                                 }
                                             });
        
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout());
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
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
                                             stop();
                                         }
                                     });
        
        stopButton.addMouseListener(
                                    new MouseListener()
                                    {
                                        public void mousePressed(MouseEvent e)
                                        {
                                            stop();
                                        }
                                        public void mouseExited(MouseEvent e) {}
                                        public void mouseEntered(MouseEvent e) {}
                                        public void mouseReleased(MouseEvent e) {}
                                        public void mouseClicked(MouseEvent e) {}
                                    });
        
        idleButton = new JButton(rb.getString("ButtonIdle"));
        constraints.gridy = 4;
        buttonPanel.add(idleButton, constraints);
        idleButton.addActionListener(
                                     new ActionListener()
                                     {
                                         public void actionPerformed(ActionEvent e)
                                         {
                                             speedSlider.setValue(0);
                                             if(speedSpinner!=null)
                                                 speedSpinner.setValue(Integer.valueOf(0));
                                             throttle.setSpeedSetting(0);
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
        
        JMenuItem propertiesItem = new JMenuItem("Properties");
        propertiesItem.addActionListener(this);
        propertiesPopup.add(propertiesItem);
        
        // Add a mouse listener all components to trigger the popup menu.
        MouseInputAdapter popupListener = new PopupListener(propertiesPopup,this);
        MouseInputAdapterInstaller.installMouseInputAdapterOnAllComponents(popupListener,this);
        
        // Install the Key bindings on all Components
        KeyListenerInstaller.installKeyListenerOnAllComponents(new ControlPadKeyListener(), this);
        
        // set by default which speed selection method is on top
        setSpeedController(_displaySlider);
    }
    
    /**
     *  Perform an emergency stop
     */
    public void stop()
    {
        if(this.throttle==null) return;
        speedSlider.setValue(0);
        if(speedSpinner!=null)
            speedSpinnerModel.setValue(Integer.valueOf(0));
        throttle.setSpeedSetting(-1);
    }
    
    /**
     *  The user has resized the Frame. Possibly change from Horizontal to Vertical
     *  layout.
     */
    private void changeOrientation()
    {
        if (mainPanel.getWidth() > mainPanel.getHeight())
            {
                speedSlider.setOrientation(JSlider.HORIZONTAL);
                mainPanel.remove(buttonPanel);
                mainPanel.add(buttonPanel, BorderLayout.EAST);
            }
        else
            {
                speedSlider.setOrientation(JSlider.VERTICAL);
                mainPanel.remove(buttonPanel);
                mainPanel.add(buttonPanel, BorderLayout.SOUTH);
            }
    }
    
    public void accelerate1() {
        if (speedSlider.isEnabled()) {
            if (speedSlider.getValue() != speedSlider.getMaximum()) {
                speedSlider.setValue(speedSlider.getValue() + 1);
            }
        } else if(speedSpinner!=null && speedSpinner.isEnabled()) {
            if (((Integer)speedSpinner.getValue()).intValue() < ((Integer)speedSpinnerModel.getMaximum()).intValue() &&
               ((Integer)speedSpinner.getValue()).intValue() >= ((Integer)speedSpinnerModel.getMinimum()).intValue() ) {
                    speedSpinner.setValue(Integer.valueOf(((Integer)speedSpinner.getValue()).intValue() + 1));
            }
        }
    }
    
    public void accelerate10() {
        if (speedSlider.isEnabled())
            {
                if (speedSlider.getValue() != speedSlider.getMaximum())
                    {
                        speedSlider.setValue(speedSlider.getValue() + 10);
                    }
            }
        else if (speedSpinner!=null && speedSpinner.isEnabled())
            {
                if (((Integer)speedSpinner.getValue()).intValue() < ((Integer)speedSpinnerModel.getMaximum()).intValue() &&
                    ((Integer)speedSpinner.getValue()).intValue() >= ((Integer)speedSpinnerModel.getMinimum()).intValue() )
                    {
                        Integer speedvalue= Integer.valueOf(((Integer)speedSpinner.getValue()).intValue() + 10);
                        if(speedvalue.intValue()<((Integer)speedSpinnerModel.getMaximum()).intValue())
                            speedSpinner.setValue(speedvalue);
                        else
                            speedSpinner.setValue(speedSpinnerModel.getMaximum());
                    }
            }
    }
    
    public void decelerate1() {
        if (speedSlider.isEnabled()) {
            if (speedSlider.getValue() != speedSlider.getMinimum()) {
                speedSlider.setValue(speedSlider.getValue() - 1);
            }
        } else if (speedSpinner!=null && speedSpinner.isEnabled()) {
            if (((Integer)speedSpinner.getValue()).intValue() <= ((Integer)speedSpinnerModel.getMaximum()).intValue() &&
                ((Integer)speedSpinner.getValue()).intValue() > ((Integer)speedSpinnerModel.getMinimum()).intValue() ) {
                    speedSpinner.setValue(Integer.valueOf(((Integer)speedSpinner.getValue()).intValue() - 1));
            }
        }
    }
    
    public void decelerate10() {
        if (speedSlider.isEnabled())
            {
                if (speedSlider.getValue() != speedSlider.getMinimum())
                    {
                        speedSlider.setValue(speedSlider.getValue() - 10);
                    }
            }
        else if (speedSpinner!=null && speedSpinner.isEnabled())
            {
                if (((Integer)speedSpinner.getValue()).intValue() <= ((Integer)speedSpinnerModel.getMaximum()).intValue() &&
                    ((Integer)speedSpinner.getValue()).intValue() > ((Integer)speedSpinnerModel.getMinimum()).intValue() )
                    {
                        Integer speedvalue= Integer.valueOf(((Integer)speedSpinner.getValue()).intValue() - 10);
                        if(speedvalue.intValue()>((Integer)speedSpinnerModel.getMinimum()).intValue())
                            speedSpinner.setValue(speedvalue);
                        else
                            speedSpinner.setValue(speedSpinnerModel.getMinimum());
                    }
            }
    }
    
    /**
     *  A KeyAdapter that listens for the keys that work the control pad buttons
     *
     * @author     glen
     * @version    $Revision: 1.91 $
     */
    class ControlPadKeyListener extends KeyAdapter
    {
        /**
         *  Description of the Method
         *
         * @param  e  Description of the Parameter
         */
        public void keyPressed(KeyEvent e)
        {
        	if (e.isAltDown() || e.isControlDown() || e.isMetaDown() || e.isShiftDown() )
        		return; // we don't want speed change while changing Frame/Panel/Window
            if ( (e.getKeyCode() == accelerateKey) || (e.getKeyCode() == accelerateKey1) ) {
                accelerate1();
            } else if ( e.getKeyCode() == accelerateKey2 ) {
                accelerate10();
            } else if ( (e.getKeyCode() == decelerateKey) || (e.getKeyCode() == decelerateKey1) ) {
                decelerate1();
            } else if ( e.getKeyCode() == decelerateKey2 ) {
                decelerate10();
            } else if (e.getKeyCode() == forwardKey) {
                if (forwardButton.isEnabled())
                    {
                        forwardButton.doClick();
                    }
            } else if (e.getKeyCode() == reverseKey) {
                if (reverseButton.isEnabled())
                    {
                        reverseButton.doClick();
                    }
            } else if (e.getKeyCode() == stopKey) {
                if (speedSlider.isEnabled() || 
                    (speedSpinner!=null && speedSpinner.isEnabled()) )
                    {
                        stop();
                    }
            } else if (e.getKeyCode() == idleKey) {
                if (speedSlider.isEnabled() || 
                    (speedSpinner!=null && speedSpinner.isEnabled()))
                    {
                        speedSlider.setValue(0);
                    }
            }
        }
    }
    
    
    // update the state of this panel if any of the properties change
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals("SpeedSetting")) {
            internalAdjust=true;
            float speed=((Float) e.getNewValue()).floatValue();
            // multiply by MAX_SPEED, and round to find the new
            //slider setting.
            int newSliderSetting = java.lang.Math.round(speed * maxSpeed) ;
            if (log.isDebugEnabled()) {log.debug( "propertyChange: new speed float: " + speed + " slider pos: " + newSliderSetting ) ;}
            speedSlider.setValue( newSliderSetting );
            if(speedSpinner!=null)
                speedSpinner.setValue(Integer.valueOf(newSliderSetting));
        } else if (e.getPropertyName().equals("SpeedSteps")) {
            int steps=((Integer) e.getNewValue()).intValue();
            setSpeedStepsMode(steps);
        } else if (e.getPropertyName().equals("IsForward")) {
            boolean Forward=((Boolean) e.getNewValue()).booleanValue();
            setIsForward(Forward);
        }
    }
    
    /**
     * Handle the selection from the popup menu.
     * @param e The ActionEvent causing the action.
     */
    public void actionPerformed(ActionEvent e)
    {
        ControlPanelPropertyEditor editor =
            new ControlPanelPropertyEditor(this);
        editor.setVisible(true);
    }
    
    /**
     * Configure the active Speed Step modes based on what is supported by 
     * the DCC system
     */
    private void configureAvailableSpeedStepModes() {
	int modes = jmri.InstanceManager.throttleManagerInstance()
            .supportedSpeedModes();
	if((modes & DccThrottle.SpeedStepMode128) != 0) {
            SpeedStep128Button.setEnabled(true);
	}else { 
            SpeedStep128Button.setEnabled(false);
	}
	if((modes & DccThrottle.SpeedStepMode28) != 0) {
            SpeedStep28Button.setEnabled(true);
	}else { 
            SpeedStep28Button.setEnabled(false);
	}
	if((modes & DccThrottle.SpeedStepMode27) != 0) {
            SpeedStep27Button.setEnabled(true);
	}else { 
            SpeedStep27Button.setEnabled(false);
	}
	if((modes & DccThrottle.SpeedStepMode14) != 0) {
            SpeedStep14Button.setEnabled(true);
	}else { 
            SpeedStep14Button.setEnabled(false);
	}
    }
    
    /**
     * A PopupListener to handle mouse clicks and releases. Handles
     * the popup menu.
     */
    static class PopupListener extends MouseInputAdapter
    {
        
	JPopupMenu _menu;
	JInternalFrame parentFrame;
        
	PopupListener(JPopupMenu menu,JInternalFrame parent){
            parentFrame = parent;
            _menu=menu;
	}
        
        /**
         * If the event is the popup trigger, which is dependent on
         * the platform, present the popup menu. Otherwise change
         * the state of the function depending on the locking state
         * of the button.
         * @param e The MouseEvent causing the action.
         */
        public void mousePressed(MouseEvent e)
        {
            if (log.isDebugEnabled()) log.debug("pressed "+(e.getModifiers() & MouseEvent.BUTTON1_MASK)+" "+e.isPopupTrigger()
                                                +" "+(e.getModifiers() & (MouseEvent.ALT_MASK+ MouseEvent.META_MASK+MouseEvent.CTRL_MASK))
                                                +(" "+MouseEvent.ALT_MASK+"/"+MouseEvent.META_MASK+"/"+MouseEvent.CTRL_MASK));
            if (e.isPopupTrigger() && parentFrame.isSelected())
                {
                    try {
                        _menu.show(e.getComponent(),  
                                   e.getX(), e.getY());
                    } catch ( java.awt.IllegalComponentStateException cs ) {
			// Message sent to a hidden component, so we need 
                    }
                    e.consume();
                }
        }
        
        /**
         * If the event is the popup trigger, which is dependent on
         * the platform, present the popup menu. Otherwise change
         * the state of the function depending on the locking state
         * of the button.
         * @param e The MouseEvent causing the action.
         */
        public void mouseReleased(MouseEvent e)
        {
            if (log.isDebugEnabled()) log.debug("released "+(e.getModifiers() & MouseEvent.BUTTON1_MASK)+" "+e.isPopupTrigger()
                                                +" "+(e.getModifiers() & (MouseEvent.ALT_MASK+InputEvent.META_MASK+MouseEvent.CTRL_MASK)));
            if (e.isPopupTrigger())
                {
                    try {
                        _menu.show(e.getComponent(),   
                                   e.getX(), e.getY());
                    } catch ( java.awt.IllegalComponentStateException cs ) {
			// Message sent to a hidden component, so we need 
                    }
                    
                    e.consume();
                }
        }
    }
    
    /**
     *  Collect the prefs of this object into XML Element
     *  <ul>
     *    <li> Window prefs
     *  </ul>
     *
     *
     * @return    the XML of this object.
     */
    public Element getXml()
    {
        Element me = new Element("ControlPanel");
        me.setAttribute("displaySpeedSlider",String.valueOf(this._displaySlider));		
        me.setAttribute("speedMode",String.valueOf(this._speedStepMode));
        me.setAttribute("trackSlider", String.valueOf(this.trackSlider));
        me.setAttribute("trackSliderMinInterval", String.valueOf(this.trackSliderMinInterval));
        //Element window = new Element("window");
        java.util.ArrayList<Element> children = new java.util.ArrayList<Element>(1);
        children.add(WindowPreferences.getPreferences(this));
        me.setContent(children);
        return me;
    }
    
    /**
     *  Set the preferences based on the XML Element.
     *  <ul>
     *    <li> Window prefs
     *  </ul>
     *
     *
     * @param  e  The Element for this object.
     */
    public void setXml(Element e)
    {	
        internalAdjust=true;
        try {			
            this.setSpeedController(e.getAttribute("displaySpeedSlider").getIntValue());
        } catch (org.jdom.DataConversionException ex)
            {
                log.error("DataConverstionException in setXml: "+ex);
            } catch (Exception em)
            {
                // in this case, recover by displaying the speed slider.
                this.setSpeedController(SLIDERDISPLAY);
            }
        try {
            // Set the speed steps in the GUI from the xml
            setSpeedStepsMode(e.getAttribute("speedMode").getIntValue());
            // Try to set the throttle speed steps
            if(throttle!=null) {
                throttle.setSpeedStepMode(e.getAttribute("speedMode").getIntValue());
            } else {
                // save value to do it later
                _speedStepModeForLater = e.getAttribute("speedMode").getIntValue();
            }
        } catch (org.jdom.DataConversionException ex)
            {
                log.error("DataConverstionException in setXml: "+ex);
            } catch (Exception em)
            {
                // in this case, recover by defaulting to 128 speed step mode.
                setSpeedStepsMode(DccThrottle.SpeedStepMode128);
                if(throttle!=null)
                    throttle.setSpeedStepMode(DccThrottle.SpeedStepMode128);
            }
        Attribute tsAtt = e.getAttribute("trackSlider");
        if (tsAtt!=null) {
            try {
                trackSlider = tsAtt.getBooleanValue();
            } catch (org.jdom.DataConversionException ex) {
                trackSlider = trackSliderDefault;
            }
        } else {
            trackSlider = trackSliderDefault;
        }
        Attribute tsmiAtt = e.getAttribute("trackSliderMinInterval");
        if (tsmiAtt!=null) {
            try {
                trackSliderMinInterval = tsmiAtt.getLongValue();
            } catch (org.jdom.DataConversionException ex) {
                trackSliderMinInterval = trackSliderMinIntervalDefault;
            }
            if (trackSliderMinInterval < trackSliderMinIntervalMin) {
                trackSliderMinInterval = trackSliderMinIntervalMin;
            } else if (trackSliderMinInterval > trackSliderMinIntervalMax) {
                trackSliderMinInterval = trackSliderMinIntervalMax;
            }
        } else {
            trackSliderMinInterval = trackSliderMinIntervalDefault;
        }
        Element window = e.getChild("window");
        WindowPreferences.setPreferences(this, window);
    }

	public void notifyAddressChosen(int newAddress, boolean isLong) {	
	}

	public void notifyAddressReleased(int address, boolean isLong) {
		this.setEnabled(false);
        if (throttle != null)
        	throttle.removePropertyChangeListener(this);
        throttle = null;		
	}

	public void notifyAddressThrottleFound(DccThrottle t) {
        if(log.isDebugEnabled()) log.debug("control panel received new throttle");
        this.throttle = t;
        this.setEnabled(true);
        this.setIsForward(throttle.getIsForward());
        this.setSpeedValues(throttle.getSpeedIncrement(),
                            throttle.getSpeedSetting());
        // Throttle now available so set speed steps from saved xml value
        if (_speedStepModeForLater != 0) {
            this.throttle.setSpeedStepMode(_speedStepModeForLater);
            _speedStepModeForLater = 0;
        }

        // Set speed steps
        internalAdjust=true;
        this.setSpeedStepsMode(throttle.getSpeedStepMode());

        this.throttle.addPropertyChangeListener(this);
        if(log.isDebugEnabled()) {
           jmri.DccLocoAddress Address=(jmri.DccLocoAddress)throttle.getLocoAddress();
           log.debug("new address is " +Address.toString());
        }		
	}
	    
    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ControlPanel.class.getName());    	
}

