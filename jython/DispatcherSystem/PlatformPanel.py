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
from java.awt.event import WindowAdapter
from java.util.concurrent import CountDownLatch

class CreateAndShowGUI7(TableModelListener):

    def __init__(self):
        # Define a listener that counts down when the window is closed
        class MyWindowListener(WindowAdapter):
            def windowClosed(self, e):
                global latch
                if "latch" in globals():
                    print "counting down latch"
                    latch.countDown()
        self.logLevel = 0

        # Create and set up the window.
        self.initialise_model()
        self.frame = JFrame("for a station group put the group name in one of the stations")
        self.frame.setSize(600, 600)
        self.completeTablePanel()
        # print "about to populate"
        self.populate_action(None)
        self.cancel = False
        self.toggle = True
        self.frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE)
        self.frame.addWindowListener(MyWindowListener())

    def completeTablePanel(self):
        self.topPanel= JPanel()
        self.topPanel.setLayout(BoxLayout(self.topPanel, BoxLayout.X_AXIS))
        self.self_table()

        scrollPane = JScrollPane(self.table)
        scrollPane.setSize(600,600)


        self.topPanel.add(scrollPane)

        self.buttonPane = JPanel()
        self.buttonPane.setLayout(BoxLayout(self.buttonPane, BoxLayout.LINE_AXIS))
        self.buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10))

        button_close = JButton("Save and Close", actionPerformed = self.close_action)
        self.buttonPane.add(button_close)
        self.buttonPane.add(Box.createHorizontalGlue())

        contentPane = self.frame.getContentPane()

        contentPane.removeAll()
        contentPane.add(self.topPanel, BorderLayout.CENTER)
        contentPane.add(self.buttonPane, BorderLayout.PAGE_END)
        self.frame.pack()
        self.frame.setVisible(True)

        return

    def initialise_model(self):

        self.model = None
        self.model = MyTableModel7()
        self.table = JTable(self.model)
        self.model.addTableModelListener(MyModelListener7(self));

    def self_table(self):

        self.table.setFillsViewportHeight(True);
        self.table.setRowHeight(30)

        columnModel = self.table.getColumnModel()

        [station_col, platform_col, station_group_col] = [0, 1, 2]
        columnModel.getColumn(station_col).setPreferredWidth(300)
        columnModel.getColumn(platform_col).setPreferredWidth(200)
        columnModel.getColumn(station_group_col).setPreferredWidth(200)

        jpane = JScrollPane(self.table)
        panel = JPanel()
        panel.add(jpane)
        result = JScrollPane(panel)
        return self.table

    def add_row_action(self, e):
        model = e.getSource()
        # data = self.model.getValueAt(0, 0)
        count = self.model.getRowCount()
        colcount = self.model.getColumnCount()
        self.model.add_row()
        self.completeTablePanel()

    def get_locations_list(self):
        LocationsManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.locations.LocationManager)
        locations_list = LocationsManager.getLocationsByNameList()
        my_list = [[loc.getName()] for loc in locations_list]

        return my_list

    def populate_action(self, event):
        items_to_put_in_dropdown = self.get_station_list()
        self.model.populate(items_to_put_in_dropdown)
        self.tidy_action(None)
        self.completeTablePanel()
        pass

    def tidy_action(self,e):
        self.model.remove_not_set_row()
        height = self.set_frame_height()
        self.frame.setPreferredSize(Dimension(600, height))


    def set_frame_height(self):
        size_of_one_row = 30
        # set height of non row data
        height = 130
        # add height of row data
        for row in reversed(range(len(self.model.data))):
            height += size_of_one_row
        height = min(height, 800)
        # print "height", height
        return height

    def close_action(self, event):
        self.save()
        self.frame.dispatchEvent(WindowEvent(self.frame, WindowEvent.WINDOW_CLOSING));

    def save_action(self, event):
        self.save()

    def save(self):
        [station_col, platform_col, station_group_col] = [0, 1, 2,]
        # print "save_action"
        # self.clear_everything()
        # print "apply action"
        for row in reversed(range(len(self.model.data))):
            # time_name = str(self.model.data[row][time_col])
            station_name = str(self.model.data[row][station_col])
            platform_name = str(self.model.data[row][platform_col])
            station_group_name = str(self.model.data[row][station_group_col])
            # repeat_name = str(self.model.data[row][repeat_col])

            # if time_name != "" and route_name != "" and train_name_val != "":
            if station_name != "":
                self.save_values(station_name, platform_name, station_group_name)
                pass
            else:
                msg = "Cannot save row: " + str(row) + " train name, route or delay is not set"
                OptionDialog().displayMessage(msg,"")
        self.completeTablePanel()
        if self.model.getRowCount() == 0:
            self.frame.dispatchEvent(WindowEvent(self.frame, WindowEvent.WINDOW_CLOSING))

    def save_values(self, station_name, platform_name, station_group_name):
        LocationManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.locations.LocationManager)
        location = LocationManager.getLocationByName(station_name)
        self.set_location_platform(location, platform_name)
        self.set_location_station_group(location, station_group_name)

    def set_location_platform(self, location, location_value):
        if location_value != None:
            # call using set_turnout(location, "Platform 1")
            tag = "IMIS:"+ location.getName().replace(" ","_") +"_Platform"
            memory = memories.provideMemory(tag)
            memory.setValue(location_value)

    def set_location_station_group(self, location, location_value):
        if location_value != None:
            # call using set_location_station_group(location, "Winchester")
            tag = "IMIS:"+ location.getName().replace(" ","_") +"_StationGroup"
            memory = memories.provideMemory(tag)
            memory.setValue(location_value)

    def get_station_list(self):
        # only display the locations that are in the routes, as these are the ones that can be displayed in the timetable
        # the locations can get out of synch if we have deleted a station
        TrainManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.trains.TrainManager)
        train_list = TrainManager.getTrainsByTimeList()
        my_scheduled_route_list = [train.getRoute() for train in train_list]
        if None in my_scheduled_route_list:
            OptionDialog().displayMessage("check scheduled routes are entered correctly\ncannot proceed with timetable")
            return []
        else:
            RouteManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.routes.RouteManager)
            station_list = []

            for route in my_scheduled_route_list:
                route_locations_list = route.getLocationsBySequenceList()
                station_list1 = [str(route_location.getName()) for route_location in route_locations_list \
                                 if ".py" not in route_location.getName()]
                for x in station_list1:
                    if x not in station_list:
                        station_list.append(x)
                station_list.sort()
            if self.logLevel > 0: print "station list", station_list
            return station_list

