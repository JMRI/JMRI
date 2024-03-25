import re

from java.awt import BorderLayout, Dimension, FlowLayout
from javax.swing import JEditorPane, JFrame, JPanel, JScrollPane
from java.awt.event import WindowAdapter, WindowEvent
import javax.swing.text.html.HTMLEditorKit as HTMLEditorKit
import java.net as net


class Timetable:

    def __init__(self, station):
        # print "start timetable"
        self.html_content = "unset"
        # print "html_content", self.html_content
        self.frame = None
        # print "creating window"
        self.createWindow(station)

    # def update(self, parameters):
    #     print "start update"
    #     if self.html_content == None:
    #         print "error html_content is None"
    #     # html_content = self.edit_html(parameters)
    #
    # def update_time(self, time):
    #
    #     # time is of the form "HH:MM"
    #     # print "about to edit html", time
    #     html_content2 = self.edit_html_time(time)
    #     # print "about to load html"
    #     self.load_html(html_content2)
    #     # print "LOADAD"

    def update_timetable(self, required_station, time, timetable):

        # print "update timetable"
        html_table = self.get_html_table(time, required_station, timetable)

        # print "html_table", html_table
        html_content2 = self.html_content.replace("$time$", time).replace("$table$", html_table)
        # html_content2 = html_table
        # print "html_content2", html_content2
        # print "about to load html", html_content2
        self.load_html(html_content2)
        # print "end update timetable"

    def get_html_table(self, time, required_station, timetable):

        # print "timetable", timetable

        if timetable == None or timetable == []:
            return ""

        string = '''<tr>
            <th>$station_departure_time$</th>
            <th>$last_station$</t"h$>
            <th>$via$</th$>
            </tr>
            '''
        # print "string_orig", string
        # html_table = "<table>\n"
        # html_table = '''<table class="class9">
        # <colgroup>
        # <col span="1" style="width: 20%;">
        # <col span="1" style="width: 30%;">
        # <col span="1" style="width: 50%;">
        # </colgroup>
        # '''
        # print "timetable", timetable
        html_table1 = '''
        <table>
            <tr>
                <th colspan="2">Departures: $station$</th>
                <th>$time$</th>
            </tr>
            <tr>
                <th class="it">Time</th>
                <th class="it">Destination</th>
                <th class="it">Via</th>
            </tr>
        '''
        html_table = html_table1.replace("$station$", required_station).replace("$time$", time)

        for timetable_entry in timetable:
            # ['SidingMiddlleLHS', '7:1',             # ['SidingMiddlleLHS', '7:1', 'SidingMiddlleLHS', '10', 'SidingTopRHS']'SidingMiddlleLHS', '10', 'SidingTopRHS']
            # print "timetable_entry", timetable_entry
            [station_name, station_departure_time, last_station, last_station_arrival_time, via] = timetable_entry
            # print "[station_name, station_departure_time, last_station, last_station_arrival_time, via]", [station_name, station_departure_time, last_station, last_station_arrival_time, via]
            if str(station_name) == required_station:
                html_table_entry = string.replace("$station_name$", station_name) \
                    .replace("$station_departure_time$", station_departure_time) \
                    .replace("$last_station$", last_station) \
                    .replace("$via$", via)
                # print "html_table_entry", html_table_entry

                html_table += html_table_entry
                # print "html_table", html_table
        html_table += "\n</table>"
        # print "html_table", html_table

        return html_table

    def createWindow(self, station):
        # print "create window start"
        self.frame = MyFrame("Timetable")    #based on JFrame see definition below
        # Set the default close operation
        # self.frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE)
        #
        # # Add a window listener to handle the close event
        # self.frame.addWindowListener(MyWindowAdapter())

        self.createUI(station)
        self.frame.setSize(560, 450)
        self.frame.setLocationRelativeTo(None)
        self.frame.setVisible(True)
        # print "created window"

    def hideWindow(self):
        if self.frame != None:
            self.frame.setVisible(False)

    def showWindow(self):
        if self.frame != None:
            self.frame.setVisible(True)

    def createUI(self, station):
        # print "in createUI"
        self.panel = JPanel()
        layout = FlowLayout()
        self.panel.setLayout(layout)

        self.jEditorPane = JEditorPane()
        self.jEditorPane.setEditable(False)
        # print "about to get_html", "html_content", self.html_content
        # if self.html_content == "unset":
        #     print "about to get_html2"
        self.html_content = self.get_html()
        self.html_content = self.edit_html_station(station)
        # print "about to load html"
        self.load_html(self.html_content)
        # print "loaded html"
        jScrollPane = JScrollPane(self.jEditorPane)
        jScrollPane.setPreferredSize(Dimension(540, 400))

        self.panel.add(jScrollPane)
        self.frame.getContentPane().add(self.panel, BorderLayout.CENTER)

    def get_html(self):
        # print "in get_html"
        file = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/Timetable.html')
        # print "file", file
        with open(file, 'r') as file:
            html_content = file.read()
        # print "html_content read"
        # html_content = html_content.replace ("$station$", station)

        return html_content

    def load_html(self, html_content):
        print "in load_html"
        # print "type html_edit", type(html_content)
        # print "html_content", html_content
        # Set the content type to HTML
        self.jEditorPane.setContentType("text/html")
        self.jEditorPane.setText(html_content)
        # try:
        #     self.jEditorPane.setText(html_content)
        # except IndexError:
        #     "********************************************error in loading html"
        # except:
        #     self.jEditorPane.setText("<html>Page not found.</html>")

    def edit_html_station(self, station):

        html_edit = self.html_content
        html_edit = html_edit.replace("$station$", station)
        # html_edit = html_edit.replace ("$time$", time)
        # print "EDITED html time"
        return html_edit

    def edit_parameters_html(self, parameters):
        # print "in edit_html"
        [param1, param2] = parameters
        # print "parameters", parameters
        # if "$param1$" in self.html_content:
        # print '$param1$ in self.html_content'
        html_edit = self.html_content
        # print "html_edit assigned"
        # if "$param1$" in html_edit:
        #     print '$param1$ in html_edit'
        # print "type html_edit", type(html_edit)
        # replacements = {"$param1$": "fred", "$param2$": "jim"}
        # print "replacements", replacements
        # html_edit = "param1"
        # print "html_edit", html_edit
        # self.custom_replace(html_edit, replacements)
        html_edit = html_edit.replace("$param1$", "fred")
        # print "html_edit after", html_edit
        html_edit = html_edit.replace("[param2]", param2)
        # if "$param1$" in html_edit:
        #     print '$param1$ in html_edit after edit'
        # else:
        #     print '$param1$ not in html_edit after edit'
        #     if param1 in html_edit:
        #         print 'param1 in html_edit after edit', "param1", param1
        # print "EDITED html"
        return html_edit

    def edit_html_time(self, time):

        html_edit = self.html_content
        html_edit = html_edit.replace("$time$", time)
        # print "EDITED html time"
        return html_edit


class MyFrame(JFrame):

    def __init__(self, Title):
        # Set the default close operation
        self.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE)

        # Add a window listener to handle the close event
        self.addWindowListener(MyWindowAdapter())


class MyWindowAdapter(WindowAdapter):

    def windowClosing(self, e):
        global run_timetable_gbl
        print("Window is closing")
        # we need to hide the frame
        frame = e.getSource()
        frame.setVisible(False)

        #make sure the frame is not created again
        run_timetable_gbl = False

