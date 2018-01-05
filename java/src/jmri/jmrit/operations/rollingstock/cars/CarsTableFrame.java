package jmri.jmrit.operations.rollingstock.cars;

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
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumnModel;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.locations.schedules.ScheduleManager;
import jmri.jmrit.operations.locations.tools.ModifyLocationsAction;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.tools.TrainsByCarTypeAction;
import jmri.swing.JTablePersistenceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for adding and editing the car roster for operations.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2008, 2009, 2010, 2011, 2012, 2013,
 * 2014
 */
public class CarsTableFrame extends OperationsFrame implements TableModelListener {

    CarsTableModel carsTableModel;
    JTable carsTable;
    boolean showAllCars;
    String locationName;
    String trackName;
    CarManager carManager = InstanceManager.getDefault(CarManager.class);

    // labels
    JLabel numCars = new JLabel();
    JLabel textCars = new JLabel(Bundle.getMessage("cars"));
    JLabel textSep1 = new JLabel("      ");

    // radio buttons
    JRadioButton sortByNumber = new JRadioButton(Bundle.getMessage("Number"));
    JRadioButton sortByRoad = new JRadioButton(Bundle.getMessage("Road"));
    JRadioButton sortByType = new JRadioButton(Bundle.getMessage("Type"));
    JRadioButton sortByColor = new JRadioButton(Bundle.getMessage("Color"));
    JRadioButton sortByLoad = new JRadioButton(Bundle.getMessage("Load"));
    JRadioButton sortByKernel = new JRadioButton(Bundle.getMessage("Kernel"));
    JRadioButton sortByLocation = new JRadioButton(Bundle.getMessage("Location"));
    JRadioButton sortByDestination = new JRadioButton(Bundle.getMessage("Destination"));
    JRadioButton sortByFinalDestination = new JRadioButton(Bundle.getMessage("FD"));
    JRadioButton sortByRwe = new JRadioButton(Bundle.getMessage("RWE"));
    JRadioButton sortByTrain = new JRadioButton(Bundle.getMessage("Train"));
    JRadioButton sortByMoves = new JRadioButton(Bundle.getMessage("Moves"));
    JRadioButton sortByBuilt = new JRadioButton(Bundle.getMessage("Built"));
    JRadioButton sortByOwner = new JRadioButton(Bundle.getMessage("Owner"));
    JRadioButton sortByValue = new JRadioButton(Setup.getValueLabel());
    JRadioButton sortByRfid = new JRadioButton(Setup.getRfidLabel());
    JRadioButton sortByWait = new JRadioButton(Bundle.getMessage("Wait"));
    JRadioButton sortByPickup = new JRadioButton(Bundle.getMessage("Pickup"));
    JRadioButton sortByLast = new JRadioButton(Bundle.getMessage("Last"));
    ButtonGroup group = new ButtonGroup();

    // major buttons
    JButton addButton = new JButton(Bundle.getMessage("ButtonAdd"));
    JButton findButton = new JButton(Bundle.getMessage("Find"));
    JButton saveButton = new JButton(Bundle.getMessage("ButtonSave"));

    JTextField findCarTextBox = new JTextField(6);

