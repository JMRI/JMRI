package jmri.jmrit.throttle;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import org.jdom2.Element;
import org.jdom2.JDOMException;

import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.ThrottleManager;
import jmri.configurexml.LoadXmlConfigAction;
import jmri.jmrit.jython.Jynstrument;
import jmri.jmrit.jython.JynstrumentFactory;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.swing.RosterEntrySelectorPanel;
import jmri.jmrit.throttle.UIImplementation.ThrottleJInternalFrameSubControl;
import jmri.jmrit.throttle.UIImplementation.ThrottleUICore;
import jmri.jmrit.throttle.interfaces.AddressListener;
import jmri.jmrit.throttle.interfaces.ThrottleControllerUI;
import jmri.jmrit.throttle.interfaces.ThrottleControllersUIContainer;
import jmri.jmrit.throttle.panels.ControlPanel;
import jmri.jmrit.throttle.panels.FunctionButton;
import jmri.jmrit.throttle.preferences.ThrottlesPreferences;
import jmri.jmrit.throttle.utils.ComponentTransparency;
import jmri.jmrit.throttle.utils.TranslucentJPanel;
import jmri.jmrit.throttle.utils.WindowPreferences;
import jmri.util.FileUtil;
import jmri.util.iharder.dnd.URIDrop;
import jmri.util.swing.TransparencyUtils;

/**
 * The classic JMRI throttle implementation as a JDesktopPane
 * 
 * Class naming is bad but kept for backwards compatibility. This is the main class for the throttle UI, it contains the 4 main panels (address, control, function and speed) as JInternalFrames and manages them. It also manages the Jynstruments that can be added to the throttle frame.
 *
 * @author Lionel Jeanson Copyright 2026
 *
 */
public class ThrottleFrame extends JDesktopPane implements ComponentListener, ThrottleControllerUI {

    private final ThrottleManager throttleManager;
    private final ThrottleFrameManager throttleFrameManager = InstanceManager.getDefault(ThrottleFrameManager.class);

    private final ThrottleUICore throuic;
    private ThrottleWindow throttleWindow;

    private static final int ADDRESS_PANEL_INDEX = 0;
    private static final int CONTROL_PANEL_INDEX = 1;
    private static final int FUNCTION_PANEL_INDEX = 2;
    private static final int SPEED_DISPLAY_INDEX = 3;
    private static final int LOCOICON_INDEX = 4;
    private static final int NUM_FRAMES = 5;
    private static final Integer BACKPANEL_LAYER = Integer.MIN_VALUE;
    private static final Integer PANEL_LAYER_FRAME = 1;
    private static final Integer PANEL_LAYER_PANEL = 2;
    
    private FrameListener frameListener;
    private JInternalFrame[] frameList;
    private int activeFrame;
    private HashMap<Container, JInternalFrame> contentPanes;

    private JInternalFrame controlPanelJIF;
    private JInternalFrame addressPanelJIF;
    private JInternalFrame functionPanelJIF;
    private JInternalFrame speedPanelJIF;
    private JInternalFrame locoIconJIF;

    private String title;

    private boolean isEditMode = true;
    private boolean willSwitch = false;

    public ThrottleFrame(ThrottleWindow tw) {
        this(tw, InstanceManager.getDefault(ThrottleManager.class));
    }

    public ThrottleFrame(ThrottleWindow tw, ThrottleManager tm) {
        super();
        throttleManager = tm;
        throttleWindow = tw;
        throuic = new ThrottleUICore(throttleManager, this);
        initGUI();
        applyPreferences();
        throttleFrameManager.getThrottlesListPanel().getTableModel().fireTableStructureChanged();
    }

