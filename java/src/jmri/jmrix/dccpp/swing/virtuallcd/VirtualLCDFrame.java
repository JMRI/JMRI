package jmri.jmrix.dccpp.swing.virtuallcd;

import java.awt.event.*;

import javax.annotation.Nonnull;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import jmri.jmrit.display.Editor;
import jmri.jmrix.dccpp.*;
import jmri.util.JmriJFrame;

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

    public VirtualLCDFrame(DCCppSystemConnectionMemo memo) {
        super(false, true); // Save window position but not window size
        _memo = memo;
        _virtualLCDPanel = new VirtualLCDPanel(this);
        _virtualLCDPanel.setMemo(memo);
    }

    @Override
    public void dispose() {
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

        // set the title, include prefix in event of multiple connections
        setTitle(Bundle.getMessage("VirtualLCDFrameTitle") + " (" + _memo.getSystemPrefix() + ")");

        //Create the popup menu.
        popup = new JPopupMenu();
        JMenuItem menuItem = new JMenuItem(Bundle.getMessage("EditVirtualLCD"));
        menuItem.addActionListener(evt -> {
            if (ConfigureVirtualLCD.editPositionableFrame != null) {
                closeDialog(null);
            }
            ConfigureVirtualLCD.editPositionableFrame = new ConfigureVirtualLCD(
                    null, _virtualLCDPanel,
                    VirtualLCDFrame.this::closeDialog);
            ConfigureVirtualLCD.editPositionableFrame.initComponents();
        });
        popup.add(menuItem);

        //Add listener to components that can bring up popup menus.
        MouseListener popupListener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                maybeShowPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                maybeShowPopup(e);
            }

            private void maybeShowPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        };
        addMouseListener(popupListener);

        // pack to layout display
        pack();
    }

    private void closeDialog(@Nonnull Editor editor) {
        ConfigureVirtualLCD.editPositionableFrame.setVisible(false);
        ConfigureVirtualLCD.editPositionableFrame.dispose();
        ConfigureVirtualLCD.editPositionableFrame = null;
        setVisible(true);
    }

//    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(VirtualLCDFrame.class);

}
