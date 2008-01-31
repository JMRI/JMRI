package jmri.jmrit.throttle;

import jmri.DccThrottle;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.PowerManager;
import jmri.ThrottleListener;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import javax.swing.JButton;

import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.powerpanel.PowerPane;
import jmri.util.JmriJFrame;

import org.jdom.Element;

/**
 *  A JFrame to contain throttle elements such as speed control, address
 *  chooser, function panel, and maybe others. <p>
 *
 *  This class requests a DccThrottle and calls methods in that object as
 *  directed by the interface.
 *
 * @author     Glen Oberhauser
 * @version    $Revision: 1.33 $
 */
/**
 * @author DSM
 *
 */
public class ThrottleFrame extends JmriJFrame implements AddressListener, ThrottleListener, java.beans.PropertyChangeListener
{
	private final Integer PANEL_LAYER = new Integer(1);
	private static int NEXT_FRAME_KEY = KeyEvent.VK_RIGHT;
	private static int PREV_FRAME_KEY = KeyEvent.VK_LEFT;

	private static int ADDRESS_PANEL_INDEX = 0;
	private static int CONTROL_PANEL_INDEX = 1;
	private static int FUNCTION_PANEL_INDEX = 2;
	private static int NUM_FRAMES = 3;

	private JInternalFrame[] frameList;
	private int activeFrame;

	private ControlPanel controlPanel;
	private FunctionPanel functionPanel;
	private AddressPanel addressPanel;

	private JCheckBoxMenuItem viewControlPanel;
	private JCheckBoxMenuItem viewFunctionPanel;
	private JCheckBoxMenuItem viewAddressPanel;
    
    private DccThrottle throttle;

    PowerPane powerControl  = new PowerPane();
    PowerManager powerMgr = null;
    JButton powerLight;
    // Load the power lights as icons to be placed in an invisible JButton so the light 
    // can be clicked to change the power status
    NamedIcon powerOnIcon = new NamedIcon("resources/GreenPowerLED.gif", "resources/GreenPowerLED.gif");
    NamedIcon powerOffIcon = new NamedIcon("resources/RedPowerLED.gif", "resources/RedPowerLED.gif");
    NamedIcon powerXIcon = new NamedIcon("resources/YellowPowerLED.gif", "resources/YellowPowerLED.gif");
    
	/**
	 *  Default constructor
	 */
	public ThrottleFrame()
	{
	    super();
        powerMgr = InstanceManager.powerManagerInstance();
        if (powerMgr == null) {
            log.info("No power manager instance found, panel not active");
        }
        else powerMgr.addPropertyChangeListener(this);
        initGUI();
	}

    /**
     * Get notification that a throttle has been found as you requested.
     * @param t An instantiation of the DccThrottle with the address requested.
     */
    public void notifyThrottleFound(DccThrottle t)
	{
    	if (throttle == null){
    		this.throttle = t;
    		addressPanel.notifyThrottleFound(t);
    		controlPanel.notifyThrottleFound(t);
    		functionPanel.notifyThrottleFound(t);
    	} else {
    		log.debug("Notify control panel to use consist throttle");
    		controlPanel.notifyThrottleFound(t);
    		addressPanel.notifyConsistThrottleFound(t);
    	}
	}

    /**
     * Receive notification that a new address has been selected.
     * @param address The address that is now selected.
     */
    public void notifyAddressChosen(int address, boolean isLong)
	{
		boolean requestOK =
			InstanceManager.throttleManagerInstance().requestThrottle(address, isLong, this);
		if (!requestOK)
		{
			JOptionPane.showMessageDialog(this, "Address in use by another throttle.");
		}
	}

	/**
	 * Receive notification that an address has been released or dispatched
	 * @param address The address released/dispatched
	 */
	public void notifyAddressReleased(int address, boolean isLong)
	{
		InstanceManager.throttleManagerInstance().cancelThrottleRequest(address, this);
		controlPanel.notifyThrottleDisposed();
		functionPanel.notifyThrottleDisposed();
		addressPanel.notifyThrottleDisposed();
		throttle = null;
	}

