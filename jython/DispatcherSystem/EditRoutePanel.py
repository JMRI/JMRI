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
from java.awt import Toolkit

# scheduled_start = "00:00"

class CreateAndShowGUI5(TableModelListener):

    def __init__(self, class_ResetButtonMaster, route_name, param_scheduled_start):
        global scheduled_start
        print "%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%route_name", route_name

        RouteManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.routes.RouteManager)
        self.route = RouteManager.getRouteByName(route_name)
        print "ROUTE", self.route.getName()

        scheduled_start = param_scheduled_start
        print "scheduled_start", scheduled_start

        print "INITIALISING EditRoutePanel"
        self.logLevel = 0
        self.class_ResetButtonMaster = class_ResetButtonMaster
        #Create and set up the window.

        self.initialise_model(class_ResetButtonMaster)
        self.frame = JFrame("Train Route")
        self.frame.setSize(600, 600)

        config = self.frame.getGraphicsConfiguration()
        bounds = config.getBounds()
        insets = Toolkit.getDefaultToolkit().getScreenInsets(config)

        x = bounds.x + bounds.width - insets.right - self.frame.getWidth() -400
        y = bounds.y + insets.top + 100
        self.frame.setLocation(x, y)

        # scrollPane.getViewport().setViewPosition(Point(0,0));

        self.completeTablePanel()
        print "about to populate"
        self.populate_action(None)
        self.cancel = False



    def completeTablePanel(self):

        self.topPanel= JPanel();
        self.topPanel.setLayout(BoxLayout(self.topPanel, BoxLayout.X_AXIS))
        self.self_table()

        scrollPane = JScrollPane(self.table);
        scrollPane.setSize(600,600);

        self.topPanel.add(scrollPane);

        self.buttonPane = JPanel();
        self.buttonPane.setLayout(BoxLayout(self.buttonPane, BoxLayout.LINE_AXIS))
        self.buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10))


        button_close = JButton("Close", actionPerformed = self.close_action)
        self.buttonPane.add(button_close)
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

    def initialise_model(self, class_ResetButtonMaster):

        self.model = MyTableModel5()
        self.model.route = self.route
        self.table = JTable(self.model)
        self.model.addTableModelListener(MyModelListener5(self, class_ResetButtonMaster))
        self.class_ResetButtonMaster = class_ResetButtonMaster

    def self_table(self):

        self.table.setPreferredScrollableViewportSize(Dimension(700, 300));
        #table.setFillsViewportHeight(True)
        #self.table.getModel().addtableModelListener(self)
        self.table.setFillsViewportHeight(True);
        self.table.setRowHeight(30);
        #table.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
        # self.resizeColumnWidth(table)
        columnModel = self.table.getColumnModel();

        [locations_col, journey_time_col, wait_time_col, duration_col, departure_time_col, delete_col] = [0, 1, 2, 3, 4, 5]
        columnModel.getColumn(locations_col).setPreferredWidth(300)

        # we are not using journey_time_col, wait_time_col at moment
        # these a re planned for use when setting the departure times
        # by running an emgine along a acheduled route
        columnModel.getColumn(journey_time_col).setMaxWidth(0)
        columnModel.getColumn(journey_time_col).setMinWidth(0)
        columnModel.getColumn(wait_time_col).setMaxWidth(0)
        columnModel.getColumn(wait_time_col).setMinWidth(0)

        # first column is the trains
        # self.trainColumn = self.table.getColumnModel().getColumn(time_col);
        # self.combobox0 = JComboBox()

        # for train in self.class_ResetButtonMaster.get_list_of_engines_to_move():
        #     self.combobox0.addItem(train)

        # self.trainColumn.setCellEditor(DefaultCellEditor(self.combobox0));
        # renderer0 = ComboBoxCellRenderer5()
        # self.trainColumn.setCellRenderer(renderer0);

        # second column is the routes

        # # self.locationColumn = self.table.getColumnModel().getColumn(location_col);
        # # self.combobox1 = JComboBox()
        #
        # RouteManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.routes.RouteManager)
        # routes = RouteManager.getRoutesByNameList()
        # for route in routes:
        #     self.combobox1.addItem(route)
        # self.locationColumn.setCellEditor(DefaultCellEditor(self.combobox1));
        # renderer1 = ComboBoxCellRenderer5()
        # self.locationColumn.setCellRenderer(renderer1);

        # first column is the trains
        # self.durationColumn = self.table.getColumnModel().getColumn(duration_col);
        # self.combobox3 = JComboBox()
        #
        # tasks = ["Once", "duration every 20 mins","duration every 30 mins", "duration every Hour", "duration every 2 Hours"]
        # for task in tasks:
        #     self.combobox3.addItem(task)
        #
        # self.durationColumn.setCellEditor(DefaultCellEditor(self.combobox3));
        # renderer3 = ComboBoxCellRenderer5()
        # self.durationColumn.setCellRenderer(renderer3);

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

    def get_station_list(self):
        RouteManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.routes.RouteManager)
        locations_list = self.route.getLocationsBySequenceList()
        my_list = [[location.getName(), location.getComment()] for location in locations_list]
        return my_list

    def populate_action(self, event):
        print "populating"
        items_to_put_in_dropdown = self.get_station_list()
        print "items_to_put_in_dropdown", items_to_put_in_dropdown
        self.model.populate(items_to_put_in_dropdown)
        print "populated"
        self.completeTablePanel()
        pass

    def tidy_action(self,e):
        self.model.remove_not_set_row()
        self.completeTablePanel()

    # def savetofile_action(self, event):
    #
    #     #Tidy
    #     self.model.remove_not_set_row()
    #     self.completeTablePanel()
    #
    #     if self.model.getRowCount() == 0:
    #         msg = "There are no valid rows"
    #         result = OptionDialog().displayMessage(msg)
    #         return
    #
    #     msg = "Saving Valid rows"
    #     result = OptionDialog().displayMessage(msg)
    #
    #
    #     dir = self.directory()
    #     j = JFileChooser(dir);
    #     j.setAcceptAllFileFilterUsed(False)
    #     filter = FileNameExtensionFilter("text files txt", ["txt"])
    #     j.addChoosableFileFilter(filter);
    #     j.setDialogTitle("Select a .txt file");
    #
    #
    #
    #     ret = j.showSaveDialog(None);
    #     if (ret == JFileChooser.APPROVE_OPTION) :
    #         file = j.getSelectedFile()
    #         if file == "" or file == None:
    #             msg = "No file selected"
    #             result = OptionDialog().displayMessage(msg)
    #             return
    #         if FilenameUtils.getExtension(file.getName()).lower() == "txt" :
    #             #filename is OK as-is
    #             pass
    #         else:
    #             #file = File(file.toString() + ".txt");  # append .txt if "foo.jpg.txt" is OK
    #             file = File(file.getParentFile(), FilenameUtils.getBaseName(file.getName())+".txt") # ALTERNATIVELY: remove the extension (if any) and replace it with ".xml"
    #
    #     else:
    #         return
    #     if self.logLevel > 0: print "savetofile action", file
    #     my_list = []
    #     [train, route, task, delay, repetitions] = [0, 1, 3, 4, 5]
    #     for row in range(len(self.model.data)):
    #         time_name = str(self.model.data[row][train])
    #         route_name = str(self.model.data[row][route])
    #         task_name = str(self.model.data[row][task])
    #         delay_name = str(self.model.data[row][delay])
    #         repetitions_name = str(self.model.data[row][repetitions])
    #         row_list = [time_name, route_name, task_name, delay_name, repetitions_name]
    #         my_list.append(row_list)
    #     self.write_list(my_list,file)


    def loadfromfile_action(self, event):
        pass
        # # load the file
        # dir = self.directory()
        # j = JFileChooser(dir);
        # j.setAcceptAllFileFilterUsed(False)
        # filter = FileNameExtensionFilter("text files txt", ["txt"])
        # j.setDialogTitle("Select a .txt file");
        # j.addChoosableFileFilter(filter);
        # ret = j.showOpenDialog(None);
        # if (ret == JFileChooser.APPROVE_OPTION) :
        #     file = j.getSelectedFile()
        #     if self.logLevel > 0: print "about to read list", file
        #     my_list = self.read_list(file)
        #     if self.logLevel > 0: print "my_list", my_list
        #     for row in reversed(range(len(self.model.data))):
        #         self.model.data.pop(row)
        #     i = 0
        #     [train, route, task, delay, repetitions] = [0, 1, 3, 4, 5]
        #     for row in my_list:
        #         [train_val, route_val, task_val, train_name_val, repetitions_val] = row
        #         self.model.add_row()
        #         self.model.data[i][train] = train_val.replace('"','')
        #         self.model.data[i][route] = route_val.replace('"','')
        #         self.model.data[i][task] = task_val.replace('"','')
        #         self.model.data[i][delay] = train_name_val.replace('"','')
        #         self.model.data[i][repetitions] = repetitions_val.replace('"','')
        #         i += 1
        #     self.completeTablePanel()
        #
        #     msg = "Deleting invalid rows"
        #     result = OptionDialog().displayMessage(msg)
        #     if result == JOptionPane.NO_OPTION:
        #         return
        #
        #     # check the loaded contents
        #     # 1) check that the trains are valid
        #     # 2) ckeck that the blocks are occupied by valid trains
        #     # if either of the above are not valic we blank the entries
        #     # 3) Tidy
        #
        #     [locations_col, journey_time_col, wait_time_col, duration_col, departure_time_col, delete_col] = [0, 1, 2, 3, 4, 5]
        #
        #     # check the trains are valid
        #
        #     # trains_to_put_in_dropdown = [t for t in self.class_ResetButtonMaster.get_list_of_engines_to_move()]
        #     # for row in reversed(range(len(self.model.data))):
        #     #     if self.model.data[row][time_col] not in trains_to_put_in_dropdown:
        #     #         self.model.data.pop(row)
        #     #
        #     # RouteManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.routes.RouteManager)
        #     # routes = [str(route) for route in RouteManager.getRoutesByNameList()]
        #     # for row in reversed(range(len(self.model.data))):
        #     #     if self.model.data[row][route_col] not in routes:
        #     #         self.model.data.pop(row)
        #     self.completeTablePanel()

    def close_action(self, event):
        self.frame.dispatchEvent(WindowEvent(self.frame, WindowEvent.WINDOW_CLOSING));

    # def delay_action(self, event):
    #     [locations_col, journey_time_col, wait_time_col, duration_col, departure_time_col, delete_col] = [0, 1, 2, 3, 4, 5]
    #     for row in reversed(range(len(self.model.data))):
    #         old_delay = int(self.model.data[0][train_name_col])
    #         if old_delay == None: old_delay = 0
    #         new_delay = self.new_delay(old_delay)
    #         self.model.data[row][train_name_col] = new_delay
    #     self.completeTablePanel()

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
        [locations_col, journey_time_col, wait_time_col, duration_col, departure_time_col, delete_col] = [0, 1, 2, 3, 4, 5]
        # self.model.getDataVector().removeAllElements();
        for row in reversed(range(len(self.model.data))):
            self.model.data.pop(row)
        # return
        # # for rowToRemove in reversed(range(len(self.model.data))):
        # for rowToRemove in range(len(self.model.data)):
        #     rowtoremove = self.table.
        #     self.model.removeRow(rowToRemove);
        #     if(table.getSelectedRow() != -1) {
        #     // remove selected row from the model
        #     model.removeRow(table.getSelectedRow());
        #     JOptionPane.showMessageDialog(null, "Selected row deleted successfully");
        #     }
        #
        #     break
        self.completeTablePanel()
        print "fered"
    def new_val(self, old_val):
        if old_val < 3:
            new_val = 3
        elif old_val < 10:
            new_val = 10
        elif old_val < 30:
            new_val = 30
        elif old_val < 100:
            new_val = 100
        else:
            new_val = 1
        return new_val

    def task_action(self, event):
        [locations_col, journey_time_col, wait_time_col, duration_col, departure_time_col, delete_col] = [0, 1, 2, 3, 4, 5]
        for row in reversed(range(len(self.model.data))):
            old_val = str(self.model.data[0][duration_col])
            if old_val == None: old_val = 0
            new_val = self.new_task(old_val)
            self.model.data[row][duration_col] = new_val
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
        [locations_col, journey_time_col, wait_time_col, duration_col, departure_time_col, delete_col] = [0, 1, 2, 3, 4, 5]
        print "save_action"
        self.clear_everything()
        # print "apply action"
        for row in reversed(range(len(self.model.data))):
            locations_name = str(self.model.data[row][locations_col])
            journey_time_name = str(self.model.data[row][journey_time_col])
            wait_time_name = str(self.model.data[row][wait_time_col])
            duration_name = str(self.model.data[row][duration_col])
            departure_time_name = str(self.model.data[row][departure_time_col])
            delete_name = str(self.model.data[row][delete_col])
            # if time_name != "" and route_name != "" and train_name_val != "":
            if locations_name != "":
                self.save_location_row(row, locations_name, journey_time_name, wait_time_name, duration_name, \
                                   departure_time_name, delete_name)
                pass
            else:
                msg = "Cannot save row: " + str(row) + " train name, route or delay is not set"
                OptionDialog().displayMessage(msg,"")
        self.completeTablePanel()
        if self.model.getRowCount() == 0:
            self.frame.dispatchEvent(WindowEvent(self.frame, WindowEvent.WINDOW_CLOSING))

    def save_location_row(self, row, locations_name, journey_time_name, wait_time_name, duration_name, \
                      departure_time_name, delete_name):
        routeLocationList = self.route.getLocationsBySequenceList()
        # print "a", routeLocationList
        routeLocation = routeLocationList[row]
        # print "b"
        # self.route.setLocation(locations)
        # print "c"
        self.set_value_in_comment(routeLocation, journey_time_name, "journey_time")
        # print "d"
        self.set_value_in_comment(routeLocation, wait_time_name, "wait_time")
        self.set_value_in_comment(routeLocation, duration_name, "duration")

    def set_value_in_comment(self, routeLocation, value, duration_string):
        # print "in set_duration"
        comment = routeLocation.getComment()    #Null
        delim_start = "[" + duration_string + "-"
        delim_end = "-" + duration_string + "]"
        value_current = MyTableModel5().find_between(comment, delim_start, delim_end)   # empty string
        # print "value_current",duration_string, value_current, "value", value
        if value_current != value:
            self.delete_between(comment, delim_start, delim_end)
        comment = ""
        comment = self.insert_between(comment, delim_start, delim_end, value)
        routeLocation.setComment(comment)

    def delete_between(self, string, delim1, delim2):
        first, _, rest = string.partition(delim1)
        _, _, rest = rest.partition(delim2)
        cleaned_text = ' '.join([first.strip(), rest.strip()])
        return cleaned_text
    
    def insert_between(self, string, delim1, delim2, value):
        #inserts value between delim1 and delim2 and adds all to end of string
        to_be_added = " " + delim1 + value + delim2
        # print "to_be_added", to_be_added
        ans = string + to_be_added
        # print "ans", ans
        return ans

    def clear_everything(self):
        TrainManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.trains.TrainManager)
        train_list = TrainManager.getTrainsByTimeList()
        for train in train_list:
            TrainManager.deregister(train)


    def directory(self):
        path = jmri.util.FileUtil.getUserFilesPath() + "dispatcher" + java.io.File.separator + "schedules"
        if not os.path.exists(path):
            os.makedirs(path)
        return path + java.io.File.separator

    def write_list(self, a_list, file):
        # store list in binary file so 'wb' mode
        #file = self.directory() + "blockDirections.txt"
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
        # except:
        #     return ["",""]

