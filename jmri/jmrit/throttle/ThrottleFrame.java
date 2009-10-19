package jmri.jmrit.throttle;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.LocoAddress;
import jmri.Throttle;
import jmri.ThrottleListener;
import jmri.jmrit.XmlFile;
import jmri.jmrit.roster.RosterEntry;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.ProcessingInstruction;

// Should be named ThrottlePanel but was already existing with that name and don't want to break dependancies (particularly in Jython code)
public class ThrottleFrame extends JDesktopPane  implements AddressListener, ThrottleListener, ComponentListener
{
    private final Integer BACKPANEL_LAYER = new Integer(Integer.MIN_VALUE);
    private final Integer PANEL_LAYER = new Integer(1);
    private static int NEXT_FRAME_KEY = KeyEvent.VK_RIGHT;
    private static int PREV_FRAME_KEY = KeyEvent.VK_LEFT;
    
    private static int ADDRESS_PANEL_INDEX = 0;
    private static int CONTROL_PANEL_INDEX = 1;
    private static int FUNCTION_PANEL_INDEX = 2;
    private static int NUM_FRAMES = 3;
    
    private JInternalFrame[] frameList;
    private int activeFrame;
    
    private ThrottleWindow throttleWindow;

    private ControlPanel controlPanel;
    private FunctionPanel functionPanel;
    private AddressPanel addressPanel;
    private BackgroundPanel backgroundPanel;
    
    private DccThrottle throttle = null;
    
    private String _throttlesBasePath = XmlFile.prefsDir()+"throttle"+File.separator ;
    private String title; 
            
    public ThrottleFrame(ThrottleWindow tw)
    {
        super();
        throttleWindow = tw;
        initGUI();
		jmri.jmrit.throttle.ThrottleFrameManager.instance().getThrottlesList().addThrottleFrame(this);
    }
    
    public void setVisible(boolean b)
    {
    	super.setVisible(b);
    	throttleWindow.setVisible(b);
    }
    
    public ControlPanel getControlPanel() { return controlPanel; }
    public FunctionPanel getFunctionPanel() { return functionPanel; }
    public AddressPanel getAddressPanel() { return addressPanel; }
    
    public RosterEntry getRosterEntry() {
    	return addressPanel.getRosterEntry();
    }
    
    public LocoAddress getLocoAddress() {
    	if (throttle == null) return null ;
    	return throttle.getLocoAddress();
    }
    
    public Throttle getThrottle() {
    	return throttle;
    }
    
    public void toFront() {
    	if (throttleWindow == null) return;
    	throttleWindow.toFront(title);
    }
    
	public void setTitle(String txt) {
		title = txt;
	}
	public String getTitle() {
		return title;
	}
	
    /**
     * Get notification that a throttle has been found as you requested.
     * @param t An instantiation of the DccThrottle with the address requested.
     */
    public void notifyThrottleFound(DccThrottle t)
    {
    	if (throttle == null){
            throttle = t;
            addressPanel.notifyThrottleFound(t);
            controlPanel.notifyThrottleFound(t);
            functionPanel.notifyThrottleFound(t);
            if (backgroundPanel != null)
            	backgroundPanel.notifyThrottleFound(t);
        	if ((jmri.jmrit.throttle.ThrottleFrameManager.instance().getThrottlesPreferences().isUsingExThrottle()) &&
            		(jmri.jmrit.throttle.ThrottleFrameManager.instance().getThrottlesPreferences().isAutoLoading()) && 
            		(addressPanel !=null) && (addressPanel.getRosterEntry() != null ))
            		loadThrottle( (_throttlesBasePath+ addressPanel.getRosterEntry().getId()).trim() +".xml" );         
    	} else {
            log.debug("Notify control panel to use consist throttle");
            controlPanel.notifyThrottleFound(t);
            addressPanel.notifyConsistThrottleFound(t);
            if (backgroundPanel != null)
            	backgroundPanel.notifyConsistThrottleFound(t);
    	}
    	setFrameTitle();
    	jmri.jmrit.throttle.ThrottleFrameManager.instance().getThrottlesList().repaint();
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
    		JOptionPane.showMessageDialog(this, "Address in use by another throttle.");    		
    }

