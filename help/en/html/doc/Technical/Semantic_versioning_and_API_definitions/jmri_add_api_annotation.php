<?php

/*

Call this script with:

php -f jmri_add_api_annotation.php <path> <md-file>

<path> is the root folder to look into, for example: /home/daniel/Dokument/GitHub/JMRI

<md-file> is the file that contains the table that specifies which item should have
which annotation.



This script reads a *.md file with a table and adds @API annotation to the enums,
interfaces, classes, methods and fields as specified by the table. The "type"
column specifies what type the name is referring to, and can be any of package,
class, method or field. Type "class" is used for enums and interfaces as well.

The "name" field tells the name of the item. For packages, it may end with .**
which refers to this package and all its sub packages. If a package should have
one annotation and all its sub packages another annotation, specify the sub
packages first.

The "status" field tells which status a enum/interface/class/method/field
should get.

The "force" field tells if the status should be put on the item even if it already
has a status. By default, the status is not changed if it already has a status,
but if the "force" field has the letter "X", it will be forced. Note that if any
other line later in the table affects a enum/interface/class/method/field that
is previously marked with forced, that line will be used even if it's not forced.
For example, if the first line is "jmri.**" and it's forced, and the fifth line
is "jmri.jmrit.beantable.**" is not forced, that fifth line will be handled as
forced since it affects classes that are already forced by the "jmri.**" rule.

The first line has the lowest priority and the last line has the highest priority.

Since this script is aimed to be used seldom, I have not focused on performance.
If there is a lot of different rules in the table, performance may be very bad.
If that becomes a problem, maybe the table can be split in several files.

The script reads the *.md file and looks for the table. Everything before the
table that doesn't look like a table is skipped, to allow comments in the file.

Comments may be added in the table. Every row that starts with a dot "." will
be treated as a comment. The first column in the table is narrow since it holds
the type, so it's recommended to write the comment in the second column, for
example:

Type | Name | Status | Force
---- | ---- | ------ | -----
package | jmri.** | EXPERIMENTAL | X
package | jmri | STABLE
. | This is a comment
package | jmri.jmrit.** | MAINTAINED
package | jmri.jmrit.beantable | STABLE

Empty lines may be inserted in the table as well, but they must start with
a dot to make the table work when viewed on GitHub.


Note!
This script has not yet support for inner classes or inner enums. It means that
inner classes and inner enums will get the same status as the outer class.




Example of the table:

Type | Name | Status | Force
---- | ---- | ------ | -----
package | jmri.** | EXPERIMENTAL | X
package | jmri | STABLE
class | jmri.Turnout | EXPERIMENTAL
method | jmri.Turnout.setState | MAINTAINED
field | jmri.NamedBean.UNKNOWN | INTERNAL
package | jmri.jmrit.** | MAINTAINED
package | jmri.jmrit.beantable | STABLE


Valid types are:
* PACKAGE
* CLASS
* METHOD
* FIELD

Valid status are:
* DEPRECATED
* EXPERIMENTAL
* INTERNAL
* MAINTAINED
* STABLE

*/






class Rule
{
	public $type;
	public $name;
	public $root_package;
	public $class_name;
	public $item_name;
	public $status;
	public $force;
}



if ($argc != 3)
{
	echo "Syntax: php -f jmri_add_api_annotation.php <path> <md-file>\n";
	exit;
}


$start_execution_time = time();

$rules = array();


$path = $argv[1];
$md_file = $argv[2];

$md_file_content = file_get_contents($md_file);
$md_file_content_array = explode("\n", $md_file_content);

// echo $md_file_content;

$found_table = false;

$count = 0;

