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
        global CreateAndShowGUI4_frame
        self.logLevel = 0
        self.class_ResetButtonMaster = class_ResetButtonMaster
        #Create and set up the window.

        self.initialise_model(class_ResetButtonMaster)
        CreateAndShowGUI4_frame = JFrame("Scheduled Trains")
        self.frame = CreateAndShowGUI4_frame
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

        button_task = JButton("Repeat", actionPerformed = self.task_action)
        self.buttonPane.add(button_task)
        self.buttonPane.add(Box.createHorizontalGlue());

        # button_task = JButton("Delay", actionPerformed = self.delay_action)
        # self.buttonPane.add(button_task)
        # self.buttonPane.add(Box.createHorizontalGlue());

        button_delete = JButton("Delete All Rows", actionPerformed = self.delete_all_action)
        self.buttonPane.add(button_delete)
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
        # row1_2_button = JButton("Save", actionPerformed = self.save_action)

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

        self.table.setPreferredScrollableViewportSize(Dimension(700, 300));
        #table.setFillsViewportHeight(True)
        #self.table.getModel().addtableModelListener(self)
        self.table.setFillsViewportHeight(True);
        self.table.setRowHeight(30);
        #table.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
        # self.resizeColumnWidth(table)
        columnModel = self.table.getColumnModel();

        [time_col, route_col, repeat_col, dont_schedule_col, train_name_col, edit_col, delete_col] = [0, 1, 2, 3, 4, 5, 6]
        columnModel.getColumn(route_col).setPreferredWidth(300);
        columnModel.getColumn(repeat_col).setPreferredWidth(210);
        columnModel.getColumn(dont_schedule_col).setPreferredWidth(150);
        columnModel.getColumn(train_name_col).setPreferredWidth(130);
        columnModel.getColumn(edit_col).setPreferredWidth(100);
        columnModel.getColumn(delete_col).setPreferredWidth(100);

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
        self.taskColumn = self.table.getColumnModel().getColumn(repeat_col);
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
        # data = self.model.getValueAt(0, 0)
        count = self.model.getRowCount()
        colcount = self.model.getColumnCount()
        self.model.add_row()
        # self.save()
        self.completeTablePanel()

    def get_route_list(self):
        TrainManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.trains.TrainManager)
        train_list = TrainManager.getTrainsByTimeList()
        my_list = [[train.getName(), train.getDepartureTime(), train.getComment(), train.getRoute()] for train in train_list]
        # print "my_list", my_list
        return my_list

    def populate_action(self, event):
        # print "populating"
        items_to_put_in_dropdown = self.get_route_list()
        # print "items_to_put_in_dropdown", items_to_put_in_dropdown
        self.model.populate(items_to_put_in_dropdown)
        # print "populated"
        self.completeTablePanel()

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
        [time_col, route_col, repeat_col, dont_schedule_col, train_name_col, edit_col, delete_col] = [0, 1, 2, 3, 4, 5, 6]
        for row in range(len(self.model.data)):
            time_name = str(self.model.data[row][time_col])
            route_name = str(self.model.data[row][route_col])
            task_name = str(self.model.data[row][repeat_col])
            dont_schedule_name = str(self.model.data[row][dont_schedule_col])
            train_name = str(self.model.data[row][train_name_col])
            edit_name = str(self.model.data[row][edit_col])
            delete_name = str(self.model.data[row][delete_col])
            row_list = [time_name, route_name, task_name, dont_schedule_name, train_name, edit_name, delete_name]
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
            [time_col, route_col, repeat_col, dont_schedule_col, train_name_col, edit_col, delete_col] = [0, 1, 2, 3, 4, 5, 6]
            for row in my_list:
                [time_val, route_val, repeat_val, dont_schedule_val, train_name_val, edit_val, delete_val] = row
                # print "reading row", row
                self.model.add_row()
                self.model.data[i][time_col] = time_val.replace('"','')
                self.model.data[i][route_col] = route_val.replace('"','')
                self.model.data[i][repeat_col] = repeat_val.replace('"','')
                # print "dont_schedule_val", dont_schedule_val, "(dont_schedule_val == 'True'') ", (dont_schedule_val == "True")
                self.model.data[i][dont_schedule_col] = (dont_schedule_val.replace('"','') == "True")    #convert string to boolean
                self.model.data[i][train_name_col] = train_name_val.replace('"','')
                # self.model.data[i][edit_col] = edit_val.replace('"','')
                # self.model.data[i][delete_col] = bool(delete_val.replace('"',''))
                i += 1
                # print "read row", row

            self.save()
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

            [time_col, route_col, repeat_col, dont_schedule_col, train_name_col, edit_col, delete_col] = [0, 1, 2, 3, 4, 5, 6]

            # check the trains are valid

            # trains_to_put_in_dropdown = [t for t in self.class_ResetButtonMaster.get_list_of_engines_to_move()]
            # for row in reversed(range(len(self.model.data))):
            #     if self.model.data[row][train_name_col] not in trains_to_put_in_dropdown:
            #         self.model.data.pop(row)
            #
            # RouteManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.routes.RouteManager)
            # routes = [str(route) for route in RouteManager.getRoutesByNameList()]
            # for row in reversed(range(len(self.model.data))):
            #     if self.model.data[row][route_col] not in routes:
            #         self.model.data.pop(row)

        self.completeTablePanel()
        self.save()

    def close_action(self, event):
        # self.completeTablePanel()
        # self.save()
        self.frame.dispatchEvent(WindowEvent(self.frame, WindowEvent.WINDOW_CLOSING));

    def delay_action(self, event):
        [time_col, route_col, repeat_col, dont_schedule_col, train_name_col, edit_col, delete_col] = [0, 1, 2, 3, 4, 5, 6]
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

    def delete_all_action(self, event):
        [time_col, route_col, repeat_col, dont_schedule_col, train_name_col, edit_col, delete_col] = [0, 1, 2, 3, 4, 5, 6]
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
        self.save()
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
        [time_col, route_col, repeat_col, dont_schedule_col, train_name_col, edit_col, delete_col] = [0, 1, 2, 3, 4, 5, 6]
        for row in reversed(range(len(self.model.data))):
            old_val = str(self.model.data[0][repeat_col])
            if old_val == None: old_val = 0
            new_val = self.new_task(old_val)
            self.model.data[row][repeat_col] = new_val
        self.save()
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


    # def save_action(self, event):
    #     self.save()
    #
    def save(self):
        [time_col, route_col, repeat_col, dont_schedule_col, train_name_col, edit_col, delete_col] = [0, 1, 2, 3, 4, 5, 6]
        # print "save_action"
        self.clear_everything()
        # print "apply action"
        for row in reversed(range(len(self.model.data))):
            # print "save row", row
            time_name = str(self.model.data[row][time_col])
            route_name = str(self.model.data[row][route_col])
            train_name = str(self.model.data[row][train_name_col])
            repeat_name = str(self.model.data[row][repeat_col])
            # print "repeat_name", repeat_name
            dont_schedule_name = str(self.model.data[row][dont_schedule_col])
            # if time_name != "" and route_name != "" and train_name_val != "":
            if train_name != "":
                # print "save schedule"
                self.save_schedule(row, time_name, route_name, repeat_name, dont_schedule_name, train_name)
                pass
            else:
                msg = "Cannot save row: " + str(row) + " train name, route or delay is not set"
                OptionDialog().displayMessage(msg,"")
        # self.completeTablePanel()
        if self.model.getRowCount() == 0:
            self.frame.dispatchEvent(WindowEvent(self.frame, WindowEvent.WINDOW_CLOSING))

    def save_schedule(self, row, time_name, route_name, repeat_name, dont_schedule_name, train_name):
        TrainManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.trains.TrainManager)
        train = TrainManager.newTrain(train_name)

        RouteManager = jmri.InstanceManager.getDefault(jmri.jmrit.operations.routes.RouteManager)
        route = RouteManager.getRouteByName(route_name)
        train.setRoute(route)

        [hour, minute] = time_name.split(":")
        train.setDepartureTime(hour, minute)
        # print "set departure time", hour, minute

        self.set_skip(train, dont_schedule_name)   # do this first
        self.set_repeat(train, repeat_name)

        train.setName(train_name)

    def set_repeat(self, train, repeat):
        # print "in set_repeat"
        comment = train.getComment()    #Null
        # if comment == None: comment = ""
        # repeat_current = MyTableModel4().find_between(comment, "[repeat-", "-repeat]")   # empty string
        # print "repeat_currenr", repeat_current, "repeat", repeat
        # if repeat_current != repeat:
        #     # self.delete_between(comment, "[repeat-", "-repeat]")
        # comment = ""
        # print "comment3a", comment
        comment = self.insert_between(comment, "[repeat-", "-repeat]", repeat)
        # print "comment3", comment
        train.setComment(comment)

    def set_skip(self, train, dont_schedule_name):

        if dont_schedule_name == "True":
            comment = "skip"
        else:
            comment = ""
        # print "dont_schedule_name", dont_schedule_name, "comment", comment

        # print "type", type(dont_schedule_name)
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
                    if i != 6: fp.write(",")
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
        self.i = 0
    def tableChanged(self, e) :
        # print "INDES", self.i
        self.i +=1
        # if self.i % 2 == 0: return
        global trains_allocated
        global CreateAndShowGUI5_glb
        row = e.getFirstRow()
        column = e.getColumn()
        # print "column", column
        self.model = e.getSource()
        columnName = self.model.getColumnName(column)

        class_CreateAndShowGUI4 = self.class_CreateAndShowGUI4
        class_ResetButtonMaster = self.class_ResetButtonMaster
        tablemodel = class_CreateAndShowGUI4.model
        [time_col, route_col, repeat_col, dont_schedule_col, train_name_col, edit_col, delete_col] = [0, 1, 2, 3, 4, 5, 6]
        if column == time_col:     #trains
            pass
        elif column == edit_col:       # sections
            if self.model.getValueAt(row, edit_col) == True:
                # print "starting edit"
                route_data = str(self.model.getValueAt(row, route_col))
                scheduled_start = self.model.getValueAt(row, time_col)
                if "CreateAndShowGUI5_glb" in globals():
                    if CreateAndShowGUI5_glb != None:
                        CreateAndShowGUI5_glb.frame.dispose()
                CreateAndShowGUI5_glb = CreateAndShowGUI5(self, route_data, scheduled_start)
                print "e"
                self.model.setValueAt(False, row, edit_col)
        elif column == delete_col:
            # print "delete col/row"
            title = ""
            msg = "delete row?"
            opt1 = "dont' delete row"
            opt2 = "delete row"
            result = OptionDialog().customQuestionMessage2str(msg, title, opt1, opt2)
            if result == opt2:
                self.delete_row(row, class_CreateAndShowGUI4)
        class_CreateAndShowGUI4.save()                      # save everything when the table is chabged
        # class_CreateAndShowGUI4.completeTablePanel()      # don't need to refresh hence commented out

    def save_route(self, class_CreateAndShowGUI4):
        class_CreateAndShowGUI4.save()

    def delete_row(self, row, class_CreateAndShowGUI4):
        self.model.data.pop(row)
        class_CreateAndShowGUI4.save()
        class_CreateAndShowGUI4.completeTablePanel()

    def show_time_picker(self):
        # Show a simple JOptionPane input dialog for time selection
        selected_time = JOptionPane.showInputDialog(None, "Select a time (HH:mm):")
        # if selected_time:
        #     print("Selected time:", selected_time)
        return selected_time


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

    columnNames = ["Time", "Route", "Repeat", "Don't Schedule", "Train Name", "Edit_Row", "Delete Row"]

    def __init__(self):
        # l1 = ["", "", False, "stop at end of route", 10, False, False]
        self.data = []

    def remove_not_set_row(self):
        b = False
        for row in reversed(range(len(self.data))):
            # print "row", row
            if self.data[row][1] == "":
                self.data.pop(row)

    def add_row(self):
        # TrainManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.trains.TrainManager)
        # train_list = TrainManager.getTrainsByTimeList()
        [time_col, route_col, repeat_col, dont_schedule_col, train_name_col, edit_col, delete_col] = [0, 1, 2, 3, 4, 5, 6]
        # for row in reversed(range(len(self.data))):

        # indices = [int(train.getName().split("Train",1)[1]) for train in train_list if train.getName().startswith("Train")]
        indices = [int(self.data[row][train_name_col].split("Train",1)[1]) for row in reversed(range(len(self.data)))
                   if self.data[row][train_name_col].startswith("Train")]
        if indices == []:
            index = 1
        else:
            index = max(indices) + 1
        train_name = "Train" + str(index)
        # print "adding row"
        self.data.append(["00:00", "", "Once", False, train_name, False, False])
        # print "added row"
        # print self.data
        # print "added"

    def populate(self, items_to_put_in_dropdown):
        # print "in populate"
        for row in reversed(range(len(self.data))):
            self.data.pop(row)
        # print "cleared everything"
        # self.data = []
        # append all trains to put in dropdown
        [time_col, route_col, repeat_col, dont_schedule_col, train_name_col, edit_col, delete_col] = [0, 1, 2, 3, 4, 5, 6]
        for [train, time, comment, route] in items_to_put_in_dropdown:
            # print "train", train
            if "skip" in comment:
                skip = True
            else:
                skip = False
            # print "skip", skip
            train_present = False
            repeat = self.find_between(comment, "[repeat-", "-repeat]")
            if repeat == "": repeat = "Once"
            # print "repeat" , repeat
            # for row in reversed(range(len(self.data))):
            #     if self.data[row][route_col] == route:
            #         train_present = True
            # if train_present == False:
            self.data.append([time, route, repeat, skip, train, False, False])
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
        except IndexError:
            return "Index Error H"

    def getColumnCount(self) :
        return len(self.columnNames)


    def getRowCount(self) :
        return len(self.data)


    def getColumnName(self, col) :
        return self.columnNames[col]


    def getValueAt(self, row, col) :
        return self.data[row][col]
        # return 'fred'

    def getColumnClass(self, col) :

        return java.lang.Boolean.getClass(self.getValueAt(0,col))
        # return java.lang.Boolean.getClass("fred")


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










