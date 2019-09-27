package apps.gui3.dp3;

import jmri.util.BusyGlassPane;

/**
 * Interface for the container of a set of PaneProgPanes. The panes use services
 * provided here to work with buttons and the busy cursor.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 */
public class DecoderPro3Panes extends javax.swing.JPanel
        implements jmri.jmrit.symbolicprog.tabbedframe.PaneContainer {

    @Override
    public boolean isBusy() {
        return false;
    }

    @Override
    public void paneFinished() {
    }

    /**
     * Enable the read/write buttons.
     * <p>
     * In addition, if a programming mode pane is present, its "set" button is
     * enabled.
     *
     * @param enable Are reads possible? If false, so not enable the read
     *               buttons.
     */
    @Override
    public void enableButtons(boolean enable) {
    }

    @Override
    public void prepGlassPane(javax.swing.AbstractButton activeButton) {
    }

    @Override
    public BusyGlassPane getBusyGlassPane() {
        return bgp;
    }

    BusyGlassPane bgp = new BusyGlassPane(null, null, null, null);
}
