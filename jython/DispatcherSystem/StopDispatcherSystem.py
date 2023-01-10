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

        button_add = JButton("Add Row", actionPerformed = self.add_row_action)
        self.buttonPane.add(button_add);
        self.buttonPane.add(Box.createRigidArea(Dimension(10, 0)))

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

        # button_cancel = JButton("No. repetitions", actionPerformed = self.repetitions_action)
        # self.buttonPane.add(button_cancel)
        # self.buttonPane.add(Box.createHorizontalGlue());
        #
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

    def initialise_model(self, class_StopMaster):

        self.model = None
        self.model = MyTableModel3()
        self.table = JTable(self.model)
        self.model.addTableModelListener(MyModelListener3(self, class_StopMaster));
        self.class_StopMaster = class_StopMaster

    def self_table(self):

        #table.setPreferredScrollableViewportSize(Dimension(500, 70));
        #table.setFillsViewportHeight(True)
        #self.table.getModel().addtableModelListener(self)
        self.table.setFillsViewportHeight(True);
        self.table.setRowHeight(30);
        #table.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
        # self.resizeColumnWidth(table)
        columnModel = self.table.getColumnModel();

        [setup_train_col, del_setup_train_col,  active_train_col, transit_col, del_transit_col, route_col, del_route_col] = [0, 1, 2, 3, 4, 5, 6]
        columnModel.getColumn(setup_train_col).setPreferredWidth(200);
        columnModel.getColumn(active_train_col).setPreferredWidth(200);
        columnModel.getColumn(transit_col).setPreferredWidth(200);
        columnModel.getColumn(route_col).setPreferredWidth(200);

        # # first column is the trains
        # self.active_train_col = self.table.getColumnModel().getColumn(active_train_col);
        # self.combobox0 = JComboBox()
        #
        # for train in self.class_StopMaster.get_list_of_engines_to_move():
        #     self.combobox0.addItem(train)
        #
        # self.trainColumn.setCellEditor(DefaultCellEditor(self.combobox0));
        # renderer0 = ComboBoxCellRenderer1()
        # self.trainColumn.setCellRenderer(renderer0);
        #
        # # second column is the routes
        #
        # self.routesColumn = self.table.getColumnModel().getColumn(route_col);
        # self.combobox1 = JComboBox()
        #
        # RouteManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.routes.RouteManager)
        # routes = RouteManager.getRoutesByNameList()
        # for route in routes:
        #     self.combobox1.addItem(route)
        # self.routesColumn.setCellEditor(DefaultCellEditor(self.combobox1));
        # renderer1 = ComboBoxCellRenderer1()
        # self.routesColumn.setCellRenderer(renderer1);
        #
        # # first column is the trains
        # self.taskColumn = self.table.getColumnModel().getColumn(task_col);
        # self.combobox3 = JComboBox()
        #
        # tasks = ["stop at end of route", "return to start position","return to start and repeat"]
        # for task in tasks:
        #     self.combobox3.addItem(task)
        #
        # self.taskColumn.setCellEditor(DefaultCellEditor(self.combobox3));
        # renderer3 = ComboBoxCellRenderer1()
        # self.taskColumn.setCellRenderer(renderer3);

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
        pass

    # def tidy_action(self,e):
    #     self.model.remove_not_set_row()
    #     self.completeTablePanel()

    def del_trains_action(self, e):
        global trains_allocated
        for train_name_to_remove in trains_allocated:
            trains_allocated.remove(train_name_to_remove)
        self.completeTablePanel()

    def delete_transits(self):
        # need to avoid concurrency issues when deleting more that one transit
        # use java.util.concurrent.CopyOnWriteArrayList  so can iterate through the transits while deleting
        DF = jmri.InstanceManager.getDefault(jmri.jmrit.dispatcher.DispatcherFrame)
        #DF.setState(DF.ICONIFIED);

        activeTrainList = java.util.concurrent.CopyOnWriteArrayList()
        for activeTrain in DF.getActiveTrainsList():
            activeTrainList.add(activeTrain)

        for activeTrain in activeTrainList:
            # print "i", i
            # activeTrain = activeTrainsList.get(i)
            if self.logLevel == 0: print ("active train", activeTrain)
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
    #         train_name = str(self.model.data[row][train])
    #         route_name = str(self.model.data[row][route])
    #         task_name = str(self.model.data[row][task])
    #         delay_name = str(self.model.data[row][delay])
    #         repetitions_name = str(self.model.data[row][repetitions])
    #         row_list = [train_name, route_name, task_name, delay_name, repetitions_name]
    #         my_list.append(row_list)
    #     self.write_list(my_list,file)
    #

    # def loadfromfile_action(self, event):
    #     # load the file
    #     dir = self.directory()
    #     j = JFileChooser(dir);
    #     j.setAcceptAllFileFilterUsed(False)
    #     filter = FileNameExtensionFilter("text files txt", ["txt"])
    #     j.setDialogTitle("Select a .txt file");
    #     j.addChoosableFileFilter(filter);
    #     ret = j.showOpenDialog(None);
    #     if (ret == JFileChooser.APPROVE_OPTION) :
    #         file = j.getSelectedFile()
    #         if self.logLevel > 0: print "about to read list", file
    #         my_list = self.read_list(file)
    #         if self.logLevel > 0: print "my_list", my_list
    #         for row in reversed(range(len(self.model.data))):
    #             self.model.data.pop(row)
    #         i = 0
    #         [train, route, task, delay, repetitions] = [0, 1, 3, 4, 5]
    #         for row in my_list:
    #             [train_val, route_val, task_val, delay_val, repetitions_val] = row
    #             self.model.add_row()
    #             self.model.data[i][train] = train_val.replace('"','')
    #             self.model.data[i][route] = route_val.replace('"','')
    #             self.model.data[i][task] = task_val.replace('"','')
    #             self.model.data[i][delay] = delay_val.replace('"','')
    #             self.model.data[i][repetitions] = repetitions_val.replace('"','')
    #             i += 1
    #         self.completeTablePanel()
    #
    #         msg = "Deleting invalid rows"
    #         result = OptionDialog().displayMessage(msg)
    #         if result == JOptionPane.NO_OPTION:
    #             return
    #
    #         # check the loaded contents
    #         # 1) check that the trains are valid
    #         # 2) ckeck that the blocks are occupied by valid trains
    #         # if either of the above are not valic we blank the entries
    #         # 3) Tidy
    #
    #         [setup_train_col, del_setup_train_col,  active_train_col, transit_col, del_transit_col, route_col, del_route_col] = [0, 1, 2, 3, 4, 5, 6]
    #
    #         # check the trains are valid
    #
    #         trains_to_put_in_dropdown = [t for t in self.class_StopMaster.get_list_of_engines_to_move()]
    #         for row in reversed(range(len(self.model.data))):
    #             if self.model.data[row][train_col] not in trains_to_put_in_dropdown:
    #                 self.model.data.pop(row)
    #
    #         RouteManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.routes.RouteManager)
    #         routes = [str(route) for route in RouteManager.getRoutesByNameList()]
    #         for row in reversed(range(len(self.model.data))):
    #             if self.model.data[row][route_col] not in routes:
    #                 self.model.data.pop(row)
    #         self.completeTablePanel()

    def cancel_action(self, event):
        self.frame.dispatchEvent(WindowEvent(self.frame, WindowEvent.WINDOW_CLOSING));

    # def repetitions_action(self, event):
    #     [setup_train_col, del_setup_train_col,  active_train_col, transit_col, del_transit_col, route_col, del_route_col] = [0, 1, 2, 3, 4, 5, 6]
    #     for row in reversed(range(len(self.model.data))):
    #         old_val = int(self.model.data[0][repetition_col])
    #         if old_val == None: old_val = 0
    #         new_val = self.new_val(old_val)
    #         self.model.data[row][repetition_col] = new_val
    #     self.completeTablePanel()
    #
    # def new_val(self, old_val):
    #     if old_val < 3:
    #         new_val = 3
    #     elif old_val < 10:
    #         new_val = 10
    #     elif old_val < 30:
    #         new_val = 30
    #     elif old_val < 1000:
    #         new_val = 1000
    #     else:
    #         new_val = 0
    #     return new_val

    # def task_action(self, event):
    #     [setup_train_col, del_setup_train_col,  active_train_col, transit_col, del_transit_col, route_col, del_route_col] = [0, 1, 2, 3, 4, 5, 6]
    #     for row in reversed(range(len(self.model.data))):
    #         old_val = str(self.model.data[0][task_col])
    #         if old_val == None: old_val = 0
    #         new_val = self.new_task(old_val)
    #         self.model.data[row][task_col] = new_val
    #     self.completeTablePanel()
    #
    # def new_task(self, old_val):
    #     tasks = ["stop at end of route", "return to start position","return to start and repeat"]
    #     if old_val == "stop at end of route":
    #         new_val = "return to start position"
    #     elif old_val == "return to start position":
    #         new_val = "return to start and repeat"
    #     else:
    #         return "stop at end of route"
    #     return new_val


    # def apply_del_trains(self, event):
    #     [setup_train_col, del_setup_train_col,  active_train_col, transit_col, del_transit_col, route_col, del_route_col] = [0, 1, 2, 3, 4, 5, 6]
    #     # print "apply action"
    #     for row in reversed(range(len(self.model.data))):
    #         train_name = str(self.model.data[row][train_col])
    #         route_name = str(self.model.data[row][route_col])
    #         delay_val = str(self.model.data[row][delay_col])
    #         if train_name != "" and route_name != "" and delay_val != "":
    #             self.run_route(row, self.model, self, self.class_StopMaster)
    #         else:
    #             msg = "not running route, train, route or delay is not set"
    #             self.od.displayMessage(msg,"")
    #     self.completeTablePanel()
    #     if self.model.getRowCount() == 0:
    #         self.frame.dispatchEvent(WindowEvent(self.frame, WindowEvent.WINDOW_CLOSING))
    #

    # def run_route(self, row, model, class_createandshowGUI2, class_StopMaster):
    #     [setup_train_col, del_setup_train_col,  active_train_col, transit_col, del_transit_col, route_col, del_route_col] = [0, 1, 2, 3, 4, 5, 6]
    #     route_name = str(model.getValueAt(row, route_col))
    #     if route_name == None:
    #         msg = "not running route is not set"
    #         self.od.displayMessage(msg,"")
    #         return
    #     RouteManager=jmri.InstanceManager.getDefault(jmri.jmrit.operations.routes.RouteManager)
    #     route = RouteManager.getRouteByName(route_name)
    #
    #     train_name = str(model.getValueAt(row, train_col))
    #     if train_name == None or train_name == "":
    #         msg = "not running route, train is not set"
    #         self.od.displayMessage(msg,"")
    #         return
    #     station_from = class_StopMaster.get_position_of_train(train_name)
    #
    #     option = str(model.getValueAt(row, task_col))
    #
    #     repeat = False
    #     dont_run_route = False
    #     no_repetitions = 0
    #     if option == "stop at end of route":
    #         station_to = None
    #         repeat = False
    #     elif option == "return to start position":
    #         station_to = station_from
    #         repeat = False
    #     elif option == "return to start and repeat":
    #         station_to = station_from
    #         repeat = True
    #     else:
    #         dont_run_route = True
    #
    #     if repeat:
    #         no_repetitions = str(model.getValueAt(row, repetition_col))
    #     else:
    #         no_repetitions = 0
    #
    #     # delay by delay_val before starting route
    #     delay_val = int(model.getValueAt(row, delay_col)) *1000
    #     self.class_StopMaster.waitMsec(delay_val)
    #
    #     if dont_run_route == False:
    #         if self.logLevel > 0: print "station_from",    station_from, "station_to",station_to, \
    #                                     "repeat",repeat, "delay", delay
    #         run_train = RunRoute(route, g.g_express, station_from, station_to, no_repetitions, train_name)
    #         run_train.setName("running_route_" + route_name )
    #         instanceList.append(run_train)
    #         run_train.start()
    #         model.data.pop(row)
    #         class_createandshowGUI2.completeTablePanel()
    #
    # def directory(self):
    #     path = jmri.util.FileUtil.getUserFilesPath() + "dispatcher" + java.io.File.separator + "routes"
    #     if not os.path.exists(path):
    #         os.makedirs(path)
    #     return path + java.io.File.separator
    #
    # def write_list(self, a_list, file):
    #     # store list in binary file so 'wb' mode
    #     #file = self.directory() + "blockDirections.txt"
    #     if self.logLevel > 0: print "block_info" , a_list
    #     if self.logLevel > 0: print "file" , file
    #     file = str(file)
    #     with open(file, 'wb') as fp:
    #         pass
    #     if self.logLevel > 0: print "V"
    #     with open(file, 'wb') as fp:
    #         if self.logLevel > 0: print "B"
    #         for items in a_list:
    #             if self.logLevel > 0: print "C", items
    #             i = 0
    #             for item in items:
    #                 if self.logLevel > 0: print "item", item
    #                 fp.write('"%s"' %item)
    #                 if i != 4: fp.write(",")
    #                 i+=1
    #             fp.write('\n')
    #             #fp.write('\n'.join(item))
    #             #fp.write(items)
    #
    # # Read list to memory
    # def read_list(self, file):
    #     file = str(file)
    #     if self.logLevel > 0: print "read list", file
    #     # for reading also binary mode is important
    #     #file = self.directory() + "blockDirections.txt"
    #     n_list = []
    #     # try:
    #     with open(file, 'rb') as fp:
    #         for line in fp:
    #             if self.logLevel > 0: print "line" , line
    #             x = line[:-1]
    #             if self.logLevel > 0: print x
    #             y = x.split(",")
    #             #y = [item.replace('"','') for item in y]
    #             if self.logLevel > 0: print "y" , y
    #             n_list.append(y)
    #
    #     return n_list
    #     # except:
    #     #     return ["",""]

