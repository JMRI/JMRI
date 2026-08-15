package jmri.jmrix.dccpp.swing.virtuallcd;


import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import jmri.InstanceManager;
import jmri.UserPreferencesManager;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrix.dccpp.*;
import jmri.jmrix.dccpp.swing.virtuallcd.configurexml.VirtualLCDConfigurationXml;
import jmri.util.JmriJFrame;
import jmri.util.swing.*;

import org.jdom2.Element;

/**
 * Frame to image the DCC-EX command station's OLED display
 *   Also sends request to DCC-EX to send copies of all LCD messages to this instance of JMRI
 *
 * @author BobJacobsen  Copyright (C) 2023
 * @author MSteveTodd   Copyright (C) 2023
 */
public class VirtualLCDFrame extends JmriJFrame  {

    private final DCCppSystemConnectionMemo _memo;
    private final VirtualLCDPanel _virtualLCDPanel;
    private JPopupMenu popup;

    private static String getElementName(DCCppSystemConnectionMemo memo) {
        return "virtual_lcd_config" + "___" + memo.getSystemPrefix();
    }

    public VirtualLCDFrame(DCCppSystemConnectionMemo memo) {
        // Save window position but not window size
        // Set the title, include prefix in event of multiple connections
        super(false, true, Bundle.getMessage("VirtualLCDFrameTitle") + " (" + memo.getSystemPrefix() + ")");
        _memo = memo;
        _virtualLCDPanel = new VirtualLCDPanel(this, false);
        _virtualLCDPanel.setMemo(memo);
    }

    @Override
    public void dispose() {
        InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent(p -> {
            Element parent = new Element(getElementName(_memo), UserPreferencesManager.GENERIC_NAMESPACE);
            Element e = VirtualLCDConfigurationXml.store(_virtualLCDPanel);
            parent.addContent(e);
            p.storeElement(parent);
        });
        _virtualLCDPanel.dispose();
        super.dispose();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponents() {
        super.initComponents();
        _virtualLCDPanel.initComponents();
        add(_virtualLCDPanel);

        //Create the popup menu.
        popup = new JPopupMenu();
        JMenuItem menuItem = new JMenuItem(Bundle.getMessage("EditVirtualLCD"));
        menuItem.addActionListener(evt -> {
            ConfigureVirtualLCD.editConfigureVirtualLCD(null, _virtualLCDPanel);
        });
        popup.add(menuItem);

        //Add listener to components that can bring up popup menus.
        JmriMouseListener popupListener = new JmriMouseAdapter() {
            @Override
            public void mousePressed(JmriMouseEvent e) {
                maybeShowPopup(e);
            }

            @Override
            public void mouseReleased(JmriMouseEvent e) {
                maybeShowPopup(e);
            }

            private void maybeShowPopup(JmriMouseEvent e) {
                if (e.isPopupTrigger()) {
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        };
        addMouseListener(JmriMouseListener.adapt(popupListener));

        // pack to layout display
        pack();

        InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent(p -> {
            Element e = p.loadElement(getElementName(_memo));
            if (e != null) {
                try {
                    VirtualLCDConfigurationXml.load(_virtualLCDPanel, e, true);
                } catch (JmriConfigureXmlException ex) {
                    log.error("Unexpected exception during loading of settings", ex);
                }
            }
        });
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(VirtualLCDFrame.class);

}
