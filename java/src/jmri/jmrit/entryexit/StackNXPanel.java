package jmri.jmrit.entryexit;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class StackNXPanel extends JPanel {

    transient EntryExitPairs manager = jmri.InstanceManager.getDefault(jmri.jmrit.entryexit.EntryExitPairs.class);

    private JPanel entryExitPanel;

    private DefaultListModel<String> listModel;
    private JList<String> list = new JList<String>();
    JScrollPane listScrollPane = new JScrollPane(list);

    public StackNXPanel() {
        super();
        initGUI();
    }

    private void initGUI() {
        listModel = new DefaultListModel<String>();
        setLayout(new BorderLayout());
        entryExitPanel = new JPanel();
        entryExitPanel.setDoubleBuffered(true);
        entryExitPanel.setLayout(new BoxLayout(entryExitPanel, BoxLayout.Y_AXIS));
        entryExitPanel.add(listScrollPane);
        JButton cancelButton = new JButton(Bundle.getMessage("ButtonCancel"));  // NOI18N
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //This method can be called only if
                //there's a valid selection
                //so go ahead and remove whatever's selected.
                if (list.getSelectedValue() != null) {
                    // basic check to see if anything was selected
                manager.cancelStackedRoute(listToDest.get(list.getSelectedValue()), false);
                } else {
                    JOptionPane.showMessageDialog(entryExitPanel,
                            Bundle.getMessage("Error1", Bundle.getMessage("ButtonCancel")),  // NOI18N
                            Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);  // NOI18N
                    // Keep Panel open
                }
            }

        });
        add(cancelButton, BorderLayout.SOUTH);
        add(entryExitPanel, BorderLayout.CENTER);

        updateGUI();
    }

    Hashtable<String, DestinationPoints> listToDest = new Hashtable<String, DestinationPoints>();

    public void updateGUI() {
        listModel.clear();
        listToDest = new Hashtable<String, DestinationPoints>();
        for (DestinationPoints dp : manager.getStackedInterlocks()) {
            listToDest.put(dp.getDisplayName(), dp);
            listModel.addElement(dp.getDisplayName());
        }
        list.setModel(listModel);
        list.setVisibleRowCount(10);
    }
}
