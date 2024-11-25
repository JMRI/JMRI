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

class CreateAndShowGUI6(TableModelListener):

    def __init__(self, class_SchedulerPanel):
        self.logLevel = 0
        self.class_SchedulerPanel = class_SchedulerPanel
        #Create and set up the window.

        self.initialise_model(class_SchedulerPanel)
        self.frame = JFrame("Routes")
        self.frame.setSize(600, 600);

        self.completeTablePanel()
        # print "about to populate"
        self.populate_action(None)
        self.cancel = False
        self.toggle = True

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

        # button_add = JButton("Add", actionPerformed = self.add_row_action)
        # self.buttonPane.add(button_add);
        # self.buttonPane.add(Box.createRigidArea(Dimension(10, 0)))

        # button_populate = JButton("Populate", actionPerformed = self.populate_action)
        # self.buttonPane.add(button_populate);
        # self.buttonPane.add(Box.createRigidArea(Dimension(10, 0)))

        # button_tidy = JButton("Tidy", actionPerformed = self.tidy_action)
        # self.buttonPane.add(button_tidy);
        # self.buttonPane.add(Box.createRigidArea(Dimension(10, 0)))
        #
        # button_apply = JButton("Save", actionPerformed = self.save_action)
        # self.buttonPane.add(button_apply)
        # self.buttonPane.add(Box.createHorizontalGlue());

        button_close = JButton("Close", actionPerformed = self.close_action)
        self.buttonPane.add(button_close)
        self.buttonPane.add(Box.createHorizontalGlue());

        button_task = JButton("Toggle Scheduled Routes", actionPerformed = self.scheduled_routes_action)
        self.buttonPane.add(button_task)
        self.buttonPane.add(Box.createHorizontalGlue());
        #
        # button_task = JButton("Delay", actionPerformed = self.delay_action)
        # self.buttonPane.add(button_task)
        # self.buttonPane.add(Box.createHorizontalGlue());

        button_repetitions = JButton("Delete All Rows", actionPerformed = self.delete_all_action)
        self.buttonPane.add(button_repetitions)
        self.buttonPane.add(Box.createHorizontalGlue());

        # button_savetofile = JButton("Save To File", actionPerformed = self.savetofile_action)
        # self.buttonPane.add(button_savetofile)
        # self.buttonPane.add(Box.createHorizontalGlue());
        #
        # button_loadfromfile = JButton("Load From File", actionPerformed = self.loadfromfile_action)
        # self.buttonPane.add(button_loadfromfile)
        # self.buttonPane.add(Box.createHorizontalGlue());

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

    def initialise_model(self, class_SchedulerPanel):

        self.model = None
        self.model = MyTableModel6()
        self.table = JTable(self.model)
        self.model.addTableModelListener(MyModelListener6(self, class_SchedulerPanel));
        # self.class_SchedulerPanel = class_SchedulerPanel


        pass
    def self_table(self):

        # self.table.setPreferredScrollableViewportSize(Dimension(500, 300));
        #table.setFillsViewpFgetvalueatortHeight(True)
        #self.table.getModel().addtableModelListener(self)
        self.table.setFillsViewportHeight(True);
        self.table.setRowHeight(30);
        #table.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
        # self.resizeColumnWidth(table)
        columnModel = self.table.getColumnModel();

        [route_col, delete_col, edit_col] = [0, 1, 2]
        columnModel.getColumn(route_col).setPreferredWidth(300);
        # columnModel.getColumn(repeat_col).setPreferredWidth(210);
        # columnModel.getColumn(dont_schedule_col).setPreferredWidth(150);

        columnModel.getColumn(delete_col).setPreferredWidth(50);
        columnModel.getColumn(edit_col).setPreferredWidth(50);

        # first column is the trains
        # self.trainColumn = self.table.getColumnModel().getColumn(time_col);
        # self.combobox0 = JComboBox()

        # for train in self.class_SchedulerPanel.get_list_of_engines_to_move():
        #     self.combobox0.addItem(train)

        # self.trainColumn.setCellEditor(DefaultCellEditor(self.combobox0));
        # renderer0 = ComboBoxCellRenderer6()
        # self.trainColumn.setCellRenderer(renderer0);

        # second column is the routes

        self.routesColumn = self.table.getColumnModel().getColumn(route_col);
        # self.combobox1 = JComboBox()

        # F
        # for route in routes:
        #     self.combobox1.addItem(route)
        # self.routesColumn.setCellEditor(DefaultCellEditor(self.combobox1));
        # renderer1 = ComboBoxCellRenderer6()
        # self.routesColumn.setCellRenderer(renderer1);

        # # first column is the trains
        # self.taskColumn = self.table.getColumnModel().getColumn(repeat_col);
        # self.combobox3 = JComboBox()
        #
        # tasks = ["Once", "Repeat every 20 mins","Repeat every 30 mins", "Repeat every Hour", "Repeat every 2 Hours"]
        # for task in tasks:
        #     self.combobox3.addItem(task)

        # self.taskColumn.setCellEditor(DefaultCellEditor(self.combobox3));
        # renderer3 = ComboBoxCellRenderer6()
        # self.taskColumn.setCellRenderer(renderer3);

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

    def get_route_list(self):
        RouteManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.routes.RouteManager)
        route_list = RouteManager.getRoutesByNameList()
        my_list = [[route.getName()] for route in route_list]

        return my_list

    def get_scheduled_route_list(self):
        TrainManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.trains.TrainManager)
        train_list = TrainManager.getTrainsByTimeList()
        my_list = [[train.getRoute().getName()] for train in train_list]

        return my_list

    def populate_action(self, event):
        # print "populating"
        items_to_put_in_dropdown = self.get_route_list()
        # print "items_to_put_in_dropdown", items_to_put_in_dropdown
        self.model.populate(items_to_put_in_dropdown)
        # print "populated"
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
            time_name = str(self.model.data[row][train])
            route_name = str(self.model.data[row][route])
            task_name = str(self.model.data[row][task])
            delay_name = str(self.model.data[row][delay])
            repetitions_name = str(self.model.data[row][repetitions])
            row_list = [time_name, route_name, task_name, delay_name, repetitions_name]
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
                [train_val, route_val, task_val, train_name_val, repetitions_val] = row
                self.model.add_row()
                self.model.data[i][train] = train_val.replace('"','')
                self.model.data[i][route] = route_val.replace('"','')
                self.model.data[i][task] = task_val.replace('"','')
                self.model.data[i][delay] = train_name_val.replace('"','')
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

            [route_col, edit_col, delete_col] = [0, 1, 2]

            # check the trains are valid

            trains_to_put_in_dropdown = [t for t in self.class_SchedulerPanel.get_list_of_engines_to_move()]
            for row in reversed(range(len(self.model.data))):
                if self.model.data[row][time_col] not in trains_to_put_in_dropdown:
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
        [route_col, edit_col, delete_col] = [0, 1, 2]
        for row in reversed(range(len(self.model.data))):
            old_delay = int(self.model.data[0][train_name_col])
            if old_delay == None: old_delay = 0
            new_delay = self.new_delay(old_delay)
            self.model.data[row][train_name_col] = new_delay
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

    def scheduled_routes_action(self, event):
        # print "self.toggle 2", self.toggle
        if self.toggle == True:
            self.toggle = False
        else:
            self.toggle = True

        # print "self.toggle 1", self.toggle

        for row in reversed(range(len(self.model.data))):
            self.model.data.pop(row)
        #     self.completeTablePanel()

        if self.toggle == True:
            items_to_put_in_dropdown = self.get_route_list()
            # print "items_to_put_in_dropdown 1", items_to_put_in_dropdown, self.toggle
            self.frame.setTitle("All Routes")
        else:
            items_to_put_in_dropdown = self.get_scheduled_route_list()
            # print "items_to_put_in_dropdown 2", items_to_put_in_dropdown, self.toggle
            self.frame.setTitle("Scheduled Routes")

        self.model.populate(items_to_put_in_dropdown)
        self.frame.setSize(self.frame.getPreferredSize().width, self.frame.getPreferredSize().height);
        self.frame.pack();
        self.frame.repaint()




    def delete_all_action(self, event):
        [route_col, edit_col, delete_col] = [0, 1, 2]
        msg = "Sure Want to Delete ALL Routes?"
        title = "Deleting All Routes"
        opt1 = "Yes DELETE All"
        opt2 = "cancel"
        reply = OptionDialog().customQuestionMessage2str(msg, title, opt1, opt2)
        if reply == opt2:
            return
        for row in reversed(range(len(self.model.data))):
            self.model.data.pop(row)
            self.completeTablePanel()
        msg = "Utterly Certain Last Chance"
        reply = OptionDialog().customQuestionMessage2str(msg, title, opt1, opt2)
        if reply == opt2:
            items_to_put_in_dropdown = CreateAndShowGUI6(None).get_route_list()
            self.model.populate(items_to_put_in_dropdown)
            return
        # delete all rows in operations routes
        RouteManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.routes.RouteManager)
        for route in RouteManager.getRoutesByNameList():
            route.deregister(route)
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
        [route_col, edit_col, delete_col] = [0, 1, 2]
        for row in reversed(range(len(self.model.data))):
            old_val = str(self.model.data[0][repeat_col])
            if old_val == None: old_val = 0
            new_val = self.new_task(old_val)
            self.model.data[row][repeat_col] = new_val
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


    def save_action(self, event):
        self.save()

    def save(self):
        [route_col, edit_col, delete_col] = [0, 1, 2]
        # print "save_action"
        self.clear_everything()
        # print "apply action"
        for row in reversed(range(len(self.model.data))):
            # time_name = str(self.model.data[row][time_col])
            route_name = str(self.model.data[row][route_col])
            # train_name = str(self.model.data[row][train_name_col])
            # repeat_name = str(self.model.data[row][repeat_col])
            edit_name = str(self.model.data[row][edit_col])
            # if time_name != "" and route_name != "" and train_name_val != "":
            if route_name != "":
                self.save_schedule(route_name, edit_name)
                pass
            else:
                msg = "Cannot save row: " + str(row) + " train name, route or delay is not set"
                OptionDialog().displayMessage(msg,"")
        self.completeTablePanel()
        if self.model.getRowCount() == 0:
            self.frame.dispatchEvent(WindowEvent(self.frame, WindowEvent.WINDOW_CLOSING))

    def save_schedule(self, route_name, edit_name):
        RouteManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.routes.RouteManager)
        route = RouteManager.newTrain(route_name)
        #
        # # RouteManager = jmri.InstanceManager.getDefault(jmri.jmrit.operations.routes.RouteManager)
        # # route = RouteManager.getRouteByName(route_name)
        # route.setRoute(route)
        #
        # # [hour, minute] = time_name.split(":")
        # # train.setDepartureTime(hour, minute)
        # #
        # # self.set_repeat(train, repeat_name)
        # # self.set_skip(train, dont_schedule_name)
        #
        route.setName(train_name)
        pass

    def set_repeat(self, train, repeat):
        # print "in set_repeat"
        comment = train.getComment()    #Null
        # if comment == None: comment = ""
        # repeat_current = MyTableModel6().find_between(comment, "[repeat-", "-repeat]")   # empty string
        # print "repeat_currenr", repeat_current, "repeat", repeat
        # if repeat_current != repeat:
        #     # self.delete_between(comment, "[repeat-", "-repeat]")
        comment = ""
        # print "comment3a", comment
        comment = self.insert_between(comment, "[repeat-", "-repeat]", repeat)
        # print "comment3", comment
        train.setComment(comment)

    def set_skip(self, train, dont_schedule_name):
        comment = train.getComment()
        if comment == None: comment = ""
        # print "retrieved comment", comment
        # print "dont_schedule_name", dont_schedule_name
        # print "type", type(dont_schedule_name)
        if dont_schedule_name == "True":
            # print "A"
            comment = "skip " + comment
            # print "comment1", comment
            train.setComment(comment)
    def delete_between(self, string, delim1, delim2):
        first, _, rest = string.partition(delim1)
        _, _, rest = rest.partition(delim2)
        cleaned_text = ' '.join([first.strip(), rest.strip()])
        return cleaned_text
    
    def insert_between(self, string, delim1, delim2, value):
        first, _, rest = string.partition(delim1)
        _, _, rest = rest.partition(delim2)
        # print "string", string, "first.strip()", first.strip(), "rest.strip()", rest.strip()
        new_val = delim1 + str(value) + delim2
        modified_text = new_val.join([first.strip(), rest.strip()])
        # print "modified_text",modified_text
        return modified_text

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