foreach ($md_file_content_array as $line)
{
//	echo "$line\n";

	if ($line == "") continue;

	if ($line[0] == ".") continue;

	$parts = explode("|", $line);

	if ($found_table)
	{
		$rule = new Rule();
		$rule->type = strtoupper(trim($parts[0]));
		$rule->name = trim($parts[1]);
		$rule->root_package = null;
		$rule->status = strtoupper(trim($parts[2]));

		if (isset($parts[3])) {
			$rule->force = strtoupper(trim($parts[3])) == "X" ? true : false;
		}

		if (substr($rule->name,-3) == ".**") $rule->root_package = substr($rule->name,0,-3);

		if ($rule->type == "CLASS") $rule->class_name = $rule->name;

		if (($rule->type == "METHOD") || ($rule->type == "FIELD")) {
			if (strrpos($rule->name,".") > 0) {
				$rule->class_name = substr($rule->name,0,strrpos($rule->name,"."));
			}

			$rule->item_name = substr($rule->name,strrpos($rule->name,".")+1);
		}

		switch ($rule->type) {
			case "PACKAGE":
			case "CLASS":
			case "METHOD":
			case "FIELD":
				break;

			default:
				echo "Unknown type: {$rule->type}\n";
				exit;
		}

		switch ($rule->status) {
			case "DEPRECATED":
			case "EXPERIMENTAL":
			case "INTERNAL":
			case "MAINTAINED":
			case "STABLE":
				break;

			default:
				echo "Unknown status: {$rule->status}\n";
				exit;
		}

		$count++;
		echo "Rule $count: {$rule->type}, {$rule->name}, {$rule->status}, " . ($rule->force ? "force" : "") . "\n";
//		echo "Rule $count: {$rule->type}, {$rule->name}, {$rule->root_package}, {$rule->class_name}, {$rule->item_name}, {$rule->status}, {$rule->force}\n";

		$rules[] = $rule;
	}


	if (substr(trim($parts[1]),0,2) == "--")
	{
		$found_table = true;
	}
}


read_folder($path, "");


$execution_time = time() - $start_execution_time;

$min = intdiv($execution_time,60);
$sec = $execution_time % 60;

echo "Total execution time: $min minutes $sec seconds\n";


exit;





function add_apiguardian_import(&$new_content_array, $file)
{
	$package_line = 0;

	for ($package_line = 0; $package_line < count($new_content_array); $package_line++) {
		if (substr(trim($new_content_array[$package_line]),0,strlen("package ")) == "package ") {
			break;
		}
	}

	if ($package_line >= count($new_content_array)) {
		echo "Error: $file has not imported apiguardian.\n";
		return;
	}

	$new_elements = array();
	$new_elements[] = "";
	$new_elements[] = "import org.apiguardian.api.API;";
	$new_elements[] = "import static org.apiguardian.api.API.Status.*;";

	array_splice($new_content_array, $package_line+1, 0, $new_elements);
}



