package jmri.jmrix.dccpp.swing.virtuallcd;


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

        // pack to layout display
        pack();
    }

//    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(VirtualLCDFrame.class);

}
