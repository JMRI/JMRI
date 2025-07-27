from javax.swing import (JTable, JScrollPane, JFrame, JPanel, JComboBox,  BorderFactory, DefaultCellEditor, JLabel,
                         UIManager, SwingConstants, JFileChooser, JTextField)
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
from java.awt import Toolkit
from java.awt.event import ActionListener
import time

programmatic_update = False

class CreateAndShowGUI5(TableModelListener):

    def __init__(self, class_ResetButtonMaster, route_name, param_scheduled_start, \
                 journey_time_row_displayed = False, add_row_columns_displayed = False, hidden = False):

        global scheduled_start
        global add_row_columns_displayed_gbl

        self.journey_time_row_displayed = journey_time_row_displayed
        self.add_row_columns_displayed = add_row_columns_displayed
        add_row_columns_displayed_gbl = add_row_columns_displayed

        RouteManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.routes.RouteManager)
        self.route = RouteManager.getRouteByName(route_name)

        scheduled_start = param_scheduled_start
        self.logLevel = 0
        self.class_ResetButtonMaster = class_ResetButtonMaster
        #Create and set up the window.

        self.frame = JFrame("Train Route: " + route_name + "  Actions listed after station take place after arrive at that station, before train moves from that station")
        self.frame.setSize(1000, 600)

        # setup self.table
        self.model = MyTableModel5()
        self.model.route = self.route
        self.model.addTableModelListener(MyModelListener5(self, class_ResetButtonMaster))
        self.class_ResetButtonMaster = class_ResetButtonMaster

        self.table = JTable(self.model)
        self.table.setPreferredScrollableViewportSize(Dimension(600, 300));

        self.table.setFillsViewportHeight(True);
        self.table.setRowHeight(30);

        # setup self.button_pane
        self.set_button_pane()

        self.scrollPane = JScrollPane(self.table)
        self.scrollPane.setSize(Dimension(600, 500))

        self.topPanel= JPanel();
        self.topPanel.setLayout(BoxLayout(self.topPanel, BoxLayout.X_AXIS))

        self.topPanel.add(self.scrollPane)

        config = self.frame.getGraphicsConfiguration()
        bounds = config.getBounds()
        insets = Toolkit.getDefaultToolkit().getScreenInsets(config)

        x = bounds.x + bounds.width - insets.right - self.frame.getWidth() -400
        y = bounds.y + insets.top + 100
        self.frame.setLocation(x, y)

        contentPane = self.frame.getContentPane()
        # contentPane.removeAll()
        contentPane.add(self.topPanel, BorderLayout.CENTER)
        contentPane.add(self.buttonPane, BorderLayout.PAGE_END)
        self.populate_action(None)
        self.cancel = False
        self.completeTablePanel()

        if hidden == False:
            self.show_frame()

    def show_frame(self):
        self.frame.setVisible(True)

    def hide_frame(self):
        self.frame.setVisible(False)

    def completeTablePanel(self):
        global fast_clock_rate

        # set the widths of the columns of self.table
        self.columnModel = self.setup_column_widths_and_comboboxes()

        # Get the size of the JFrame
        size = self.scrollPane.getSize();
        original_height = size.height
        # print "original_height", original_height
        # set height of scrollPane
        size_of_one_row = 30
        height = 50
        for row in reversed(range(len(self.model.data))):
            height += size_of_one_row
        height = min(height, original_height)
        self.scrollPane.setPreferredSize(Dimension(600, height))

        # set whether stop sensor column is displayed
        [locations_col, journey_time_col, wait_time_col, duration_sec_col, duration_col, departure_time_col, add_loc_col, add_action_col, delete_col, stop_sensor_col] = [0, 1, 2, 3, 4, 5, 6, 7,8,9]
        if self.route is None : print "************ self.route is None C **********************"
        routelocations_rows_list = [routelocation.getSequenceNumber()-1 \
                                    for routelocation in self.route.getLocationsBySequenceList() \
                                    if ".py" not in routelocation.getName()]

        stop_sensor_present_list = []

        stop_sensor_present_list = [row1 for row1 in routelocations_rows_list
                                    if row1 > self.model.find_row_first_location()
                                    if self.model.stop_sensor_present(row1,
                                                                self.model.getValueAt(self.model.find_row_prev_location(row1), locations_col),
                                                                self.model.getValueAt(row1, locations_col)) == True]

        columnModel = self.table.getColumnModel()

        if not stop_sensor_present_list:   # An empty list evaluates to False
            columnModel.getColumn(stop_sensor_col).setMaxWidth(0)
            columnModel.getColumn(stop_sensor_col).setMinWidth(0)

        self.topPanel.add(self.scrollPane)
        self.frame.pack();
        self.frame.setVisible(True)
        return

    def set_button_pane(self):
        global fast_clock_rate

        self.buttonPane = JPanel();
        self.buttonPane.setLayout(BoxLayout(self.buttonPane, BoxLayout.LINE_AXIS))
        self.buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10))

        button_close = JButton("Close", actionPerformed = self.close_action)
        self.buttonPane.add(button_close)
        self.buttonPane.add(Box.createHorizontalGlue())

        button_add_row = JButton("Toggle Add Row", actionPerformed = self.add_row_action)
        self.buttonPane.add(button_add_row)
        self.buttonPane.add(Box.createHorizontalGlue())

        button_show_wait = JButton("Toggle Journey/Wait Times", actionPerformed = self.show_wait_action)
        self.buttonPane.add(button_show_wait)
        self.buttonPane.add(Box.createHorizontalGlue())
        # if self.journey_time_row_displayed == False:

        if self.journey_time_row_displayed == True:
            button_wait_time = JButton("Set Wait Times", actionPerformed = self.change_wait_time_action)
            self.buttonPane.add(button_wait_time)
            self.buttonPane.add(Box.createHorizontalGlue())


        if "fast_clock_rate" not in globals():
            fast_clock_rate = 10
        if fast_clock_rate is not None:
            secs_in_fast_minute = int(1.0 / float(str(fast_clock_rate)) * 60.0)
            label_info = JLabel("Calculates to the precision of 1 fast minute. " + str(secs_in_fast_minute) + " seconds = 1 fast minute")
        else:
            label_info = JLabel("Displays to the precision of 1 fast minute. Several seconds = 1 fast minute")
        self.buttonPane.add(label_info)
        self.buttonPane.add(Box.createHorizontalGlue())


    class CustomRenderer(TableCellRenderer):
        global add_row_columns_displayed_gbl
        def __init__(self, all_locations, all_actions):
            self.all_locations = all_locations
            self.all_actions = all_actions

        def getTableCellRendererComponent(self, table, value, isSelected, hasFocus, row, column):
            # Create JComboBox dynamically based on row
            [locations_col, journey_time_col, wait_time_col, duration_sec_col, duration_col, departure_time_col, add_loc_col, add_action_col, delete_col, stop_sensor_col] = [0, 1, 2, 3, 4, 5, 6, 7,8,9]
            if ".py" in value:
                combo_box = JComboBox(self.all_actions)
            else:
                combo_box = JComboBox(self.all_locations)
            combo_box.setSelectedItem(value)  # Set the current value
            if add_row_columns_displayed_gbl == True:
                combo_box.setEditable(True)  # Make it editable for consistency
            else:
                combo_box.setEditable(False)
            return combo_box

        def get_locations_list(self):
            LocationsManager = jmri.InstanceManager.getDefault(jmri.jmrit.operations.locations.LocationManager)
            locations_list = LocationsManager.getLocationsByNameList()
            my_list = [loc.getName() for loc in locations_list]
            return my_list



    class CustomEditor(DefaultCellEditor):
        global add_row_columns_displayed_gbl
        global programmatic_update

        def __init__(self, all_locations, all_actions, model, class_CreateAndShowGUI5):
            super(DefaultCellEditor, self).__init__(JComboBox())
            self.all_locations = all_locations
            self.all_actions = all_actions
            # print "self.all_actions", self.all_actions
            self.model = model
            self.class_CreateAndShowGUI5 = class_CreateAndShowGUI5

        def getTableCellEditorComponent(self, table, value, isSelected, row, column):
            # Create JComboBox dynamically based on row
            if ".py" in value:
                combo_box = JComboBox(self.all_actions)
            else:
                combo_box = JComboBox(self.all_locations)
            if add_row_columns_displayed_gbl == True:
                combo_box.setEditable(True)  # Make it editable
            else:
                combo_box.setEditable(False)
            # Define an action listener to capture changes
            combo_box.addActionListener(self.ComboBoxListener(combo_box, self.model, row, column, self.class_CreateAndShowGUI5))

            combo_box.setSelectedItem(value)  # Set the current value in the jTable
            self.model.setValueAt(value, row, column)  # Set the value in the model

            self.editorComponent = combo_box
            return combo_box

        def getCellEditorValue(self):
            # Return the value of the editor (selected or entered value)
            return self.editorComponent.getSelectedItem()

        class ComboBoxListener(ActionListener):
            def __init__(self, combo_box, model, row, column, class_CreateAndShowGUI5):
                self.combo_box = combo_box
                self.model = model
                self.row = row
                self.column = column
                self.class_CreateAndShowGUI5 = class_CreateAndShowGUI5

            def actionPerformed(self, event):

                # get location to add
                location_name = str(self.combo_box.getSelectedItem())
                LocationManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.locations.LocationManager)
                location = LocationManager.newLocation(location_name)
                # get location to remove
                if self.class_CreateAndShowGUI5.route is None:
                    OptionDialog().displayMessage("route has not been set up")
                else:
                    routeLocation = self.class_CreateAndShowGUI5.route.getRouteLocationBySequenceNumber(self.row+1)
                    self.class_CreateAndShowGUI5.route.deleteLocation(routeLocation)
                    self.class_CreateAndShowGUI5.route.addLocation(location, self.row+1)


    def setup_column_widths_and_comboboxes(self):

        # setup widths of columns

        columnModel = self.table.getColumnModel();

        [locations_col, journey_time_col, wait_time_col, duration_sec_col, duration_col, departure_time_col, add_loc_col, add_action_col, delete_col, stop_sensor_col] = [0, 1, 2, 3, 4, 5, 6, 7,8,9]
        columnModel.getColumn(locations_col).setPreferredWidth(300)
        columnModel.getColumn(stop_sensor_col).setPreferredWidth(120)

        if self.journey_time_row_displayed == False:
            columnModel.getColumn(journey_time_col).setMaxWidth(0)
            columnModel.getColumn(journey_time_col).setMinWidth(0)
            columnModel.getColumn(wait_time_col).setMaxWidth(0)
            columnModel.getColumn(wait_time_col).setMinWidth(0)

        if self.add_row_columns_displayed == False:
            columnModel.getColumn(add_loc_col).setMaxWidth(0)
            columnModel.getColumn(add_loc_col).setMinWidth(0)
            columnModel.getColumn(add_action_col).setMaxWidth(0)
            columnModel.getColumn(add_action_col).setMinWidth(0)

        # setup dropdowns for stations and actions

        self.stopSensorColumn = self.table.getColumnModel().getColumn(stop_sensor_col)
        # self.stopSensorColumn.setPreferredWidth(100)
        self.combobox0 = JComboBox()
        self.required_items_to_put_in_dropdown = ["Use Default", "Use Stop Sensor", "Use Speed Profile"]
        for mode in self.required_items_to_put_in_dropdown:
            self.combobox0.addItem(mode)
        self.stopSensorColumn.setCellEditor(DefaultCellEditor(self.combobox0));

        # setup Editor and Renderer for comboboxes

        self.locations_col = self.table.getColumnModel().getColumn(locations_col)

        all_locations = [str(loc) for loc in self.get_locations_list() if not ".py" in loc]
        all_actions = [loc for loc in self.get_locations_list() if ".py" in loc]

        my_all_actions = self.all_actions()

        self.renderer_locations = ComboBoxCellRenderer()
        self.renderer_actions = ComboBoxCellRenderer()

        if self.add_row_columns_displayed:
            self.locations_col.setCellEditor(self.CustomEditor(all_locations, my_all_actions, self.model, self))
        else:
            self.locations_col.setCellEditor(DefaultCellEditor(JTextField()))
        if self.add_row_columns_displayed:
            self.locations_col.setCellRenderer(self.CustomRenderer(all_locations, my_all_actions))
        else:
            self.locations_col.setCellRenderer(DefaultTableCellRenderer())

        return columnModel

    def get_locations_list(self):
        LocationsManager = jmri.InstanceManager.getDefault(jmri.jmrit.operations.locations.LocationManager)
        locations_list = LocationsManager.getLocationsByNameList()
        my_list = [loc.getName() for loc in locations_list]

        return my_list

    def all_actions(self):
        directory1 = DispatchMaster().action_directory_in_DispatcherSystem()
        files = os.listdir(directory1)

        python_files = [f for f in files if f.endswith(".py")]
        # print "python_files", python_files
        directory = DispatchMaster().action_directory()
        files = os.listdir(directory)
        python_files2 = [f for f in files if f.endswith(".py")]
        # print "python_files2", python_files2
        python_files.extend(python_files2)
        return python_files

    def update_duration_action(self, e):
        # model = e.getSource()
        # we need to update the journey times (with existing values) so that the update can take place
        # in the TableListener routines
        [locations_col, journey_time_col, wait_time_col, duration_sec_col, duration_col, departure_time_col, add_loc_col, add_action_col, delete_col, stop_sensor_col] = [0, 1, 2, 3, 4, 5, 6, 7,8,9]
        for row in range(len(self.model.data)):
            value = self.model.getValueAt(row, duration_col)
            if value is not None:
                self.model.setValueAt(str(value), row, duration_col)
        self.completeTablePanel()

    def update_journey_time_action(self, e):
        global programmatic_update
        # we need to update the journey times (with existing values) so that the update can take place
        # in the TableListener routines
        [locations_col, journey_time_col, wait_time_col, duration_sec_col, duration_col, departure_time_col, add_loc_col, add_action_col, delete_col, stop_sensor_col] = [0, 1, 2, 3, 4, 5, 6, 7,8,9]
        for row in range(len(self.model.data)):
            value = self.model.getValueAt(row, journey_time_col)
            if value is not None:
                programmatic_update = False    # so it updates!
                self.model.setValueAt(value, row, journey_time_col)

    def add_row_action(self, e):
        columnModel = self.table.getColumnModel();

        [locations_col, journey_time_col, wait_time_col, duration_sec_col, duration_col, departure_time_col, add_loc_col, add_action_col, delete_col, stop_sensor_col] = [0, 1, 2, 3, 4, 5, 6, 7,8,9]
        columnModel.getColumn(locations_col).setPreferredWidth(300)

        # hide the action and station columns when required

        if self.add_row_columns_displayed:
            self.add_row_columns_displayed = False
            columnModel.getColumn(add_loc_col).setMaxWidth(0)
            columnModel.getColumn(add_loc_col).setMinWidth(0)
            columnModel.getColumn(add_action_col).setMaxWidth(0)
            columnModel.getColumn(add_action_col).setMinWidth(0)
        else:
            self.add_row_columns_displayed = True
            columnModel.getColumn(add_loc_col).setMaxWidth(400)
            columnModel.getColumn(add_loc_col).setMinWidth(100)
            columnModel.getColumn(add_action_col).setMaxWidth(400)
            columnModel.getColumn(add_action_col).setMinWidth(100)
        self.completeTablePanel()

    def get_station_list(self):
        RouteManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.routes.RouteManager)
        if self.route is None :
            OptionDialog().displayMessage("route has not been filled in")
            return []
        else:
            locations_list = self.route.getLocationsBySequenceList()
            my_list = [[location.getName(), location.getComment()] for location in locations_list]
            return my_list

    def populate_action(self, event):

        items_to_put_in_dropdown = self.get_station_list()
        self.model.populate(items_to_put_in_dropdown)
        self.completeTablePanel()

    def close_action(self, event):
        self.frame.dispatchEvent(WindowEvent(self.frame, WindowEvent.WINDOW_CLOSING));

    def show_wait_action(self, event):
        columnModel = self.table.getColumnModel();

        [locations_col, journey_time_col, wait_time_col, duration_sec_col, duration_col, departure_time_col, add_loc_col, add_action_col, delete_col, stop_sensor_col] = [0, 1, 2, 3, 4, 5, 6, 7,8,9]
        columnModel.getColumn(locations_col).setPreferredWidth(300)

        if self.journey_time_row_displayed:
            self.journey_time_row_displayed = False
            columnModel.getColumn(journey_time_col).setMaxWidth(0)
            columnModel.getColumn(journey_time_col).setMinWidth(0)
            columnModel.getColumn(wait_time_col).setMaxWidth(0)
            columnModel.getColumn(wait_time_col).setMinWidth(0)
        else:
            self.journey_time_row_displayed = True
            columnModel.getColumn(journey_time_col).setMaxWidth(400)
            columnModel.getColumn(journey_time_col).setMinWidth(100)
            columnModel.getColumn(wait_time_col).setMaxWidth(400)
            columnModel.getColumn(wait_time_col).setMinWidth(100)

        self.set_button_pane()
        contentPane = self.frame.getContentPane()
        contentPane.removeAll()
        contentPane.add(self.topPanel, BorderLayout.CENTER)
        contentPane.add(self.buttonPane, BorderLayout.PAGE_END)
        self.completeTablePanel()

    def new_delay(self, old_val):
        if old_val < 3:
            new_val = 3
        elif old_val < 5:
            new_val = 5
        elif old_val < 10:
            new_val = 10
        elif old_val < 15:
            new_val = 15
        else:
            new_val = 0
        return new_val

    def delete_all_action(self, event):
        [locations_col, journey_time_col, wait_time_col, duration_sec_col, duration_col, departure_time_col, add_loc_col, add_action_col, delete_col, stop_sensor_col] = [0, 1, 2, 3, 4, 5, 6, 7,8,9]
        for row in reversed(range(len(self.model.data))):
            self.model.data.pop(row)
        self.completeTablePanel()

    def new_val(self, old_val):
        old_val = int(old_val)
        if old_val == 0:
            new_val = 2
        elif old_val < 2:
            new_val = 2
        elif old_val < 3:
            new_val = 3
        elif old_val < 5:
            new_val = 5
        elif old_val < 10:
            new_val = 10
        else:
            new_val = 0
        return str(new_val)

    def change_wait_time_action(self, event):
        [locations_col, journey_time_col, wait_time_col, duration_sec_col, duration_col, departure_time_col, add_loc_col, add_action_col, delete_col, stop_sensor_col] = [0, 1, 2, 3, 4, 5, 6, 7,8,9]
        if self.route is None : print "************ self.route is None A **********************"
        routeLocationList = self.route.getLocationsBySequenceList()

        routelocationsSequenceNumber_list = [ [routelocation, routelocation.getSequenceNumber()] \
                                              for routelocation in self.route.getLocationsBySequenceList() \
                                              if ".py" not in routelocation.getName()]
        index = 0
        for row in reversed(range(len(self.model.data))):
            if self.model.isCellEditable(row, wait_time_col): # only get the rows we want to change
                if index == 0:
                    old_val = str(self.model.data[row][wait_time_col])
                    if old_val == "": old_val = "0"
                    new_val = self.new_val(old_val)
                    self.model.setValueAt(new_val,row,wait_time_col)
                    # save value in operations
                    routeLocation = routeLocationList[row]
                    self.set_value_in_comment(routeLocation, new_val, "wait_time")
                else:
                    self.model.setValueAt(new_val,row,wait_time_col)  # set all the .,mn\ wait times to the same value

        self.completeTablePanel()

    def new_task(self, old_val):

        if old_val == "Once":
            new_val = "duration every 20 mins"
        elif old_val == "duration every 20 mins":
            new_val = "duration every 30 mins"
        elif old_val == "duration every 30 mins":
            new_val = "duration every Hour"
        elif old_val == "duration every Hour":
            new_val = "duration every 2 Hours"
        elif old_val == "duration every 2 Hours":
            new_val = "Once"
        else:
            return "Once"
        return new_val


    def save_action(self, event):
        self.save()

    def save(self):
        [locations_col, journey_time_col, wait_time_col, duration_sec_col, duration_col, departure_time_col, add_loc_col, add_action_col, delete_col, stop_sensor_col] = [0, 1, 2, 3, 4, 5, 6, 7,8,9]
        for row in reversed(range(len(self.model.data))):
            locations_name = str(self.model.data[row][locations_col])
            journey_time_name = str(self.model.data[row][journey_time_col])
            wait_time_name = str(self.model.data[row][wait_time_col])
            duration_sec_name = str(self.model.data[row][duration_sec_col])
            duration_name = str(self.model.data[row][duration_col])
            departure_time_name = str(self.model.data[row][departure_time_col])
            add_loc_name = str(self.model.data[row][add_loc_col])
            add_action_name = str(self.model.data[row][add_action_col])
            delete_name = str(self.model.data[row][delete_col])
            stop_sensor_name = str(self.model.data[row][stop_sensor_col])
            if locations_name != "" :
                self.save_location_row(row, locations_name, journey_time_name, wait_time_name, duration_sec_name, \
                                        departure_time_name, add_loc_name, add_action_name, delete_name, stop_sensor_name)
            else:
                msg = "Cannot save row: " + str(row) + " train name, route or delay is not set"
                OptionDialog().displayMessage(msg,"")
        self.completeTablePanel()
        if self.model.getRowCount() == 0:
            self.frame.dispatchEvent(WindowEvent(self.frame, WindowEvent.WINDOW_CLOSING))

    def save_location_row(self, row, locations_name, journey_time_name, wait_time_name, duration_sec_name, \
                      departure_time_name, add_loc_name, add_action_name, delete_name, stop_sensor_name):
        if self.route is None : print "************ self.route is None K **********************"
        routeLocationList = self.route.getLocationsBySequenceList()
        routeLocation = routeLocationList[row]

        self.set_value_in_comment(routeLocation, wait_time_name, "wait_time")

        if journey_time_name != None and journey_time_name != "":
            self.set_value_in_comment(routeLocation, journey_time_name, "journey_time")
        else:
            self.set_value_in_comment(routeLocation, duration_sec_name, "duration_sec")

    def set_value_in_comment(self, routeLocation, value, duration_string):

        comment = routeLocation.getComment()    #Null

        if comment == None:
            comment = ""

        delim_start = "[" + duration_string + "-"
        delim_end = "-" + duration_string + "]"

        comment = self.insert_between(comment, delim_start, delim_end, value)

        routeLocation.setComment(comment)

    def delete_between(self, string, delim1, delim):
        first, _, rest = string.partition(delim1)
        _, _, rest = rest.partition(delim2)
        cleaned_text = ' '.join([first.strip(), rest.strip()])
        return cleaned_text

    def insert_between(self, string, delim1, delim2, value):
        first, _, rest = string.partition(delim1)
        _, _, rest = rest.partition(delim2)

        new_val = delim1 + str(value) + delim2
        modified_text = new_val.join([first.strip(), rest.strip()])
        return modified_text

    def directory(self):
        path = jmri.util.FileUtil.getUserFilesPath() + "dispatcher" + java.io.File.separator + "schedules"
        if not os.path.exists(path):
            os.makedirs(path)
        return path + java.io.File.separator

    def write_list(self, a_list, file):
        # store list in binary file so 'wb' mode
        if self.logLevel > 0: print "block_info" , a_list
        if self.logLevel > 0: print "file" , file
        file = str(file)
        with open(file, 'wb') as fp:
            pass
        if self.logLevel > 0: print "V"
        with open(file, 'wb') as fp:
            if self.logLevel > 0: print "B"
            for items in a_list:
                if self.logLevel > 0: print "C", items
                i = 0
                for item in items:
                    if self.logLevel > 0: print "item", item
                    fp.write('"%s"' %item)
                    if i != 4: fp.write(",")
                    i+=1
                fp.write('\n')
                #fp.write('\n'.join(item))
                #fp.write(items)

    # Read list to memory
    def read_list(self, file):
        file = str(file)
        if self.logLevel > 0: print "read list", file
        # for reading also binary mode is important
        #file = self.directory() + "blockDirections.txt"
        n_list = []
        # try:
        with open(file, 'rb') as fp:
            for line in fp:
                if self.logLevel > 0: print "line" , line
                x = line[:-1]
                if self.logLevel > 0: print x
                y = x.split(",")
                #y = [item.replace('"','') for item in y]
                if self.logLevel > 0: print "y" , y
                n_list.append(y)

        return n_list