function update_class($file, $class, $class_status, $method_field_rules, $force)
{
//	echo "Update class: $class\n";

	$changed = false;

	$file_content = file_get_contents($file);
	$file_content_array = explode("\n", $file_content);

	$new_content = array();

//	echo "File: $file, Class $class, Class status: $class_status\n";

	$last_line_has_api = false;
	$import_section_found = false;
	$has_apiguardian_import = false;

	$import_apiguardian_1 = "import org.apiguardian.api.API;";
	$import_apiguardian_2 = "import static org.apiguardian.api.API.Status.*;";

	foreach ($file_content_array as $line) {

		if (substr($line,0,strlen("import ")) == "import ") {
			$import_section_found = true;

			if ($line == $import_apiguardian_1) {
				$has_apiguardian_import = true;
			} else {
				if (!$has_apiguardian_import && strcmp($line,$import_apiguardian_1) > 0) {
					$new_content_array[] = $import_apiguardian_1;
					$new_content_array[] = $import_apiguardian_2;
					$has_apiguardian_import = true;
				}
			}
		} else if (!$has_apiguardian_import && (trim($line) != "") && $import_section_found) {
			$new_content_array[] = $import_apiguardian_1;
			$new_content_array[] = $import_apiguardian_2;
			$new_content_array[] = "";
			$has_apiguardian_import = true;
		}



		$has_added_api = false;

		if ($class_status != null) {
			if (preg_match("/^\s*((public|protected|static|final)\s+)+(enum|interface|class)\s+$class\s*/", $line) !== 0) {

				if ($class_status != null) {
					$new_content_array[] = "@API(status = $class_status)";
					$changed = true;
					$has_added_api = true;

					if (! $has_apiguardian_import) {
						add_apiguardian_import($new_content_array, $file);
						$has_apiguardian_import = true;
					}
				}
			}
		}



		foreach ($method_field_rules as $mf_rule) {

			$name = substr($mf_rule->name,strrpos($mf_rule->name,".")+1);
			$status = $mf_rule->status;

			if (($mf_rule->type == "METHOD") || ($mf_rule->type == "FIELD")) {
				if (preg_match("/^(\s*)((public|protected|static|final)\s+)+\w+\s+$name\s*/", $line, $matches) !== 0) {

					if ($status != null) {

						if (substr(trim($new_content_array[count($new_content_array)-1]),0,strlen("@API(")) == "@API(") {
							$new_content_array[count($new_content_array)-1] = $matches[1] . "@API(status = $status)";
						} else {
							$new_content_array[] = $matches[1] . "@API(status = $status)";
						}
						$changed = true;
						$has_added_api = true;
						$last_line_has_api = true;

						if (! $has_apiguardian_import) {
							add_apiguardian_import($new_content_array, $file);
							$has_apiguardian_import = true;
						}
					}
				}
			}
		}

		if (substr(trim($line),0,strlen("@API(")) == "@API(") {
			if ($has_added_api) continue;
			if ($force) continue;
			if ($last_line_has_api) continue;
			$last_line_has_api = true;
		} else {
			if (! $has_added_api) $last_line_has_api = false;
		}

		$new_content_array[] = $line;
	}

	if ($changed) {
		$new_content = implode("\n", $new_content_array);
		file_put_contents($file, $new_content);
	}
}



function check_file($path, $class, $package, $package_and_file, $file)
{
//	echo "Check file: $file\n";

	global $rules;

	$method_field_rules = array();

	$force = false;
	$last_rule = null;

	$class_status = null;
	$item_status = null;

	foreach ($rules as $rule)
	{
//		echo "{$rule->type}, {$rule->name}\n";

		if (($rule->type == "PACKAGE") && ($package == $rule->name)) {
			$last_rule = $rule;
			$force |= $rule->force;
			$class_status = $rule->status;
		}

		if (($rule->type == "PACKAGE") && ($rule->root_package != null) && (substr($package,0,strlen($rule->root_package)) == $rule->root_package)) {
			$last_rule = $rule;
			$force |= $rule->force;
			$class_status = $rule->status;
		}

		if ($package_and_file == $rule->class_name) {
			$last_rule = $rule;

			if (($rule != null) && (($rule->type == "CLASS") || ($rule->type == "PACKAGE"))) {
				$class_status = $rule->status;
				$force |= $rule->force;
				$item_status = null;
				$method_field_rules = array();	// Clear the array
			}

			if (($rule->type == "METHOD") || ($rule->type == "FIELD")) {
				$method_field_rules[] = $rule;
			}
		}
	}

	if ($last_rule != null) {
		update_class($file, $class, $class_status, $method_field_rules, $force);
	}
}



function read_folder($path, $package)
{
//	echo "Scan files in folder $path\n";

	$files = scandir($path);

	foreach ($files as $file)
	{
		if (($path != "") && (substr($path,-1) != '/'))
			$path_and_file = $path . "/" . $file;
		else
			$path_and_file = $path . $file;
		
		if (($file == ".") || ($file == "..")) continue;
		
		if ($package != "")
			$package_and_file = $package . "." . $file;
		else
			$package_and_file = $file;

		if (is_dir($path_and_file)) {
			read_folder($path_and_file, $package_and_file);
		} else {
			if (substr($package_and_file,-5) == ".java") {
				$class = substr($file,0,-5);
				$package_and_file = substr($package_and_file,0,-5);
				check_file($path, $class, $package, $package_and_file, $path_and_file);
			}
		}
	}
}



?>