    private void initGUI() {
        setOpaque(false);
        setDoubleBuffered(true);
        contentPanes = new HashMap<>();
        frameListener = new FrameListener();

        controlPanelJIF = new ThrottleJInternalFrameSubControl(Bundle.getMessage("ThrottleMenuViewControlPanel"),throuic.getControlPanel(),true);
        controlPanelJIF.addInternalFrameListener(frameListener);

        addressPanelJIF = new ThrottleJInternalFrameSubControl(Bundle.getMessage("ThrottleMenuViewAddressPanel"),throuic.getAddressPanel(),true);
        addressPanelJIF.addInternalFrameListener(frameListener);

        functionPanelJIF = new ThrottleJInternalFrameSubControl(Bundle.getMessage("ThrottleMenuViewFunctionPanel"),throuic.getFunctionPanel(),true);
        functionPanelJIF.addInternalFrameListener(frameListener);

        speedPanelJIF = new ThrottleJInternalFrameSubControl(Bundle.getMessage("ThrottleMenuViewSpeedPanel"),throuic.getSpeedPanel(),false);
        speedPanelJIF.addInternalFrameListener(frameListener);

        locoIconJIF = new ThrottleJInternalFrameSubControl(Bundle.getMessage("ThrottleMenuViewLocoIconPanel"),throuic.getLocoIconPanel(),false);
        locoIconJIF.addInternalFrameListener(frameListener);

        controlPanelJIF.pack();
        addressPanelJIF.pack();
        functionPanelJIF.pack();
        speedPanelJIF.pack();
        locoIconJIF.pack();

        // assumes button width of 54, height of 30 (set in class FunctionButton) with
        // horiz and vert gaps of 5 each (set in FunctionPanel class)
        // with 4 buttons across and 6 rows high
        int width = 4 * (FunctionButton.getButtonWidth()) + 2 * 4 * 5 + 10;   
        int height = 6 * (FunctionButton.getButtonHeight()) + 2 * 6 * 5 + 20; 
        
        functionPanelJIF.setSize(width, height);
        functionPanelJIF.setLocation(controlPanelJIF.getWidth(), 0);

        if (addressPanelJIF.getWidth()<functionPanelJIF.getWidth()) {
            addressPanelJIF.setSize(functionPanelJIF.getWidth(),addressPanelJIF.getHeight());
        }
        addressPanelJIF.setLocation(controlPanelJIF.getWidth(), functionPanelJIF.getHeight());

        if (controlPanelJIF.getHeight() < functionPanelJIF.getHeight() + addressPanelJIF.getHeight()) {
            controlPanelJIF.setSize(controlPanelJIF.getWidth(), functionPanelJIF.getHeight() + addressPanelJIF.getHeight());
        }
        if (controlPanelJIF.getHeight() > functionPanelJIF.getHeight() + addressPanelJIF.getHeight()) {
            addressPanelJIF.setSize(addressPanelJIF.getWidth(), controlPanelJIF.getHeight() - functionPanelJIF.getHeight());
        }
//        if (functionPanelJIF.getWidth() < addressPanelJIF.getWidth()) {
//            functionPanelJIF.setSize(addressPanelJIF.getWidth(), functionPanelJIF.getHeight());
//        }

        speedPanelJIF.setSize(addressPanelJIF.getWidth() + controlPanelJIF.getWidth(), addressPanelJIF.getHeight() / 2);
        speedPanelJIF.setLocation(0, controlPanelJIF.getHeight());

        locoIconJIF.setSize(functionPanelJIF.getWidth(), addressPanelJIF.getHeight() / 2);
        locoIconJIF.setLocation(controlPanelJIF.getWidth()+functionPanelJIF.getWidth(), 0 );

        setPreferredSize(new Dimension(
                Math.max(controlPanelJIF.getWidth() + functionPanelJIF.getWidth(), controlPanelJIF.getWidth() + addressPanelJIF.getWidth()),
                Math.max(addressPanelJIF.getHeight() + functionPanelJIF.getHeight(), controlPanelJIF.getHeight()) ));

        add(controlPanelJIF, PANEL_LAYER_FRAME);
        add(functionPanelJIF, PANEL_LAYER_FRAME);
        add(addressPanelJIF, PANEL_LAYER_FRAME);
        add(speedPanelJIF, PANEL_LAYER_FRAME);
        add(locoIconJIF, PANEL_LAYER_FRAME);

        addComponentListener(throuic.getBackgroundPanel()); // backgroudPanel warned when desktop resized
        add(throuic.getBackgroundPanel(), BACKPANEL_LAYER);

        addComponentListener(this);  // to force sub windows repositionning

        frameList = new JInternalFrame[NUM_FRAMES];
        frameList[ADDRESS_PANEL_INDEX] = addressPanelJIF;
        frameList[CONTROL_PANEL_INDEX] = controlPanelJIF;
        frameList[FUNCTION_PANEL_INDEX] = functionPanelJIF;
        frameList[SPEED_DISPLAY_INDEX] = speedPanelJIF;
        frameList[LOCOICON_INDEX] = locoIconJIF;
        activeFrame = ADDRESS_PANEL_INDEX;

        // #JYNSTRUMENT# Bellow prepare drag'n drop receptacle:
        new URIDrop(throuic.getBackgroundPanel(), uris -> {
                if (isEditMode) {
                    for (URI uri : uris ) {
                        ynstrument(new File(uri).getPath());
                    }
                }
            });
            
        try {
            addressPanelJIF.setSelected(true);
        } catch (PropertyVetoException ex) {
            log.error("Error selecting InternalFrame: {}", ex.getMessage());
        }

    }

