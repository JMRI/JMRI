# Example of setting a route
#
# Also used in a JMRI test class
#

import jmri
r = routes.getRoute("StartUp")
r.activateRoute()
r.setRoute()
