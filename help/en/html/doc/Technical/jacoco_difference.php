<?php

// This script reads two Jacoco Coverage reports and prints the differences, sorted by number of missing lines.

// Author: Daniel Bergqvist


// Set to true to show the DOM tree instead of the html page.
// Useful if the original Jacoco page is changed and the script
// needs to be changed.
$showDOMTree = false;

// Print some information to the screen
$printDebugOutput = false;


error_reporting(E_ERROR | E_WARNING | E_PARSE | E_NOTICE);
ini_set("display_errors", 1);

// https://stackoverflow.com/questions/10524300/php-domdocument-loadhtml-error
// In many cases it is advisable to use libxml_use_internal_errors(true); before $dom->loadHTML($content);
libxml_use_internal_errors(true);

// $url = 'http://jmri.tagadab.com/jenkins/job/Development/job/JaCoCo/lastStableBuild/jacoco/';
$url = 'http://jmri.tagadab.com/jenkins/job/Development/job/JaCoCo/';

// Is the script run from the command line?
if (isset($argc) && ($argc > 0)) {

	die("Not supported from command line\n");
/*
	if ($argc == 2) {	// Package list
		parse_page($argv[1], '', false);

	} elseif ($argc == 3) {	// Class list
		parse_page($argv[1], $argv[2], true);

	} else {	// Wrong number of arguments
		echo "\n";
		echo "jacoco_sort_report.php <filename>\n";
		echo "jacoco_sort_report.php <filename> <package>\n";
		echo "\n";
		echo "Examples:\n";
		echo "jacoco_sort_report.php index.html\n";
		echo "jacoco_sort_report.php apps.DecoderPro/index.html apps.DecoderPro\n";
		echo "\n";
	}
*/
} else {
	if (!isset($_REQUEST['report1']) || !isset($_REQUEST['report2'])) {
		echo <<<END_FORM
<html>
<head>
	<title>Show the difference between two Jacoco reports</title>
</head>
<body>
	<form action="jacoco_difference.php" method="GET">
		First report: <input type="text" name="report1" value=""><br>
		Second report: <input type="text" name="report2" value=""><br>
		<input type="submit" value="Submit"><p>
		Enter the build number or the keyword <b>lastStableBuild</b> .
	</form>
</body>
</html>
END_FORM;
	} else {
		$report1 = trim($_REQUEST['report1']);
		$report2 = trim($_REQUEST['report2']);

		$url_1 = $url . $report1 . "/jacoco/";
		$url_2 = $url . $report2 . "/jacoco/";

		if (isset($_REQUEST['package'])) {
			$packageName = $_REQUEST['package'];

			if (preg_match('/^(\w+\.)*\w+$/', $packageName) == 1) {
				parse_page($url_1.$packageName, $url_2.$packageName, $packageName, true);
			} else {
				echo "Invalid package name. Only characters and dots are allowed in a package name.<br/>\n";
			}
		} else {
			parse_page($url_1, $url_2, '', false);
		}
	}
}

exit;




function getNodeType($nodeType)
{
	switch ($nodeType)
	{
		case XML_ELEMENT_NODE: return 'XML_ELEMENT_NODE';
		case XML_ATTRIBUTE_NODE: return 'XML_ATTRIBUTE_NODE';
		case XML_TEXT_NODE: return 'XML_TEXT_NODE';
		case XML_CDATA_SECTION_NODE: return 'XML_CDATA_SECTION_NODE';
		case XML_ENTITY_REF_NODE: return 'XML_ENTITY_REF_NODE';
		case XML_ENTITY_NODE: return 'XML_ENTITY_NODE';
		case XML_PI_NODE: return 'XML_PI_NODE';
		case XML_COMMENT_NODE: return 'XML_COMMENT_NODE';
		case XML_DOCUMENT_NODE: return 'XML_DOCUMENT_NODE';
		case XML_DOCUMENT_TYPE_NODE: return 'XML_DOCUMENT_TYPE_NODE';
		case XML_DOCUMENT_FRAG_NODE: return 'XML_DOCUMENT_FRAG_NODE';
		case XML_NOTATION_NODE: return 'XML_NOTATION_NODE';
		case XML_HTML_DOCUMENT_NODE: return 'XML_HTML_DOCUMENT_NODE';
		case XML_DTD_NODE: return 'XML_DTD_NODE';
		case XML_ELEMENT_DECL_NODE: return 'XML_ELEMENT_DECL_NODE';
		case XML_ATTRIBUTE_DECL_NODE: return 'XML_ATTRIBUTE_DECL_NODE';
		case XML_ENTITY_DECL_NODE: return 'XML_ENTITY_DECL_NODE';
		case XML_NAMESPACE_DECL_NODE: return 'XML_NAMESPACE_DECL_NODE';
		case XML_ATTRIBUTE_CDATA: return 'XML_ATTRIBUTE_CDATA';
		case XML_ATTRIBUTE_ID: return 'XML_ATTRIBUTE_ID';
		case XML_ATTRIBUTE_IDREF: return 'XML_ATTRIBUTE_IDREF';
		case XML_ATTRIBUTE_IDREFS: return 'XML_ATTRIBUTE_IDREFS';
		case XML_ATTRIBUTE_ENTITY: return 'XML_ATTRIBUTE_ENTITY';
		case XML_ATTRIBUTE_NMTOKEN: return 'XML_ATTRIBUTE_NMTOKEN';
		case XML_ATTRIBUTE_NMTOKENS: return 'XML_ATTRIBUTE_NMTOKENS';
		case XML_ATTRIBUTE_ENUMERATION: return 'XML_ATTRIBUTE_ENUMERATION';
		case XML_ATTRIBUTE_NOTATION: return 'XML_ATTRIBUTE_NOTATION';
		default: return "Unknown node type";
	}
}



