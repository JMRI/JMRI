<!DOCTYPE html>
<html>
<head>
	<title>JMRI Icons</title>
</head>
<body>
<h1>JMRI Resources</h1>


<?php

function endswith($string, $test) {
    $strlen = strlen($string);
    $testlen = strlen($test);
    if ($testlen > $strlen) return false;
    return substr_compare(strtolower($string), strtolower($test), -$testlen) === 0;
}


echo "<h2>Subdirectories</h2>\n";

$d = dir(".");
while (false !== ($entry = $d->read())) {
   if (is_dir($entry)) {
    echo '<a href="'.$entry.'">'.$entry.'</a></td><p>'."\n";
   }
}
$d->close();

echo "<h2>Icons</h2>\n";

echo '<table border="1">';

$d = dir(".");
while (false !== ($entry = $d->read())) {
   if (endswith($entry, ".gif") || endswith($entry, ".jpg") || endswith($entry, ".png")) {
    echo '<tr><td>'.$entry.' </td><td><a href="'.$entry.'"><img src="'.$entry.'"></a></td>'."\n";
   }
}
$d->close();

?>
</table>


</body>
</html>
