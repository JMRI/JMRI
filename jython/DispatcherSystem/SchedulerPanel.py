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



class CreateAndShowGUI4(TableModelListener):

    def __init__(self, class_ResetButtonMaster):
        self.logLevel = 0
        self.class_ResetButtonMaster = class_ResetButtonMaster
        #Create and set up the window.

        self.initialise_model(class_ResetButtonMaster)
        self.frame = JFrame("Allocate Routes")
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
        scrollPane.setSize(600,600);

        self.topPanel.add(scrollPane);

        self.buttonPane = JPanel();
        self.buttonPane.setLayout(BoxLayout(self.buttonPane, BoxLayout.LINE_AXIS))
        self.buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10))

        button_add = JButton("Add", actionPerformed = self.add_row_action)
        self.buttonPane.add(button_add);
        self.buttonPane.add(Box.createRigidArea(Dimension(10, 0)))

        button_populate = JButton("Populate", actionPerformed = self.populate_action)
        self.buttonPane.add(button_populate);
        self.buttonPane.add(Box.createRigidArea(Dimension(10, 0)))

        button_tidy = JButton("Tidy", actionPerformed = self.tidy_action)
        self.buttonPane.add(button_tidy);
        self.buttonPane.add(Box.createRigidArea(Dimension(10, 0)))

        button_apply = JButton("Run Routes", actionPerformed = self.apply_action)
        self.buttonPane.add(button_apply)
        self.buttonPane.add(Box.createHorizontalGlue());

        button_close = JButton("Close", actionPerformed = self.close_action)
        self.buttonPane.add(button_close)
        self.buttonPane.add(Box.createHorizontalGlue());

        button_task = JButton("Task", actionPerformed = self.task_action)
        self.buttonPane.add(button_task)
        self.buttonPane.add(Box.createHorizontalGlue());

        button_task = JButton("Delay", actionPerformed = self.delay_action)
        self.buttonPane.add(button_task)
        self.buttonPane.add(Box.createHorizontalGlue());

        button_repetitions = JButton("No. repetitions", actionPerformed = self.repetitions_action)
        self.buttonPane.add(button_repetitions)
        self.buttonPane.add(Box.createHorizontalGlue());

        button_savetofile = JButton("Save To File", actionPerformed = self.savetofile_action)
        self.buttonPane.add(button_savetofile)
        self.buttonPane.add(Box.createHorizontalGlue());

        button_loadfromfile = JButton("Load From File", actionPerformed = self.loadfromfile_action)
        self.buttonPane.add(button_loadfromfile)
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

        self.model = None
        self.model = MyTableModel4()
        self.table = JTable(self.model)
        self.model.addTableModelListener(MyModelListener4(self, class_ResetButtonMaster));
        self.class_ResetButtonMaster = class_ResetButtonMaster


        pass
    def self_table(self):

        #table.setPreferredScrollableViewportSize(Dimension(500, 70));
        #table.setFillsViewportHeight(True)
        #self.table.getModel().addtableModelListener(self)
        self.table.setFillsViewportHeight(True);
        self.table.setRowHeight(30);
        #table.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
        # self.resizeColumnWidth(table)
        columnModel = self.table.getColumnModel();

        [train_col, route_col, run_route_col, task_col, delay_col, repetition_col] = [0, 1, 2, 3, 4, 5]
        columnModel.getColumn(route_col).setPreferredWidth(200);
        columnModel.getColumn(task_col).setPreferredWidth(150);

        # first column is the trains
        self.trainColumn = self.table.getColumnModel().getColumn(train_col);
        self.combobox0 = JComboBox()

        # for train in self.class_ResetButtonMaster.get_list_of_engines_to_move():
        #     self.combobox0.addItem(train)

        self.trainColumn.setCellEditor(DefaultCellEditor(self.combobox0));
        renderer0 = ComboBoxCellRenderer4()
        self.trainColumn.setCellRenderer(renderer0);

        # second column is the routes

        self.routesColumn = self.table.getColumnModel().getColumn(route_col);
        self.combobox1 = JComboBox()

        RouteManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.routes.RouteManager)
        routes = RouteManager.getRoutesByNameList()
        for route in routes:
            self.combobox1.addItem(route)
        self.routesColumn.setCellEditor(DefaultCellEditor(self.combobox1));
        renderer1 = ComboBoxCellRenderer4()
        self.routesColumn.setCellRenderer(renderer1);

        # first column is the trains
        self.taskColumn = self.table.getColumnModel().getColumn(task_col);
        self.combobox3 = JComboBox()

        tasks = ["Once", "Repeat every 20 mins","Repeat every 30 mins", "Repeat every Hour", "Repeat every 2 Hours"]
        for task in tasks:
            self.combobox3.addItem(task)

        self.taskColumn.setCellEditor(DefaultCellEditor(self.combobox3));
        renderer3 = ComboBoxCellRenderer4()
        self.taskColumn.setCellRenderer(renderer3);

        jpane = JScrollPane(self.table)
        panel = JPanel()
        panel.add(jpane)
        result = JScrollPane(panel)
        return self.table

    def add_row_action(self, e):
        model = e.getSource()
        data = self.model.getValueAt(0, 0)
        count = self.model.getRowCount()
        colcount = self.model.getColumnCount()
        self.model.add_row()
        self.completeTablePanel()

    def populate_action(self, event):
        trains_to_put_in_dropdown = self.class_ResetButtonMaster.get_list_of_engines_to_move()
        self.model.populate(trains_to_put_in_dropdown)
        self.completeTablePanel()
        pass

    def tidy_action(self,e):
        self.model.remove_not_set_row()
        self.completeTablePanel()

    def savetofile_action(self, event):

        #Tidy
        self.model.remove_not_set_row()
        self.completeTablePanel()

        if self.model.getRowCount() == 0:
            msg = "There are no valid rows"
            result = OptionDialog().displayMessage(msg)
            return

        msg = "Saving Valid rows"
        result = OptionDialog().displayMessage(msg)


        dir = self.directory()
        j = JFileChooser(dir);
        j.setAcceptAllFileFilterUsed(False)
        filter = FileNameExtensionFilter("text files txt", ["txt"])
        j.addChoosableFileFilter(filter);
        j.setDialogTitle("Select a .txt file");



        ret = j.showSaveDialog(None);
        if (ret == JFileChooser.APPROVE_OPTION) :
            file = j.getSelectedFile()
            if file == "" or file == None:
                msg = "No file selected"
                result = OptionDialog().displayMessage(msg)
                return
            if FilenameUtils.getExtension(file.getName()).lower() == "txt" :
                #filename is OK as-is
                pass
            else:
                #file = File(file.toString() + ".txt");  # append .txt if "foo.jpg.txt" is OK
                file = File(file.getParentFile(), FilenameUtils.getBaseName(file.getName())+".txt") # ALTERNATIVELY: remove the extension (if any) and replace it with ".xml"

        else:
            return
        if self.logLevel > 0: print "savetofile action", file
        my_list = []
        [train, route, task, delay, repetitions] = [0, 1, 3, 4, 5]
        for row in range(len(self.model.data)):
            train_name = str(self.model.data[row][train])
            route_name = str(self.model.data[row][route])
            task_name = str(self.model.data[row][task])
            delay_name = str(self.model.data[row][delay])
            repetitions_name = str(self.model.data[row][repetitions])
            row_list = [train_name, route_name, task_name, delay_name, repetitions_name]
            my_list.append(row_list)
        self.write_list(my_list,file)


    def loadfromfile_action(self, event):
        # load the file
        dir = self.directory()
        j = JFileChooser(dir);
        j.setAcceptAllFileFilterUsed(False)
        filter = FileNameExtensionFilter("text files txt", ["txt"])
        j.setDialogTitle("Select a .txt file");
        j.addChoosableFileFilter(filter);
        ret = j.showOpenDialog(None);
        if (ret == JFileChooser.APPROVE_OPTION) :
            file = j.getSelectedFile()
            if self.logLevel > 0: print "about to read list", file
            my_list = self.read_list(file)
            if self.logLevel > 0: print "my_list", my_list
            for row in reversed(range(len(self.model.data))):
                self.model.data.pop(row)
            i = 0
            [train, route, task, delay, repetitions] = [0, 1, 3, 4, 5]
            for row in my_list:
                [train_val, route_val, task_val, delay_val, repetitions_val] = row
                self.model.add_row()
                self.model.data[i][train] = train_val.replace('"','')
                self.model.data[i][route] = route_val.replace('"','')
                self.model.data[i][task] = task_val.replace('"','')
                self.model.data[i][delay] = delay_val.replace('"','')
                self.model.data[i][repetitions] = repetitions_val.replace('"','')
                i += 1
            self.completeTablePanel()

            msg = "Deleting invalid rows"
            result = OptionDialog().displayMessage(msg)
            if result == JOptionPane.NO_OPTION:
                return

            # check the loaded contents
            # 1) check that the trains are valid
            # 2) ckeck that the blocks are occupied by valid trains
            # if either of the above are not valic we blank the entries
            # 3) Tidy

            [train_col, route_col, run_route_col, task_col, delay_col, repetition_col] = [0, 1, 2, 3, 4, 5]

            # check the trains are valid

            trains_to_put_in_dropdown = [t for t in self.class_ResetButtonMaster.get_list_of_engines_to_move()]
            for row in reversed(range(len(self.model.data))):
                if self.model.data[row][train_col] not in trains_to_put_in_dropdown:
                    self.model.data.pop(row)

            RouteManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.routes.RouteManager)
            routes = [str(route) for route in RouteManager.getRoutesByNameList()]
            for row in reversed(range(len(self.model.data))):
                if self.model.data[row][route_col] not in routes:
                    self.model.data.pop(row)
            self.completeTablePanel()

    def close_action(self, event):
        self.frame.dispatchEvent(WindowEvent(self.frame, WindowEvent.WINDOW_CLOSING));

    def delay_action(self, event):
        [train_col, route_col, run_route_col, task_col, delay_col, repetition_col] = [0, 1, 2, 3, 4, 5]
        for row in reversed(range(len(self.model.data))):
            old_delay = int(self.model.data[0][delay_col])
            if old_delay == None: old_delay = 0
            new_delay = self.new_delay(old_delay)
            self.model.data[row][delay_col] = new_delay
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

    def repetitions_action(self, event):
        [train_col, route_col, run_route_col, task_col, delay_col, repetition_col] = [0, 1, 2, 3, 4, 5]
        for row in reversed(range(len(self.model.data))):
            old_val = int(self.model.data[0][repetition_col])
            if old_val == None: old_val = 0
            new_val = self.new_val(old_val)
            self.model.data[row][repetition_col] = new_val

        self.completeTablePanel()
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
        [train_col, route_col, run_route_col, task_col, delay_col, repetition_col] = [0, 1, 2, 3, 4, 5]
        for row in reversed(range(len(self.model.data))):
            old_val = str(self.model.data[0][task_col])
            if old_val == None: old_val = 0
            new_val = self.new_task(old_val)
            self.model.data[row][task_col] = new_val
        self.completeTablePanel()

    def new_task(self, old_val):


        if old_val == "Once":
            new_val = "Repeat every 20 mins"
        elif old_val == "Repeat every 20 mins":
            new_val = "Repeat every 30 mins"
        elif old_val == "Repeat every 30 mins":
            new_val = "Repeat every Hour"
        elif old_val == "Repeat every Hour":
            new_val = "Repeat every 2 Hours"
        elif old_val == "Repeat every 2 Hours":
            new_val = "Once"
        else:
            return "Once"
        return new_val


    def apply_action(self, event):
        [train_col, route_col, run_route_col, task_col, delay_col, repetition_col] = [0, 1, 2, 3, 4, 5]
        # print "apply action"
        for row in reversed(range(len(self.model.data))):
            train_name = str(self.model.data[row][train_col])
            route_name = str(self.model.data[row][route_col])
            delay_val = str(self.model.data[row][delay_col])
            if train_name != "" and route_name != "" and delay_val != "":
                self.run_route(row, self.model, self, self.class_ResetButtonMaster)
            else:
                msg = "not running route, train, route or delay is not set"
                OptionDialog().displayMessage(msg,"")
        self.completeTablePanel()
        if self.model.getRowCount() == 0:
            self.frame.dispatchEvent(WindowEvent(self.frame, WindowEvent.WINDOW_CLOSING))


    def run_route(self, row, model, class_CreateAndShowGUI4, class_ResetButtonMaster):
        [train_col, route_col, run_route_col, task_col, delay_col, repetition_col] = [0, 1, 2, 3, 4, 5]
        route_name = str(model.getValueAt(row, route_col))
        if route_name == None:
            msg = "not running route is not set"
            self.od.displayMessage(msg,"")
            return
        RouteManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.routes.RouteManager)
        route = RouteManager.getRouteByName(route_name)

        train_name = str(model.getValueAt(row, train_col))
        if train_name == None or train_name == "":
            msg = "not running route, train is not set"
            self.od.displayMessage(msg,"")
            return
        station_from = class_ResetButtonMaster.get_position_of_train(train_name)

        option = str(model.getValueAt(row, task_col))

        repeat = False
        dont_run_route = False
        no_repetitions = 0
        if option == "stop at end of route":
            station_to = None
            repeat = False
        elif option == "return to start position":
            station_to = station_from
            repeat = False
        elif option == "return to start and repeat":
            station_to = station_from
            repeat = True
        else:
            dont_run_route = True

        if repeat:
            no_repetitions = str(model.getValueAt(row, repetition_col))
        else:
            no_repetitions = 0

        # delay by delay_val before starting route
        delay_val = int(model.getValueAt(row, delay_col)) *1000

        if dont_run_route == False:
            if self.logLevel > 0: print "station_from",    station_from, "station_to",station_to, \
                "repeat",repeat, "delay", delay_val, "no_repetitions", no_repetitions
            run_train = RunRoute(route, g.g_express, station_from, station_to, no_repetitions, train_name, delay_val)
            run_train.setName("running_route_" + route_name)
            instanceList.append(run_train)
            run_train.start()
            model.data.pop(row)
            class_CreateAndShowGUI4.completeTablePanel()


    def directory(self):
        path = jmri.util.FileUtil.getUserFilesPath() + "dispatcher" + java.io.File.separator + "routes"
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

