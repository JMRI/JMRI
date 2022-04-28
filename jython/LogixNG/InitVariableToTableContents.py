# Sample script that is used to initialize a local variable
# or a global variable in LogixNG.
#
# The variable is initialized to a map that has the values
# from a LogixNG Table.

import jmri
import java

# Configure this script
# tableName is the name of the table
# columnName is the name of the column you want to use for the values
tableName = "MyTable"
columnName = "Turnouts"



myTable = logixngTables.getNamedTable(tableName)

print "Table: "
print myTable

# ConcurrentHashMap is thread safe
myMap = java.util.concurrent.ConcurrentHashMap()

print "Table: " + myTable.getSystemName()

column = myTable.getColumnNumber(columnName)

for row in range(1, myTable.numRows()):
    key = myTable.getCell(row, 0)
    print "key: " + key
    value = myTable.getCell(row, column)
    print "value: " + value
    myMap.put(key, value)


variable.set(myMap)