	/**
	 *  Place and initialize the GUI elements.
	 *  <ul>
	 *    <li> ControlPanel
	 *    <li> FunctionPanel
	 *    <li> AddressPanel
	 *    <li> JMenu
	 *  </ul>
	 *
	 */
	private void initGUI()
	{
		setTitle("Throttle");
		JDesktopPane desktop = new JDesktopPane();
		this.setContentPane(desktop);
		this.addWindowListener(
			new WindowAdapter()
			{
				public void windowClosing(WindowEvent e)
				{
					ThrottleFrame me = (ThrottleFrame)e.getSource();
					ThrottleFrameManager.instance().requestThrottleFrameDestruction(me);
				}
			});

		initializeMenu();

		FrameListener frameListener = new FrameListener();

		controlPanel = new ControlPanel();
		controlPanel.setResizable(true);
		controlPanel.setClosable(true);
		controlPanel.setIconifiable(true);
		controlPanel.setTitle("Control Panel");
                controlPanel.pack();
		controlPanel.setVisible(true);
		controlPanel.setEnabled(false);
		controlPanel.addInternalFrameListener(frameListener);

		functionPanel = new FunctionPanel();
		functionPanel.setResizable(true);
		functionPanel.setClosable(true);
		functionPanel.setIconifiable(true);
		functionPanel.setTitle("Function Panel");

                // assumes button width of 54, height of 30 (set in class FunctionButton) with
                // horiz and vert gaps of 5 each (set in FunctionPanel class)
                // with 3 buttons across and 6 rows high
        int width = 3*(FunctionButton.BUT_WDTH) + 2*3*5; 		// = 192
        int height = 6*(FunctionButton.BUT_HGHT) + 2*6*5 +10;	// = 240 (but there seems to be another 10 needed for some LAFs)
        functionPanel.setSize(width, height);
		functionPanel.setLocation(controlPanel.getWidth(), 0);
		functionPanel.setVisible(true);
		functionPanel.setEnabled(false);
		functionPanel.addInternalFrameListener(frameListener);

		addressPanel = new AddressPanel();
		addressPanel.setResizable(true);
		addressPanel.setClosable(true);
		addressPanel.setIconifiable(true);
		addressPanel.setTitle("Address Panel");
                addressPanel.pack();
//                if (addressPanel.getWidth()<functionPanel.getWidth()) {addressPanel.setSize(functionPanel.getWidth(),addressPanel.getHeight());}
		addressPanel.setLocation(controlPanel.getWidth(), functionPanel.getHeight());
		addressPanel.setVisible(true);
		addressPanel.addInternalFrameListener(frameListener);
		addressPanel.addAddressListener(this);

                if (controlPanel.getHeight() < functionPanel.getHeight() + addressPanel.getHeight())
                   {controlPanel.setSize(controlPanel.getWidth(),functionPanel.getHeight() + addressPanel.getHeight());}
                if (controlPanel.getHeight() > functionPanel.getHeight() + addressPanel.getHeight())
                   {addressPanel.setSize(addressPanel.getWidth(),controlPanel.getHeight()-functionPanel.getHeight());}
                if (functionPanel.getWidth() < addressPanel.getWidth())
                   {functionPanel.setSize(addressPanel.getWidth(),functionPanel.getHeight());}
   		desktop.add(controlPanel, PANEL_LAYER);
		desktop.add(functionPanel, PANEL_LAYER);
		desktop.add(addressPanel, PANEL_LAYER);

		frameList = new JInternalFrame[NUM_FRAMES];
		frameList[ADDRESS_PANEL_INDEX] = addressPanel;
		frameList[CONTROL_PANEL_INDEX] = controlPanel;
		frameList[FUNCTION_PANEL_INDEX] = functionPanel;
		activeFrame = ADDRESS_PANEL_INDEX;

		desktop.setPreferredSize(new Dimension(
                     Math.max(controlPanel.getWidth()+functionPanel.getWidth(),controlPanel.getWidth()+addressPanel.getWidth()),
                     Math.max(addressPanel.getHeight()+functionPanel.getHeight(),controlPanel.getHeight())));

		KeyListenerInstaller.installKeyListenerOnAllComponents(
						new FrameCyclingKeyListener(), this);

		try
		{
			addressPanel.setSelected(true);

		}
		catch (java.beans.PropertyVetoException ex)
		{
			log.error("Error selecting InternalFrame:" + ex);
		}

	}