class MyModelListener5(TableModelListener):

    def __init__(self, class_CreateAndShowGUI5, class_ResetButtonMaster):
        self.class_CreateAndShowGUI5 = class_CreateAndShowGUI5
        self.class_ResetButtonMaster = class_ResetButtonMaster
        self.cancel = False
        self.logLevel = 0
        self.i = 0

    def tableChanged(self, e) :
        print "INDES", self.i
        self.i +=1
        # if self.i % 2 == 0: return
        global trains_allocated
        row = e.getFirstRow()
        column = e.getColumn()
        self.model = e.getSource()
        columnName = self.model.getColumnName(column)

        class_CreateAndShowGUI5 = self.class_CreateAndShowGUI5
        class_ResetButtonMaster = self.class_ResetButtonMaster
        tablemodel = class_CreateAndShowGUI5.model
        [locations_col, journey_time_col, wait_time_col, duration_col, departure_time_col, delete_col] = [0, 1, 2, 3, 4, 5]
        print "a"
        if column == duration_col:     #trains
            # when duration is clicked
            # 1) calculate the relevant departure
            if row != self.model.find_row_first_location():
                departure = self.calc_departure_time(row)
                # departure = "00:58"
                self.model.setValueAt(departure, row, departure_time_col)
            # 2) departure sequence will be triggered
            # see sequence below
            class_CreateAndShowGUI5.save()

        elif column == departure_time_col:       # sections
            # when departure is clicked
            # a) calculate the relevant duration
            print "%%%%%%%%%%%%%%%%%%%%%%%%%%%%%row", row,  \
                "self.model.find_first_location()", self.model.find_row_first_location()
            if row != self.model.find_row_first_location():
                print "# a) calculate the relevant duration"
                duration = self.calculate_duration(row)
                current_duration = self.model.getValueAt(row, duration_col)
                if duration != current_duration:
                    self.model.setValueAt(duration, row, duration_col)
                class_CreateAndShowGUI5.save()

            # b) touch the durations later
            next_row = self.model.find_row_next_location(row)
            print "&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&row", row, "next_row", next_row
            if next_row != None:
                try:
                    print "# b) touch the next duration","next_row", next_row
                    value = self.model.getValueAt(next_row, duration_col)
                    print "%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%value", value
                    self.model.setValueAt(value, next_row, duration_col)
                    print "touched"
                except:
                    pass
                # self.update_departure_time_col(row)
        elif column == delete_col:
            # class_CreateAndShowGUI5.run_route(row, model, class_CreateAndShowGUI5, class_ResetButtonMaster)
            self.delete_row(row)
            class_CreateAndShowGUI5.completeTablePanel()
            pass

        # class_CreateAndShowGUI5.save()    # save everything when the table is chabged

    def update_departure_time_col(self, current_row):
        [locations_col, journey_time_col, wait_time_col, duration_col, departure_time_col, delete_col] = [0, 1, 2, 3, 4, 5]
        routelocations_rows_list = [routelocation.getSequenceNumber()-1 \
                                    for routelocation in self.model.route.getLocationsBySequenceList() \
                                    if ".py" not in routelocation.getName()]

        print "routelocations_rows_list", routelocations_rows_list
        for row in routelocations_rows_list:
            if row > current_row:
                # trigger the update command by touching the element
                value = self.model.getValueAt(row, departure_time_col)
                self.model.setValueAt(value, row, departure_time_col)
                break


    def calculate_duration(self, row):
        print "CALCULATE DURATION", "row", row
        # calculate duration from current and previous departure times
        [locations_col, journey_time_col, wait_time_col, duration_col, departure_time_col, delete_col] = [0, 1, 2, 3, 4, 5]
        current_departure_time = self.model.getValueAt(row, departure_time_col)
        prev_row = self.model.find_row_prev_location(row)
        if prev_row == None:
            print "prev_row none", "returning"
            return
        print "prev_row", prev_row
        previous_departure_time = self.model.getValueAt(prev_row, departure_time_col)
        print "previous_departure_time", previous_departure_time
        # time and prev_time are in HH:MM
        hh, _, mm = current_departure_time.partition(":")
        hhprev, _, mmprev = previous_departure_time.partition(":")
        duration = int(hh) * 60 - int(hhprev) * 60 + int(mm) - int(mmprev)
        print "DURATION", duration
        return str(duration)

    def calc_departure_time(self, row):
        # calculate departure time from durastion and prev departure time
        print "calc departure time", "row", row
        [locations_col, journey_time_col, wait_time_col, duration_col, departure_time_col, delete_col] = [0, 1, 2, 3, 4, 5]
        current_duration = self.model.getValueAt(row, duration_col)
        print "CURRENT_DURATION", current_duration
        prev_row = self.model.find_row_prev_location(row)
        print "prev_row", prev_row
        if prev_row == None:
            return
        previous_departure_time = self.model.getValueAt(prev_row, departure_time_col)
        print "previous_departure_time", previous_departure_time
        hhprev, _, mmprev = previous_departure_time.partition(":")
        departure_time_mins = str((int(mmprev) + int(current_duration)) % 60).zfill(2)
        departure_time_hours = str((int(mmprev) + int(current_duration)) // 60).zfill(2)
        departure_time = departure_time_hours + ":" + departure_time_mins
        print "departure time", departure_time
        return departure_time

    def delete_row(self, row):
        self.model.data.pop(row)


    # def show_time_picker(self):
    #     # Show a simple JOptionPane input dialog for time selection
    #     selected_time = JOptionPane.showInputDialog(None, "Select a time (HH:mm):")
    #     if selected_time:
    #         print("Selected time:", selected_time)
    #     return selected_time


# class ComboBoxCellRenderer5 (TableCellRenderer):
#
#     def getTableCellRendererComponent(self, jtable, value, isSelected, hasFocus, row, column) :
#         panel = self.createPanel(value)
#         return panel
#
#     def createPanel(self, s) :
#         p = JPanel(BorderLayout())
#         p.add(JLabel(str(s), JLabel.LEFT), BorderLayout.WEST)
#         icon = UIManager.getIcon("Table.descendingSortIcon");
#         p.add(JLabel(icon, JLabel.RIGHT), BorderLayout.EAST);
#         p.setBorder(BorderFactory.createLineBorder(Color.blue));
#         return p;

class MyTableModel5 (DefaultTableModel):

    columnNames = ["Station / Action", "Journey Time", "Wait Time", "Duration", "Departure Time", "Delete Row"]

    def __init__(self):
        l1 = ["", "", False, "stop at end of route", 10, 0]
        self.data = [l1]
        self.route = None    # updated from outside class

    def remove_not_set_row(self):
        b = False
        for row in reversed(range(len(self.data))):
            # print "row", row
            if self.data[row][1] == "":
                self.data.pop(row)

    def add_row(self):
        pass
        # [locations_col, journey_time_col, wait_time_col, duration_col, departure_time_col, delete_col] = [0, 1, 2, 3, 4, 5]
        #
        # indices = [int(self.data[row][train_name_col].split("Train",1)[1]) for row in reversed(range(len(self.data)))
        #            if self.data[row][train_name_col].startswith("Train")]
        # if indices == []:
        #     index = 1
        # else:
        #     index = max(indices) + 1
        # train_name = "Train" + str(index)
        # self.data.append(["00:00", "", "Once", False, train_name, False])
        # # print self.data
        # # print "added"

    def populate(self, items_to_put_in_dropdown):
        global scheduled_start
        print "in populate"
        for row in reversed(range(len(self.data))):
            self.data.pop(row)
        print "cleared everything"
        # self.data = []
        # append all trains to put in dropdown
        [locations_col, journey_time_col, wait_time_col, duration_col, departure_time_col, delete_col] = [0, 1, 2, 3, 4, 5]
        for [location, comment] in items_to_put_in_dropdown:
            if ".py" not in location:    # omit actions
                print "location", location
                if "skip" in comment:
                    skip = True
                else:
                    skip = False
                print "skip", skip

                journey_time = self.find_between(comment, "[journey_time-", "-journey_time]")
                print "journey_time" , journey_time

                wait_time = self.find_between(comment, "[wait_time-", "-wait_time]")
                print "wait_time" , wait_time

                duration = self.find_between(comment, "[duration-", "-duration]")
                if duration == "": duration = "0"
                print "duration" , duration

                # departure_time = self.find_between(comment, "[departure_time-", "-departure_time]")
                # print "departure_time" , departure_time

                departure_time = "00:00"     # departure times will be filled in from durations
            else:
                departure_time = None
                duration = None
                wait_time = None

            self.data.append([location, journey_time, wait_time, duration, departure_time, False])
        print "populated"

        # now update the first location which is a station with the time of the schedule start

        [locations_col, journey_time_col, wait_time_col, duration_col, departure_time_col, delete_col] = [0, 1, 2, 3, 4, 5]
        row = self.find_row_first_location()
        self.setValueAt(scheduled_start, row, departure_time_col)

        # delete rows with no trains
        # for row in reversed(range(len(self.data))):
        #     if self.data[row][time_col] == None or self.data[row][dont_schedule_col] == "":
        #         self.data.pop(row)

    def find_row_first_location(self):
        print "find_row_first_location"
        # get the row (sequenceNo) of the first location that is not an action (a python file  xx.py)
        routelocationsSequenceNumber_list = [ [routelocation, routelocation.getSequenceNumber()] \
                for routelocation in self.route.getLocationsBySequenceList() \
                        if ".py" not in routelocation.getName()]

        # [i for i,x in enumerate(testlist) if x == 1]

        # ["foo", "bar", "baz"].index("bar")

        print "routelocationsSequenceNumber_list", routelocationsSequenceNumber_list
        current_val = [[routelocation, sequenceNo] \
                         for [routelocation, sequenceNo] in routelocationsSequenceNumber_list \
                         if 1 == sequenceNo][0]
        print "current_val", current_val

        current_index = routelocationsSequenceNumber_list.index(current_val)
        print "current_index", current_index
        # print "routelocations_list", routelocations_list, "index", index
        try:
            [routelocation, row] = routelocationsSequenceNumber_list[current_index]
            # row = routelocationsSequenceNumber_list[currentIndex + 1]
        except:
            row = None
        print "row", row
        return row - 1    # row number starts from 0

        # return 0

    def find_row_next_location(self, row):
        # get the row (sequenceNo) of the first location that is not an action (a python file  xx.py)
        #
        # the sequencenumber is the row

        routelocationsSequenceNumber_list = [ [routelocation, routelocation.getSequenceNumber()] \
                                              for routelocation in self.route.getLocationsBySequenceList() \
                                              if ".py" not in routelocation.getName()]

        print "routelocationsSequenceNumber_list", routelocationsSequenceNumber_list
        current_val = [[routelocation, sequenceNo] \
                       for [routelocation, sequenceNo] in routelocationsSequenceNumber_list \
                       if row == sequenceNo-1][0]
        print "current_val", current_val

        current_index = routelocationsSequenceNumber_list.index(current_val)
        print "current_index", current_index
        # print "routelocations_list", routelocations_list, "index", index
        try:
            # if (current_index + 1) < (len(routelocationsSequenceNumber_list) - 1):
            [routelocation, row] = routelocationsSequenceNumber_list[current_index + 1]
            # row = routelocationsSequenceNumber_list[currentIndex + 1]
        except:
            row = None
        print "row", row
        return row - 1  # row number starts from 0


        # routelocationsSequenceNumber_list = [ [index, routelocation.getSequenceNumber()] \
        #                         for enumerate(routelocation) in self.route.getLocationsBySequenceList() \
        #                         if ".py" not in routelocation.getName()]
        # print "routelocationsSequenceNumber_list", routelocationsSequenceNumber_list
        # currentIndex = [index for index, s_row in routelocationsSequenceNumber_list if row == s_row][0]
        # print "currentIndex", currentIndex
        # try:
        #     row = routelocationsSequenceNumber_list[currentIndex + 1]
        # except:
        #     row = None
        # return ans
        # return 0

    def find_row_prev_location(self, row):
        # get the row (sequenceNo) of the first location that is not an action (a python file  xx.py)
        routelocationsSequenceNumber_list = [ [routelocation, routelocation.getSequenceNumber()] \
                                              for routelocation in self.route.getLocationsBySequenceList() \
                                              if ".py" not in routelocation.getName()]

        print "routelocationsSequenceNumber_list", routelocationsSequenceNumber_list
        fred1_current_val = [[routelocation, sequenceNo] \
                            for [routelocation, sequenceNo] in routelocationsSequenceNumber_list \
                            ]
        print "fred1_current_val", fred1_current_val
        print "row", row
        fred_current_val = [[routelocation, sequenceNo] \
                      for [routelocation, sequenceNo] in routelocationsSequenceNumber_list \
                      if row == sequenceNo-1]
        print "fred_current_val", fred_current_val
        current_val = [[routelocation, sequenceNo] \
                       for [routelocation, sequenceNo] in routelocationsSequenceNumber_list \
                       if row == sequenceNo-1][0]
        print "current_val", current_val

        current_index = routelocationsSequenceNumber_list.index(current_val)
        print "current_index", current_index
        # print "routelocations_list", routelocations_list, "index", index
        try:
            [routelocation, row] = routelocationsSequenceNumber_list[current_index - 1]
            # row = routelocationsSequenceNumber_list[currentIndex - 1]
        except:
            row = None
        print "row", row
        return row - 1     # row number starts from 0

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
        [locations_col, journey_time_col, wait_time_col, duration_col, departure_time_col, delete_col] = [0, 1, 2, 3, 4, 5]
        # do not allow editing of duration in action cols
        routelocations_rows_list = [routelocation.getSequenceNumber()-1 \
                                    for routelocation in self.route.getLocationsBySequenceList() \
                                    if ".py" not in routelocation.getName()]
        if row not in routelocations_rows_list:
            if col == duration_col or col == departure_time_col:
                return False
        return True

    # only include if data can change.
    def setValueAt(self, value, row, col) :
        print "row1", row, "col", col, "value", value
        [locations_col, journey_time_col, wait_time_col, duration_col, departure_time_col, delete_col] = [0, 1, 2, 3, 4, 5]
        if col == journey_time_col or col == wait_time_col or col == departure_time_col:
            print "row2", row, "col", col, "value", value
            if not self.isValidTimeFormat(value):
                return
        if col == duration_col:
            if not value.isdigit():
                return
        self.data[row][col] = value
        self.fireTableCellUpdated(row, col)

    def isValidTimeFormat(self, input_string):
        import re
        pattern = r"^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$"  # Matches HH.MM format
        my_match = re.match(pattern, input_string) is not None
        print "m", re.match(pattern, input_string) is not None
        print "my_match", re.match(pattern, input_string)

        return my_match










