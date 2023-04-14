<?php

// This php script is used to test and demonstrate the LogixNG action WebRequest.


error_reporting(E_ALL);
ini_set("display_errors", 1);

if (isset($_REQUEST['action'])) {
    $action = $_REQUEST['action'];
} else {
    $action = "menu";
}


if ($action === "menu") {

    echo <<<END_STARTPAGE
        <html>
        <head>
        <title>Test of JMRI LogixNG action WebRequest</title>
        </head>
        <body>
        <h1>Test of JMRI LogixNG action WebRequest</h1>
        <p>
        <h2>Throw turnout</h2>
        Throw turnout <a href="?action=throw&turnout=Chicago32">Chicago32</a><br>
        Throw turnout <a href="?action=throw&turnout=MiamiWest">MiamiWest</a><br>
        Throw turnout <a href="?action=throw&turnout=TorontoFirst">TorontoFirst</a><br>
        </p>
        <h2>Test cookies</h2>
        Set cookie <a href="?action=cookies&cookie=Blue">Blue</a><br>
        Set cookie <a href="?action=cookies&cookie=Green">Green</a><br>
        Set cookie <a href="?action=cookies&cookie=Yellow">Yellow</a><br>
        <a href="?action=cookies&cookie=delete_all">Delete cookies</a><br>
        </p>
        <p>
        <h2>Login</h2>
        <form action="#" method="post">
        <input type="hidden" name="action" value="login">
        <label for="fname">First name:</label>
        <input type="text" id="fname" name="fname" value="John"><br>
        <label for="lname">Last name:</label>
        <input type="text" id="lname" name="lname" value="Doe"><br><br>
        <input type="submit" value="Submit">
        </form>
        </p>
        </body>
        </html>
END_STARTPAGE;



} else if ($action === "throw") {

    $turnout = $_GET["turnout"];

    switch ($turnout) {
        case "Chicago32":
            echo "Turnout Chicago32 is thrown\n";
            exit(0);
        case "MiamiWest":
            echo "Turnout MiamiWest is thrown\n";
            exit(0);
        case "TorontoFirst":
            echo "Turnout TorontoFirst is thrown\n";
            exit(0);
        default:
            echo "Unknown turnout!\n";
            exit(0);
    }



} else if ($action === "cookies") {

    $cookie = $_GET["cookie"];

    $cookies = "";

    foreach ($_COOKIE as $key => $value) {
        if ($cookies !== "") $cookies .= ", ";
        $cookies .= $key . "=" . $value;
    }

    $fifteenDaysFromNow = time() + 86400 * 15;

    switch ($cookie) {
        case "Blue":
            setcookie("Blue", "BlueBlue!", $fifteenDaysFromNow);
            echo "Cookie Blue is set. Cookies from client: $cookies\n";
            exit(0);

        case "Green":
            setcookie("Green", "GreenGreen!", $fifteenDaysFromNow);
            echo "Cookie Green is set. Cookies from client: $cookies\n";
            exit(0);

        case "Yellow":
            setcookie("Yellow", "YellowYellow!", $fifteenDaysFromNow);
            echo "Cookie Yellow is set. Cookies from client: $cookies\n";
            exit(0);

        case "delete_all":
            foreach ($_COOKIE as $key => $value) {
                $cookies .= $key . "=" . $value;
                setcookie($key, $value, time()-3600);
            }
            echo "All cookies have been deleted!\n";
            exit(0);

        default:
            echo "Unknown cookie!\n";
            exit(0);
    }



} else if ($action === "login") {

    $fname = $_POST["fname"];
    $lname = $_POST["lname"];

    echo "Logged in. First name: $fname, last name: $lname\n";
    exit(0);

}



// phpinfo();

?>