    public CarsTableFrame(boolean showAllCars, String locationName, String trackName) {
        super(Bundle.getMessage("TitleCarsTable"));
        this.showAllCars = showAllCars;
        this.locationName = locationName;
        this.trackName = trackName;
        // general GUI configuration
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // Set up the table in a Scroll Pane..
        carsTableModel = new CarsTableModel(showAllCars, locationName, trackName);
        carsTable = new JTable(carsTableModel);
        JScrollPane carsPane = new JScrollPane(carsTable);
        carsPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        carsTableModel.initTable(carsTable, this);

        // load the number of cars and listen for changes
        updateNumCars();
        carsTableModel.addTableModelListener(this);

        // Set up the control panel
        // row 1
        JPanel cp1 = new JPanel();
        cp1.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("SortBy")));
        cp1.add(sortByNumber);
        cp1.add(sortByRoad);
        cp1.add(sortByType);

        JPanel clp = new JPanel();
        clp.setBorder(BorderFactory.createTitledBorder(""));
        clp.add(sortByColor);
        clp.add(sortByLoad);
        cp1.add(clp);
        cp1.add(sortByKernel);
        cp1.add(sortByLocation);

        JPanel destp = new JPanel();
        destp.setBorder(BorderFactory.createTitledBorder(""));
        destp.add(sortByDestination);
        destp.add(sortByFinalDestination);
        destp.add(sortByRwe);
        cp1.add(destp);
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
        if (InstanceManager.getDefault(ScheduleManager.class).numEntries() > 0) {
            movep.add(sortByWait);
            movep.add(sortByPickup);
        }
        movep.add(sortByLast);
        cp1.add(movep);

        // row 2
        JPanel cp2 = new JPanel();
        cp2.setLayout(new BoxLayout(cp2, BoxLayout.X_AXIS));

        JPanel cp2Add = new JPanel();
        cp2Add.setBorder(BorderFactory.createTitledBorder(""));
        cp2Add.add(numCars);
        cp2Add.add(textCars);
        cp2Add.add(textSep1);
        cp2Add.add(addButton);
        cp2.add(cp2Add);

        JPanel cp2Find = new JPanel();
        cp2Find.setBorder(BorderFactory.createTitledBorder(""));
        findButton.setToolTipText(Bundle.getMessage("findCar"));
        findCarTextBox.setToolTipText(Bundle.getMessage("findCar"));
        cp2Find.add(findButton);
        cp2Find.add(findCarTextBox);
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
        sortByFinalDestination.setToolTipText(Bundle.getMessage("FinalDestination"));
        sortByRwe.setToolTipText(Bundle.getMessage("ReturnWhenEmpty"));
        sortByPickup.setToolTipText(Bundle.getMessage("TipPickup"));
        sortByLast.setToolTipText(Bundle.getMessage("TipLastMoved"));

        JScrollPane controlPane = new JScrollPane(controlPanel);

        getContentPane().add(carsPane);
        getContentPane().add(controlPane);

        // setup buttons
        addButtonAction(addButton);
        addButtonAction(findButton);
        addButtonAction(saveButton);

        sortByNumber.setSelected(true);
        addRadioButtonAction(sortByNumber);
        addRadioButtonAction(sortByRoad);
        addRadioButtonAction(sortByType);
        addRadioButtonAction(sortByColor);
        addRadioButtonAction(sortByLoad);
        addRadioButtonAction(sortByKernel);
        addRadioButtonAction(sortByLocation);
        addRadioButtonAction(sortByDestination);
        addRadioButtonAction(sortByFinalDestination);
        addRadioButtonAction(sortByRwe);
        addRadioButtonAction(sortByTrain);
        addRadioButtonAction(sortByMoves);
        addRadioButtonAction(sortByBuilt);
        addRadioButtonAction(sortByOwner);
        addRadioButtonAction(sortByValue);
        addRadioButtonAction(sortByRfid);
        addRadioButtonAction(sortByWait);
        addRadioButtonAction(sortByPickup);
        addRadioButtonAction(sortByLast);

        group.add(sortByNumber);
        group.add(sortByRoad);
        group.add(sortByType);
        group.add(sortByColor);
        group.add(sortByLoad);
        group.add(sortByKernel);
        group.add(sortByLocation);
        group.add(sortByDestination);
        group.add(sortByFinalDestination);
        group.add(sortByRwe);
        group.add(sortByTrain);
        group.add(sortByMoves);
        group.add(sortByBuilt);
        group.add(sortByOwner);
        group.add(sortByValue);
        group.add(sortByRfid);
        group.add(sortByWait);
        group.add(sortByPickup);
        group.add(sortByLast);

        // sort by location
        if (!showAllCars) {
            sortByLocation.doClick();
            if (locationName != null) {
                String title = Bundle.getMessage("TitleCarsTable") + " " + locationName;
                if (trackName != null) {
                    title = title + " " + trackName;
                }
                setTitle(title);
            }
        }

        // build menu
        JMenuBar menuBar = new JMenuBar();
        JMenu toolMenu = new JMenu(Bundle.getMessage("MenuTools"));
        toolMenu.add(new CarRosterMenu(Bundle.getMessage("TitleCarRoster"), CarRosterMenu.MAINMENU, this));
        toolMenu.add(new ShowCheckboxesCarsTableAction(carsTableModel));
        toolMenu.add(new ResetCheckboxesCarsTableAction(carsTableModel));
        toolMenu.add(new ModifyLocationsAction());
        toolMenu.add(new TrainsByCarTypeAction());
        toolMenu.add(new CarsSetFrameAction(carsTable));
        menuBar.add(toolMenu);
        menuBar.add(new jmri.jmrit.operations.OperationsMenu());
        setJMenuBar(menuBar);
        addHelpMenu("package.jmri.jmrit.operations.Operations_Cars", true); // NOI18N

        initMinimumSize();

        addHorizontalScrollBarKludgeFix(controlPane, controlPanel);

        // create ShutDownTasks
        createShutDownTask();
    }

    @Override
    public void radioButtonActionPerformed(java.awt.event.ActionEvent ae) {
        log.debug("radio button activated");
        // clear any sorts by column
        clearTableSort(carsTable);
        if (ae.getSource() == sortByNumber) {
            carsTableModel.setSort(carsTableModel.SORTBY_NUMBER);
        }
        if (ae.getSource() == sortByRoad) {
            carsTableModel.setSort(carsTableModel.SORTBY_ROAD);
        }
        if (ae.getSource() == sortByType) {
            carsTableModel.setSort(carsTableModel.SORTBY_TYPE);
        }
        if (ae.getSource() == sortByColor) {
            carsTableModel.setSort(carsTableModel.SORTBY_COLOR);
        }
        if (ae.getSource() == sortByLoad) {
            carsTableModel.setSort(carsTableModel.SORTBY_LOAD);
        }
        if (ae.getSource() == sortByKernel) {
            carsTableModel.setSort(carsTableModel.SORTBY_KERNEL);
        }
        if (ae.getSource() == sortByLocation) {
            carsTableModel.setSort(carsTableModel.SORTBY_LOCATION);
        }
        if (ae.getSource() == sortByDestination) {
            carsTableModel.setSort(carsTableModel.SORTBY_DESTINATION);
        }
        if (ae.getSource() == sortByFinalDestination) {
            carsTableModel.setSort(carsTableModel.SORTBY_FINALDESTINATION);
        }
        if (ae.getSource() == sortByRwe) {
            carsTableModel.setSort(carsTableModel.SORTBY_RWE);
        }
        if (ae.getSource() == sortByTrain) {
            carsTableModel.setSort(carsTableModel.SORTBY_TRAIN);
        }
        if (ae.getSource() == sortByMoves) {
            carsTableModel.setSort(carsTableModel.SORTBY_MOVES);
        }
        if (ae.getSource() == sortByBuilt) {
            carsTableModel.setSort(carsTableModel.SORTBY_BUILT);
        }
        if (ae.getSource() == sortByOwner) {
            carsTableModel.setSort(carsTableModel.SORTBY_OWNER);
        }
        if (ae.getSource() == sortByValue) {
            carsTableModel.setSort(carsTableModel.SORTBY_VALUE);
        }
        if (ae.getSource() == sortByRfid) {
            carsTableModel.setSort(carsTableModel.SORTBY_RFID);
        }
        if (ae.getSource() == sortByWait) {
            carsTableModel.setSort(carsTableModel.SORTBY_WAIT);
        }
        if (ae.getSource() == sortByPickup) {
            carsTableModel.setSort(carsTableModel.SORTBY_PICKUP);
        }
        if (ae.getSource() == sortByLast) {
            carsTableModel.setSort(carsTableModel.SORTBY_LAST);
        }
    }

    public List<Car> getSortByList() {
        return carsTableModel.carList;
    }

    CarEditFrame f = null;

    // add, find or save button
    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        // log.debug("car button activated");
        if (ae.getSource() == findButton) {
            int rowindex = carsTableModel.findCarByRoadNumber(findCarTextBox.getText());
            if (rowindex < 0) {
                JOptionPane.showMessageDialog(this, MessageFormat.format(Bundle.getMessage("carWithRoadNumNotFound"),
                        new Object[]{findCarTextBox.getText()}), Bundle.getMessage("carCouldNotFind"),
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            // clear any sorts by column
            clearTableSort(carsTable);
            carsTable.changeSelection(rowindex, 0, false, false);
            return;
        }
        if (ae.getSource() == addButton) {
            if (f != null) {
                f.dispose();
            }
            f = new CarEditFrame();
            f.initComponents(); // default is add car
        }
        if (ae.getSource() == saveButton) {
            if (carsTable.isEditing()) {
                log.debug("cars table edit true");
                carsTable.getCellEditor().stopCellEditing();
            }
            OperationsXml.save();
            if (Setup.isCloseWindowOnSaveEnabled()) {
                dispose();
            }
        }
    }

    protected int[] getCurrentTableColumnWidths() {
        TableColumnModel tcm = carsTable.getColumnModel();
        int[] widths = new int[tcm.getColumnCount()];
        for (int i = 0; i < tcm.getColumnCount(); i++) {
            widths[i] = tcm.getColumn(i).getWidth();
        }
        return widths;
    }

    @Override
    public void dispose() {
        carsTableModel.removeTableModelListener(this);
        carsTableModel.dispose();
        if (f != null) {
            f.dispose();
        }
        InstanceManager.getOptionalDefault(JTablePersistenceManager.class).ifPresent(tpm -> {
            tpm.stopPersisting(carsTable);
        });
        super.dispose();
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Table changed");
        }
        updateNumCars();
    }

    private void updateNumCars() {
        String totalNumber = Integer.toString(InstanceManager.getDefault(CarManager.class).getNumEntries());
        if (showAllCars) {
            numCars.setText(totalNumber);
            return;
        }
        String showNumber = Integer.toString(getSortByList().size());
        numCars.setText(showNumber + "/" + totalNumber);
    }

    private final static Logger log = LoggerFactory.getLogger(CarsTableFrame.class);

}
