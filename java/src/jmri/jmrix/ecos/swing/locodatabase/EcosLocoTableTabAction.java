package jmri.jmrix.ecos.swing.locodatabase;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import jmri.Manager;
import jmri.jmrit.beantable.AbstractTableAction;
import jmri.jmrit.beantable.AbstractTableTabAction;
import jmri.jmrix.ecos.EcosSystemConnectionMemo;

public class EcosLocoTableTabAction extends AbstractTableTabAction {

    /**
     *
     */
    private static final long serialVersionUID = 6641977899170854725L;

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
                    TabbedTableItem itemModel = new TabbedTableItem(eMemo.getUserName(), true, eMemo.getLocoAddressManager(), getNewTableAction(eMemo.getUserName(), eMemo));
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
                public void stateChanged(ChangeEvent evt) {
                    setMenuBar(f);
                }
            });
            dataPanel.add(dataTabs, BorderLayout.CENTER);
        }
        init = true;
    }

    protected AbstractTableAction getNewTableAction(String choice) {
        return null;
    }

    protected AbstractTableAction getNewTableAction(String choice, EcosSystemConnectionMemo eMemo) {
        return new EcosLocoTableAction(choice, eMemo);
    }

    protected Manager getManager() {
        return null;
    }

    public void addToFrame(jmri.jmrit.beantable.BeanTableFrame f) {
        if (tabbedTableArray.size() > 1) {
            super.addToFrame(f);
        }
    }

    public void setMenuBar(jmri.jmrit.beantable.BeanTableFrame f) {
        if (tabbedTableArray.size() > 1) {
            super.setMenuBar(f);
        }
    }

    @Override
    protected void setTitle() {
        //atf.setTitle("multiple turnouts");
    }

    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.EcosLocoTable";
    }

    protected String getClassName() {
        return EcosLocoTableAction.class.getName();
    }

    public String getClassDescription() {
        return "Ecos Loco Table";
    }
}
