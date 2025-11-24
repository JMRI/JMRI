package jmri.jmrit.symbolicprog.tabbedframe;

import javax.swing.JTabbedPane;
import jmri.jmrit.symbolicprog.ArithmeticQualifier;
import jmri.jmrit.symbolicprog.VariableValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Qualify a JMRI DecoderPro pane on a numerical relation by enabling/disabling
 * the tab.
 *
 * @author Bob Jacobsen Copyright (C) 2010, 2014
 */
public class PaneQualifier extends ArithmeticQualifier {

    PaneProgPane pane;
    JTabbedPane tabs;
    String name;

    public PaneQualifier(PaneProgPane qualifiedPane, VariableValue watchedVal, int value, String relation, JTabbedPane tabPane, String name) {
        super(watchedVal, value, relation);

        this.pane = qualifiedPane;
        this.tabs = tabPane;
        this.name = name;

        setWatchedAvailable(currentDesiredState());
    }

    @Override
    public void setWatchedAvailable(boolean enable) {
        jmri.util.ThreadingUtil.runOnGUIEventually( ()->{
            log.debug("setWatchedAvailable with {} on {} index {}", enable, name, tabs.indexOfTab(name));
            tabs.setEnabledAt(tabs.indexOfTab(name), enable);
        });
    }

    @Override
    protected boolean currentAvailableState() {
        return tabs.isEnabledAt(tabs.indexOfTab(name));
    }

    private final static Logger log = LoggerFactory.getLogger(PaneQualifier.class);

}
