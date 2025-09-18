package jmri.jmrit.operations.rollingstock.engines.gui;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumnModel;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.rollingstock.engines.tools.*;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.swing.JTablePersistenceManager;
import jmri.util.swing.JmriJOptionPane;

/**
 * Frame for adding and editing the engine roster for operations.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2008, 2011, 2012, 2013, 2025
 */
public class EnginesTableFrame extends OperationsFrame implements TableModelListener {

    public EnginesTableModel enginesTableModel;
    public JTable enginesTable; // public for testing
    boolean showAllLocos = true;
    JScrollPane enginesPane;
    EngineManager engineManager = InstanceManager.getDefault(EngineManager.class);

    // labels
    JLabel numEngines = new JLabel();
    JLabel textEngines = new JLabel(Bundle.getMessage("engines"));
    JLabel textSep1 = new JLabel("          ");

    // radio buttons
    JRadioButton sortByNumber = new JRadioButton(Bundle.getMessage("Number"));
    JRadioButton sortByRoad = new JRadioButton(Bundle.getMessage("Road"));
    JRadioButton sortByModel = new JRadioButton(Bundle.getMessage("Model"));
    JRadioButton sortByConsist = new JRadioButton(Bundle.getMessage("Consist"));
    JRadioButton sortByLocation = new JRadioButton(Bundle.getMessage("Location"));
    JRadioButton sortByDestination = new JRadioButton(Bundle.getMessage("Destination"));
    JRadioButton sortByTrain = new JRadioButton(Bundle.getMessage("Train"));
    JRadioButton sortByMoves = new JRadioButton(Bundle.getMessage("Moves"));
    JRadioButton sortByBuilt = new JRadioButton(Bundle.getMessage("Built"));
    JRadioButton sortByOwner = new JRadioButton(Bundle.getMessage("Owner"));
    JRadioButton sortByValue = new JRadioButton(Setup.getValueLabel());
    JRadioButton sortByRfid = new JRadioButton(Setup.getRfidLabel());
    JRadioButton sortByDcc = new JRadioButton(Bundle.getMessage("DccAddress"));
    JRadioButton sortByLast = new JRadioButton(Bundle.getMessage("Last"));
    JRadioButton sortByComment = new JRadioButton(Bundle.getMessage("Comment"));
    ButtonGroup group = new ButtonGroup();

    // major buttons
    JButton addButton = new JButton(Bundle.getMessage("TitleEngineAdd"));
    JButton findButton = new JButton(Bundle.getMessage("Find"));
    JButton saveButton = new JButton(Bundle.getMessage("ButtonSave"));

    JTextField findEngineTextBox = new JTextField(6);

