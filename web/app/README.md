JMRI Angular Web Application
============================

This folder contains the core [Angular 1.5][1]-based JMRI web application. This
web application uses the [PatternFly][5] user interface framework.

JavaScript dependencies for the web application are managed using [NPM][2],
which is a standard technology for managing JavaScript-based applications.

To install dependencies, use ```npm install``` within this folder.

Unlike most NPM-managed web applications, npm does not provide management of
the index.html file. That is managed within JMRI itself using manifests
(described below).

# Manifests

Because this web application is designed to be more modular than a standard
Angular application, it uses manifests to provide a builder sufficient data to
construct the web application so it can be regenerated with custom information
provided by users or third parties as needed (these manifests are inspired by
the [Cockpit Project manifests][4]).

## Java WebManifest

WebManifest instances loaded by a Java Service Provider provide a pure-Java
mechanism for a Java servlet run within JMRI to ensure the web application loads
any Javascript or CSS files needed for the servlet to be used in the web
application. Refer to the Javadocs for details.

## manifest.json

When constructing the web application, JMRI searches for any files named
__manifest.json__ within the folder _web_ in the JMRI portable paths _program:_,
_preferences:_, _profile:_, and _settings:_. This file provides the same
information as a WebManifest.

### Format

manifest.json is a JSON object with the following properties:
- navigation
- scripts
- styles
- dependencies
- sources
- __translations__ - a list of locale that need to be pre-loaded before the
  controllers for a module are loaded. Use an asterisk as the placeholder for
  the requested language (English (en) will be substituted for missing languages
  in the translation).

# Localization

## Translations

The JMRI Web App uses the [angular-translate][6] service to provide translation
services. The angular-translate service is configured to use URLs in the pattern
http://server:port/PART/locale-LOCALE.json to load translation strings where
PART is the path to the locale file for the module and LOCALE is the requested
locale. The locale _en (English)_ **must** be provided as that is the fall-back
locale in the absence of others. See
https://angular-translate.github.io/docs/#/guide/02_getting-started for details
of the file structure.

A separate file (or files) can be provided to provide pre-loaded translations
that might need to be used in other places even if the controller (if any) is
loaded. These files need to be listed in the _translations_ section of the
manifest file, with an asterisk used as a placeholder for the locale.

# Included with JMRI

## Core app

The core app templates are in the app directory under this one.

## package.files

List of files in this directory that are included in the JMRI distribution. This
list is maintained due to the extreme size of the dependencies installed using
npm. This file is only used by JMRI developers and can otherwise be ignored.

[1]: https://angularjs.org
[2]: https://www.npmjs.com
[3]: http://jmri.org/JavaDoc/doc/jmri/server/web/spi/WebManifest.html
[4]: http://cockpit-project.org/guide/latest/packages.html#package-manifest
[5]: https://www.patternfly.org

## Updating

(From a note in PR #6614 by @rhwood)

```
cd web/app
npm update
npm audit fix
```
followed by commit, etc, as needed.  Further:

> `npm outdated` showed additional dependencies, but one (@rhwood) had to report since it was incorrectly published by Red Hat, so that needs to be fixed before the remaining dependencies are updated.

