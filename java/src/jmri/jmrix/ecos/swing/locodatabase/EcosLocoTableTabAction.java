package jmri.jmrix.ecos.swing.locodatabase;

import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;

import jmri.*;
import jmri.jmrit.beantable.AbstractTableAction;
import jmri.jmrit.beantable.AbstractTableTabAction;
import jmri.jmrix.ecos.EcosSystemConnectionMemo;

/**
 * There is no specific subtype of NamedBean here, see EcosLocoAddressManager.
 */
public class EcosLocoTableTabAction extends AbstractTableTabAction<NamedBean> {

    public EcosLocoTableTabAction(String s) {
        super(s);
    }

    public EcosLocoTableTabAction() {
        this("Multiple Tabbed");
    }

    @Override
    protected void createModel() {
        dataTabs = new JTabbedPane();
        java.util.List<EcosSystemConnectionMemo> list = InstanceManager.getList(EcosSystemConnectionMemo.class);
        for (EcosSystemConnectionMemo eMemo : list) {
            //We only want to add connections that have an active loco address manager
            if (eMemo.getLocoAddressManager() != null) {
                TabbedTableItem<NamedBean> itemModel = new TabbedTableItem<>(
                    eMemo.getUserName(), true, eMemo.getLocoAddressManager(),
                    getNewTableAction(eMemo.getUserName(), eMemo));
                tabbedTableArray.add(itemModel);
            }
        }

        for (int x = 0; x < tabbedTableArray.size(); x++) {
            EcosLocoTableAction table = (EcosLocoTableAction) tabbedTableArray.get(x).getAAClass();
            table.addToPanel(this);
            dataTabs.addTab(tabbedTableArray.get(x).getItemString(), tabbedTableArray.get(x).getPanel());
        }
        dataTabs.addChangeListener((ChangeEvent evt) -> setMenuBar(f));
        init = true;
    }

    @Override
    protected AbstractTableAction<NamedBean> getNewTableAction(String choice) {
        return null;
    }

    protected AbstractTableAction<NamedBean> getNewTableAction(String choice, EcosSystemConnectionMemo eMemo) {
        return new EcosLocoTableAction(choice, eMemo);
    }

    @Override
    protected Manager<NamedBean> getManager() {
        return null;
    }

    @Override
    public void addToFrame(jmri.jmrit.beantable.BeanTableFrame<NamedBean> f) {
        if (tabbedTableArray.size() > 1) {
            super.addToFrame(f);
        }
    }

    @Override
    public void setMenuBar(jmri.jmrit.beantable.BeanTableFrame<NamedBean> f) {
        if (tabbedTableArray.size() > 1) {
            super.setMenuBar(f);
        }
    }

    @Override
    protected void setTitle() {
        //atf.setTitle("multiple turnouts");
    }

    @Override
    protected String helpTarget() {
        return "package.jmri.jmrix.ecos.swing.locodatabase.EcosLocoTable"; // very simple help page
    }

    @Override
    protected String getClassName() {
        return EcosLocoTableAction.class.getName();
    }

    @Override
    public String getClassDescription() {
        return Bundle.getMessage("EcosLocoTableTitle");
    }

}
