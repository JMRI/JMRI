[![Build Status](https://travis-ci.org/patternfly/angular-patternfly.svg?branch=master)](https://travis-ci.org/patternfly/angular-patternfly)
[![Dependency Status](https://gemnasium.com/badges/github.com/patternfly/angular-patternfly.svg)](https://gemnasium.com/github.com/patternfly/angular-patternfly)
[![Code Climate](https://codeclimate.com/github/patternfly/angular-patternfly/badges/gpa.svg)](https://codeclimate.com/github/patternfly/angular-patternfly)

[![Join the chat at https://gitter.im/patternfly/angular-patternfly](https://badges.gitter.im/patternfly/angular-patternfly.svg)](https://gitter.im/patternfly/angular-patternfly?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

# AngularJS directives for [PatternFly](https://www.patternfly.org) 

This project will provide a set of common AngularJS directives for use with the PatternFly reference implementation.

* Web site: https://www.patternfly.org
* API Docs: http://www.patternfly.org/angular-patternfly/#/api
* Build Status: https://travis-ci.org/patternfly/angular-patternfly.svg?branch=master

## Getting started

You have to install required software before you're able to use grunt:

* Install Node.js - Find more information on [Node.js](http://nodejs.org/)

  Angular Patternfly stays up to date with the Node LTS [Release Schedule](https://github.com/nodejs/LTS#lts_schedule). If you're using Angular Patternfly downstream, we suggest the use of an actively supported version of Node/NPM, although prior versions of Node may work. 

* Install npm - If npm is not already installed with Node.js, you have to install it manually. Find more information on [NPM](https://www.npmjs.org/)

* Install Grunt globally - Find more information on [Grunt](http://gruntjs.com/)

        npm install -g grunt-cli

* Install npm dependencies with:

        npm install

You should have your environment ready now.

Angular-PatternFly can now be built with:
```shell
grunt build
```

To see all the grunt tasks that are available:
```shell
grunt help
```

### Using Angular-PatternFly In Your Application

Note:

1. Add Angular and Angular-PatternFly as dependencies for your project and you'll receive all the libraries you'll need:

        $ npm install angular --save
        $ npm install angular-patternfly --save

2. Add the core Patternfly CSS and script includes to your HTML file(s):

        Please see:  https://github.com/patternfly/patternfly/blob/master/QUICKSTART.md

3. Add the following CSS include to your HTML file(s):

        <!-- Angular-PatternFly Styles -->
        <link rel="stylesheet" href="node_modules/angular-patternfly/dist/styles/angular-patternfly.min.css" />

4. Add the following script includes to your HTML file(s), adjusting where necessary to pull in only what you need:

        <!-- Angular -->
        <script src="node_modules/angular-patternfly/node_modules/angular/angular.min.js"></script>

        <!-- Angular-Bootstrap -->
        <script src="node_modules/angular-patternfly/node_modules/angular-ui-bootstrap/dist/ui-bootstrap.js"></script>
        <script src="node_modules/angular-patternfly/node_modules/angular-ui-bootstrap/dist/ui-bootstrap-tpls.js"></script>

        <!-- Angular-Sanitize -->
        <script src="node_modules/angular-patternfly/node_modules/angular-sanitize/angular-sanitize.min.js"></script>

        <!-- Angular-PatternFly  -->
        <script src="node_modules/angular-patternfly/dist/angular-patternfly.min.js"></script>

        <!-- C3, D3 - Charting Libraries. Only required if you are using the 'patternfly.charts' module-->
        <script src="node_modules/angular-patternfly/node_modules/patternfly/node_modules/c3/c3.min.js"></script>
        <script src="node_modules/angular-patternfly/node_modules/patternfly/node_modules/d3/d3.min.js"></script>

5. (optional) The 'patternfly.charts' module is not a dependency in the default angular 'patternfly' module.
   In order to use patternfly charts you must add it as a dependency in your application:

        my-app.module.js:

        angular.module('myApp', [
          'patternfly',
          'patternfly.charts'
        ]);

6. (optional) The 'patternfly.table' module is not a dependency in the default angular 'patternfly' module.
   In order to use pfTableView, you must add 'patternfly.table' as a dependency in your application:

        my-app.module.js:

        angular.module('myApp', [
          'patternfly',
          'patternfly.table'
        ]);

   Add the following CSS includes to your HTML file(s):

        <!-- Place before any patternfly css -->
        <link rel="stylesheet" href="node_modules/datatables.net-dt/css/jquery.dataTables.css" />

   Add the following Javascript includes to your HTML file(s):

        <script src="node_modules/jquery/dist/jquery.js"></script>
        <script src="node_modules/datatables.net/js/jquery.dataTables.js"></script>
        <script src="node_modules/datatables.net-select/js/dataTables.select.js"></script>
        <script src="node_modules/angular-datatables/dist/angular-datatables.min.js"></script>
        <script src="node_modules/angular-datatables/dist/plugins/select/angular-datatables.select.min.js"></script>

7. (optional) The 'patternfly.canvas' module is not a dependency in the default angular 'patternfly' module.
   In order to use pfCanvasEditor or pfCanvas, you must add 'patternfly.canvas' as a dependency in your application:

        my-app.module.js:

        angular.module('myApp', [
          'patternfly',
          'patternfly.canvas'
        ]);

   Add the following Javascript includes to your HTML file(s):

        <!-- jquery before angular.js -->
        <script src="node_modules/jquery/dist/jquery.js"></script>
        <script src="node_modules/components-jqueryui/jquery-ui.min.js"></script>

        <script src="node_modules/angular/angular.js"></script>

        <!-- angular-dragdrop after angular.js -->
        <script src="node_modules/angular-dragdrop/src/angular-dragdrop.js"></script>

   Also, the canvas background grid image is located in 'node_modules/angular-patternfly/dist/imgs/canvas-dot-grid.png'
   please copy this image to your application's main images directory and reference it by overridding the '.canvas' css
   class:

         <style>
             .canvas {
                 background-image: url('/myapp/imgs/canvas-dot-grid.png');
                 background-repeat: repeat;
             }
         </style>

### Using with Webpack

In order to use Angular-Patternfly in a Webpack-bundled application there are some things you need to keep in mind:

#### Create an alias for the jQuery module

In order to let Webpack find the correct jQuery module when assembling all the dependencies you need to create an alias for it in the webpack.conf.js file:

```
...
resolve: {
  alias: {
    "jquery": "angular-patternfly/node_modules/patternfly/node_modules/jquery"
  }
}
...
```

Additionally, you have to use the `webpack.ProvidePlugin` so the $ and the jQuery variables are added to the `window` object, making them available to the other modules (Patternfly included):
```
...
plugins: [
  new webpack.ProvidePlugin({
    $: "jquery",
    jQuery: "jquery",
    "window.jQuery": "jquery",
    "window.jquery": "jquery"
  })
]
...
```

## API documentation

The API documentation can be built with:
```shell
grunt ngdocs
```

If you're interested in reading the docs right away, you can use special target, which will start a web server:
```shell
grunt ngdocs:view
```

After executing this tasks you'll be able to access the documentation at [http://localhost:8000/](http://localhost:8000/). If a different port is desired, it may be specified on as an option:
```shell
grunt ngdocs:view --port=8002
```

## Releasing

Angular PatternFly is released through Bower and npm. To release a new version version of Angular PatternFly, edit `bower.json` and `package.json` accordingly.

Update the version listed in `bower.json` by editing the file and changing the line:

```
"version": "<new_version>"
```

Update the patternfly reference version listed in `bower.json` by editing the file and changing the line below. Angular patternfly has a dependency on the patternfly reference implementation so the major and minor version numbers of the two project should be the same:
```
"patternfly": "<new_version>"
```


Update the version listed in `package.json` by editing the file and changing the line:

```
"version": "<new_version>"
```

Commit the version bump:

```
git commit -m "Version bump to <new_version>"
```

Publish a new set of release notes with ```new version``` as the tag version:
https://github.com/patternfly/angular-patternfly/releases/new

## Contributing

We're always interested in contributions from the community.

Please ensure that your PR provides the following:

* Detailed description of the proposed changes
* Follows the style rules for [javascript](eslint.yaml) and [html](.htmlhintrc).
* Rebased onto the latest master commit
