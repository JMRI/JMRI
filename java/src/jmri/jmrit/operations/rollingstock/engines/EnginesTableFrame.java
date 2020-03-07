package jmri.jmrit.operations.rollingstock.engines;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.TableColumnModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.rollingstock.engines.tools.NceConsistEngineAction;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.swing.JTablePersistenceManager;

/**
 * Frame for adding and editing the engine roster for operations.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2008, 2011, 2012, 2013
 */
public class EnginesTableFrame extends OperationsFrame implements PropertyChangeListener {

    public EnginesTableModel enginesModel;
    javax.swing.JTable enginesTable;
    JScrollPane enginesPane;
    EngineManager engineManager = InstanceManager.getDefault(EngineManager.class);

    // labels
    JLabel numEngines = new JLabel();
    JLabel textEngines = new JLabel();
    JLabel textSep1 = new JLabel("          ");

    // radio buttons
    JRadioButton sortByNumber = new JRadioButton(Bundle.getMessage("Number"));
    JRadioButton sortByRoad = new JRadioButton(Bundle.getMessage("Road"));
    JRadioButton sortByModel = new JRadioButton(Bundle.getMessage("Model"));
    public JRadioButton sortByConsist = new JRadioButton(Bundle.getMessage("Consist"));
    JRadioButton sortByLocation = new JRadioButton(Bundle.getMessage("Location"));
    JRadioButton sortByDestination = new JRadioButton(Bundle.getMessage("Destination"));
    JRadioButton sortByTrain = new JRadioButton(Bundle.getMessage("Train"));
    JRadioButton sortByMoves = new JRadioButton(Bundle.getMessage("Moves"));
    JRadioButton sortByBuilt = new JRadioButton(Bundle.getMessage("Built"));
    JRadioButton sortByOwner = new JRadioButton(Bundle.getMessage("Owner"));
    public JRadioButton sortByValue = new JRadioButton(Setup.getValueLabel());
    public JRadioButton sortByRfid = new JRadioButton(Setup.getRfidLabel());
    JRadioButton sortByDcc = new JRadioButton(Bundle.getMessage("DccAddress"));
    JRadioButton sortByLast = new JRadioButton(Bundle.getMessage("Last"));
    ButtonGroup group = new ButtonGroup();

    // major buttons
    JButton addButton = new JButton(Bundle.getMessage("ButtonAdd"));
    JButton findButton = new JButton(Bundle.getMessage("Find"));
    JButton saveButton = new JButton(Bundle.getMessage("ButtonSave"));

    JTextField findEngineTextBox = new JTextField(6);

