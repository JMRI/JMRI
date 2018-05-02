package jmri.jmrit.mastbuilder;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a MastBuilder object
 *
 * @author Bob Jacobsen Copyright (C) 2003
 */
public class MastBuilderAction extends AbstractAction {

    public MastBuilderAction(String s) {
        super(s);

        // disable ourself if there is no command Station object available
        if (jmri.InstanceManager.getNullableDefault(jmri.CommandStation.class) == null) {
            setEnabled(false);
        }
    }

    public MastBuilderAction() {
        this(Bundle.getMessage("MastBuilderTitle"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // create a MastBuilderPane
        MastBuilderPane f = new MastBuilderPane();
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.error("Exception: " + ex.toString());
        }
        f.setVisible(true);
    }

    private final static Logger log = LoggerFactory.getLogger(MastBuilderAction.class);

}
