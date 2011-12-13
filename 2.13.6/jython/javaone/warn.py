# define utility for displaying warnings

import javax.swing

class warn(javax.swing.JFrame) :
    def whenMyButtonClicked(self, event) :
        self.setVisible(False)
    def display(self, msg) :
        # create the close button, and add an action routine to it
        b = javax.swing.JButton("OK")
        b.actionPerformed = self.whenMyButtonClicked

        # create a frame to hold the button, put button in it, and display
        self.contentPane.setLayout(javax.swing.BoxLayout(self.contentPane, javax.swing.BoxLayout.Y_AXIS))
        self.contentPane.add(javax.swing.JLabel(msg))
        self.contentPane.add(b)
        self.pack()
        self.setSize(600, 200)
        self.setLocation(500,500)
        self.show()
        return

