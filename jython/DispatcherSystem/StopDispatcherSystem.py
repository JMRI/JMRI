from javax.swing import JTable, JScrollPane, JFrame, JPanel, JComboBox,  BorderFactory, DefaultCellEditor, JLabel, UIManager, SwingConstants, JFileChooser
from javax.swing.table import  TableCellRenderer, DefaultTableCellRenderer
from java.awt.event import MouseAdapter,MouseEvent, WindowListener, WindowEvent
from java.awt import GridLayout, Dimension, BorderLayout, Color
from javax.swing.table import AbstractTableModel, DefaultTableModel
from java.lang.Object import getClass
import jarray
from javax.swing.event import TableModelListener, TableModelEvent
from javax.swing.filechooser import FileNameExtensionFilter
from org.apache.commons.io import FilenameUtils
from java.io import File
import java.awt.Dimension

class createandshowGUI3(TableModelListener):

    def __init__(self, class_StopMaster):
        global DF
        DF = jmri.InstanceManager.getDefault(jmri.jmrit.dispatcher.DispatcherFrame)
        #DF.setState(DF.ICONIFIED);
        self.activeTrainsList = DF.getActiveTrainsList()

        self.logLevel = 0
        self.class_StopMaster = class_StopMaster
        #Create and set up the window.

        self.initialise_model(class_StopMaster)
        self.frame = JFrame("Modify System")
        self.frame.setSize(600, 600);

        self.completeTablePanel()
        # print "about to populate"
        self.populate_action(None)
        self.cancel = False


    def completeTablePanel(self):

        self.topPanel= JPanel();
        self.topPanel.setLayout(BoxLayout(self.topPanel, BoxLayout.X_AXIS))
        self.self_table()

        scrollPane = JScrollPane(self.table);
        scrollPane.setPreferredSize( Dimension(1000, 300))

        self.topPanel.add(scrollPane);

        self.buttonPane = JPanel();
        self.buttonPane.setLayout(BoxLayout(self.buttonPane, BoxLayout.LINE_AXIS))
        self.buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10))
        #
        # button_add = JButton("Add Row", actionPerformed = self.add_row_action)
        # self.buttonPane.add(button_add);
        # self.buttonPane.add(Box.createRigidArea(Dimension(10, 0)))

        button_populate = JButton("Refresh", actionPerformed = self.populate_action)
        self.buttonPane.add(button_populate);
        self.buttonPane.add(Box.createRigidArea(Dimension(10, 0)))

        # button_tidy = JButton("Tidy", actionPerformed = self.tidy_action)
        # self.buttonPane.add(button_tidy);
        # self.buttonPane.add(Box.createRigidArea(Dimension(10, 0)))

        button_del_trains = JButton("Delete Trains", actionPerformed = self.del_trains_action)
        self.buttonPane.add(button_del_trains)
        self.buttonPane.add(Box.createHorizontalGlue());

        button_del_transits = JButton("Delete Transits", actionPerformed = self.del_transits_action)
        self.buttonPane.add(button_del_transits)
        self.buttonPane.add(Box.createHorizontalGlue());

        button_del_routes = JButton("Delete Routes", actionPerformed = self.del_routes_action)
        self.buttonPane.add(button_del_routes)
        self.buttonPane.add(Box.createHorizontalGlue());

        button_cancel = JButton("Close", actionPerformed = self.cancel_action)
        self.buttonPane.add(button_cancel)
        self.buttonPane.add(Box.createHorizontalGlue());

        contentPane = self.frame.getContentPane()

        contentPane.removeAll()
        contentPane.add(self.topPanel, BorderLayout.CENTER)
        contentPane.add(self.buttonPane, BorderLayout.PAGE_END)

        self.frame.pack();
        self.frame.setVisible(True)
        return

    def buttonPanel(self):
        row1_1_button = JButton("Add Row", actionPerformed = self.add_row_action)
        row1_2_button = JButton("Save", actionPerformed = self.save_action)

        row1 = JPanel()
        row1.setLayout(BoxLayout(row1, BoxLayout.X_AXIS))

        row1.add(Box.createVerticalGlue())
        row1.add(Box.createRigidArea(Dimension(20, 0)))
        row1.add(row1_1_button)
        row1.add(Box.createRigidArea(Dimension(20, 0)))
        row1.add(row1_2_button)

        layout = BorderLayout()
        # layout.setHgap(10);
        # layout.setVgap(10);

        jPanel = JPanel()
        jPanel.setLayout(layout);
        jPanel.add(self.table,BorderLayout.NORTH)
        jPanel.add(row1,BorderLayout.SOUTH)

        #return jPanel
        return topPanel

    def initialise_model(self, class_StopMaster):

        self.model = None
        self.model = MyTableModel3()
        self.table = JTable(self.model)
        self.model.addTableModelListener(MyModelListener3(self, class_StopMaster));
        self.class_StopMaster = class_StopMaster

    def self_table(self):

        self.table.setFillsViewportHeight(True);
        self.table.setRowHeight(30);
        columnModel = self.table.getColumnModel();

        [setup_train_col, del_setup_train_col,  active_train_col, transit_col, del_transit_col, route_col, del_route_col] = [0, 1, 2, 3, 4, 5, 6]
        columnModel.getColumn(setup_train_col).setPreferredWidth(200);
        columnModel.getColumn(active_train_col).setPreferredWidth(200);
        columnModel.getColumn(transit_col).setPreferredWidth(200);
        columnModel.getColumn(route_col).setPreferredWidth(200);

        jpane = JScrollPane(self.table)
        panel = JPanel()
        panel.add(jpane)
        result = JScrollPane(panel)
        return self.table

    def add_row_action(self, e):
        # model = e.getSource()
        # data = self.model.getValueAt(0, 0)
        # count = self.model.getRowCount()
        # colcount = self.model.getColumnCount()
        self.model.add_row()
        self.completeTablePanel()

    def populate_action(self, event):

        DF = jmri.InstanceManager.getDefault(jmri.jmrit.dispatcher.DispatcherFrame)
        self.activeTrainsList = DF.getActiveTrainsList()
        trains_to_put_in_firstcol = self.activeTrainsList
        self.model.populate()
        self.completeTablePanel()

    # def tidy_action(self,e):
    #     self.model.remove_not_set_row()
    #     self.completeTablePanel()

    def del_trains_action(self, e):
        global trains_allocated
        for train_name_to_remove in trains_allocated:
            trains_allocated.remove(train_name_to_remove)
        self.completeTablePanel()

    def delete_transits(self):
        global trains
        # need to avoid concurrency issues when deleting more that one transit
        # use java.util.concurrent.CopyOnWriteArrayList  so can iterate through the transits while deleting
        DF = jmri.InstanceManager.getDefault(jmri.jmrit.dispatcher.DispatcherFrame)
        #DF.setState(DF.ICONIFIED);

        activeTrainList = java.util.concurrent.CopyOnWriteArrayList()
        for activeTrain in DF.getActiveTrainsList():
            activeTrainList.add(activeTrain)

        for activeTrain in activeTrainList:
            if self.logLevel > 0: print ("active train", activeTrain)
            #swap the direction of the train
            # (found need to do this as train went off in wrong direction after setting new trainsit)
            train_name = activeTrain.getTrainName()
            self.swap_direction(train_name)
            DF.terminateActiveTrain(activeTrain)
        DF = None

    def del_transits_action(self,e):
        self.delete_transits()
        self.completeTablePanel()

    def delete_routes(self):
        self.class_StopMaster.stop_route_threads()

    def del_routes_action(self, e):
        self.delete_routes()
        self.completeTablePanel()

    def cancel_action(self, event):
        self.frame.dispatchEvent(WindowEvent(self.frame, WindowEvent.WINDOW_CLOSING));
    def refresh(self):
        self.completeTablePanel()