class MyModelListener6(TableModelListener):

    def __init__(self, class_CreateAndShowGUI6, class_SchedulerPanel):
        self.class_CreateAndShowGUI6 = class_CreateAndShowGUI6
        self.class_SchedulerPanel = class_SchedulerPanel
        self.cancel = False
        self.logLevel = 0
        self.i = 0
    def tableChanged(self, e) :
        global CreateAndShowGUI5_glb
        # print "INDES", self.i
        # self.i +=1
        # # if self.i % 2 == 0: return
        # global trains_allocated
        row = e.getFirstRow()
        column = e.getColumn()
        # print "table changed", column
        self.model = e.getSource()
        # columnName = self.model.getColumnName(column)
        #
        # class_CreateAndShowGUI6 = self.class_CreateAndShowGUI6
        # class_SchedulerPanel = self.class_SchedulerPanel
        # tablemodel = class_CreateAndShowGUI6.model
        [route_col, edit_col, delete_col] = [0, 1, 2]
        # print "a"
        if column == edit_col:     #trains
            if self.model.getValueAt(row, edit_col) == True:
                # print "$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$starting edit"
                route_data = str(self.model.getValueAt(row, route_col))
                scheduled_start = "00:00"
                if "CreateAndShowGUI5_glb" in globals():
                    # print "*******************************88 IN GLOBALS ****************************************"
                    if CreateAndShowGUI5_glb != None:
                        CreateAndShowGUI5_glb.frame.dispose()
                CreateAndShowGUI5_glb = CreateAndShowGUI5(self, route_data, scheduled_start)
                self.model.setValueAt(False, row, edit_col)
        elif column == delete_col:
            # class_CreateAndShowGUI6.run_route(row, model, class_CreateAndShowGUI6, class_SchedulerPanel)
            # delete the Operations route
            route_name = self.model.getValueAt(row, route_col)
            RouteManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.routes.RouteManager)
            route = RouteManager.getRouteByName(route_name)
            # print "route name", route.getName()
            #delete the route row
            RouteManager.deregister(route)
            self.delete_row(row)
            self.class_CreateAndShowGUI6.completeTablePanel()
        #
        # class_CreateAndShowGUI5.save()    # save everything when the table is chabged
        
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