    public EnginesTableFrame() {
        super(Bundle.getMessage("TitleEnginesTable"));
        // general GUI config

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // Set up the jtable in a Scroll Pane..
        enginesModel = new EnginesTableModel();
        enginesTable = new JTable(enginesModel);
        enginesPane = new JScrollPane(enginesTable);
        enginesPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        enginesModel.initTable(enginesTable, this);

        // load the number of engines and listen for changes
        numEngines.setText(Integer.toString(engineManager.getNumEntries()));
        engineManager.addPropertyChangeListener(this);
        textEngines.setText(Bundle.getMessage("engines"));

        // Set up the control panel
        // row 1
        JPanel cp1 = new JPanel();
        cp1.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("SortBy")));

        cp1.add(sortByNumber);
        cp1.add(sortByRoad);
        cp1.add(sortByModel);
        cp1.add(sortByConsist);
        cp1.add(sortByLocation);
        cp1.add(sortByDestination);
        cp1.add(sortByTrain);
        JPanel movep = new JPanel();
        movep.setBorder(BorderFactory.createTitledBorder(""));
        movep.add(sortByMoves);
        movep.add(sortByBuilt);
        movep.add(sortByOwner);
        if (Setup.isValueEnabled()) {
            movep.add(sortByValue);
        }
        if (Setup.isRfidEnabled()) {
            movep.add(sortByRfid);
        }
        movep.add(sortByDcc);
        movep.add(sortByLast);
        cp1.add(movep);

        // row 2
        JPanel cp2 = new JPanel();
        cp2.setLayout(new BoxLayout(cp2, BoxLayout.X_AXIS));

        JPanel cp2Add = new JPanel();
        cp2Add.setBorder(BorderFactory.createTitledBorder(""));
        cp2Add.add(numEngines);
        cp2Add.add(textEngines);
        cp2Add.add(textSep1);
        cp2Add.add(addButton);
        cp2.add(cp2Add);

        JPanel cp2Find = new JPanel();
        cp2Find.setBorder(BorderFactory.createTitledBorder(""));
        findButton.setToolTipText(Bundle.getMessage("findEngine"));
        findEngineTextBox.setToolTipText(Bundle.getMessage("findEngine"));
        cp2Find.add(findButton);
        cp2Find.add(findEngineTextBox);
        cp2.add(cp2Find);

        JPanel cp2Save = new JPanel();
        cp2Save.setBorder(BorderFactory.createTitledBorder(""));
        cp2Save.add(saveButton);
        cp2.add(cp2Save);

        // place controls in scroll pane
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.add(cp1);
        controlPanel.add(cp2);

        // some tool tips
        sortByLast.setToolTipText(Bundle.getMessage("TipLastMoved"));

        JScrollPane controlPane = new JScrollPane(controlPanel);

        getContentPane().add(enginesPane);
        getContentPane().add(controlPane);

        // setup buttons
        addButtonAction(addButton);
        addButtonAction(findButton);
        addButtonAction(saveButton);

        sortByNumber.setSelected(true);
        addRadioButtonAction(sortByNumber);
        addRadioButtonAction(sortByRoad);
        addRadioButtonAction(sortByModel);
        addRadioButtonAction(sortByConsist);
        addRadioButtonAction(sortByLocation);
        addRadioButtonAction(sortByDestination);
        addRadioButtonAction(sortByTrain);
        addRadioButtonAction(sortByMoves);
        addRadioButtonAction(sortByBuilt);
        addRadioButtonAction(sortByOwner);
        addRadioButtonAction(sortByValue);
        addRadioButtonAction(sortByRfid);
        addRadioButtonAction(sortByDcc);
        addRadioButtonAction(sortByLast);

        group.add(sortByNumber);
        group.add(sortByRoad);
        group.add(sortByModel);
        group.add(sortByConsist);
        group.add(sortByLocation);
        group.add(sortByDestination);
        group.add(sortByTrain);
        group.add(sortByMoves);
        group.add(sortByBuilt);
        group.add(sortByOwner);
        group.add(sortByValue);
        group.add(sortByRfid);
        group.add(sortByDcc);
        group.add(sortByLast);
        
        sortByDcc.setToolTipText(Bundle.getMessage("TipDccAddressFromRoster"));

        // build menu
        JMenuBar menuBar = new JMenuBar();
        JMenu toolMenu = new JMenu(Bundle.getMessage("MenuTools"));
        toolMenu.add(new EngineRosterMenu(Bundle.getMessage("TitleEngineRoster"), EngineRosterMenu.MAINMENU, this));
        toolMenu.add(new NceConsistEngineAction(Bundle.getMessage("MenuItemNceSync"), this));
        menuBar.add(toolMenu);
        menuBar.add(new jmri.jmrit.operations.OperationsMenu());
        setJMenuBar(menuBar);
        addHelpMenu("package.jmri.jmrit.operations.Operations_Locomotives", true); // NOI18N

        initMinimumSize();

        addHorizontalScrollBarKludgeFix(controlPane, controlPanel);

        // create ShutDownTasks
        createShutDownTask();
    }

    @Override
    public void radioButtonActionPerformed(java.awt.event.ActionEvent ae) {
        log.debug("radio button activated");
        // clear any sorts by column
        clearTableSort(enginesTable);
        if (ae.getSource() == sortByNumber) {
            enginesModel.setSort(enginesModel.SORTBY_NUMBER);
        }
        if (ae.getSource() == sortByRoad) {
            enginesModel.setSort(enginesModel.SORTBY_ROAD);
        }
        if (ae.getSource() == sortByModel) {
            enginesModel.setSort(enginesModel.SORTBY_MODEL);
        }
        if (ae.getSource() == sortByConsist) {
            enginesModel.setSort(enginesModel.SORTBY_CONSIST);
        }
        if (ae.getSource() == sortByLocation) {
            enginesModel.setSort(enginesModel.SORTBY_LOCATION);
        }
        if (ae.getSource() == sortByDestination) {
            enginesModel.setSort(enginesModel.SORTBY_DESTINATION);
        }
        if (ae.getSource() == sortByTrain) {
            enginesModel.setSort(enginesModel.SORTBY_TRAIN);
        }
        if (ae.getSource() == sortByMoves) {
            enginesModel.setSort(enginesModel.SORTBY_MOVES);
        }
        if (ae.getSource() == sortByBuilt) {
            enginesModel.setSort(enginesModel.SORTBY_BUILT);
        }
        if (ae.getSource() == sortByOwner) {
            enginesModel.setSort(enginesModel.SORTBY_OWNER);
        }
        if (ae.getSource() == sortByValue) {
            enginesModel.setSort(enginesModel.SORTBY_VALUE);
        }
        if (ae.getSource() == sortByRfid) {
            enginesModel.setSort(enginesModel.SORTBY_RFID);
        }
        if (ae.getSource() == sortByLast) {
            enginesModel.setSort(enginesModel.SORTBY_LAST);
        }
        if (ae.getSource() == sortByDcc) {
            enginesModel.setSort(enginesModel.SORTBY_DCC_ADDRESS);
        }
    }

    public List<Engine> getSortByList() {
        return enginesModel.getSelectedEngineList();
    }

    EngineEditFrame engineEditFrame = null;

    // add, save or find button
    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        // log.debug("engine button activated");
        if (ae.getSource() == findButton) {
            int rowindex = enginesModel.findEngineByRoadNumber(findEngineTextBox.getText());
            if (rowindex < 0) {
                JOptionPane.showMessageDialog(this, MessageFormat.format(
                        Bundle.getMessage("engineWithRoadNumNotFound"), new Object[]{findEngineTextBox.getText()}),
                        Bundle.getMessage("engineCouldNotFind"), JOptionPane.INFORMATION_MESSAGE);
                return;

            }
            // clear any sorts by column
            clearTableSort(enginesTable);
            enginesTable.changeSelection(rowindex, 0, false, false);
            return;
        }
        if (ae.getSource() == addButton) {
            if (engineEditFrame != null) {
                engineEditFrame.dispose();
            }
            engineEditFrame = new EngineEditFrame();
            engineEditFrame.initComponents();
        }
        if (ae.getSource() == saveButton) {
            if (enginesTable.isEditing()) {
                log.debug("locomotives table edit true");
                enginesTable.getCellEditor().stopCellEditing();
            }
            OperationsXml.save();
            if (Setup.isCloseWindowOnSaveEnabled()) {
                dispose();
            }
        }
    }

    protected int[] getCurrentTableColumnWidths() {
        TableColumnModel tcm = enginesTable.getColumnModel();
        int[] widths = new int[tcm.getColumnCount()];
        for (int i = 0; i < tcm.getColumnCount(); i++) {
            widths[i] = tcm.getColumn(i).getWidth();
        }
        return widths;
    }

    @Override
    public void dispose() {
        engineManager.removePropertyChangeListener(this);
        enginesModel.dispose();
        if (engineEditFrame != null) {
            engineEditFrame.dispose();
        }
        InstanceManager.getOptionalDefault(JTablePersistenceManager.class).ifPresent(tpm -> {
            tpm.stopPersisting(enginesTable);
        });
        super.dispose();
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                    .getNewValue());
        }
        if (e.getPropertyName().equals(EngineManager.LISTLENGTH_CHANGED_PROPERTY)) {
            numEngines.setText(Integer.toString(engineManager.getNumEntries()));
        }
    }

    private final static Logger log = LoggerFactory.getLogger(EnginesTableFrame.class);
}
