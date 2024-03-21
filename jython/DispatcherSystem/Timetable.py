import re

from java.awt import BorderLayout, Dimension, FlowLayout
from  javax.swing import JEditorPane, JFrame, JPanel, JScrollPane
import javax.swing.text.html.HTMLEditorKit as HTMLEditorKit
import java.net as net

class Timetable:

    def __init__(self, station):
        self.html_content = "unset"
        print "html_content", self.html_content
        self.frame = None
        print "creating window"
        self.createWindow(station)

    def update(self, parameters):
        if self.html_content == None:
            print "error html_content is None"
        # html_content = self.edit_html(parameters)

    def update_time(self, time):

        # time is of the form "HH:MM"
        print "about to edit html", time
        html_content2 = self.edit_html_time(time)
        print "about to load html"
        self.load_html(html_content2)
        print "LOADAD"

    def update_timetable(self, time, timetable):
        print "update table"
        html_table = self.get_html_table(timetable)
        print "html_table", html_table
        html_content2 = self.html_content.replace ("$time$", time).replace ("$table$", html_table)
        print "html_content2", html_content2
        print "about to load html", html_content2
        self.load_html(html_content2)
        print "LOADAD"

    def get_html_table(self, timetable):

        print "timetable", timetable

        string = '''  
            <tr>
            <th>$station_name$</th$>
            <th>$station_departure_time$</th>
            <th>$last_station$</th$>
            <th>$via$</th$>
            </tr>
            '''
        print "string_orig", string
        html_table = "<table>\n"
        for timetable_entry in timetable:
            # ['SidingMiddlleLHS', '7:1', 'SidingMiddlleLHS', '10', 'SidingTopRHS']
            print "timetable_entry", timetable_entry
            [station_name, station_departure_time, last_station, last_station_arrival_time, via] = timetable_entry
            print "[station_name, station_departure_time, last_station, last_station_arrival_time, via]", [station_name, station_departure_time, last_station, last_station_arrival_time, via]
            html_table_entry = string.replace("$station_name$", station_name)\
                                    .replace("$station_departure_time$", station_departure_time)\
                                    .replace("$last_station$", last_station) \
                                    .replace("$via$", via)
            print "html_table_entry", html_table_entry

            html_table += html_table_entry
            print "html_table", html_table
        html_table +="\n</table>"
        print "html_table", html_table

        return html_table








        # time is of the form "HH:MM"
        # html_content2 = self.edit_html_timetable(station, time)
        # print "about to load html"
        # self.load_html(html_content2)


    def createWindow(self, station):
        print "create window start"
        self.frame = JFrame("Swing Tester")
        # self.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
        print "creating UI"
        self.createUI(station)
        self.frame.setSize(560, 450)
        self.frame.setLocationRelativeTo(None)
        self.frame.setVisible(True)
        print "created window"

    def createUI(self, station):
        print "in createUI"
        self.panel = JPanel()
        layout = FlowLayout()
        self.panel.setLayout(layout)

        self.jEditorPane = JEditorPane()
        self.jEditorPane.setEditable(False)
        print "about to get_html", "html_content", self.html_content
        # if self.html_content == "unset":
        #     print "about to get_html2"
        self.html_content = self.get_html()
        self.html_content = self.edit_html_station(station)
        print "about to load html"
        self.load_html(self.html_content)
        print "loaded html"
        jScrollPane = JScrollPane(self.jEditorPane)
        jScrollPane.setPreferredSize(Dimension(540,400))

        self.panel.add(jScrollPane)
        self.frame.getContentPane().add(self.panel, BorderLayout.CENTER)

    def get_html(self):
        print "in get_html"
        file = jmri.util.FileUtil.getExternalFilename('program:jython/DispatcherSystem/timetable.html')
        print "file", file
        with open(file, 'r') as file:
            html_content = file.read()
        print "html_content read"
        # html_content = html_content.replace ("$station$", station)

        return html_content
        
    def load_html(self, html_content):
        # print "in load_html"
        # print "type html_edit", type(html_content)
        # print "html_content", html_content
        # Set the content type to HTML
        self.jEditorPane.setContentType("text/html")
        try:
            self.jEditorPane.setText(html_content)
        except:
            self.jEditorPane.setText("<html>Page not found.</html>")

    def edit_html_station(self, station):

        html_edit = self.html_content
        html_edit = html_edit.replace ("$station$", station)
        # html_edit = html_edit.replace ("$time$", time)
        print "EDITED html time"
        return html_edit

    def edit_parameters_html(self, parameters):
        print "in edit_html"
        [param1, param2] = parameters
        print "parameters", parameters
        if "$param1$" in self.html_content:
            print '$param1$ in self.html_content'
        html_edit = self.html_content
        print "html_edit assigned"
        if "$param1$" in html_edit:
            print '$param1$ in html_edit'
        # print "type html_edit", type(html_edit)
        # replacements = {"$param1$": "fred", "$param2$": "jim"}
        # print "replacements", replacements
        # html_edit = "param1"
        # print "html_edit", html_edit
        # self.custom_replace(html_edit, replacements)
        html_edit = html_edit.replace ("$param1$", "fred")
        # print "html_edit after", html_edit
        html_edit =  html_edit.replace ("[param2]", param2)
        if "$param1$" in html_edit:
            print '$param1$ in html_edit after edit'
        else:
            print '$param1$ not in html_edit after edit'
            if param1 in html_edit:
                print 'param1 in html_edit after edit', "param1", param1
        print "EDITED html"
        return html_edit

    def edit_html_time(self, time):

        html_edit = self.html_content
        html_edit = html_edit.replace ("$time$", time)
        print "EDITED html time"
        return html_edit

    # def replace(self,string1, string2):
    #     # Replace "is" with "is not" using word boundaries
    #     s = re.sub(r\"\\bis\\b\", \"is not\", s)

    # def custom_replace(self, string, rep_dict):
    #     print "in custom replace"
    #     pattern = re.compile("|".join([re.escape(k) for k in sorted(rep_dict, key=len, reverse=True)]), flags=re.DOTALL)
    #     return pattern.sub(lambda x: rep_dict[x.group(0)], string)
        