    public JInternalFrame getControlPanelJIF() {
        return controlPanelJIF;
    }

    public JInternalFrame getAddressPanelJIF() {
        return addressPanelJIF;
    }

    public JInternalFrame getFunctionPanelJIF() {
        return functionPanelJIF;
    }

    public JInternalFrame getSpeedPanelJIF() {
        return speedPanelJIF;
    }

    public JInternalFrame getLocoIconPanelJIF() {
        return locoIconJIF;
    }

    @Override
    public void updateGUI() {
        throttleWindow.updateGUI();        
    }

    public void applyPreferences() {        
        if (willSwitch) {
            setEditMode(true);
            willSwitch = false;
        } 
        throuic.applyPreferences();
        throuic.loadDefaultThrottle();
    }


    public void setAddressPanelVisible(boolean visible) {
        addressPanelJIF.setVisible(visible);
    }

    public void setControlPanelVisible(boolean visible) {
        controlPanelJIF.setVisible(visible);
    }

    public void setFunctionPanelVisible(boolean visible) {
        functionPanelJIF.setVisible(visible);
    }

    public void setSpeedPanelVisible(boolean visible) {
        speedPanelJIF.setVisible(visible);
    }

    public void setLocoIconPanelVisible(boolean visible) {
        locoIconJIF.setVisible(visible);
    }

    public void resetFunctionPanelButton() {
        throuic.getFunctionPanel().resetFnButtons();
        throuic.getFunctionPanel().setEnabled();
    }

    /**
     * Handle my own destruction.
     * <ol>
     * <li> dispose of sub windows.
     * <li> notify my manager of my demise.
     * </ol>
     */
    public void dispose() {
        log.debug("Disposing");
        URIDrop.remove(throuic.getBackgroundPanel());
        throuic.dispose();        
    }

    @Override
    public ThrottleWindow getThrottleControllersContainer() {
        return throttleWindow;
    }
    
    @Override
    public void setThrottleControllersContainer(ThrottleControllersUIContainer tw) {
        if (tw instanceof ThrottleWindow) {
            throttleWindow = (ThrottleWindow) tw;
        } else {
            log.warn("Unable to set throttle controllers container, provided container is not an instance of ThrottleWindow");
        }        
    }

    @Override
    public void toFront() {
        if (throttleWindow == null) {
            return;
        }
        throttleWindow.toFront(title);
    }

    @Override
    public void setRosterEntry(RosterEntry re) {
        throuic.setRosterEntry(re);
    }

    @Override
    public RosterEntry getRosterEntry() {
        return throuic.getRosterEntry();
    }    

    @Override
    public void setAddress(DccLocoAddress la) {
        throuic.setAddress(la);
    }

    @Override
    public DccLocoAddress getAddress() {
        return throuic.getAddress();
    }

    @Override
    public void setConsistAddress(DccLocoAddress la) {
        throuic.setConsistAddress(la);
    }

    @Override
    public void dispatchAddress() {
        throuic.getAddressPanel().dispatchAddress();
    }

    @Override
    public boolean isUsingAddress(DccLocoAddress la) {                    
        if ( getThrottle() != null && 
                ( la.equals( throuic.getAddressPanel().getCurrentAddress()) || la.equals( throuic.getAddressPanel().getConsistAddress()) ) ) {
            return true;
        }
        return false;
    }

    @Override
    public void eStop() {
        throuic.eStop();
    }

    @Override
    public boolean isRunning() {
        return ((getThrottle() != null) && (getThrottle().getSpeedSetting() > 0));
    }

    @Override
    public boolean isActive() {
        return ( (getThrottle() != null) && ( (getThrottle().getSpeedSetting() > 0) || (throuic.hasActiveFunction())));
    }

    @Override
    public DccThrottle getThrottle() {
        return throuic.getAddressPanel().getThrottle();  
    }

    @Override
    public DccThrottle getFunctionThrottle() {
        return throuic.getAddressPanel().getFunctionThrottle();  
     }

    @Override
    public JLabel getLabel() {
        JLabel ret = new JLabel(throuic.getLocoIconPanel().getLabel().getText(), throuic.getLocoIconPanel().getLabel().getIcon(), JLabel.RIGHT);
        return ret;
    }

