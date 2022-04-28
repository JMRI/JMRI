# Sample script that is used to initialize a local variable
# or a global variable in LogixNG.
#
# The variable is initialized to a map that has the values
# from a LogixNG Table.
#
# In this example, we use ConcurrentHashMap since it's thread safe


# Configure this script
#
# tableName is the name of the table
# columnName is the name of the column you want to use for the values

tableName = "MyTable"
columnName = "Turnouts"



import jmri
import java

myTable = logixngTables.getNamedTable(tableName)

myMap = java.util.concurrent.ConcurrentHashMap()

print "Table: " + myTable.getSystemName()

column = myTable.getColumnNumber(columnName)

for row in range(1, myTable.numRows()+1):
    key = myTable.getCell(row, 0)
    value = myTable.getCell(row, column)
    myMap.put(key, value)


variable.set(myMap)
