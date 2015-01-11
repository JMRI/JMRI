# Read Me for the JMRI Web Server

Maintenance notes as a text file, so they are readable in a terminal window.

## Directories

### /web
Except as described later, the contents of _/web_ are served directly to a
client device.

### /web/servlet
_/web/servlet_ and its subdirectories contain files that are processed by the
JMRI Web Server before being served to a client device.

## Adding Content

**Add alternate versions of existing JavaScript libraries only if existing
versions cannot be used.**

If you must add an alternate version of an existing JavaScript library, discuss
this need with the JMRI developers--we may need to also update the library we
use, and we may be able to assist you in sub-classing objects in the library
instead of rewriting them.

## Maintenance

**Only use stable releases from upstream vendors**

### Updating Bootstrap
1. Download current stable release from
[Bootstrap](http://getbootstrap.com/getting-started/)
2. Unzip the contents of that file directly into _/web_. The contents of that
file should overwrite the older version of Bootstrap.
3. As always, test before committing.

### Updating jQuery
We only use the 1.x version of jQuery to maintain support for Internet
Explorer 8 (the oldest version supported by Bootstrap).
1. Download current stable 1.x release from
[jQuery](http://jquery.com/download/)
2. Copy that file into _/web/js_.
3. Run your favorite search and replace command to update the jQuery version in
every file that references it.
4. As always, test before committing.

Use the [Markdown syntax](http://daringfireball.net/projects/markdown/) when
editing this file.