class MyModelListener3(TableModelListener):

    def __init__(self, class_createandshowGUI3, class_StopMaster):
        self.class_createandshowGUI3 = class_createandshowGUI3
        self.class_StopMaster = class_StopMaster
        self.cancel = False
        self.logLevel = 1
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
            print "trains_allocated", trains_allocated
            print "train_name", train_name
            if train_name != "":
                trains_allocated.remove(train_name)
            model.fireTableDataChanged()
        elif column == del_transit_col:

            transit = str(model.getValueAt(row, transit_col))
            train_name = [active_train.getTrainName() for active_train in self.java_active_trains_list \
                       if active_train.getTransit().getUserName() == transit]
            if len(train_name) > 0:
                self.delete_transit(train_name[0])
                model.fireTableDataChanged()


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
                model.fireTableDataChanged()
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
            #print "train in trains_alloceted", train, ": trains_allocated", trains_allocated
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
        print ("active_train", active_train)
        if len(active_train) > 0:
            DF.terminateActiveTrain(active_train[0])

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

    def remove_not_set_row(self):
        for row in reversed(range(len(self.data))):
            if self.data[row][1] == "":
                self.data.pop(row)

    def add_row(self):
        # print "addidn row"
        # if row < len(self.data):
        # print "add"
        self.data.append(["", False, "", "", False, "", False])
        # print self.data
        # print "added"

    def populate(self):
        global trains_allocated
        global trains_allocated
        global trains_dispatched
        DF = jmri.InstanceManager.getDefault(jmri.jmrit.dispatcher.DispatcherFrame)
        java_active_trains_list = DF.getActiveTrainsList()
        java_active_trains_Arraylist= java.util.ArrayList(java_active_trains_list)
        print ("populate")
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
            if self.logLevel == 0: print "active_train", active_train, "len(active_train)", len(active_train)
            if len(active_train) > 0 :
                active_train = active_train[0]
                active_train_name = active_train.getTrainName()
                transit = [active_train.getTransit() for active_train in java_active_trains_list \
                           if active_train.getTrainName() == active_train_name]
                if self.logLevel == 0: print "active_train_name", active_train_name
                if self.logLevel == 0: print "transit"  , transit
                transit_name = transit[0].getUserName()
            else:
                active_train = ""
                active_train_name = ""
                transit_name = ""
            if self.logLevel == 0: print("train", active_train)
            if self.logLevel == 0: print("transit_name", transit_name)
            route_name = self.get_route(active_train_name)
            if self.logLevel == 0: print ("route_name", route_name)
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
            if self.logLevel == 0: print("train", active_train_name)
            route_name = self.get_route(active_train_name)
            if self.logLevel == 0: print ("route_name", route_name)
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
                if self.logLevel == 0: print "thread name", thread_name, "route_name", route_name, "thread_train_name", thread_train_name, "train_name", train_name
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