    @Override
    public boolean isSpeedDisplayContinuous() {
        return throuic.getControlPanel().getDisplaySlider() == ControlPanel.SLIDERDISPLAYCONTINUOUS;
    }

    public void forceAddressPanelSelected() {
        try {
            addressPanelJIF.setSelected(true);
        } catch (PropertyVetoException ex) {
            log.warn("Unable to force selection of address panel", ex);
        }
    }

    public boolean isAddressPanelVisible() {
        return addressPanelJIF.isVisible();
    }

    public boolean isControlPanelVisible() {
        return controlPanelJIF.isVisible();
    }

    public boolean isFunctionPanelVisible() {
        return functionPanelJIF.isVisible();
    }

    public boolean isSpeedPanelVisible() {
        return speedPanelJIF.isVisible();
    }

    public boolean isLocoIconPanelVisible() {
        return locoIconJIF.isVisible();
    }

    public boolean isKnownLastUsedSaveFile() {
        return (throuic.getLastUsedSaveFile() != null);
    }

    public boolean isKnownRosterEntry() {
        return ( throuic.getRosterEntry() != null);
    }

    public void setTitle(String s) {
        title = s;     
    }

    public void saveRosterChanges() {
        throuic.saveRosterChanges();
    }

    @Override
    public RosterEntrySelectorPanel getRosterEntrySelector() {
        return throuic.getAddressPanel().getRosterEntrySelector();
    }

    @Override
    public void addAddressListener(AddressListener l) {
        throuic.getAddressPanel().addAddressListener(l);
    }

    @Override
    public void removeAddressListener(AddressListener l) {
        throuic.getAddressPanel().removeAddressListener(l);
    }

    /**
     * Sets the location of a throttle frame on the screen according to x and y
     * coordinates
     *
     * @see java.awt.Component#setLocation(int, int)
     */
    @Override
    public void setLocation(int x, int y) {
        if (throttleWindow == null) {
            return;
        }
        throttleWindow.setLocation(new Point(x, y));
    }    

    @Override
    public void componentResized(ComponentEvent e) {
        //  checkPosition ();
    }

    @Override
    public void componentMoved(ComponentEvent e) {
        // do nothing
    }

    @Override
    public void componentShown(ComponentEvent e) {
        throttleWindow.setCurrentThrottleFrame(this);
        if (willSwitch) {
            setEditMode(this.throttleWindow.isEditMode());
            repaint();
        }
        throttleWindow.updateGUI();
        // bring addresspanel to front if no allocated throttle
        if (getThrottle() == null && throttleWindow.isEditMode()) {
            if (!addressPanelJIF.isVisible()) {
                addressPanelJIF.setVisible(true);
            }
            if (addressPanelJIF.isIcon()) {
                try {
                    addressPanelJIF.setIcon(false);
                } catch (PropertyVetoException ex) {
                    log.debug("JInternalFrame uniconify, vetoed");
                }
            }
            addressPanelJIF.requestFocus();
            addressPanelJIF.toFront();
            try {
                addressPanelJIF.setSelected(true);
            } catch (java.beans.PropertyVetoException ex) {
                log.debug("JInternalFrame selection, vetoed");
            }
        }
    }

    @Override
    public void componentHidden(ComponentEvent e) {
        // do nothing
    }

    // #JYNSTRUMENT# here instantiate the Jynstrument, put it in a component, initialize the context and start it
    public JInternalFrame ynstrument(String path) {
        if (path == null) {
            return null;
        }
        Jynstrument it = JynstrumentFactory.createInstrument(path, this); // everything is there
        if (it == null) {
            log.error("Error while creating Jynstrument {}", path);
            return null;
        }
        ComponentTransparency.setTransparentBackground(it);
        JInternalFrame newiFrame = new JInternalFrame(it.getClassName());
        newiFrame.add(it);
        newiFrame.addInternalFrameListener(frameListener);
        newiFrame.setDoubleBuffered(true);
        newiFrame.setResizable(true);
        newiFrame.setClosable(true);
        newiFrame.setIconifiable(true);
        newiFrame.getContentPane().addContainerListener(new ContainerListener() {
            @Override
            public void componentAdded(ContainerEvent e) {
            }

            @Override
            public void componentRemoved(ContainerEvent e) {
                Container c = e.getContainer();
                while ((!(c instanceof JInternalFrame)) && (!(c instanceof TranslucentJPanel))) {
                    c = c.getParent();
                }
                c.setVisible(false);
                remove(c);
                repaint();
            }
        });
        newiFrame.pack();
        add(newiFrame, PANEL_LAYER_FRAME);
        newiFrame.setVisible(true);
        return newiFrame;
    }