class MyModelListener5(TableModelListener):

    def __init__(self, class_CreateAndShowGUI5, class_ResetButtonMaster):
        global programmatic_update
        self.class_CreateAndShowGUI5 = class_CreateAndShowGUI5
        self.class_ResetButtonMaster = class_ResetButtonMaster
        self.cancel = False
        self.logLevel = 0
        self.i = 0
        programmatic_update = False
        self.mode = "none"

    def tableChanged(self, e):
        self.i +=1
        global programmatic_update
        global trains_allocated
        row = e.getFirstRow()
        column = e.getColumn()
        self.model = e.getSource()
        columnName = self.model.getColumnName(column)

        class_CreateAndShowGUI5 = self.class_CreateAndShowGUI5
        class_ResetButtonMaster = self.class_ResetButtonMaster
        tablemodel = class_CreateAndShowGUI5.model
        [locations_col, journey_time_col, wait_time_col, duration_sec_col, duration_col, departure_time_col, add_loc_col, add_action_col, delete_col, stop_sensor_col] = [0, 1, 2, 3, 4, 5, 6, 7,8,9]
        if self.model.route is None : print "************ self.model.route is None J **********************"
        routelocations_rows_list = [routelocation.getSequenceNumber()-1 \
                                    for routelocation in self.model.route.getLocationsBySequenceList() \
                                    if ".py" not in routelocation.getName()]
        if row in routelocations_rows_list:
            if column == duration_sec_col:
                # print "updating duration_sec_col", self.mode

                # print "PROGRAMMATIC UPDATE", programmatic_update
                # print "self.mode", self.mode

                self.save_value_to_operations(row, duration_sec_col)

                mode_on_entry = programmatic_update
                if self.mode == "forwards" or programmatic_update == False:
                    programmatic_update == True
                    self.mode = "forwards"
                    # print "calculate_duration_from_duration_sec forwards"
                    # update duration
                    if row != self.model.find_row_first_location():
                        duration = str(self.calc_duration_from_duration_sec(row))
                        self.model.setValueAt(duration, row, duration_col)
                    # update departure time
                    if row != self.model.find_row_first_location():
                        departure = self.calc_departure_time(row)
                        self.setValueAt_programmatically(departure, row, departure_time_col)
                    programmatic_update == False

                elif self.mode == "backwards" or programmatic_update == False:
                    # calculate journey time from wait time and duration_sec
                    programmatic_update == True
                    self.mode = "backwards"
                    if row != self.model.find_row_first_location():
                        # print "backwards calculate_journey_time_from_duration_sec_and_wait_time"
                        journey_time = self.calculate_journey_time_from_duration_sec_and_wait_time(row)
                        # print "journey time", journey_time
                        self.setValueAt_programmatically(journey_time, row, journey_time_col)
                        self.save_value_to_operations(row, journey_time_col)
                    programmatic_update == False

                # update subsequent rows if user updates
                if mode_on_entry == False:
                    self.update_subsequent_rows(row)

            elif column == duration_col:     #trains
                # print
                # print "we have updated duration_col mode =", self.mode
                # 1) Calculate the duration in secs (from fast minutes
                if self.mode == "backwards":
                    duration_sec = self.calc_duration_sec_from_duration(row)
                    # print "updating duration_sec", duration_sec
                    existing_val = self.model.getValueAt(row, duration_sec_col)
                    if existing_val != duration_sec:
                        self.setValueAt_programmatically(duration_sec, row, duration_sec_col)
                        self.save_value_to_operations(row, duration_sec_col)

            elif column == departure_time_col:       # sections
                # print "departure_time_col set mode backwards", "programmatic_update", programmatic_update

                # update subsequent rows only if first row
                if row == self.model.find_row_first_location():
                    self.update_subsequent_rows(row)
                    return

                # if we are calculating from duration_sec we have set programmatic_update to true
                if programmatic_update == True:
                    # update subsequent rows
                    self.update_subsequent_rows(row)
                    return

                # only done if user updates
                self.mode = "backwards"
                # when departure is clicked
                # a) calculate the relevant duration
                if row != self.model.find_row_first_location():
                    duration = self.calculate_duration_from_current_and_prev_departure_times(row)
                    # print "duration", duration
                    current_duration = self.model.getValueAt(row, duration_col)
                    # print "current_duration", current_duration, "duration", duration
                    if duration != current_duration:
                        self.model.setValueAt(duration, row, duration_col)

                # update subsequent rows
                self.update_subsequent_rows(row)

            # elif column == delete_col:
                # done below so can delete actions
            elif column == journey_time_col or column == wait_time_col:

                if programmatic_update == True: return

                # only done if user updates
                # print "journey_time_col set mode forwards"
                self.mode = "forwards"
                if row != self.model.find_row_first_location():
                    my_duration = self.calc_duration_sec_from_journey_time_and_wait_time(row)
                    self.model.setValueAt(my_duration, row, duration_sec_col)
                    # print "set value"

                    self.save_value_to_operations(row, journey_time_col)
                    self.save_value_to_operations(row, wait_time_col)
                    self.save_value_to_operations(row, duration_sec_col)

                # update subsequent rows
                self.update_subsequent_rows(row)

            elif column == stop_sensor_col:
                self.save_value_to_operations(row, stop_sensor_col)

        # done here so can deal with action rows
        if column == delete_col:
            listener = self
            self.model.removeTableModelListener(listener)
            routelocation = class_CreateAndShowGUI5.route.getRouteLocationBySequenceNumber(row+1)
            class_CreateAndShowGUI5.route.deleteLocation(routelocation)
            self.delete_row(row)
            class_CreateAndShowGUI5.completeTablePanel()
            time.sleep(0.2)
            self.model.addTableModelListener(listener)

        elif column == add_loc_col:
            #reset check box
            listener = self
            self.model.removeTableModelListener(listener)
            self.model.setValueAt(False, row, column)


            # add the station after the selected row
            location_string = self.model.add_loc_at(row)
            LocationManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.locations.LocationManager)
            location = LocationManager.newLocation(location_string)
            routeLocation = class_CreateAndShowGUI5.route.addLocation(location, row+2)

            # self.class_CreateAndShowGUI5.set_value_in_comment(routeLocation, "0", "duration_sec")
            # self.class_CreateAndShowGUI5.set_value_in_comment(routeLocation, "-", "stopMode")
            class_CreateAndShowGUI5.completeTablePanel()
            time.sleep(0.2)
            self.model.addTableModelListener(listener)
            return

        elif column == add_action_col:
            #reset check box
            listener = self
            self.model.removeTableModelListener(listener)
            self.model.setValueAt(False, row, column)

            # add the action after the selected row
            location_string = self.model.add_action_at(row)
            LocationManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.locations.LocationManager)
            location = LocationManager.newLocation(location_string)
            class_CreateAndShowGUI5.route.addLocation(location, row+2)
            class_CreateAndShowGUI5.completeTablePanel()
            time.sleep(0.2)
            self.model.addTableModelListener(listener)
            return

        # elif column == locations_col:
        #     # the change is done in a combo box, so we do the work in the combo box listener

        class_CreateAndShowGUI5.save()    # save everything when the table is changed

    def update_subsequent_rows(self, current_row):

        [locations_col, journey_time_col, wait_time_col, duration_sec_col, duration_col, departure_time_col, add_loc_col, add_action_col, delete_col, stop_sensor_col] = [0, 1, 2, 3, 4, 5, 6, 7,8,9]
        global programmatic_update

        # calculates forwards and backwards if programmatic_update False (see tableChanged duration_sec_col)
        programmatic_update = False
        if self.model.route is None : print "************ self.model.route is None H **********************"
        routelocations_rows_list = [routelocation.getSequenceNumber()-1 \
                                    for routelocation in self.model.route.getLocationsBySequenceList() \
                                    if ".py" not in routelocation.getName()]

        for row in routelocations_rows_list:
            if row > current_row:
                # trigger the update command by touching the element
                value = self.model.getValueAt(row, duration_sec_col)
                self.model.setValueAt(value, row, duration_sec_col)
                break

    def save_value_to_operations(self, row, col):
        value = self.model.getValueAt(row, col)
        if self.model.route is None :
            print "************ self.model.route is None I **********************"
            return
        # get routeLocation
        routeLocationList = self.model.route.getLocationsBySequenceList()
        routeLocation = routeLocationList[row]

        [locations_col, journey_time_col, wait_time_col, duration_sec_col, duration_col, departure_time_col, add_loc_col, add_action_col, delete_col, stop_sensor_col] = [0, 1, 2, 3, 4, 5, 6, 7,8,9]

        if col == journey_time_col:
            self.class_CreateAndShowGUI5.set_value_in_comment(routeLocation, value, "journey_time")

        if col == wait_time_col:
            self.class_CreateAndShowGUI5.set_value_in_comment(routeLocation, value, "wait_time")

        if col == duration_sec_col:
            self.class_CreateAndShowGUI5.set_value_in_comment(routeLocation, value, "duration_sec")

        if col == stop_sensor_col:
            self.class_CreateAndShowGUI5.set_value_in_comment(routeLocation, value, "stopMode")


    def setValueAt_programmatically(self, value, row, col) :
        global programmatic_update
        programmatic_update = True   # inhibit triggering further updates
        self.model.setValueAt(value, row, col)
        programmatic_update = False



    def update_departure_time_col(self, current_row):
        [locations_col, journey_time_col, wait_time_col, duration_sec_col, duration_col, departure_time_col, add_loc_col, add_action_col, delete_col, stop_sensor_col] = [0, 1, 2, 3, 4, 5, 6, 7,8,9]
        if self.model.route is None : print "************ self.model.route is None H **********************"
        routelocations_rows_list = [routelocation.getSequenceNumber()-1 \
                                    for routelocation in self.model.route.getLocationsBySequenceList() \
                                    if ".py" not in routelocation.getName()]

        for row in routelocations_rows_list:
            if row > current_row:
                # trigger the update command by touching the element
                value = self.model.getValueAt(row, departure_time_col)
                self.model.setValueAt(value, row, departure_time_col)
                break

    def update_duration_sec_col(self, current_row):
        # print "update_duration_sec_col"
        [locations_col, journey_time_col, wait_time_col, duration_sec_col, duration_col, departure_time_col, add_loc_col, add_action_col, delete_col, stop_sensor_col] = [0, 1, 2, 3, 4, 5, 6, 7,8,9]
        if self.model.route is None : print "************ self.model.route is None H **********************"
        routelocations_rows_list = [routelocation.getSequenceNumber()-1 \
                                    for routelocation in self.model.route.getLocationsBySequenceList() \
                                    if ".py" not in routelocation.getName()]

        for row in routelocations_rows_list:
            if row > current_row:
                # trigger the update command by touching the element
                value = self.model.getValueAt(row, duration_sec_col)
                self.model.setValueAt(value, row, duration_sec_col)
                break

    def calculate_duration_from_current_and_prev_departure_times(self, row):
        # calculate duration from current and previous departure times
        [locations_col, journey_time_col, wait_time_col, duration_sec_col, duration_col, departure_time_col, add_loc_col, add_action_col, delete_col, stop_sensor_col] = [0, 1, 2, 3, 4, 5, 6, 7,8,9]
        current_departure_time = self.model.getValueAt(row, departure_time_col)
        prev_row = self.model.find_row_prev_location(row)
        if prev_row == None:
            return
        previous_departure_time = self.model.getValueAt(prev_row, departure_time_col)
        # time and prev_time are in HH:MM
        hh, _, mm = current_departure_time.partition(":")
        hhprev, _, mmprev = previous_departure_time.partition(":")
        # print "mmprev", mmprev, "mm", mm
        duration = int(hh) * 60 - int(hhprev) * 60 + int(mm) - int(mmprev)
        return str(duration)

    def calc_departure_time(self, row):
        # calculate departure time from duration and prev departure time
        [locations_col, journey_time_col, wait_time_col, duration_sec_col, duration_col, departure_time_col, add_loc_col, add_action_col, delete_col, stop_sensor_col] = [0, 1, 2, 3, 4, 5, 6, 7,8,9]
        current_duration = self.model.getValueAt(row, duration_col)
        prev_row = self.model.find_row_prev_location(row)
        if prev_row == None:
            return
        previous_departure_time = self.model.getValueAt(prev_row, departure_time_col)
        hhprev, _, mmprev = previous_departure_time.partition(":")
        departure_time_mins = str((int(mmprev) + int(float(current_duration))) % 60).zfill(2)
        departure_time_hours = str(int(hhprev) + (int(mmprev) + int(float(current_duration))) // 60).zfill(2)
        departure_time = departure_time_hours + ":" + departure_time_mins
        return departure_time

    def calc_duration_sec_from_duration(self, row):
        global fast_clock_rate
        global scheduler_master

        [locations_col, journey_time_col, wait_time_col, duration_sec_col, duration_col, departure_time_col, add_loc_col, add_action_col, delete_col, stop_sensor_col] = [0, 1, 2, 3, 4, 5, 6, 7,8,9]

        try:
            [locations_col, journey_time_col, wait_time_col, duration_sec_col, duration_col, departure_time_col, add_loc_col, add_action_col, delete_col, stop_sensor_col] = [0, 1, 2, 3, 4, 5, 6, 7,8,9]
            current_duration = str(int(self.model.getValueAt(row, duration_col)))         # secs
            # set fast clock rate
            if "fast_clock_rate" not in globals():
                [start_hour_gbl, end_hour_gbl, fast_clock_rate, speed_not_operational_gbl, \
                 scheduling_margin_gbl, scheduling_in_operation_gbl] = scheduler_master.read_list()
            # convert to fast_minutes
            current_duration_sec = (float(current_duration) / float(str(fast_clock_rate))) * 60.0  # fast minutes
            # print "calc_duration_sec_from_duration", "current_duration_sec", current_duration_sec, "current_duration", current_duration
        except:
            # use the existing value
            current_duration_sec = self.model.getValueAt(row, duration_sec_col)
        return str(int(float(current_duration_sec)))


    def calc_duration_sec_from_journey_time_and_wait_time(self, row):
        global fast_clock_rate

        [locations_col, journey_time_col, wait_time_col, duration_sec_col, duration_col, departure_time_col, add_loc_col, add_action_col, delete_col, stop_sensor_col] = [0, 1, 2, 3, 4, 5, 6, 7,8,9]

        try:
            [locations_col, journey_time_col, wait_time_col, duration_sec_col, duration_col, departure_time_col, add_loc_col, add_action_col, delete_col, stop_sensor_col] = [0, 1, 2, 3, 4, 5, 6, 7,8,9]
            current_journey_time = int(self.model.getValueAt(row, journey_time_col))    # secs
            current_wait_time = int(self.model.getValueAt(row, wait_time_col))          # secs

            current_duration_sec = current_journey_time + current_wait_time
            # round up to the next multiple of current_duration_sec
            secs_in_fast_minute = int(1.0 / float(str(fast_clock_rate)) * 60.0)
            current_duration_sec1 = (current_duration_sec // secs_in_fast_minute) * secs_in_fast_minute
            # print ("current_journey_time", current_journey_time, "current_wait_time", current_wait_time, \
            #        "secs_in_fast_minute", secs_in_fast_minute, "current_duration_sec1", current_duration_sec1, "current_duration_sec", current_duration_sec)
            if current_duration_sec1 != current_duration_sec:
                current_duration_sec = current_duration_sec1 + secs_in_fast_minute
        except:
            # use the existing value
            current_duration_sec = self.model.getValueAt(row, duration_sec_col)
        return str(int(current_duration_sec))

    def calculate_journey_time_from_duration_sec_and_wait_time(self, row):
        global fast_clock_rate

        [locations_col, journey_time_col, wait_time_col, duration_sec_col, duration_col, departure_time_col, add_loc_col, add_action_col, delete_col, stop_sensor_col] = [0, 1, 2, 3, 4, 5, 6, 7,8,9]

        try:
            [locations_col, journey_time_col, wait_time_col, duration_sec_col, duration_col, departure_time_col, add_loc_col, add_action_col, delete_col, stop_sensor_col] = [0, 1, 2, 3, 4, 5, 6, 7,8,9]
            current_duration_sec = int(self.model.getValueAt(row, duration_sec_col))    # secs
            current_wait_time = int(self.model.getValueAt(row, wait_time_col))          # secs

            current_journey_time = current_duration_sec - current_wait_time

        except:
            # use the existing value
            current_duration_sec = self.model.getValueAt(row, duration_sec_col)
        return str(int(current_journey_time))

    def calc_duration_from_duration_sec(self,row):
        global fast_clock_rate

        try:
            [locations_col, journey_time_col, wait_time_col, duration_sec_col, duration_col, departure_time_col, add_loc_col, add_action_col, delete_col, stop_sensor_col] = [0, 1, 2, 3, 4, 5, 6, 7,8,9]
            current_duration_sec = self.model.getValueAt(row, duration_sec_col)          # secs

            # convert to fast_minutes
            current_duration = (float(current_duration_sec) * int(str(fast_clock_rate))) / 60.0  # fast minutes
        except:
            # use the existing value
            current_duration = self.model.getValueAt(row, duration_col)
        return str(int(current_duration))


    def calc_duration_from_journey_time_and_wait_time(self, row):

        global fast_clock_rate

        [locations_col, journey_time_col, wait_time_col, duration_sec_col, duration_col, departure_time_col, add_loc_col, add_action_col, delete_col, stop_sensor_col] = [0, 1, 2, 3, 4, 5, 6, 7,8,9]

        try:
            [locations_col, journey_time_col, wait_time_col, duration_sec_col, duration_col, departure_time_col, add_loc_col, add_action_col, delete_col, stop_sensor_col] = [0, 1, 2, 3, 4, 5, 6, 7,8,9]
            current_journey_time = int(self.model.getValueAt(row, journey_time_col))    # secs
            current_wait_time = int(self.model.getValueAt(row, wait_time_col))          # secs

            # convert to fast_minutes
            current_journey_time = (current_journey_time * int(str(fast_clock_rate))) / 60.0  # fast minutes
            current_wait_time = (current_wait_time * int(str(fast_clock_rate)))/ 60.0     # fast minutes

            current_duration = current_journey_time + current_wait_time
        except:
            # use the existing value
            current_duration = self.model.getValueAt(row, duration_col)
        return str(int(current_duration))


    def delete_row(self, row):
        self.model.data.pop(row)

class ComboBoxCellRenderer (TableCellRenderer):

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

class MyTableModel5 (DefaultTableModel):

    columnNames = ["Station / Action", "Journey Time", "Wait Time", "Duration (secs)", "Duration (f mins)", "Departure Time", "Add Station", "Add Action","Delete Row", "Use of Stop Sensor"]

    def __init__(self):
        l1 = ["", "", False, "stop at end of route", "0.0", "0.0", False, False, False, "-"]
        self.data = [l1]
        self.route = None    # updated from outside class

    def remove_not_set_row(self):
        b = False
        for row in reversed(range(len(self.data))):
            if self.data[row][1] == "":
                self.data.pop(row)

    def add_loc_at(self, row_index):
        location = [str(loc) for loc in self.get_locations_list() if ".py" not in loc][0]
        row_data = [location, "0", "0", "0","0", "00:00", False, False, False, "-"]
        self.data.insert(row_index+1, row_data)
        return location

    def add_action_at(self, row_index):
        action = self.all_actions()[0]
        row_data = [action, "", "", "","", "", False, False, False, "-"]
        self.data.insert(row_index+1, row_data)
        return action

    def get_locations_list(self):
        LocationsManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.locations.LocationManager)
        locations_list = LocationsManager.getLocationsByNameList()
        my_list = [loc.getName() for loc in locations_list]

        return my_list

    def all_actions(self):
        directory1 = DispatchMaster().action_directory_in_DispatcherSystem()
        files = os.listdir(directory1)
        # print "files in dispatcher system action directory", files

        python_files = [f for f in files if f.endswith(".py")]
        # print "directory1", directory1, "python_files", python_files

        directory = DispatchMaster().action_directory()
        files = os.listdir(directory)
        python_files2 = [f for f in files if f.endswith(".py")]
        # print "directory", directory, "python_files2", python_files2
        python_files.extend(python_files2)
        return python_files


    def populate(self, items_to_put_in_dropdown):
        # print "populate"
        global scheduled_start
        global programmatic_update
        for row in reversed(range(len(self.data))):
            self.data.pop(row)
        # append all trains to put in dropdown
        [locations_col, journey_time_col, wait_time_col, duration_sec_col, duration_col, departure_time_col, add_loc_col, add_action_col, delete_col, stop_sensor_col] = [0, 1, 2, 3, 4, 5, 6, 7,8,9]
        duration_sec_array = []
        i = 0
        for [location, comment] in items_to_put_in_dropdown:
            if ".py" not in location:    # omit actions
                journey_time = self.find_between(comment, "[journey_time-", "-journey_time]")
                if i == 0:
                    if journey_time == "": journey_time = ""
                else:
                    if journey_time == "": journey_time = "0"

                # get default wait time
                memory = memories.getMemory("IM:" + "DS_wait_time")
                # print "memory value" , memory.getValue()
                if memory is None or memory.getValue() == "" or memory.getValue() is None:
                    memory = memories.provideMemory("IM:" + "DS_wait_time")
                    memory.setValue(3)
                default = memory.getValue()

                wait_time = self.find_between(comment, "[wait_time-", "-wait_time]")
                # print "wait_time", wait_time
                if i == 0:
                    if wait_time == "" or wait_time is "None" : wait_time = ""
                else:
                    if wait_time == "" or "None" in wait_time : wait_time = str(default)
                    # print "set to default"

                duration_sec = str(self.find_between(comment, "[duration_sec-", "-duration_sec]"))

                if i == 0:
                    if duration_sec == "0": duration_sec = ""
                else:
                    if duration_sec == "": duration_sec = "0"

                duration_sec_array.append(duration_sec)

                departure_time = "00:00"
                duration = "0"

                stop_at_stop_sensor = self.get_route_location_stop_mode(location)
            else:
                journey_time = None
                departure_time = None
                duration_sec = None
                duration = None
                wait_time = None
                duration_sec_array.append(duration_sec)
                stop_at_stop_sensor = None
            i += 1
            programmatic_update = True
            self.data.append([location, journey_time, wait_time, duration_sec, duration, departure_time, False,  False, False, stop_at_stop_sensor])
            programmatic_update = False

        # update the first location which is a station with the time of the schedule start
        [locations_col, journey_time_col, wait_time_col, duration_sec_col, duration_col, departure_time_col, add_loc_col, add_action_col, delete_col, stop_sensor_col] = [0, 1, 2, 3, 4, 5, 6, 7,8,9]
        row = self.find_row_first_location()
        if scheduled_start is not None:
            self.setValueAt(scheduled_start, row, departure_time_col)

        # # do the following after updating the first location so the departure times are calculated correctly
        # i = 0
        # for [location, comment] in items_to_put_in_dropdown:
        #     if ".py" not in location:    # omit actions
        #         self.setValueAt(duration_sec_array[i], i, duration_sec_col)
        #     i += 1

    def get_route_location_stop_mode(self, station_to_name):
        route_location = self.route.getLastLocationByName(station_to_name)
        comment = route_location.getComment()
        stop_mode = self.find_between(comment, "[stopMode-", "-stopMode]")
        return stop_mode

    def find_row_first_location(self):
        if self.route is None : print "************ self.route is None G **********************"
        routelocationsSequenceNumber_list = [ [routelocation, routelocation.getSequenceNumber()] \
                                                   for routelocation in self.route.getLocationsBySequenceList() \
                                                   if ".py" not in routelocation.getName()]
        first_loc_sequence_no = routelocationsSequenceNumber_list[0][1]
        route_location = routelocationsSequenceNumber_list[0][0]

        row = first_loc_sequence_no - 1
        return row    # row number starts from 0

    def find_row_next_location(self, row):
        # given the row (rows start at 0, sequencenos start at 1) of a location that is not an action (a python file  xx.py)
        # find the next row of a location that is not an action
        if self.route is None : print "************ self.route is None F **********************"
        routelocationsSequenceNumber_list = [ [routelocation, routelocation.getSequenceNumber()] \
                                              for routelocation in self.route.getLocationsBySequenceList() \
                                              if ".py" not in routelocation.getName()]

        current_val = [[routelocation, sequenceNo] \
                       for [routelocation, sequenceNo] in routelocationsSequenceNumber_list \
                       if row == sequenceNo - 1][0]

        current_index = routelocationsSequenceNumber_list.index(current_val) # starts at 0
        # try:
        if len(routelocationsSequenceNumber_list) > current_index + 1:       # len starts at 1
            [routelocation, sequenceNo] = routelocationsSequenceNumber_list[current_index + 1]
        else:
            [routelocation, sequenceNo] = routelocationsSequenceNumber_list[current_index]
        row1 = sequenceNo - 1
        if row == row1:
            return None
        return row1  # row number starts from 0

    def find_row_prev_location(self, row):
        # get the row (sequenceNo) of the first location that is not an action (a python file  xx.py)
        if self.route is None : print "************ self.route is None E **********************"
        routelocationsSequenceNumber_list = [ [routelocation, routelocation.getSequenceNumber()] \
                                              for routelocation in self.route.getLocationsBySequenceList() \
                                              if ".py" not in routelocation.getName()]
        current_val = [[routelocation, sequenceNo] \
                       for [routelocation, sequenceNo] in routelocationsSequenceNumber_list \
                       if row == sequenceNo - 1][0]
        current_index = routelocationsSequenceNumber_list.index(current_val) # starts at 0
        if current_index > 0:  #index starts at 0
            [routelocation, sequenceNo] = routelocationsSequenceNumber_list[current_index - 1]
        else:
            [routelocation, sequenceNo] = routelocationsSequenceNumber_list[current_index]
        row1 = sequenceNo - 1
        if row == row1:
            return None
        return row1  # row number starts from 0

    def find_between(self, s, first, last):
        try:
            start = s.index(first) + len(first)
            end = s.index(last, start)
            return s[start:end]
        except Exception as e:
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
        [locations_col, journey_time_col, wait_time_col, duration_sec_col, duration_col, departure_time_col, add_loc_col, add_action_col, delete_col, stop_sensor_col] = [0, 1, 2, 3, 4, 5, 6, 7,8,9]
        # do not allow editing of duration in action cols
        if self.route is None : print "************ self.route is None D **********************"
        routelocations_rows_list = [routelocation.getSequenceNumber()-1 \
                                    for routelocation in self.route.getLocationsBySequenceList() \
                                    if ".py" not in routelocation.getName()]
        if row not in routelocations_rows_list:
            if col == duration_sec_col or col == duration_col or col == departure_time_col or col == wait_time_col or col == journey_time_col:
                return False
        if row == self.find_row_first_location():
            if col == duration_sec_col or col == duration_col or col == departure_time_col or col == wait_time_col or col == journey_time_col:
                return False
        stop_sensor_present_list = [row1 for row1 in routelocations_rows_list
                                        if row1 > self.find_row_first_location()
                    if self.stop_sensor_present(row1,
                                                self.getValueAt(self.find_row_prev_location(row1), locations_col),
                                                self.getValueAt(row1, locations_col)) == True]
        if col == stop_sensor_col:
            if row not in stop_sensor_present_list:
                return False

        if col == duration_col:
            return False
        return True

    def stop_sensor_present(self, row, prev_station, last_station):
        global g
        # it is too difficult to determine whether a stop sensor is present
        # (maybe later: would have to determine the last section of the last transit joining the previous and current locations)
        # (and we wanted to determine this to see whether we could modify the stop sensor column cell)
        # so we are going to determine whether we have set the use of a stop sensor in the past
        # the
        if row <= 0:
            return False
        if prev_station is None:
            # print "prev_station is None"
            return False
        if last_station is None:
            # print "last_station is None"
            return False
        if prev_station == last_station:
            # print "prev_station == last_station"
            return False
        DM = DispatchMaster()
        if g.g_express.containsVertex(prev_station) and g.g_express.containsVertex(last_station):
            transit_name = DM.get_transit_name(prev_station, last_station)
        else:
            OptionDialog().displayMessage("You have an old route with a station that does not exist. \n Delete the old station!!\n\n" +\
                                          "Either " + prev_station + " or " + last_station + "\n\n" + \
                                          "You may have to close this panel repeatedly if there are several instances of old stations \n" + \
                                          "in the route")
            return False
        if DM.forward_stopping_sensor_exists(transit_name):
            return True


        return False

    # only include if data can change.
    def setValueAt(self, value, row, col) :
        [locations_col, journey_time_col, wait_time_col, duration_sec_col, duration_col, departure_time_col, add_loc_col, add_action_col, delete_col, stop_sensor_col] = [0, 1, 2, 3, 4, 5, 6, 7,8,9]
        if col == departure_time_col:
            if not self.isValidTimeFormat(value):
                return
        if col == duration_col or col == journey_time_col or col == wait_time_col:
            if value == None:
                return
            try:
                float(value)
            except:
                return

        self.data[row][col] = value
        self.fireTableCellUpdated(row, col)

    def isValidTimeFormat(self, input_string):
        import re
        pattern = r"^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$"  # Matches HH.MM format
        my_match = re.match(pattern, input_string) is not None
        return my_match
