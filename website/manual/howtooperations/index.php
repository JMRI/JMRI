<!DOCTYPE html>
<html>
<head>
	<title>JMRI Operations How-To Files</title>
</head>
<body>
<h1>JMRI Operations How-To Files</h1>


<?php
// find showIcons directory
$name = getcwd();

$n = strrpos($name, "/manual");
require(substr($name,0,$n)."/resources/showIcons.php");

showSubdirs();
showFiles();

?>
</table>


</body>
</html>