    // make sure components are inside this frame bounds
    private void checkPosition(Component comp) {
        if ((this.getWidth() < 1) || (this.getHeight() < 1)) {
            return;
        }

        Rectangle pos = comp.getBounds();
        if (pos.width > this.getWidth()) { // Component largest than container
            pos.width = this.getWidth() - 2;
            pos.x = 1;
        }
        if (pos.x + pos.width > this.getWidth()) { // Component to large        
            pos.x = this.getWidth() - pos.width - 1;
        }
        if (pos.x < 0) { // Component to far on the left
            pos.x = 1;
        }

        if (pos.height > this.getHeight()) { // Component higher than container
            pos.height = this.getHeight() - 2;
            pos.y = 1;
        }
        if (pos.y + pos.height > this.getHeight()) { // Component to low
            pos.y = this.getHeight() - pos.height - 1;
        }
        if (pos.y < 0) { // Component to high
            pos.y = 1;
        }
        comp.setBounds(pos);
    }

    public void makeAllComponentsInBounds() {
        Component[] cmps = getComponents();
        for (Component cmp : cmps) {
            checkPosition(cmp);
        }
    }

    private void translude(JInternalFrame jif) {
        Dimension cpSize = jif.getContentPane().getSize();
        Point cpLoc = jif.getContentPane().getLocationOnScreen();
        TranslucentJPanel pane = new TranslucentJPanel();
        pane.setLayout(new BorderLayout());
        contentPanes.put(pane, jif);
        pane.add(jif.getContentPane(), BorderLayout.CENTER);
        TransparencyUtils.setTransparent(pane, true);
        jif.setContentPane(new JPanel());
        jif.setVisible(false);
        Point loc = new Point(cpLoc.x - this.getLocationOnScreen().x, cpLoc.y - this.getLocationOnScreen().y);
        add(pane, PANEL_LAYER_PANEL);
        pane.setLocation(loc);
        pane.setSize(cpSize);
    }

    private void playRendering() {
        Component[] cmps = getComponentsInLayer(PANEL_LAYER_FRAME);
        contentPanes = new HashMap<>();
        for (Component cmp : cmps) {
            if ((cmp instanceof JInternalFrame) && (cmp.isVisible())) {
                translude((JInternalFrame)cmp);
            }
        }
    }

    private void editRendering() {
        Component[] cmps = getComponentsInLayer(PANEL_LAYER_PANEL);
        for (Component cmp : cmps) {
            if (cmp instanceof JPanel) {
                JPanel pane = (JPanel) cmp;
                JInternalFrame jif = contentPanes.get(pane);
                jif.setContentPane((Container) pane.getComponent(0));
                TransparencyUtils.setTransparent(jif, false);
                jif.setVisible(true);
                remove(pane);
            }
        }
    }

    public void setEditMode(boolean mode) {
        if (mode == isEditMode)
            return;
        if (isVisible()) {
            if (!mode) {
                playRendering();
            } else {
                editRendering();
            }
            isEditMode = mode;
            willSwitch = false;
        } else {
            willSwitch = true;
        }
        throttleWindow.updateGUI();
    }

    public boolean getEditMode() {
        return isEditMode;
    }

    public void activateNextJInternalFrame() {
        try {
            int initialFrame = activeFrame; // avoid infinite loop
            do {
                activeFrame = (activeFrame + 1) % NUM_FRAMES;
                frameList[activeFrame].setSelected(true);
            } while ((frameList[activeFrame].isClosed() || frameList[activeFrame].isIcon() || (!frameList[activeFrame].isVisible())) && (initialFrame != activeFrame));
        } catch (PropertyVetoException ex) {
            log.warn("Exception selecting internal frame:{}", ex.getMessage());
        }
    }

    public void activatePreviousJInternalFrame() {
        try {
            int initialFrame = activeFrame; // avoid infinite loop
            do {
                activeFrame--;
                if (activeFrame < 0) {
                    activeFrame = NUM_FRAMES - 1;
                }
                frameList[activeFrame].setSelected(true);
            } while ((frameList[activeFrame].isClosed() || frameList[activeFrame].isIcon() || (!frameList[activeFrame].isVisible())) && (initialFrame != activeFrame));
        } catch (PropertyVetoException ex) {
            log.warn("Exception selecting internal frame:{}", ex.getMessage());
        }
    }

