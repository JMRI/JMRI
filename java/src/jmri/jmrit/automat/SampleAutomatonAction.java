package jmri.jmrit.automat;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Swing action to create and register a SampleAutomaton object
 *
 * @author Bob Jacobsen Copyright (C) 2003
 */
public class SampleAutomatonAction extends AbstractAction {

    public SampleAutomatonAction(String s) {
        super(s);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // create a SampleAutomaton
        AbstractAutomaton f = new SampleAutomaton();
        f.start();
    }
}