class MyModelListener3(TableModelListener):

    def __init__(self, class_createandshowGUI3, class_StopMaster):
        self.class_createandshowGUI3 = class_createandshowGUI3
        self.class_StopMaster = class_StopMaster
        self.cancel = False
        self.logLevel = 0
        DF = jmri.InstanceManager.getDefault(jmri.jmrit.dispatcher.DispatcherFrame)
        self.java_active_trains_list = DF.getActiveTrainsList()

    def tableChanged(self, e) :
        [setup_train_col, del_setup_train_col,  active_train_col, transit_col, del_transit_col, route_col, del_route_col] = [0, 1, 2, 3, 4, 5, 6]

        global trains_allocated
        row = e.getFirstRow()
        column = e.getColumn()
        model = e.getSource()
        columnName = model.getColumnName(column)
        data = model.getValueAt(row, column)
        tablemodel = self.class_createandshowGUI3.model

        if column == del_setup_train_col:
            train_name = str(model.getValueAt(row, setup_train_col))
            if self.logLevel > 0: print "trains_allocated", trains_allocated
            if self.logLevel > 0: print "train_name", train_name
            for train in trains_allocated:
                if train == train_name:
                    trains_allocated.remove(train)
            self.class_StopMaster.remove_values(train)

            # model.fireTableDataChanged()
            # self.class_createandshowGUI3.refresh()
            # self.class_createandshowGUI3.completeTablePanel()
            self.class_createandshowGUI3.populate_action(None)

        elif column == del_transit_col:

            transit = str(model.getValueAt(row, transit_col))
            train_name = [active_train.getTrainName() for active_train in self.java_active_trains_list \
                       if active_train.getTransit().getUserName() == transit]
            if len(train_name) > 0:
                self.delete_transit(train_name[0])
                #tablemodel.fireTableDataChanged()
                #self.class_createandshowGUI3.refresh()
                self.class_createandshowGUI3.populate_action(None)


            # train_name = str(model.getValueAt(row, setup_train_col))
            # self.delete_transit(train_name)
            # #self.class_createandshowGUI3.completeTablePanel()
            # model.fireTableDataChanged()

        elif column == del_route_col:
            transit = str(model.getValueAt(row, transit_col))
            train_name = [active_train.getTrainName() for active_train in self.java_active_trains_list \
                          if active_train.getTransit().getUserName() == transit]
            # train_name = str(model.getValueAt(row, setup_train_col))
            if len(train_name) > 0:
                self.delete_route(train_name[0])
                #tablemodel.fireTableDataChanged()
                #self.class_createandshowGUI3.refresh()
                self.class_createandshowGUI3.populate_action(None)
        else:
            pass

    def delete_route(self, train_name):
        global instanceList
        #stop all threads
        activeThreadList = java.util.concurrent.CopyOnWriteArrayList()
        for thread in instanceList:
            activeThreadList.add(thread)

        for thread in activeThreadList:
            thread_name = "" + thread.getName()
            if thread_name.startswith("running_route_"):
                #determine the train nme
                thread_train_name = self.class_StopMaster.determine_train_name(thread_name,thread)
                #remove the train from the transit
                if train_name == thread_train_name:
                    #remove the train from the list of trains
                    self.remove_train_name(train_name)
                if thread is not None:
                    if thread.isRunning():
                        if self.logLevel > 0: print 'Stop "{}" thread'.format(thread.getName())
                        thread.stop()
                        instanceList = [instance for instance in instanceList if instance != thread]
                    else:
                        #need this for scheduler in wait state
                        thread.stop()
                        instanceList = [instance for instance in instanceList if instance != thread]

    def remove_train_name(self, train_name):
        global trains_allocated
        global trains_dispatched
        if self.logLevel > 0: print("train to remove", train_name)
        # for train in trains_allocated:
        #     if self.logLevel > 0: print "train in trains_allocated", train, ": trains_allocated", trains_allocated
        #     if train == train_name:
        #         trains_allocated.remove(train)
        for train in trains_dispatched:
            #print "train in trains_allocated", train, ": trains_allocated", trains_allocated
            if train == train_name:
                trains_dispatched.remove(train)


    def delete_transit(self, train_name):
        DF = jmri.InstanceManager.getDefault(jmri.jmrit.dispatcher.DispatcherFrame)
        #DF.setState(DF.ICONIFIED);
        activeTrainList = java.util.concurrent.CopyOnWriteArrayList()
        for activeTrain in DF.getActiveTrainsList():
            activeTrainList.add(activeTrain)

        active_train = [activeTrain for activeTrain in activeTrainList \
                        if activeTrain.getTrainName() == train_name]
        if self.logLevel > 0: print ("active_train", active_train)
        if len(active_train) > 0:
            DF.terminateActiveTrain(active_train[0])
        # train train_name needs its direction swapped
        # (found need to do this as train went off in wrong direction after setting new trainsit)
        self.swap_direction(train_name)

    def swap_direction(self, train_name):
        global trains
        train = [trains[t_name] for t_name in trains if t_name ==train_name]
        print "train", train
        direction_of_train = train["direction"]
        print "direction_of_train", direction_of_train
        if direction_of_train == "forward":
            direction_of_train = "reverse"
        else:
            direction_of_train = "forward"
        train["direction"] = direction_of_train


