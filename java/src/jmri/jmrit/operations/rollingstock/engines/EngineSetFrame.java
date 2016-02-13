// EngineSetFrame.java
package jmri.jmrit.operations.rollingstock.engines;

import java.util.List;
import java.util.ResourceBundle;
import javax.swing.JOptionPane;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.RollingStockSetFrame;

/**
 * Frame for user to place engine on the layout
 *
 * @author Dan Boudreau Copyright (C) 2008, 2010
 * @version $Revision$
 */
public class EngineSetFrame extends RollingStockSetFrame implements
        java.beans.PropertyChangeListener {

    /**
     *
     */
    private static final long serialVersionUID = -7608591085014836578L;

    protected static final ResourceBundle rb = ResourceBundle
            .getBundle("jmri.jmrit.operations.rollingstock.engines.JmritOperationsEnginesBundle");

    EngineManager manager = EngineManager.instance();
    EngineManagerXml managerXml = EngineManagerXml.instance();

    Engine _engine;

    public EngineSetFrame() {
        super(Bundle.getMessage("TitleEngineSet"));
    }

    public void initComponents() {
        super.initComponents();

        // build menu
        addHelpMenu("package.jmri.jmrit.operations.Operations_LocomotivesSet", true); // NOI18N

        // disable location unknown, return when empty, final destination fields
        locationUnknownCheckBox.setVisible(false);
        paneOptional.setVisible(false);
        pFinalDestination.setVisible(false);
        autoTrainCheckBox.setVisible(false);

        // tool tips
        outOfServiceCheckBox.setToolTipText(getRb().getString("TipLocoOutOfService"));

        packFrame();
    }

    public void loadEngine(Engine engine) {
        _engine = engine;
        load(engine);
    }

    protected ResourceBundle getRb() {
        return rb;
    }

    protected boolean save() {
        if (!super.save()) {
            return false;
        }
        // check for train change
        checkTrain(_engine);
        // is this engine part of a consist?
        if (_engine.getConsist() != null) {
            if (JOptionPane.showConfirmDialog(this, Bundle.getMessage("engineInConsist"),
                    Bundle.getMessage("enginePartConsist"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                // convert cars list to rolling stock list
                List<RollingStock> list = _engine.getConsist().getGroup();
                if (!updateGroup(list)) {
                    return false;
                }
            }
        }
        OperationsXml.save();
        return true;
    }

//    private final static Logger log = LoggerFactory.getLogger(EngineSetFrame.class.getName());
}