    /**
     * Receive notification that an address has been released or dispatched
     * @param address The address released/dispatched
     */
    public void notifyAddressReleased(int address, boolean isLong)
    {      	
    	throttleWindow.setTitle("Throttle");
        InstanceManager.throttleManagerInstance().cancelThrottleRequest(address, this);
        controlPanel.notifyThrottleDisposed();
        functionPanel.notifyThrottleDisposed();
        addressPanel.notifyThrottleDisposed();
        if (backgroundPanel != null)
        	backgroundPanel.notifyThrottleDisposed();
        throttle = null;
    	jmri.jmrit.throttle.ThrottleFrameManager.instance().getThrottlesList().repaint();
    }
    
    private void saveThrottle(String sfile) {
    	// Save throttle: title / window position
    	// as strongly linked to extended throttles and roster presence, do not save function buttons and background window as they're stored in the roster entry
		XmlFile xf = new XmlFile(){};   // odd syntax is due to XmlFile being abstract
		xf.makeBackupFile(sfile);
		File file=new File(sfile);
		try {
			//The file does not exist, create it before writing
			File parentDir=file.getParentFile();
			if(!parentDir.exists())
				parentDir.mkdir();
			file.createNewFile();
		} catch (Exception exp) {
			log.error("Exception while writing the throttle file, may not be complete: "+exp);
		}
   
		try {
			Element root = new Element("throttle-config");
			Document doc = XmlFile.newDocument(root, XmlFile.dtdLocation+"throttle-config.dtd");
			// add XSLT processing instruction
			// <?xml-stylesheet type="text/xsl" href="XSLT/throttle.xsl"?>
			java.util.Map<String,String> m = new java.util.HashMap<String,String>();
			m.put("type", "text/xsl");
			m.put("href", jmri.jmrit.XmlFile.xsltLocation+"throttle.xsl");
			ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m);
			doc.addContent(0,p);
			Element throttleElement = getXml();
			// don't save the loco address or consist address
			throttleElement.getChild("AddressPanel").removeChild("locoaddress");
			throttleElement.getChild("AddressPanel").removeChild("locoaddress");
		    // don't save function buttons labels, they're in roster entry
			throttleElement.getChild("FunctionPanel").removeChildren("FunctionButton");
			
			root.setContent(throttleElement);
			xf.writeXML(file, doc);
		}
		catch (Exception ex){
    		log.warn("Exception in storing throttles preferences xml: "+ex);
    	}
	}
    
    private void loadThrottle(String sfile) {
		try {
			XmlFile xf = new XmlFile(){};   // odd syntax is due to XmlFile being abstract
			File f=new File(sfile);
			Element root = xf.rootFromFile(f);
			Element conf = root.getChild("ThrottleFrame");
			setXml(conf);
		} catch (Exception ex) {
			if (log.isDebugEnabled())
				log.debug("Loading throttle exception ",ex);
		}
    	checkPosition(controlPanel);
		checkPosition(functionPanel);
		checkPosition(addressPanel);
		return ;
	}

	/**
     * Notify that a new throttle roster entry is to be used.
     * Works the same way as selecting a roster entry from the 
     * address menu.
     * @param entry The RosterEntry to use for this throttle
     */
    public void notifyRosterEntryChosen(RosterEntry entry){
    	addressPanel.setRosterEntry(entry);
    	notifyAddressChosen(entry.getDccLocoAddress().getNumber(), entry.getDccLocoAddress().isLongAddress());
   }
    
	private void setTransparent(JComponent jcomp)
	{
		jcomp.setOpaque(false);
		Component[] comps = jcomp.getComponents();
		JComponent jcmp2;
		for (int i=0; i<comps.length; i++)
		{
			try
			{
				jcmp2 = (JComponent) comps[i];
				setTransparent( jcmp2 );
			}
			catch(Exception e)
			{ // Do nothing, just go on
			}
		}
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
        functionPanel.setAddressPanel(addressPanel); // so the function panel can get access to the roster
        
        if (controlPanel.getHeight() < functionPanel.getHeight() + addressPanel.getHeight())
            {controlPanel.setSize(controlPanel.getWidth(),functionPanel.getHeight() + addressPanel.getHeight());}
        if (controlPanel.getHeight() > functionPanel.getHeight() + addressPanel.getHeight())
            {addressPanel.setSize(addressPanel.getWidth(),controlPanel.getHeight()-functionPanel.getHeight());}
        if (functionPanel.getWidth() < addressPanel.getWidth())
            {functionPanel.setSize(addressPanel.getWidth(),functionPanel.getHeight());}
        add(controlPanel, PANEL_LAYER);
        add(functionPanel, PANEL_LAYER);
        add(addressPanel, PANEL_LAYER);

        if ( jmri.jmrit.throttle.ThrottleFrameManager.instance().getThrottlesPreferences().isUsingExThrottle() ) {
        	if ( jmri.jmrit.throttle.ThrottleFrameManager.instance().getThrottlesPreferences().isUsingTransparentCtl() ) {
        		setTransparent(functionPanel);
        		setTransparent(addressPanel);
        		setTransparent(controlPanel);
        	}
        	if ( jmri.jmrit.throttle.ThrottleFrameManager.instance().getThrottlesPreferences().isUsingRosterImage() ) {
        		backgroundPanel = new BackgroundPanel();
        		backgroundPanel.setAddressPanel(addressPanel); // reusing same way to do it than existing thing in functionPanel
        		addComponentListener(backgroundPanel); // backgroudPanel warned when desktop resized
        		add(backgroundPanel, BACKPANEL_LAYER);
        	}
        	addComponentListener(this); // to force sub windows repositionning
        }

        frameList = new JInternalFrame[NUM_FRAMES];
        frameList[ADDRESS_PANEL_INDEX] = addressPanel;
        frameList[CONTROL_PANEL_INDEX] = controlPanel;
        frameList[FUNCTION_PANEL_INDEX] = functionPanel;
        activeFrame = ADDRESS_PANEL_INDEX;
        
        setPreferredSize(new Dimension( Math.max(controlPanel.getWidth()+functionPanel.getWidth(),controlPanel.getWidth()+addressPanel.getWidth()),
                                        Math.max(addressPanel.getHeight()+functionPanel.getHeight(),controlPanel.getHeight())) );
        
        KeyListenerInstaller.installKeyListenerOnAllComponents(new FrameCyclingKeyListener(), this);

        try {
        	addressPanel.setSelected(true);
        }
        catch (java.beans.PropertyVetoException ex) {
        	log.error("Error selecting InternalFrame:" + ex);
        }
    }

    // make sure components are inside this frame bounds
	private void checkPosition(JComponent comp)	{ 	
		if ( (this.getWidth()<1) || (this.getHeight()<1)) return;
		
		Rectangle pos = comp.getBounds();
		
		if (pos.width > this.getWidth())
			pos.width = this.getWidth() - 8;
		if (pos.height > this.getHeight())
			pos.height = this.getHeight() - 48;
		
		if ( ( pos.x < 0 ) || (pos.x + pos.width > this.getWidth()) )
			pos.x = this.getWidth() - pos.width - 8;
		if (pos.x < 1)
			pos.x = 10;
		if ( ( pos.y < 0 ) || (pos.y + pos.height > this.getHeight()) )
			pos.y = this.getHeight() - pos.height - 48;
		if (pos.y < 1)
			pos.y = 10;
		
		comp.setBounds(pos);
	}
	
    // overwritten in order to be able to check sub windows positions
    public void pack() {
    	checkPosition(controlPanel);
		checkPosition(functionPanel);
		checkPosition(addressPanel);
    }

    /**
	 * Handle my own destruction.
	 * <ol>
	 * <li> dispose of sub windows.
	 * <li> notify my manager of my demise.
	 * </ol>
	 * 
	 */
    public void dispose() {
    	log.debug("Disposing "+getTitle());
		jmri.jmrit.throttle.ThrottleFrameManager.instance().getThrottlesList().removeThrottleFrame(this);
        // check for any special disposing in InternalFrames
        controlPanel.destroy();
        functionPanel.destroy();
        // dispose of this last because it will release and destroy throttle.
        addressPanel.destroy();
                
        // Handle disposing of the throttle
        if (throttle != null)
            {
                DccLocoAddress l = (DccLocoAddress) throttle.getLocoAddress();
                InstanceManager.throttleManagerInstance().
                    cancelThrottleRequest(l.getNumber(), this);
            }
    }
       
    public void resetFuncButtons(){
    	functionPanel.initGUI();
    	functionPanel.setEnabled(false);
    }
    
    public void saveRosterChanges(){
    	RosterEntry rosterEntry = addressPanel.getRosterEntry();
    	if (rosterEntry == null){
			JOptionPane.showMessageDialog(this, "Select loco using roster menu in Address Panel", "No Loco Roster Entry Selected",
					JOptionPane.ERROR_MESSAGE);
    		return;
    	}
		if (JOptionPane.showConfirmDialog(this,
				"Save function buttons and loco image to your loco's roster?", "Update Roster Entry",
				JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
			return;
		}
		functionPanel.saveFunctionButtonsToRoster(rosterEntry);
        if ( jmri.jmrit.throttle.ThrottleFrameManager.instance().getThrottlesPreferences().isUsingExThrottle() )
        	backgroundPanel.saveImageToRoster(rosterEntry);
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
            		throttleWindow.getViewControlPanel().setSelected(false);
                    controlPanel.setVisible(false);
                }
            else if (e.getSource() == addressPanel)
                {
            		throttleWindow.getViewAddressPanel().setSelected(false);
                    addressPanel.setVisible(false);
                }
            else if (e.getSource() == functionPanel)
                {
            		throttleWindow.getViewFunctionPanel().setSelected(false);
                    functionPanel.setVisible(false);
                }
        }
        
        /**
         *  Listen for the activation of an internal frame record this property for
         *  correct processing of the frame cycling key.
         *
         * @param  e  The InternalFrameEvent leading to this action
         */
        public void internalFrameActivated(InternalFrameEvent e) {
        	if (e.getSource() == controlPanel)
        		activeFrame = CONTROL_PANEL_INDEX;
        	else if (e.getSource() == addressPanel)
        		activeFrame = ADDRESS_PANEL_INDEX;
        	else if (e.getSource() == functionPanel)
        		activeFrame = FUNCTION_PANEL_INDEX;
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
    public Element getXml() {
		Dimension bDim = new Dimension (0,0);

        Element me = new Element("ThrottlePanel");
        
        if (((javax.swing.plaf.basic.BasicInternalFrameUI) getControlPanel().getUI()).getNorthPane() != null) {
            bDim = ((javax.swing.plaf.basic.BasicInternalFrameUI) getControlPanel().getUI()).getNorthPane().getPreferredSize();
            me.setAttribute("border",Integer.toString(bDim.height));
        }
        
        java.util.ArrayList<Element> children = new java.util.ArrayList<Element>(1);
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
    public void setXml(Element e) {
    	int bSize = 23;
        // Get InternalFrame border size
        if (e.getAttribute("border") != null) bSize = Integer.parseInt((e.getAttribute("border").getValue()));

        Element controlPanelElement = e.getChild("ControlPanel");
        controlPanel.setXml(controlPanelElement);
        if (((javax.swing.plaf.basic.BasicInternalFrameUI) controlPanel.getUI()).getNorthPane() != null)
            ((javax.swing.plaf.basic.BasicInternalFrameUI) controlPanel.getUI()).getNorthPane().setPreferredSize( new Dimension(0,bSize));
        Element functionPanelElement = e.getChild("FunctionPanel");
        functionPanel.setXml(functionPanelElement);
        if (((javax.swing.plaf.basic.BasicInternalFrameUI) controlPanel.getUI()).getNorthPane() != null)
            ((javax.swing.plaf.basic.BasicInternalFrameUI) functionPanel.getUI()).getNorthPane().setPreferredSize( new Dimension(0,bSize));
        Element addressPanelElement = e.getChild("AddressPanel");
        addressPanel.setXml(addressPanelElement);
        if (((javax.swing.plaf.basic.BasicInternalFrameUI) controlPanel.getUI()).getNorthPane() != null)
            ((javax.swing.plaf.basic.BasicInternalFrameUI) addressPanel.getUI()).getNorthPane().setPreferredSize( new Dimension(0,bSize));
        
        setFrameTitle();
    }
    
    /**
     * setFrameTitle - set the frame title based on type, text and address
     */
    public void setFrameTitle() {
    	String addr = "Throttle";
    	if (throttle != null) 
    		addr = throttle.getLocoAddress().toString();    	
    	if ( throttleWindow.getTitleTextType().compareTo("address") == 0) {
    		throttleWindow.setTitle(addr);
    	} else if (throttleWindow.getTitleTextType().compareTo("text") == 0) {
    		throttleWindow.setTitle(throttleWindow.getTitleText());
    	} else if (throttleWindow.getTitleTextType().compareTo("addressText") == 0) {
    		throttleWindow.setTitle(addr + " " + throttleWindow.getTitleText());
    	} else if (throttleWindow.getTitleTextType().compareTo("textAddress") == 0) {
    		throttleWindow.setTitle(throttleWindow.getTitleText() + " " + addr);
    	} else if (throttleWindow.getTitleTextType().compareTo("rosterID") == 0) {
    		if ( (addressPanel.getRosterEntry() != null) && (addressPanel.getRosterEntry().getId() != null) 
    				&& (addressPanel.getRosterEntry().getId().length()>0))
    			throttleWindow.setTitle(addressPanel.getRosterEntry().getId()) ;
    		else
    			throttleWindow.setTitle(addr);
    	}
    }
    
	public void componentHidden(ComponentEvent e) {		
	}

	public void componentMoved(ComponentEvent e) {		
	}

	public void componentResized(ComponentEvent e) {
    	checkPosition(controlPanel);
		checkPosition(functionPanel);
		checkPosition(addressPanel);	
	}

	public void componentShown(ComponentEvent e) {
		throttleWindow.setCurentThrottleFrame(this);
		throttleWindow.updateGUI();
	}
	
	public void saveThrottle() {
		 saveThrottle( (_throttlesBasePath+ addressPanel.getRosterEntry().getId()).trim() +".xml" ); 		
	}
	
	public void notifyThrottleLost(DccLocoAddress dccAddress){
		int address = dccAddress.getNumber();
		boolean isLong = dccAddress.isLongAddress();
		notifyAddressReleased(address, isLong);
	}

    /**
	 * A KeyAdapter that listens for the key that cycles through the
	 * JInternalFrames.
	 * 
	 * @author glen
	 */
	class FrameCyclingKeyListener extends KeyAdapter {
		/**
		 * Description of the Method
		 * 
		 * @param e
		 *            Description of the Parameter
		 */
		public void keyReleased(KeyEvent e) {
			if (e.isControlDown() && e.getKeyCode() == NEXT_FRAME_KEY) {
				try {
					activeFrame = (activeFrame + 1) % NUM_FRAMES;
					frameList[activeFrame].setSelected(true);
				}
				catch (java.beans.PropertyVetoException ex) {
					log.warn("Exception selecting internal frame:" + ex);
				}
			}
			else if (e.isControlDown() && e.getKeyCode() == PREV_FRAME_KEY) {
				try {
					activeFrame--;
					if (activeFrame < 0)
						activeFrame = NUM_FRAMES - 1;    
					frameList[activeFrame].setSelected(true);
				}
				catch (java.beans.PropertyVetoException ex) {
					log.warn("Exception selecting internal frame:" + ex);
				}
			}
		}
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ThrottleFrame.class.getName());	
}