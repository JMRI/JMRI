<!DOCTYPE html>
<html>
<head>
	<title>JMRI Icons</title>
</head>
<body>
<h1>JMRI Resources</h1>


<?php
// find showIcons directory
$name = getcwd();
$n = strrpos($name, "/resources");

require(substr($name,0,$n+strlen("/resources"))."/showIcons.php");

showSubdirs();
showFiles();

?>
</table>


</body>
</html>
