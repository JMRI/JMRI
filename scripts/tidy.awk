$1 == "Filename:" {file = $2}
$6 == "Warning:" {print file " " $0}
