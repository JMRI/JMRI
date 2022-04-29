# Sample script that is used to initialize a local variable or
# a global variable in LogixNG.
#
# The variable is initialized to a multi dimensional map that
# has the values from a LogixNG Table.
#
# The first map has the rows. Each row has a map with the column
# header as the key and the cell value as the value.
#
# In this example, we use ConcurrentHashMap since it's thread safe


# Configure this script
#
# tableName is the name of the table

tableName = "MyTable"



import jmri
import java

myTable = logixngTables.getNamedTable(tableName)

myMap = java.util.concurrent.ConcurrentHashMap()

print "Table: " + myTable.getSystemName()

for row in range(1, myTable.numRows()+1):
    rowKey = myTable.getCell(row, 0)
    rowMap = java.util.concurrent.ConcurrentHashMap()

    for col in range(1, myTable.numColumns()+1):
        columnKey = myTable.getCell(0, col)
        cellValue = myTable.getCell(row, col)
        rowMap.put(columnKey, cellValue)

    myMap.put(rowKey, rowMap)


variable.set(myMap)
