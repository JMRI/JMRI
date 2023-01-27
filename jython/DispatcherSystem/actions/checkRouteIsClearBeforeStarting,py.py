import java
import jmri
import sys

class CheckRoute(jmri.jmrit.automat.AbstractAutomaton):

    def init(self, dispatch):
        self.dispatch = dispatch

    def handle(self):


if __name__ == '__main__':   # will only run when script1.py is run directly
    cr = CheckRoute(sys.argv[1])
    check_route(sys.argv[1])