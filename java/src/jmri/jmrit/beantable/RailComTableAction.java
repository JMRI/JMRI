package jmri.jmrit.beantable;

import java.awt.event.ActionEvent;

import javax.annotation.Nonnull;

import jmri.InstanceManager;
import jmri.IdTag;
import jmri.RailComManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a RailCommTable GUI.
 *
 * @author  Bob Jacobsen Copyright (C) 2003
 * @author  Matthew Harris Copyright (C) 2011
 * @since 2.11.4
 */
public class RailComTableAction extends AbstractTableAction<IdTag> {

    /**
     * Create an action with a specific title.
     * <p>
     * Note that the argument is the Action title, not the title of the
     * resulting frame. Perhaps this should be changed?
     *
     * @param actionName title of the action
     */
    public RailComTableAction(String actionName) {
        super(actionName);

        includeAddButton = false;
    }

    @Nonnull
    protected RailComManager tagManager = InstanceManager.getDefault(RailComManager.class);

    public RailComTableAction() {
        this("Rail Com Table");
    }

    /**
     * Create the JTable DataModel, along with the changes for the specific case
     * of RailComm objects
     */
    @Override
    protected void createModel() {
        m = new RailComTableDataModel(tagManager);
    }

    @Override
    protected void setTitle() {
        f.setTitle(Bundle.getMessage("TitleRailComTable"));
    }

    @Override
    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.RailComTable";
    }

    @Override
    protected void addPressed(ActionEvent e) {
    }

    @Override
    public String getClassDescription() {
        return Bundle.getMessage("TitleRailComTable");
    }

    @Override
    public void addToFrame(BeanTableFrame<IdTag> f) {
        log.debug("Added CheckBox in addToFrame method");
    }

    @Override
    public void addToPanel(AbstractTableTabAction<IdTag> f) {
        log.debug("Added CheckBox in addToPanel method");
    }

    @Override
    protected String getClassName() {
        return RailComTableAction.class.getName();
    }
    private static final Logger log = LoggerFactory.getLogger(RailComTableAction.class);
}