    /**
     * setFrameTitle - set the frame title based on type, text and address
     * 
     */
    @Override
    public void updateFrameTitle() {
        String winTitle = Bundle.getMessage("ThrottleTitle");
        if (throttleWindow.getTitleTextType().compareTo("text") == 0) {
            winTitle = throttleWindow.getTitleText();
        } else  if ( getThrottle() != null) {
            String addr  = throuic.getAddressPanel().getCurrentAddress().toString();        
            if (throttleWindow.getTitleTextType().compareTo("address") == 0) {
                winTitle = addr;         
            } else if (throttleWindow.getTitleTextType().compareTo("addressText") == 0) {
                winTitle = addr + " " + throttleWindow.getTitleText();
            } else if (throttleWindow.getTitleTextType().compareTo("textAddress") == 0) {
                winTitle = throttleWindow.getTitleText() + " " + addr;
            } else if (throttleWindow.getTitleTextType().compareTo("rosterID") == 0) {
                if ( (throuic.getRosterEntry() != null) && (throuic.getRosterEntry().getId() != null)
                        && (throuic.getRosterEntry().getId().length() > 0)) {
                    winTitle = throuic.getRosterEntry().getId();
                } else {
                    winTitle = addr; // better than nothing in that particular case
                }
            }
        }
        throttleWindow.setTitle(winTitle);        
    }

    public Element getXmlFile() {
        if (throuic.getLastUsedSaveFile() == null) { // || (getRosterEntry()==null))
            return null;
        }
        Element me = new Element("ThrottleFrame");
        me.setAttribute("ThrottleXMLFile", FileUtil.getPortableFilename(throuic.getLastUsedSaveFile()));
        return me;
    }

    public void setXml(Element e) {
        if (e == null) {
            return;
        }

        String sfile = e.getAttributeValue("ThrottleXMLFile");
        if (sfile != null) {
            loadThrottleFile(FileUtil.getExternalFilename(sfile));
            return;
        }

        boolean switchAfter = false;
        if (!isEditMode) {
            setEditMode(true);
            switchAfter = true;
        }

        int bSize = 23;
        // Get InternalFrame border size
        if (e.getAttribute("border") != null) {
            bSize = Integer.parseInt((e.getAttribute("border").getValue()));
        }
        if (((javax.swing.plaf.basic.BasicInternalFrameUI) controlPanelJIF.getUI()).getNorthPane() != null) {
            ((javax.swing.plaf.basic.BasicInternalFrameUI) controlPanelJIF.getUI()).getNorthPane().setPreferredSize(new Dimension(0, bSize));
        }
        if (((javax.swing.plaf.basic.BasicInternalFrameUI) functionPanelJIF.getUI()).getNorthPane() != null) {
            ((javax.swing.plaf.basic.BasicInternalFrameUI) functionPanelJIF.getUI()).getNorthPane().setPreferredSize(new Dimension(0, bSize));
        }
        if (((javax.swing.plaf.basic.BasicInternalFrameUI) addressPanelJIF.getUI()).getNorthPane() != null) {
            ((javax.swing.plaf.basic.BasicInternalFrameUI) addressPanelJIF.getUI()).getNorthPane().setPreferredSize(new Dimension(0, bSize));
        }
        if (((javax.swing.plaf.basic.BasicInternalFrameUI) speedPanelJIF.getUI()).getNorthPane() != null) {
            ((javax.swing.plaf.basic.BasicInternalFrameUI) speedPanelJIF.getUI()).getNorthPane().setPreferredSize(new Dimension(0, bSize));
        }
        if (((javax.swing.plaf.basic.BasicInternalFrameUI) locoIconJIF.getUI()).getNorthPane() != null) {
            ((javax.swing.plaf.basic.BasicInternalFrameUI) locoIconJIF.getUI()).getNorthPane().setPreferredSize(new Dimension(0, bSize));
        }   

        setWindowXML(e.getChild("ControlPanel"), controlPanelJIF);
        setWindowXML(e.getChild("FunctionPanel"), functionPanelJIF);
        setWindowXML(e.getChild("AddressPanel"), addressPanelJIF);
        setWindowXML(e.getChild("SpeedPanel"), speedPanelJIF);
        
        List<Element> jinsts = e.getChildren("Jynstrument");
        if ((jinsts != null) && (jinsts.size() > 0)) {
            for (Element jinst : jinsts) {
                JInternalFrame jif = ynstrument(FileUtil.getExternalFilename(jinst.getAttributeValue("JynstrumentFolder")));
                Element window = jinst.getChild("window");
                if (jif != null) {
                    if (window != null) {
                        WindowPreferences.setPreferences(jif, window);
                    }
                    Component[] cmps2 = jif.getContentPane().getComponents();
                    int j = 0;
                    while ((j < cmps2.length) && (!(cmps2[j] instanceof Jynstrument))) {
                        j++;
                    }
                    if ((j < cmps2.length) && (cmps2[j] instanceof Jynstrument)) {
                        ((Jynstrument) cmps2[j]).setXml(jinst);
                    }

                    jif.repaint();
                }
            }
        }
        updateFrameTitle();
        if (switchAfter) {
            setEditMode(false);
        }
    }