class MyModelListener7(TableModelListener):

    # def __init__(self, class_CreateAndShowGUI6, class_SchedulerPanel):
    def __init__(self, class_CreateAndShowGUI6):
        self.class_CreateAndShowGUI6 = class_CreateAndShowGUI6
        # self.class_SchedulerPanel = class_SchedulerPanel
        self.cancel = False
        self.logLevel = 0
        self.i = 0
    def tableChanged(self, e) :
        global CreateAndShowGUI6_glb
        # print "INDES", self.i
        row = e.getFirstRow()
        column = e.getColumn()
        # print "table changed", column
        self.model = e.getSource()
        self.class_CreateAndShowGUI6.save()    # save everything when the table is chabged
        
    def delete_row(self, row):
        # print "deleting row"
        self.model.data.pop(row)


    def show_time_picker(self):
        # Show a simple JOptionPane input dialog for time selection
        selected_time = JOptionPane.showInputDialog(None, "Select a time (HH:mm):")
        # if selected_time:
            # print("Selected time:", selected_time)
        return selected_time


class ComboBoxCellRenderer6 (TableCellRenderer):

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

class MyTableModel7 (DefaultTableModel):

    columnNames = ["Station", "Platform", "Station Group"]

    def __init__(self):
        l1 = ["", "", ""]
        self.data = [l1]

    def remove_not_set_row(self):
        b = False
        for row in reversed(range(len(self.data))):
            # print "row", row
            if self.data[row][0] == "":
                self.data.pop(row)

    def populate(self, items_to_put_in_dropdown):
        # print "in populate"
        for row in reversed(range(len(self.data))):
            self.data.pop(row)
        # print "cleared everything"
        # self.data = []
        # append all trains to put in dropdown
        [station_col, platform_col, station_group_col] = [0, 1, 2]
        for location_name in items_to_put_in_dropdown:
            LocationManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.locations.LocationManager)
            location = LocationManager.getLocationByName(location_name)
            platform = self.get_location_platform(location)
            station_group = self.get_location_station_group(location)
            self.data.append([location_name, platform, station_group])
        # print "populated"

    def find_between(self, s, first, last):
        try:
            start = s.index(first) + len(first)
            end = s.index(last, start)
            return s[start:end]
        except ValueError:
            return ""

    def getColumnCount(self) :
        return len(self.columnNames)


    def getRowCount(self) :
        return len(self.data)


    def getColumnName(self, col) :
        return self.columnNames[col]


    def getValueAt(self, row, col) :
        return self.data[row][col]

    def getColumnClass(self, col) :
        return java.lang.Boolean.getClass(self.getValueAt(0,col))

    #only include if table editable
    def isCellEditable(self, row, col) :
        # Note that the data/cell address is constant,
        # no matter where the cell appears onscreen.
        # cell_value = self.getValueAt(row, col)
        return True

    # only include if data can change.
    def setValueAt(self, value, row, col) :
        # print "row", row, "col", col, "value", value
        self.data[row][col] = value
        self.fireTableCellUpdated(row, col)

        # if (isValidValue(aValue)) {
        # data[rowIndex][columnIndex] = aValue;
        # fireTableCellUpdated(rowIndex, columnIndex); // Notify the table
        # }

    def get_location_platform(self, location):
        #item_tag is "platform" or "station_group"
        tag = "IMIS:"+ location.getName().replace(" ","_") + "_Platform"
        memory = memories.getMemory(tag)
        if memory != None:
            # print "$$$$$$$$$$$$$$$$", turnout, 'IMIS:the_turnout_' +turnout_str
            # print "value", turnout.getValue()
            return str(memory.getValue())
        else:
            return " "

    def set_location_platform(self, location, location_value):
        if location_value != None:
            # call using set_turnout(location, "Platform 1")
            tag = "IMIS:"+ location.getName().replace(" ","_") +"_Platform"
            memory = memories.provideMemory(tag)
            memory.setValue(location_value)

    def get_location_station_group(self, location):
        # call using Station_group = self.get_location_station_group(location)
        tag = "IMIS:"+ location.getName().replace(" ","_") +"_StationGroup"
        memory = memories.getMemory(tag)
        if memory != None:
            # print "$$$$$$$$$$$$$$$$", turnout, 'IMIS:the_turnout_' +turnout_str
            # print "value", turnout.getValue()
            return str(memory.getValue())
        else:
            return " "
