    public EnginesTableFrame(boolean showAllLocos, String locationName, String trackName) {
        super(Bundle.getMessage("TitleEnginesTable"));
        this.showAllLocos = showAllLocos;

        // general GUI config
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // Set up the jtable in a Scroll Pane..
        enginesTableModel = new EnginesTableModel(showAllLocos, locationName, trackName);
        enginesTable = new JTable(enginesTableModel);
        enginesPane = new JScrollPane(enginesTable);
        enginesPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        enginesTableModel.initTable(enginesTable, this);

        // load the number of engines and listen for changes
        updateNumEngines();
        enginesTableModel.addTableModelListener(this);

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
        movep.add(sortByComment);
        cp1.add(movep);

        // row 2
        JPanel cp2 = new JPanel();
        cp2.setLayout(new BoxLayout(cp2, BoxLayout.X_AXIS));

        JPanel cp2Add = new JPanel();
        cp2Add.setBorder(BorderFactory.createTitledBorder(""));
        addButton.setToolTipText(Bundle.getMessage("TipAddButton"));
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
        addRadioButtonAction(sortByComment);

        findEngineTextBox.addActionListener(this::textBoxActionPerformed);

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
        group.add(sortByComment);
        
        // sort by location
        if (!showAllLocos) {
            sortByLocation.doClick();
            if (locationName != null) {
                String title = Bundle.getMessage("TitleEnginesTable") + " " + locationName;
                if (trackName != null) {
                    title = title + " " + trackName;
                }
                setTitle(title);
            }
        }

        sortByDcc.setToolTipText(Bundle.getMessage("TipDccAddressFromRoster"));

        // build menu
        JMenuBar menuBar = new JMenuBar();
        JMenu toolMenu = new JMenu(Bundle.getMessage("MenuTools"));
        toolMenu.add(new EngineRosterMenu(Bundle.getMessage("TitleEngineRoster"), EngineRosterMenu.MAINMENU, this));
        toolMenu.addSeparator();
        toolMenu.add(new ShowCheckboxesEnginesTableAction(enginesTableModel));
        toolMenu.add(new ResetCheckboxesEnginesTableAction(enginesTableModel));
        toolMenu.addSeparator();
        toolMenu.add(new EnginesSetFrameAction(enginesTable));
        toolMenu.add(new NceConsistEngineAction());
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
    public void radioButtonActionPerformed(ActionEvent ae) {
        log.debug("radio button activated");
        // clear any sorts by column
        clearTableSort(enginesTable);
        if (ae.getSource() == sortByNumber) {
            enginesTableModel.setSort(enginesTableModel.SORTBY_NUMBER);
        }
        if (ae.getSource() == sortByRoad) {
            enginesTableModel.setSort(enginesTableModel.SORTBY_ROAD);
        }
        if (ae.getSource() == sortByModel) {
            enginesTableModel.setSort(enginesTableModel.SORTBY_MODEL);
        }
        if (ae.getSource() == sortByConsist) {
            enginesTableModel.setSort(enginesTableModel.SORTBY_CONSIST);
        }
        if (ae.getSource() == sortByLocation) {
            enginesTableModel.setSort(enginesTableModel.SORTBY_LOCATION);
        }
        if (ae.getSource() == sortByDestination) {
            enginesTableModel.setSort(enginesTableModel.SORTBY_DESTINATION);
        }
        if (ae.getSource() == sortByTrain) {
            enginesTableModel.setSort(enginesTableModel.SORTBY_TRAIN);
        }
        if (ae.getSource() == sortByMoves) {
            enginesTableModel.setSort(enginesTableModel.SORTBY_MOVES);
        }
        if (ae.getSource() == sortByBuilt) {
            enginesTableModel.setSort(enginesTableModel.SORTBY_BUILT);
        }
        if (ae.getSource() == sortByOwner) {
            enginesTableModel.setSort(enginesTableModel.SORTBY_OWNER);
        }
        if (ae.getSource() == sortByValue) {
            enginesTableModel.setSort(enginesTableModel.SORTBY_VALUE);
        }
        if (ae.getSource() == sortByRfid) {
            enginesTableModel.setSort(enginesTableModel.SORTBY_RFID);
        }
        if (ae.getSource() == sortByLast) {
            enginesTableModel.setSort(enginesTableModel.SORTBY_LAST);
        }
        if (ae.getSource() == sortByDcc) {
            enginesTableModel.setSort(enginesTableModel.SORTBY_DCC_ADDRESS);
        }
        if (ae.getSource() == sortByComment) {
            enginesTableModel.setSort(enginesTableModel.SORTBY_COMMENT);
        }
    }

    public List<Engine> getSortByList() {
        return enginesTableModel.getSelectedEngineList();
    }

    EngineEditFrame engineEditFrame = null;

    // add, save or find button
    @Override
    public void buttonActionPerformed(ActionEvent ae) {
        // log.debug("engine button activated");
        if (ae.getSource() == findButton) {
            findEngine();
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

    public void textBoxActionPerformed(ActionEvent ae) {
        findEngine();
    }

    private void findEngine() {
        int rowindex = enginesTableModel.findEngineByRoadNumber(findEngineTextBox.getText());
        if (rowindex < 0) {
            JmriJOptionPane.showMessageDialog(this,
                    Bundle.getMessage("engineWithRoadNumNotFound", findEngineTextBox.getText()),
                    Bundle.getMessage("engineCouldNotFind"), JmriJOptionPane.INFORMATION_MESSAGE);
            return;

        }
        // clear any sorts by column
        clearTableSort(enginesTable);
        enginesTable.changeSelection(rowindex, 0, false, false);
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
        enginesTableModel.removeTableModelListener(this);
        enginesTableModel.dispose();
        if (engineEditFrame != null) {
            engineEditFrame.dispose();
        }
        InstanceManager.getOptionalDefault(JTablePersistenceManager.class).ifPresent(tpm -> {
            tpm.stopPersisting(enginesTable);
        });
        super.dispose();
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Table changed");
        }
        updateNumEngines();
    }

    private void updateNumEngines() {
        String count = Integer.toString(engineManager.getNumEntries());
        if (showAllLocos) {
            numEngines.setText(count);
        } else {
            int showCount = getSortByList().size();
            numEngines.setText(showCount + "/" + count);
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EnginesTableFrame.class);
}
