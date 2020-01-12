package jmri.jmrix.ecos.swing.locodatabase;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import jmri.*;
import jmri.jmrit.beantable.AbstractTableAction;
import jmri.jmrit.beantable.AbstractTableTabAction;
import jmri.jmrix.ecos.EcosSystemConnectionMemo;

public class EcosLocoTableTabAction extends AbstractTableTabAction<NamedBean> {  // there is no specific subtype of NamedBean here, see EcosLocoAddressManager

    public EcosLocoTableTabAction(String s) {
        super(s);
    }

    public EcosLocoTableTabAction() {
        this("Multiple Tabbed");
    }

    @Override
    protected void createModel() {
        dataPanel = new JPanel();
        dataTabs = new JTabbedPane();
        dataPanel.setLayout(new BorderLayout());
        java.util.List<EcosSystemConnectionMemo> list = jmri.InstanceManager.getList(EcosSystemConnectionMemo.class);
        if (list != null) {
            for (EcosSystemConnectionMemo eMemo : list) {
                //We only want to add connections that have an active loco address manager
                if (eMemo.getLocoAddressManager() != null) {
                    TabbedTableItem<NamedBean> itemModel = new TabbedTableItem<>(eMemo.getUserName(), true, eMemo.getLocoAddressManager(), getNewTableAction(eMemo.getUserName(), eMemo));
                    tabbedTableArray.add(itemModel);
                }
            }
        }
        if (tabbedTableArray.size() == 1) {
            EcosLocoTableAction table = (EcosLocoTableAction) tabbedTableArray.get(0).getAAClass();
            table.addToPanel(this);
            dataPanel.add(tabbedTableArray.get(0).getPanel(), BorderLayout.CENTER);
        } else {
            for (int x = 0; x < tabbedTableArray.size(); x++) {
                EcosLocoTableAction table = (EcosLocoTableAction) tabbedTableArray.get(x).getAAClass();
                table.addToPanel(this);
                dataTabs.addTab(tabbedTableArray.get(x).getItemString(), null, tabbedTableArray.get(x).getPanel(), null);
            }
            dataTabs.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent evt) {
                    setMenuBar(f);
                }
            });
            dataPanel.add(dataTabs, BorderLayout.CENTER);
        }
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
