import jmri
from javax.swing import JOptionPane, JFrame, JLabel, JButton, JTextField, JFileChooser, JMenu, JMenuItem, JMenuBar,JComboBox,JDialog,JList
from javax.swing import JOptionPane, WindowConstants, JScrollPane

#
# class OptionDialog
# Some Swing dialogs
#

class OptionDialog( jmri.jmrit.automat.AbstractAutomaton ) :
    CLOSED_OPTION = False
    logLevel = 0

    def List(self, title, list_items, preferred_size = "default"):
        my_list = JList(list_items)
        my_list.setSelectedIndex(0)
        scrollPane = JScrollPane(my_list)
        if preferred_size != "default":
            scrollPane.setPreferredSize(preferred_size)     # preferred_size should be set to Dimension(300, 500) say
        else:
            no_rows_to_display = min(40, len(list_items))
            my_list.setVisibleRowCount(no_rows_to_display)
            dim = my_list.getPreferredScrollableViewportSize()
            w = int(dim.getWidth())
            h = int(dim.getHeight()) + 10  # to leave a bit of space at bottom. Height of row = approx 20
            scrollPane.setPreferredSize(Dimension(w,h))
        i = []
        self.CLOSED_OPTION = False
        options = ["OK"]
        while len(i) == 0:
            s = JOptionPane().showOptionDialog(None,
                                               scrollPane,
                                               title,
                                               JOptionPane.YES_NO_OPTION,
                                               JOptionPane.PLAIN_MESSAGE,
                                               None,
                                               options,
                                               options[0])
            if s == JOptionPane.CLOSED_OPTION:
                self.CLOSED_OPTION = True
                if self.logLevel > 1 : print "closed Option"
                return
            i = my_list.getSelectedIndices()
        index = i[0]
        return list_items[index]


    #list and option buttons
    def ListOptions(self, list_items, title, options, preferred_size = "default"):
        my_list = JList(list_items)
        if list_items != []:
            my_list.setSelectedIndex(0)
        scrollPane = JScrollPane(my_list)
        if preferred_size != "default":
            scrollPane.setPreferredSize(preferred_size)   # preferred_size should be set to Dimension(300, 500) say
        else:
            no_rows_to_display = min(40, len(list_items))
            my_list.setVisibleRowCount(no_rows_to_display)
            dim = my_list.getPreferredScrollableViewportSize()
            w = int(dim.getWidth()) + 20
            h = int(dim.getHeight() + 20) # to leave a bit of space at bottom. Height of row = approx 20
            scrollPane.setPreferredSize(Dimension(w,h))
        self.CLOSED_OPTION = False
        s = JOptionPane.showOptionDialog(None,
                                         scrollPane,
                                         title,
                                         JOptionPane.YES_NO_OPTION,
                                         JOptionPane.PLAIN_MESSAGE,
                                         None,
                                         options,
                                         options[0])
        if s == JOptionPane.CLOSED_OPTION:
            self.CLOSED_OPTION = True
            return [None,None]
        if list_items == []:
            return [None, options[s]]
        index = my_list.getSelectedIndices()[0]
        return [list_items[index], options[s]]

        # call using
        # list_items = ["list1","list2"]
        # options = ["opt1", "opt2", "opt3"]
        # title = "title"
        # [list, option] = OptionDialog().ListOptions(list_items, title, options)
        # print "option= " ,option, " list = ",list

    def MultipleListOptions(self, list_items, title, options, preferred_size = "default"):
        my_list = JList(list_items)
        if list_items != []:
            my_list.setSelectedIndex(0)
        scrollPane = JScrollPane(my_list)
        if preferred_size != "default":
            scrollPane.setPreferredSize(preferred_size)   # preferred_size should be set to Dimension(300, 500) say
        else:
            no_rows_to_display = min(40, len(list_items))
            my_list.setVisibleRowCount(no_rows_to_display)
            dim = my_list.getPreferredScrollableViewportSize()
            w = int(dim.getWidth()) + 20
            h = int(dim.getHeight() + 20)  # to leave a bit of space at bottom. Height of row = approx 20
            scrollPane.setPreferredSize(Dimension(w,h))
        self.CLOSED_OPTION = False
        s = JOptionPane.showOptionDialog(None,
                                         scrollPane,
                                         title,
                                         JOptionPane.YES_NO_OPTION,
                                         JOptionPane.PLAIN_MESSAGE,
                                         None,
                                         options,
                                         options[0])
        if s == JOptionPane.CLOSED_OPTION:
            self.CLOSED_OPTION = True
            return [None,"Cancel"]
        if list_items == []:
            return [None, options[s]]
        indices = my_list.getSelectedIndices()
        list_items = [list_items[index] for index in indices]
        return [list_items, options[s]]

    def variable_combo_box(self, options, default, msg, title = None, type = JOptionPane.QUESTION_MESSAGE):


        result = JOptionPane.showInputDialog(
            None,                                   # parentComponent
            msg,                                    # message text
            title,                                  # title
            type,                                   # messageType
            None,                                   # icon
            options,                                # selectionValues
            default                                 # initialSelectionValue
        )

        return result

    def displayMessageNonModal(self, msg, jButtonMsg = "OK"):
        global customDialog
        customDialog = JDialog(None, msg, False); # 'true' for modal
        # customDialog.addWindowListener(WindowAdapter())
        #     def windowClosing(self, e):
        #         print("jdialog window closing event received")
        #         # Add your custom closing logic herecustomDialog.addWindowListener(WindowAdapter():


        # Add components to the customDialog
        # customDialog.setSize(1200, 1200)
        dimension = Dimension(400,150)
        customDialog.setPreferredSize(dimension)


        pane = customDialog.getContentPane();
        pane.setLayout(None);
        button = JButton(jButtonMsg, actionPerformed = self.click_action) ;

        button.setBounds(10,10,300,60);
        pane.add(button)
        customDialog.setLocationRelativeTo(None);
        # customDialog.setUndecorated(True)
        customDialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE)
        customDialog.pack();
        customDialog.setVisible(True);

    def click_action(self,e):
        global customDialog
        # global jdialog_closed
        sensors.getSensor("Jdialog_closed").setKnownState(ACTIVE)
        # print "&&&&&&&&&&&&&& jdialog_closed", jdialog_closed
        customDialog.dispose()
        return

    def displayMessage(self, msg, title = ""):
        self.CLOSED_OPTION = False
        s = JOptionPane.showOptionDialog(None,
                                         msg,
                                         title,
                                         JOptionPane.YES_NO_OPTION,
                                         JOptionPane.PLAIN_MESSAGE,
                                         None,
                                         ["OK"],
                                         None)
        #JOptionPane.showMessageDialog(None, msg, 'Message', JOptionPane.WARNING_MESSAGE)
        if s == JOptionPane.CLOSED_OPTION:
            self.CLOSED_OPTION = True
            return
        return s

    def customQuestionMessage2str(self, msg, title, opt1, opt2):
        self.CLOSED_OPTION = False
        options = [opt1, opt2]
        s = JOptionPane.showOptionDialog(None,
                                         msg,
                                         title,
                                         JOptionPane.YES_NO_OPTION,
                                         JOptionPane.QUESTION_MESSAGE,
                                         None,
                                         options,
                                         options[1])
        if s == JOptionPane.CLOSED_OPTION:
            self.CLOSED_OPTION = True
            return
        if s == JOptionPane.YES_OPTION:
            s1 = opt1
        else:
            s1 = opt2
        return s1

    def customQuestionMessage3str(self, msg, title, opt1, opt2, opt3):
        self.CLOSED_OPTION = False
        options = [opt1, opt2, opt3]
        s = JOptionPane.showOptionDialog(None,
                                         msg,
                                         title,
                                         JOptionPane.YES_NO_CANCEL_OPTION,
                                         JOptionPane.QUESTION_MESSAGE,
                                         None,
                                         options,
                                         options[0])
        if s == JOptionPane.CLOSED_OPTION:
            self.CLOSED_OPTION = True
            return
        if s == JOptionPane.YES_OPTION:
            s1 = opt1
        elif s == JOptionPane.NO_OPTION:
            s1 = opt2
        else:
            s1 = opt3
        return s1

    def customQuestionMessage4str(self, msg, title, opt1, opt2, opt3, opt4):
        self.CLOSED_OPTION = False
        options = [opt1, opt2, opt3, opt4]
        s = JOptionPane.showOptionDialog(None,
                                         msg,
                                         title,
                                         JOptionPane.DEFAULT_OPTION,
                                         JOptionPane.QUESTION_MESSAGE,
                                         None,
                                         options,
                                         options[0])
        if s == JOptionPane.CLOSED_OPTION:
            self.CLOSED_OPTION = True
            return
        if s == 0:
            s1 = opt1
        elif s == 1:
            s1 = opt2
        elif s == 2:
            s1 = opt3
        else:
            s1 = opt4
        return s1

    def customQuestionMessage5str(self, msg, title, opt1, opt2, opt3, opt4, opt5):
        self.CLOSED_OPTION = False
        options = [opt1, opt2, opt3, opt4, opt5]
        s = JOptionPane.showOptionDialog(None,
                                         msg,
                                         title,
                                         JOptionPane.DEFAULT_OPTION,
                                         JOptionPane.QUESTION_MESSAGE,
                                         None,
                                         options,
                                         options[0])
        if s == JOptionPane.CLOSED_OPTION:
            self.CLOSED_OPTION = True
            return
        if s == 0:
            s1 = opt1
        elif s == 1:
            s1 = opt2
        elif s == 2:
            s1 = opt3
        elif s == 3:
            s1 = opt4
        else:
            s1 = opt5
        return s1

    def customMessage(self, msg, title, opt1):
        self.CLOSED_OPTION = False
        options = [opt1]
        s = JOptionPane.showOptionDialog(None,
                                         msg,
                                         title,
                                         JOptionPane.YES_OPTION,
                                         JOptionPane.PLAIN_MESSAGE,
                                         None,
                                         options,
                                         options[0])
        if s == JOptionPane.CLOSED_OPTION:
            self.CLOSED_OPTION = True
            return
        return s

    def input(self,msg, title, default_value):
        options = None
        x = JOptionPane.showInputDialog( None, msg,title, JOptionPane.QUESTION_MESSAGE, None, options, default_value);
        if x == None:
            self.CLOSED_OPTION = True
            return
        return x

class modifiableJComboBox:

    def __init__(self, list, msg, default = ""):
        jcb = JComboBox(list)
        jcb.setMaximumRowCount(30);
        jcb.setSelectedItem(default);
        jcb.setEditable(True)
        JOptionPane.showMessageDialog( None, jcb, msg, JOptionPane.QUESTION_MESSAGE)
        self.ans = str(jcb.getSelectedItem())

    def return_val(self):
        return self.ans

if __name__ == '__builtin__':

    sensors.getSensor("stopMasterSensor").setKnownState(INACTIVE)
    sensors.getSensor("modifyMasterSensor").setKnownState(INACTIVE)

    msg = "Wait few seconds to finish starting up, then\n\n    Set up a train in a section\n    before dispatching a train "
    OptionDialog().displayMessage(msg)

    RunDispatchMaster = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/RunDispatchMaster.py')
    exec(open(RunDispatchMaster).read())
    RunDispatcherMaster()