    private void setWindowXML(Element e, JInternalFrame jif) {
        if (e == null || jif == null) {
            return;
        }
        Element window = e.getChild("window");
        if (window == null) { 
            return;
        }
        WindowPreferences.setPreferences(jif, window);                    
    }

    public Element getXml() {
        boolean switchAfter = false;
        if (!isEditMode) {
            setEditMode(true);
            switchAfter = true;
        }

        Element me = new Element("ThrottleFrame");

        if (((javax.swing.plaf.basic.BasicInternalFrameUI) controlPanelJIF.getUI()).getNorthPane() != null) {
            Dimension bDim = ((javax.swing.plaf.basic.BasicInternalFrameUI) controlPanelJIF.getUI()).getNorthPane().getPreferredSize();
            me.setAttribute("border", Integer.toString(bDim.height));
        }

        // Save Jynstruments
        ArrayList<Element> children = new ArrayList<>(4);
        throuic.getXml(children);
        for (Element child : children) { // this is ugly but preserves backward comptabilility
            if (child.getName().equals("ControlPanel")) {
               addWindowXML(child, controlPanelJIF);
            }
            if (child.getName().equals("FunctionPanel")) {
               addWindowXML(child, functionPanelJIF);
            }
            if (child.getName().equals("AddressPanel")) {
               addWindowXML(child, addressPanelJIF);
            }
            if (child.getName().equals("SpeedPanel")) {
               addWindowXML(child, speedPanelJIF);
            }                        
        }        

        Component[] cmps = getComponents();
        for (Component cmp : cmps) {
            try {
                if (cmp instanceof JInternalFrame) {
                    Component[] cmps2 = ((JInternalFrame) cmp).getContentPane().getComponents();
                    int j = 0;
                    while ((j < cmps2.length) && (!(cmps2[j] instanceof Jynstrument))) {
                        j++;
                    }
                    if ((j < cmps2.length) && (cmps2[j] instanceof Jynstrument)) {
                        Jynstrument jyn = (Jynstrument) cmps2[j];
                        Element elt = new Element("Jynstrument");
                        elt.setAttribute("JynstrumentFolder", FileUtil.getPortableFilename(jyn.getFolder()));
                        ArrayList<Element> jychildren = new ArrayList<>(1);
                        jychildren.add(WindowPreferences.getPreferences((JInternalFrame) cmp));
                        Element je = jyn.getXml();
                        if (je != null) {
                            jychildren.add(je);
                        }
                        elt.setContent(jychildren);
                        children.add(elt);
                    }
                }
            } catch (Exception ex) {
                log.debug("Got exception (no panic) {}", ex.getMessage());
            }
        }
        me.setContent(children);
        if (switchAfter) {
            setEditMode(false);
        }
        return me;
    }

    private void addWindowXML(Element e, JInternalFrame jif) {
        if (e == null || jif == null) {
            return;
        }
        java.util.ArrayList<Element> children = new java.util.ArrayList<Element>(1);        
        children.add(WindowPreferences.getPreferences(jif));        
        e.addContent(0,children); // has to be the first one as per DTD
    }

    public void saveThrottle() {
        throuic.saveThrottle(getXml());
    }

    public void saveThrottleAs() {
        throuic.saveThrottleAs(getXml());
    }    