	/**
	 *  Set up View, Edit and Power Menus
	 */
	private void initializeMenu()
	{
		JMenu viewMenu = new JMenu("View");
		viewAddressPanel = new JCheckBoxMenuItem("Address Panel");
		viewAddressPanel.setSelected(true);
		viewAddressPanel.addItemListener(
			new ItemListener()
			{
				public void itemStateChanged(ItemEvent e)
				{
					addressPanel.setVisible(e.getStateChange() == ItemEvent.SELECTED);
				}
			});

		viewControlPanel = new JCheckBoxMenuItem("Control Panel");
		viewControlPanel.setSelected(true);
		viewControlPanel.addItemListener(
			new ItemListener()
			{
				public void itemStateChanged(ItemEvent e)
				{
					controlPanel.setVisible(e.getStateChange() == ItemEvent.SELECTED);
				}
			});
		viewFunctionPanel = new JCheckBoxMenuItem("Function Panel");
		viewFunctionPanel.setSelected(true);
		viewFunctionPanel.addItemListener(
			new ItemListener()
			{
				public void itemStateChanged(ItemEvent e)
				{
					functionPanel.setVisible(e.getStateChange() == ItemEvent.SELECTED);
				}
			});
        
		viewMenu.add(viewAddressPanel);
		viewMenu.add(viewControlPanel);
		viewMenu.add(viewFunctionPanel);

		JMenu editMenu = new JMenu("Edit");
		JMenuItem preferencesItem = new JMenuItem("Frame Properties");
		editMenu.add(preferencesItem);
		preferencesItem.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					editPreferences();
				}
			});
        
		this.setJMenuBar(new JMenuBar());
		this.getJMenuBar().add(viewMenu);
		this.getJMenuBar().add(editMenu);

        if (powerMgr !=null) {
            JMenu powerMenu = new JMenu("  Power:");
            JMenuItem powerOn = new JMenuItem("Power On");
            powerMenu.add(powerOn);
            powerOn.addActionListener(
                    new ActionListener() {
                        public void actionPerformed(ActionEvent e){
                            powerControl.onButtonPushed();
                        }
                    });
            
            JMenuItem powerOff = new JMenuItem("Power Off");
            powerMenu.add(powerOff);
            powerOff.addActionListener(
                    new ActionListener() {
                        public void actionPerformed(ActionEvent e){
                            powerControl.offButtonPushed();
                        }
                    });
            
            this.getJMenuBar().add(powerMenu);
            powerLight = new JButton();
            setPowerIcons();
            // make the button itself invisible, just display the power LED
            powerLight.setBorderPainted(false);
            powerLight.setContentAreaFilled(false);
            powerLight.setFocusPainted(false);
            this.getJMenuBar().add(powerLight);
            powerLight.addActionListener(
                    new ActionListener() {
                        public void actionPerformed(ActionEvent e){
                            try {
                                if (powerMgr.getPower()==PowerManager.ON) powerControl.offButtonPushed();
                                else if (powerMgr.getPower()==PowerManager.OFF)powerControl.onButtonPushed();
                                    else if (powerMgr.getPower()==PowerManager.UNKNOWN)powerControl.offButtonPushed();
                            } catch (JmriException ex) {
                                powerLight.setIcon(powerXIcon);
                            }
                        }
                    }
            );
        }
	}

	private void editPreferences()
	{
		ThrottleFramePropertyEditor editor =
			ThrottleFrameManager.instance().getThrottleFrameEditor();
		editor.setThrottleFrame(this);
		//editor.setLocation(this.getLocationOnScreen());
		editor.setLocationRelativeTo(this);
		editor.setVisible(true);
	}

	/**
	 *  Handle my own destruction.
	 *  <ol>
	 *    <li> dispose of sub windows.
	 *    <li> notify my manager of my demise.
	 *  </ol>
	 *
	 */
	public void dispose()
	{
		// check for any special disposing in InternalFrames
		controlPanel.destroy();
		functionPanel.destroy();
		// dispose of this last because it will release and destroy throttle.
		addressPanel.destroy();

        if (powerMgr!=null) powerMgr.removePropertyChangeListener(this);
        
		// Handle disposing of the throttle
		if (throttle != null)
		{
		    DccLocoAddress l = (DccLocoAddress) throttle.getLocoAddress();
			InstanceManager.throttleManagerInstance().
				cancelThrottleRequest(l.getNumber(), this);
		}

		super.dispose();
	}


	/**
	 *  A KeyAdapter that listens for the key that cycles through
	 * the JInternalFrames.
	 *
	 * @author     glen
	 */
	class FrameCyclingKeyListener extends KeyAdapter
	{
		/**
		 *  Description of the Method
		 *
		 * @param  e  Description of the Parameter
		 */
		public void keyPressed(KeyEvent e)
		{
			if (e.isControlDown() && e.getKeyCode() == NEXT_FRAME_KEY)
			{
				try
				{
					activeFrame = (activeFrame + 1) % NUM_FRAMES;
					frameList[activeFrame].setSelected(true);
				}
				catch (java.beans.PropertyVetoException ex)
				{
					log.warn("Exception selecting internal frame:" + ex);
				}

			}
			else if (e.isControlDown() && e.getKeyCode() == PREV_FRAME_KEY)
			{
				try
				{
					activeFrame--;
					if (activeFrame < 0)
					{
						activeFrame = NUM_FRAMES - 1;
					}
					frameList[activeFrame].setSelected(true);
				}
				catch (java.beans.PropertyVetoException ex)
				{
					log.warn("Exception selecting internal frame:" + ex);
				}
			}
		}
	}

    /**
     *  implement a property change listener to monitor the power state and change
     *  the power LED displayed as appropriate
     */
    public void propertyChange(java.beans.PropertyChangeEvent ev) {
        setPowerIcons();
    }
 
    /**
     *  change the power LED displayed as appropriate and set corresponding tooltip
     *  
     */
    public void setPowerIcons() {
        if (powerMgr==null) return;
        try {
            if (powerMgr.getPower()==PowerManager.ON) {
                powerLight.setIcon(powerOnIcon);
                powerLight.setToolTipText("Layout Power On.  Click light to turn off, or use Power menu");
            }
            else if (powerMgr.getPower()==PowerManager.OFF) {
                powerLight.setIcon(powerOffIcon);
                powerLight.setToolTipText("Layout Power Off.  Click light to turn on, or use Power menu");
            }
            else if (powerMgr.getPower()==PowerManager.UNKNOWN) {
                powerLight.setIcon(powerXIcon);
                powerLight.setToolTipText("Layout Power state unknown.  Click light to turn off, or use Power menu");
            }
            else {
                powerLight.setIcon(powerXIcon);
                powerLight.setToolTipText("Layout Power state unknown.  Click light to turn off, or use Power menu");
                log.error("Unexpected state value: +"+powerMgr.getPower());
            }
        } catch (JmriException ex) {
            powerLight.setIcon(powerXIcon);
            powerLight.setToolTipText("Layout Power state unknown.  Click light to turn off, or use Power menu");
        }
    }
    
    
	/**
	 *  An extension of InternalFrameAdapter for listening to the closing of of
	 *  this frame's internal frames.
	 *
	 * @author     glen
	 */
	class FrameListener extends InternalFrameAdapter
	{
		/**
		 *  Listen for the closing of an internal frame and set the "View" menu
		 *  appropriately. Then hide the closing frame
		 *
		 * @param  e  The InternalFrameEvent leading to this action
		 */
		public void internalFrameClosing(InternalFrameEvent e)
		{
			if (e.getSource() == controlPanel)
			{
				viewControlPanel.setSelected(false);
				controlPanel.setVisible(false);
			}
			else if (e.getSource() == addressPanel)
			{
				viewAddressPanel.setSelected(false);
				addressPanel.setVisible(false);
			}
			else if (e.getSource() == functionPanel)
			{
				viewFunctionPanel.setSelected(false);
				functionPanel.setVisible(false);
			}
		}

		/**
		 *  Listen for the activation of an internal frame record this property for
		 *  coorect processing of the frame cycling key.
		 *
		 * @param  e  The InternalFrameEvent leading to this action
		 */
		public void internalFrameActivated(InternalFrameEvent e)
		{
			if (e.getSource() == controlPanel)
			{
				activeFrame = CONTROL_PANEL_INDEX;
			}
			else if (e.getSource() == addressPanel)
			{
				activeFrame = ADDRESS_PANEL_INDEX;
			}
			else if (e.getSource() == functionPanel)
			{
				activeFrame = FUNCTION_PANEL_INDEX;
			}
		}

	}


	/**
	 *  Collect the prefs of this object into XML Element
	 *  <ul>
	 *    <li> Window prefs
	 *    <li> ControlPanel
	 *    <li> FunctionPanel
	 *    <li> AddressPanel
	 *  </ul>
	 *
	 *
	 * @return    the XML of this object.
	 */
	public Element getXml()
	{
		Element me = new Element("ThrottleFrame");
		me.setAttribute("title", this.getTitle());
		java.util.ArrayList children =
				new java.util.ArrayList(1);
		WindowPreferences wp = new WindowPreferences();

		children.add(wp.getPreferences(this));
		children.add(controlPanel.getXml());
		children.add(functionPanel.getXml());
		children.add(addressPanel.getXml());
		me.setContent(children);
		return me;
	}

	/**
	 *  Set the preferences based on the XML Element.
	 *  <ul>
	 *    <li> Window prefs
	 *	  <li> Frame title
	 *    <li> ControlPanel
	 *    <li> FunctionPanel
	 *    <li> AddressPanel
	 *  </ul>
	 *
	 *
	 * @param  e  The Element for this object.
	 */
	public void setXml(Element e)
	{
		this.setTitle(e.getAttribute("title").getValue());
		Element window = e.getChild("window");
		WindowPreferences wp = new WindowPreferences();
		wp.setPreferences(this, window);
		Element controlPanelElement = e.getChild("ControlPanel");
		controlPanel.setXml(controlPanelElement);
		Element functionPanelElement = e.getChild("FunctionPanel");
		functionPanel.setXml(functionPanelElement);
		Element addressPanelElement = e.getChild("AddressPanel");
		addressPanel.setXml(addressPanelElement);
	}

	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ThrottleFrame.class.getName());

}