function show_tree($pad, $id, $element)
{
	if (isset($element->tagName) && in_array($element->tagName, array("link", "script")))
		return;

	if ($element->nodeType == XML_TEXT_NODE) {
		echo $pad."Text node: '".$element->nodeValue."'<br>\n";
	}
	else {
		$attr = "";

		if (is_object($element->attributes))
		{
			for ($i = 0; $i < $element->attributes->length; $i++)
			{
				$attr .= $element->attributes->item($i)->name . ": " . $element->attributes->item($i)->value . ", ";
			}
		}

		if (isset($element->tagName))
			echo $pad.$id.": &lt;".$element->tagName."&gt;&nbsp;&nbsp;&nbsp;" . $attr . "<br />\n";
		else
			echo $pad."Tag has no name. Type: ".$element->nodeType."<br>\n";

		if (is_object($element->childNodes))
		{
			for ($i = 0; $i < $element->childNodes->length; $i++)
			{
				$child = $element->childNodes->item($i);
				show_tree($pad."&nbsp;&nbsp;&nbsp;", $id.'.'.$i, $child);
			}
		}
		else
		{
			echo "\$element->childNodes is not an object. ".getNodeType($element->nodeType)."<br>\n";
		}
	}
}







class CoverageInfo {
	public $element;
	public $name;
	public $numMissedInstructions;
	public $numMissedBranches;
	public $numMissedComplexities;
	public $numMissedLines;
	public $numMissedMethods;
	public $numMissedClasses;

	public $numMissedLinesTotal;
}