class MyTableModel6 (DefaultTableModel):

    columnNames = ["Route", "Edit", "Delete Row"]

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
        # TrainManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.trains.TrainManager)
        # train_list = TrainManager.getTrainsByTimeList()
        [route_col, edit_col, delete_col] = [0, 1, 2]
        # for row in reversed(range(len(self.data))):

        # indices = [int(train.getName().split("Train",1)[1]) for train in train_list if train.getName().startswith("Train")]
        indices = [int(self.data[row][train_name_col].split("Train",1)[1]) for row in reversed(range(len(self.data)))
                   if self.data[row][train_name_col].startswith("Train")]
        if indices == []:
            index = 1
        else:
            index = max(indices) + 1
        train_name = "Train" + str(index)
        self.data.append(["00:00", "", "Once", False, train_name, False])
        # print self.data
        # print "added"

    def populate(self, items_to_put_in_dropdown):
        # print "in populate"
        for row in reversed(range(len(self.data))):
            self.data.pop(row)
        # print "cleared everything"
        # self.data = []
        # append all trains to put in dropdown
        [time_col, delete_col, edit_col] = [0, 1, 2]
        for [route] in items_to_put_in_dropdown:
            # print "train", train
            # if "skip" in comment:
            #     skip = True
            # else:
            #     skip = False
            # print "skip", skip
            # train_present = False
            # repeat = self.find_between(comment, "[repeat-", "-repeat]")
            # if repeat == "": repeat = "Once"
            # print "repeat" , repeat
            # # for row in reversed(range(len(self.data))):
            # #     if self.data[row][route_col] == route:
            # #         train_present = True
            # # if train_present == False:
            self.data.append([route, False, False])
        # print "populated"
        # delete rows with no trains
        # for row in reversed(range(len(self.data))):
        #     if self.data[row][time_col] == None or self.data[row][dont_schedule_col] == "":
        #         self.data.pop(row)

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
        # print "row1", row, "col", col, "value", value
        if col == 0:
            # print "row2", row, "col", col, "value", value
            if not self.isValidTimeFormat(value):
                return
        # print "row", row, "col", col, "value", value
        self.data[row][col] = value
        self.fireTableCellUpdated(row, col)

        # if (isValidValue(aValue)) {
        # data[rowIndex][columnIndex] = aValue;
        # fireTableCellUpdated(rowIndex, columnIndex); // Notify the table
        # }

    def isValidTimeFormat(self, input_string):
        import re
        pattern = r"^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$"  # Matches HH.MM format
        my_match = re.match(pattern, input_string) is not None
        # print "m", re.match(pattern, input_string) is not None
        # print "my_match", re.match(pattern, input_string)

        return my_match