class ComboBoxCellRenderer1 (TableCellRenderer):

    def getTableCellRendererComponent(self, jtable, value, isSelected, hasFocus, row, column) :
        panel = self.createPanel(value)
        return panel

    def createPanel(self, s) :
        p = JPanel(BorderLayout())
        p.add(JLabel(str(s), JLabel.LEFT), BorderLayout.WEST)
        icon = UIManager.getIcon("Table.descendingSortIcon");
        p.add(JLabel(icon, JLabel.RIGHT), BorderLayout.EAST);
        p.setBorder(BorderFactory.createLineBorder(Color.blue));
        return p;


class MyTableModel3 (DefaultTableModel):

    columnNames = ["Train", "Delete Train", "Active Train", "Transit", "Del Transit", "Route", "Del Route"]

    def __init__(self):
        l1 = ["", False, "", "", False, "", False]
        self.data = [l1]
        self.logLevel = 0

    def remove_not_set_row(self):
        for row in reversed(range(len(self.data))):
            if self.data[row][1] == "":
                self.data.pop(row)

    def populate(self):
        global trains_allocated
        global trains_allocated
        global trains_dispatched
        DF = jmri.InstanceManager.getDefault(jmri.jmrit.dispatcher.DispatcherFrame)
        java_active_trains_list = DF.getActiveTrainsList()
        java_active_trains_Arraylist= java.util.ArrayList(java_active_trains_list)
        if self.logLevel > 0: print ("populate")
        for row in reversed(range(len(self.data))):
            self.data.pop(row)
        # append all active trains to put in dropdown
        active_train_name = ""
        transit = ""
        transit_name = ""
        route_name = ""
        for setup_train in trains_allocated:
            active_train = [active_train for active_train in java_active_trains_list \
                            if active_train.getTrainName() == setup_train]
            if self.logLevel > 0: print "active_train", active_train, "len(active_train)", len(active_train)
            if len(active_train) > 0 :
                active_train = active_train[0]
                active_train_name = active_train.getTrainName()
                transit = [active_train.getTransit() for active_train in java_active_trains_list \
                           if active_train.getTrainName() == active_train_name]
                if self.logLevel > 0: print "active_train_name", active_train_name
                if self.logLevel > 0: print "transit"  , transit
                transit_name = transit[0].getUserName()
            else:
                active_train = ""
                active_train_name = ""
                transit_name = ""
            if self.logLevel > 0: print("train", active_train)
            if self.logLevel > 0: print("transit_name", transit_name)
            route_name = self.get_route(active_train_name)
            if self.logLevel > 0: print ("route_name", route_name)
            self.data.append([setup_train, False, active_train_name, transit_name, False, route_name, False])

        active_trains_not_setup = [active_train_name for active_train_name in trains_dispatched \
                                   if active_train_name not in trains_allocated]
        # for active_train in java_active_trains_list:
        #     if active_train.getTrainName() not in trains_allocated:
        for active_train_name in active_trains_not_setup:
            active_train = [active_train for active_train in java_active_trains_list \
                            if active_train.getTrainName() == active_train_name]
            if len(active_train) > 0 :
                active_train = active_train[0]
                active_train_name = active_train.getTrainName()
                transit = [active_train.getTransit() for active_train in java_active_trains_list \
                           if active_train.getTrainName() == active_train_name]
                transit_name = transit[0].getUserName()
            else:
                active_train = ""
                active_train_name = ""
                transit_name = ""
            if self.logLevel > 0: print("train", active_train_name)
            route_name = self.get_route(active_train_name)
            if self.logLevel > 0: print ("route_name", route_name)
            self.data.append(["", False, active_train_name, transit_name, False, route_name, False])


    def get_route(self, train_name):
        #look at all threads
        activeThreadList = java.util.concurrent.CopyOnWriteArrayList()
        for thread in instanceList:
            activeThreadList.add(thread)

        for thread in instanceList:
            thread_name = "" + thread.getName()
            if thread_name.startswith("running_route_"):
                route_name = thread_name.replace("running_route_", "")
                thread_train_name = StopMaster().determine_train_name(thread_name,thread)
                if self.logLevel > 0: print "thread name", thread_name, "route_name", route_name, "thread_train_name", thread_train_name, "train_name", train_name
                # #remove the train from the transit
                if train_name == thread_train_name:
                    return route_name
                #return route_name
        return ""


    def getColumnCount(self) :
        return len(self.columnNames)


    def getRowCount(self) :
        return len(self.data)


    def getColumnName(self, col) :
        return self.columnNames[col]


    def getValueAt(self, row, col) :
        return self.data[row][col]

    def getColumnClass(self, c) :
        # if c <= 1:
        #     return java.lang.Boolean.getClass(JComboBox)
        return java.lang.Boolean.getClass(self.getValueAt(0,c))


    #only include if table editable
    def isCellEditable(self, row, col) :
        # Note that the data/cell address is constant,
        # no matter where the cell appears onscreen.
        return True

    # only include if data can change.
    def setValueAt(self, value, row, col) :
        self.data[row][col] = value
        self.fireTableCellUpdated(row, col)