function parse_page_prim($page, $url, $package, $isClassList, $buildNo)
{
	global $missedLinesArray;
	global $showDOMTree;
	global $printDebugOutput;

	$page = trim($page);

	$DOM = new DOMDocument;
	$DOM->loadHTML($page);


	// Show the DOM tree?
	if ($showDOMTree) {

		show_tree("", '0', $DOM->documentElement);

	} else {

		$elements = $DOM->documentElement->getElementsByTagName('script');
		foreach ($elements as $element) {
			$element->parentNode->removeChild($element);
		}

		
		$bodyElement = $DOM->documentElement->childNodes->item(1);

		if ($isClassList) {		// Class list
			$tableElement = $bodyElement->childNodes->item(2)->childNodes->item(1)->childNodes->item(6);
		} else {				// Package list
			$tableElement = $bodyElement->childNodes->item(2)->childNodes->item(1)->childNodes->item(7);
		}

		for ($i=1; $i < $tableElement->childNodes->length; $i++) {

			$tableRowElement = $tableElement->childNodes->item($i);

			$coverageInfo = new CoverageInfo;

			$coverageInfo->element = $tableRowElement;
			
			$elements = $tableRowElement->getElementsByTagName('a');
			foreach ($elements as $element) {
				if ($element->hasAttribute('href')) {

					// The A-tag has the name of the package or class
					$coverageInfo->name = $element->nodeValue;

					if ($isClassList) {		// Class list
						$href = $element->getAttribute('href');
						$element->setAttribute('href', 'http://jmri.tagadab.com/jenkins/job/Development/job/JaCoCo/lastStableBuild/jacoco/'.$package.'/'.$href);
					} else {				// Package list
						$href = $element->nodeValue;
						$element->setAttribute('href', 'jacoco_sort_report.php?package='.$href);
					}

				}
			}

			$aElement = $tableRowElement->childNodes->item(0)->childNodes->item(0);

			$instructionElement = $tableRowElement->childNodes->item(1)->childNodes->item(1)->childNodes->item(0)->childNodes->item(0)->childNodes->item(0);
			$instructionArray = explode(" ", $instructionElement->nodeValue);
			$coverageInfo->numMissedInstructions = $instructionArray[1];

			$branchElement = $tableRowElement->childNodes->item(3)->childNodes->item(1)->childNodes->item(0)->childNodes->item(0)->childNodes->item(0);
			$branchArray = explode(" ", $branchElement->nodeValue);
			$coverageInfo->numMissedBranches = $branchArray[1];

			$complexityElement = $tableRowElement->childNodes->item(5)->childNodes->item(1)->childNodes->item(0)->childNodes->item(0)->childNodes->item(0);
			$complexityArray = explode(" ", $complexityElement->nodeValue);
			$coverageInfo->numMissedComplexities = $complexityArray[1];

			$linesElement = $tableRowElement->childNodes->item(7)->childNodes->item(1)->childNodes->item(0)->childNodes->item(0)->childNodes->item(0);
			$linesArray = explode(" ", $linesElement->nodeValue);
			$coverageInfo->numMissedLines = $linesArray[1];

			$methodElement = $tableRowElement->childNodes->item(9)->childNodes->item(1)->childNodes->item(0)->childNodes->item(0)->childNodes->item(0);
			$methodArray = explode(" ", $methodElement->nodeValue);
			$coverageInfo->numMissedMethods = $methodArray[1];

			$classElement = $tableRowElement->childNodes->item(11)->childNodes->item(1)->childNodes->item(0)->childNodes->item(0)->childNodes->item(0);
			$classArray = explode(" ", $classElement->nodeValue);
			$coverageInfo->numMissedClasses = $classArray[1];

			$coverageInfo->numMissedLinesTotal = $coverageInfo->numMissedLines;

			$missedLinesArray[$aElement->childNodes->item(0)->nodeValue][$buildNo] = $coverageInfo->numMissedLines;
			

			if ($printDebugOutput) {
				echo "Package: " . $aElement->childNodes->item(0)->nodeValue
					. ", instr: " . $coverageInfo->numMissedInstructions
					. ", branch: " . $coverageInfo->numMissedBranches
					. ", compl: " . $coverageInfo->numMissedComplexities
					. ", lines: " . $coverageInfo->numMissedLines
					. ", method: " . $coverageInfo->numMissedMethods
					. ", class: " . $coverageInfo->numMissedClasses
					. "<br />";
			}
		}

		while ($tableElement->childNodes->length > 1) {
			$tableElement->removeChild($tableElement->childNodes->item(1));
		}
	}
}



function parse_page($url_1, $url_2, $package, $isClassList)
{
	global $missedLinesArray;

	$missedLinesArray = array();

	$page = file_get_contents($url_1);
	parse_page_prim($page, $url_1, $package, $isClassList, 1);

	$page = file_get_contents($url_2);
	parse_page_prim($page, $url_2, $package, $isClassList, 2);

	$linesDiff = array();

	foreach ($missedLinesArray as $name => $lines) {
		if (!isset($lines[1])) $lines[1] = 0;
		if (!isset($lines[2])) $lines[2] = 0;

		$linesDiff[$name] = $lines[2] - $lines[1];
	}

	arsort($linesDiff);

	echo "<html><head><title>Show the difference between two Jacoco reports</title></head><body>";
	echo "The first column is the difference in missed lines count.<br>";
	echo "Positive number: The number of missed lines has increased.<br>";
	echo "Negative number: The number of missed lines has decreased.<p>";
	echo "Packages and classes with zero difference is not shown.<p>";
	echo "<table>";

	$queryStr = $_SERVER["QUERY_STRING"];
	foreach ($linesDiff as $name => $linesDiff) {
		if ($linesDiff != 0) {
			if (isset($_REQUEST['package'])) {
				echo "<tr><td align=\"right\">$linesDiff</td><td>&nbsp;</td><td>$name</td></tr>\n";
			} else {
				echo "<tr><td align=\"right\">$linesDiff</td><td>&nbsp;</td><td><a href=\"jacoco_difference.php?$queryStr&package=$name\">$name</a></td></tr>\n";
			}
		}
	}
	echo "</table></body></html>\n";
}



?>