class MyModelListener4(TableModelListener):

    def __init__(self, class_CreateAndShowGUI4, class_ResetButtonMaster):
        self.class_CreateAndShowGUI4 = class_CreateAndShowGUI4
        self.class_ResetButtonMaster = class_ResetButtonMaster
        self.cancel = False
        self.logLevel = 0
    def tableChanged(self, e) :
        global trains_allocated
        row = e.getFirstRow()
        column = e.getColumn()
        model = e.getSource()
        columnName = model.getColumnName(column)
        data = model.getValueAt(row, column)
        class_CreateAndShowGUI4 = self.class_CreateAndShowGUI4
        class_ResetButtonMaster = self.class_ResetButtonMaster
        tablemodel = class_CreateAndShowGUI4.model
        [train_col, route_col, run_route_col, task_col, delay_col, repetition_col] = [0, 1, 2, 3, 4, 5]
        if column == 0:     #trains
            pass
        elif column == 1:       # sections
            pass
        elif column == run_route_col:
            class_CreateAndShowGUI4.run_route(row, model, class_CreateAndShowGUI4, class_ResetButtonMaster)

class ComboBoxCellRenderer4 (TableCellRenderer):

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

class MyTableModel4 (DefaultTableModel):

    columnNames = ["Time", "Route", "Don't Run", "Task", "Delay (secs)", "No. Repetitons"]

    def __init__(self):
        l1 = ["", "", False, "stop at end of route", 10, 0]
        self.data = [l1]

    def remove_not_set_row(self):
        b = False
        for row in reversed(range(len(self.data))):
            # print "row", row
            if self.data[row][1] == "":
                self.data.pop(row)

    def add_row(self):
        # print "addidn row"
        # if row < len(self.data):
        # print "add"
        self.data.append(["00:00", "", False, "Once", 10, 0])
        # print self.data
        # print "added"

    def populate(self, trains_to_put_in_dropdown):
        # for row in reversed(range(len(self.data))):
        #     self.data.pop(row)
        # self.data = []
        # append all trains to put in dropdown
        [train_col, route_col, run_route_col, task_col, delay_col, repetition_col] = [0, 1, 2, 3, 4, 5]
        for train in trains_to_put_in_dropdown:
            train_present = False
            for row in reversed(range(len(self.data))):
                if self.data[row][train_col] == train:
                    train_present = True
            if train_present == False:
                self.data.append([train, "", False, "stop at end of route", 10, 3])
        # delete rows with no trains
        for row in reversed(range(len(self.data))):
            if self.data[row][train_col] == None or self.data[row][train_col] == "":
                self.data.pop(row)

    def getColumnCount(self) :
        return len(self.columnNames)


    def getRowCount(self) :
        return len(self.data)


    def getColumnName(self, col) :
        return self.columnNames[col]


    def getValueAt(self, row, col) :
        return self.data[row][col]

    def getColumnClass(self, c) :
        if c <= 1:
            return java.lang.Boolean.getClass(JComboBox)
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

from javax.swing import JFrame, JButton, JOptionPane
from java.awt.event import ActionListener

class TimePickerExample:
    def __init__(self):
        self.frame = JFrame("Time Picker Example")
        self.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
        self.frame.setSize(300, 200)

        self.pick_time_button = JButton("Pick Time", actionPerformed=self.show_time_picker)
        self.frame.add(self.pick_time_button)

        self.frame.setVisible(True)

    def show_time_picker(self, event):
        # Show a simple JOptionPane input dialog for time selection
        selected_time = JOptionPane.showInputDialog(self.frame, "Select a time (HH:mm):")
        if selected_time:
            print("Selected time:", selected_time)





