package jmri.jmrit.operations.rollingstock.engines.tools;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jmri.jmrit.operations.rollingstock.engines.gui.EnginesTableFrame;

/**
 * Action to print locomotive roster
 *
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Dennis Miller Copyright (C) 2005
 * @author Daniel Boudreau Copyright (C) 2008, 2011, 2014, 2019, 2023
 */
public class PrintEngineRosterAction extends AbstractAction {

    public PrintEngineRosterAction(boolean isPreview, EnginesTableFrame enginesTableFrame) {
        super(isPreview ? Bundle.getMessage("MenuItemPreview") : Bundle.getMessage("MenuItemPrint"));
        _isPreview = isPreview;
        _enginesTableFrame = enginesTableFrame;
    }

    boolean _isPreview;
    EnginesTableFrame _enginesTableFrame;
    PrintEngineRosterFrame perf = null;

    @Override
    public void actionPerformed(ActionEvent e) {
        if (perf == null) {
            perf = new PrintEngineRosterFrame(_isPreview, _enginesTableFrame);
        } else {
            perf.setVisible(true);
            perf.initComponents();
        }
    }
}
