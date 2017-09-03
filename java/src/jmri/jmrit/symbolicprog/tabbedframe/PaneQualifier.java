package jmri.jmrit.symbolicprog.tabbedframe;

import javax.swing.JTabbedPane;
import jmri.jmrit.symbolicprog.ArithmeticQualifier;
import jmri.jmrit.symbolicprog.VariableValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Qualify a JMRI DecoderPro pane on a numerical relation by enabling/disabling
 * the tab
 *
 * @author Bob Jacobsen Copyright (C) 2010, 2014
 *
 */
public class PaneQualifier extends ArithmeticQualifier {

    PaneProgPane pane;
    JTabbedPane tabs;
    int index;

    public PaneQualifier(PaneProgPane qualifiedPane, VariableValue watchedVal, int value, String relation, JTabbedPane tabPane, int index) {
        super(watchedVal, value, relation);

        this.pane = qualifiedPane;
        this.tabs = tabPane;
        this.index = index;

        setWatchedAvailable(currentDesiredState());
    }

    @Override
    public void setWatchedAvailable(boolean enable) {
        log.debug("setWatchedAvailable with " + enable + " on " + index);
        tabs.setEnabledAt(index, enable);
    }

    @Override
    protected boolean currentAvailableState() {
        return tabs.isEnabledAt(index);
    }

    private final static Logger log = LoggerFactory.getLogger(PaneQualifier.class);

}