    @Override
    public void loadThrottleFile(String sfile) {
        if (sfile == null) { // null file, ask user to select one
            JFileChooser fileChooser = jmri.jmrit.XmlFile.userFileChooser(Bundle.getMessage("PromptXmlFileTypes"), "xml");
            fileChooser.setCurrentDirectory(new File(ThrottleUICore.getDefaultThrottleFolder()));
            fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
            java.io.File file = LoadXmlConfigAction.getFile(fileChooser, this);
            if (file == null) {
                return ;
            }
        }
        boolean switchAfter = false;
        if (!isEditMode) {
            setEditMode(true);
            switchAfter = true;
        }
        // close all existing Jynstruments
        Component[] cmps = getComponents();
        for (Component cmp : cmps) {
            try {
                if (cmp instanceof JInternalFrame) {
                    JInternalFrame jyf = (JInternalFrame) cmp;
                    Component[] cmps2 = jyf.getContentPane().getComponents();
                    for (Component cmp2 : cmps2) {
                        if (cmp2 instanceof Jynstrument) {
                            ((Jynstrument) cmp2).exit();
                            jyf.dispose();
                        }
                    }
                }
            } catch (Exception ex) {
                log.debug("Got exception (no panic) {}", ex.getMessage());
            }
        }

        try {
            Element conf = throuic.loadThrottle(sfile);
            setXml(conf);
        } catch (FileNotFoundException ex) {
            // Don't show error dialog if file is not found
            log.debug("Loading throttle exception: {}", ex.getMessage());
            log.debug("Tried loading throttle file \"{}\" , reverting to default, if any", sfile);
            throuic.loadDefaultThrottle(); // revert to loading default one
        } catch (NullPointerException | IOException | JDOMException ex) {
            log.debug("Loading throttle exception: {}", ex.getMessage());
            log.debug("Tried loading throttle file \"{}\" , reverting to default, if any", sfile);
            jmri.configurexml.ConfigXmlManager.creationErrorEncountered(
                    null, "parsing file " + sfile,
                    "Parse error", null, null, ex);
            throuic.loadDefaultThrottle(); // revert to loading default one
        }
        //     checkPosition();
        if (switchAfter) {
            setEditMode(false);
        }
    }

   /**
     * An extension of InternalFrameAdapter for listening to the closing of of
     * this frame's internal frames.
     *
     * @author glen
     */
    class FrameListener extends InternalFrameAdapter {

        /**
         * Listen for the closing of an internal frame and set the "View" menu
         * appropriately. Then hide the closing frame
         *
         * @param e The InternalFrameEvent leading to this action
         */
        @Override
        public void internalFrameClosing(InternalFrameEvent e) {
            if (e.getSource() == controlPanelJIF) {
                throttleWindow.getViewControlPanel().setSelected(false);
                controlPanelJIF.setVisible(false);
            } else if (e.getSource() == addressPanelJIF) {
                throttleWindow.getViewAddressPanel().setSelected(false);
                addressPanelJIF.setVisible(false);
            } else if (e.getSource() == functionPanelJIF) {
                throttleWindow.getViewFunctionPanel().setSelected(false);
                functionPanelJIF.setVisible(false);
            } else if (e.getSource() == speedPanelJIF) {
                throttleWindow.getViewSpeedPanel().setSelected(false);
                speedPanelJIF.setVisible(false);
            } else if (e.getSource() == locoIconJIF) {
                throttleWindow.getViewLocoIconPanel().setSelected(false);
                locoIconJIF.setVisible(false);
            } else {
                try { // #JYNSTRUMENT#, Very important, clean the Jynstrument
                    if ((e.getSource() instanceof JInternalFrame)) {
                        Component[] cmps = ((JInternalFrame) e.getSource()).getContentPane().getComponents();
                        int i = 0;
                        while ((i < cmps.length) && (!(cmps[i] instanceof Jynstrument))) {
                            i++;
                        }
                        if ((i < cmps.length) && (cmps[i] instanceof Jynstrument)) {
                            ((Jynstrument) cmps[i]).exit();
                        }
                    }
                } catch (Exception exc) {
                    log.debug("Got exception, can ignore: ", exc);
                }
            }
        }

        /**
         * Listen for the activation of an internal frame record this property
         * for correct processing of the frame cycling key.
         *
         * @param e The InternalFrameEvent leading to this action
         */
        @Override
        public void internalFrameActivated(InternalFrameEvent e) {
            if (e.getSource() == controlPanelJIF) {
                activeFrame = CONTROL_PANEL_INDEX;
            } else if (e.getSource() == addressPanelJIF) {
                activeFrame = ADDRESS_PANEL_INDEX;
            } else if (e.getSource() == functionPanelJIF) {
                activeFrame = FUNCTION_PANEL_INDEX;
            } else if (e.getSource() == speedPanelJIF) {
                activeFrame = SPEED_DISPLAY_INDEX;
            } else if (e.getSource() == locoIconJIF) {
                activeFrame = LOCOICON_INDEX;
            }
        }
    }    

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ThrottleFrame.class);
}
