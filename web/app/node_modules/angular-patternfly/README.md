[![Build Status](https://travis-ci.org/patternfly/angular-patternfly.svg?branch=master)](https://travis-ci.org/patternfly/angular-patternfly)
[![Dependency Status](https://gemnasium.com/badges/github.com/patternfly/angular-patternfly.svg)](https://gemnasium.com/github.com/patternfly/angular-patternfly)
[![Code Climate](https://codeclimate.com/github/patternfly/angular-patternfly/badges/gpa.svg)](https://codeclimate.com/github/patternfly/angular-patternfly)
[![NSP Status](https://nodesecurity.io/orgs/angular-patternfly/projects/690b94c3-4f36-4208-887d-fdb5f22f65fc/badge)](https://nodesecurity.io/orgs/angular-patternfly/projects/690b94c3-4f36-4208-887d-fdb5f22f65fc)
[![Join the chat at https://gitter.im/patternfly/angular-patternfly](https://badges.gitter.im/patternfly/angular-patternfly.svg)](https://gitter.im/patternfly/angular-patternfly?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Coverage Status](https://coveralls.io/repos/github/patternfly/angular-patternfly/badge.svg)](https://coveralls.io/github/patternfly/angular-patternfly)

# AngularJS components for [PatternFly](https://www.patternfly.org)

This project will provide a set of common AngularJS components for use with the PatternFly reference implementation.

* Web site: https://www.patternfly.org
* API Docs: http://www.patternfly.org/angular-patternfly/#/api
* Build Status: https://travis-ci.org/patternfly/angular-patternfly.svg?branch=master

## Getting started

You have to install required software before you're able to use grunt:

* Install Node.js - Find more information on [Node.js](http://nodejs.org/)

  Angular Patternfly stays up to date with the Node LTS [Release Schedule](https://github.com/nodejs/LTS#lts_schedule). If you're using Angular Patternfly downstream, we suggest the use of an actively supported version of Node/NPM, although prior versions of Node may work.

* Install npm - If npm is not already installed with Node.js, you have to install it manually. Find more information on [NPM](https://www.npmjs.org/)

* Install Grunt globally - Find more information on [Grunt](http://gruntjs.com/)
    ```shell
    $ npm install -g grunt-cli
    ```

* Install npm dependencies with:
    ```shell
    $ npm install
    ```

You should have your environment ready now.

Angular-PatternFly can now be built with:
```shell
$ npm run build
```

To see all the grunt tasks that are available:
```shell
$ npm run help
```

### Using Angular-PatternFly In Your Application

Note:

1. Add Angular-PatternFly as dependencies for your project and you'll receive all the libraries you'll need:
    ```shell
    $ npm install angular-patternfly --save
    ```
2. Add the core Patternfly CSS and script includes to your HTML file(s):

    Please see:  https://github.com/patternfly/patternfly/blob/master/QUICKSTART.md

    Alternatively, the minimum you will need:

       <!-- PatternFly Styles -->
       <!-- Note: No other CSS files are needed regardless of what other JS packages located in patternfly/components that you decide to pull in -->
       <link rel="stylesheet" href="node_modules/angular-patternfly/node_modules/patternfly/dist/css/patternfly.min.css">
       <link rel="stylesheet" href="node_modules/angular-patternfly/node_modules/patternfly/dist/css/patternfly-additions.min.css">

       <!-- Patternfly required settings (no jquery or further JS dependencies required by this include) -->
       <script src="node_modules/angular-patternfly/node_modules/patternfly/dist/js/patternfly_settings.min.js"></script>


3. Add the following CSS include to your HTML file(s):

    ```html
    <!-- Angular-PatternFly Styles -->
    <link rel="stylesheet" href="node_modules/angular-patternfly/dist/styles/angular-patternfly.min.css" />
    ```
4. Add the following script includes to your HTML file(s), adjusting where necessary to pull in only what you need:

    ```html
    <!-- Angular -->
    <script src="node_modules/angular-patternfly/node_modules/angular/angular.min.js"></script>

    <!-- Bootstrap-Select (Optional): The following lines are only required if you use the pfBootstrapSelect directive -->
    <script src="node_modules/bootstrap/dist/js/bootstrap.min.js"></script>
    <script src="node_modules/bootstrap-select/js/bootstrap-select.js"></script>

    <!-- Angular-Bootstrap -->
    <script src="node_modules/angular-patternfly/node_modules/angular-ui-bootstrap/dist/ui-bootstrap.js"></script>
    <script src="node_modules/angular-patternfly/node_modules/angular-ui-bootstrap/dist/ui-bootstrap-tpls.js"></script>

    <!-- Angular-Sanitize -->
    <script src="node_modules/angular-patternfly/node_modules/angular-sanitize/angular-sanitize.min.js"></script>
    
    <!-- Angular-Animate -->
    <script src="node_modules/angular-patternfly/node_modules/angular-animate/angular-animate.js"></script>

    <!-- Angular-PatternFly  -->
    <script src="node_modules/angular-patternfly/dist/angular-patternfly.min.js"></script>

    <!-- Lodash -->
    <script src="node_modules/angular-patternfly/node_modules/lodash/lodash.min.js"></script>
    ```

5. (optional) The 'patternfly.charts' module is not a dependency in the default angular 'patternfly' module.
   In order to use patternfly charts you must add it as a dependency in your application:

    ```javascript
    my-app.module.js:

    angular.module('myApp', [
     'patternfly',
     'patternfly.charts'
    ]);
    ```

    And script includes to your HTML file:
    ```html
    <!-- C3, D3 - Charting Libraries. -->
    <script src="node_modules/angular-patternfly/node_modules/patternfly/node_modules/c3/c3.min.js"></script>
    <script src="node_modules/angular-patternfly/node_modules/patternfly/node_modules/d3/d3.min.js"></script>
    ````

6. (optional) The 'patternfly.table' module is not a dependency in the default angular 'patternfly' module.
   In order to use pfTableView, you must add 'patternfly.table' as a dependency in your application:

    ```javascript
    my-app.module.js:

    angular.module('myApp', [
     'patternfly',
     'patternfly.table'
    ]);
    ```

   Add the npm dependency:
    ```shell
    $ npm install angularjs-datatables --save
    ```

   Add the following CSS includes to your HTML file(s):


    ```html
    <!-- Place before any patternfly css -->
    <link rel="stylesheet" href="node_modules/angular-patternfly/node_modules/datatables.net-dt/css/jquery.dataTables.css" />
    ```
   Add the following Javascript includes to your HTML file(s):

   ```html
   <script src="node_modules/angular-patternfly/node_modules/patternfly/node_modules/jquery/dist/jquery.js"></script>
   <script src="node_modules/angular-patternfly/node_modules/patternfly/node_modules/datatables.net/js/jquery.dataTables.js"></script>
   <script src="node_modules/angular-patternfly/node_modules/patternfly/node_modules/datatables.net-select/js/dataTables.select.js"></script>
   <script src="node_modules/angularjs-datatables/dist/angular-datatables.min.js"></script>
   <script src="node_modules/angularjs-datatables/dist/plugins/select/angular-datatables.select.min.js"></script>
   ```
7. (optional) The 'patternfly.canvas' module is not a dependency in the default angular 'patternfly' module.
   In order to use pfCanvasEditor or pfCanvas, you must add 'patternfly.canvas' as a dependency in your application:

    ```javascript
    my-app.module.js:

    angular.module('myApp', [
     'patternfly',
     'patternfly.canvas'
    ]);
    ```

   Add the npm dependencies:
   ```shell
   $ npm install jquery-ui-dist --save
   $ npm install angular-dragdrop --save
   $ npm install angular-svg-base-fix --save
   ```

   Add the following Javascript includes to your HTML file(s):

   ```html
    <!-- jquery before angular.js -->
    <script src="node_modules/angular-patternfly/node_modules/patternfly/node_modules/jquery/dist/jquery.js"></script>
    <script src="node_modules/jquery-ui-dist/jquery-ui.js"></script>

    <!-- angular-dragdrop and angular-svg-base-fix after angular.js -->
    <script src="node_modules/angular-dragdrop/src/angular-dragdrop.js"></script>
    <script src="node_modules/angular-svg-base-fix/src/svgBaseFix.js"></script>
    ```

   Also, the canvas background grid image is located in 'node_modules/angular-patternfly/dist/imgs/canvas-dot-grid.png'
   please copy this image to your application's main images directory and reference it by overridding the '.canvas' css
   class:

    ```html
    <style>
     .canvas {
       background-image: url('/myapp/imgs/canvas-dot-grid.png');
       background-repeat: repeat;
     }
    </style>
    ```

#### Less to Sass Conversion

During the build process Less files are converted to Sass files in `/dist/sass`.  Then the Sass files are compiled into `/dist/sass/angular-patternfly.css`. If you would like to copy the Sass generated css into the main `/dist/styles` directory, execute:

```
grunt build --sass
```

This task will copy `/dist/sass/angular-patternfly.css` to `/dist/styles/angular-patternfly.css`.  Then the build process will minimize the css in `/dist/styles`.

The Less to Sass Conversion step will be accomplished and managed as a part of any Pull Request which includes Less file changes. Although contributors may want to build and test their style changes with Sass before submitting a Pull Request, this step should always be tested and validated by reviewers before a style change is merged and released. If a contributor is having issues with Sass conversion that they cannot resolve, Pull Request reviewers will need to ensure that the Sass conversion step is successfully accomplished, tested, and included in the Pull Request before it is approved and merged.

For more detailed information, please read [PatternFly Less to Sass Conversion](https://github.com/patternfly/patternfly#less-to-sass-conversion)

*Note:* When a Less file is added/deleted/renamed it needs to be updated in the main Less import file `/styles/angular-patternfly.less` and the main Sass import file `styles/_angular-patternfly.scss`.

### Using with Webpack

In order to use Angular-Patternfly in a Webpack-bundled application there are some things you need to keep in mind:

#### Create an alias for the jQuery module (if using JQuery dependency)

In order to let Webpack find the correct jQuery module when assembling all the dependencies you need to create an alias for it in the webpack.conf.js file:
```
...
resolve: {
  alias: {
    "jquery": "patternfly/node_modules/jquery"
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
$ npm run uidocs
```

If you're interested in reading the docs right away, you can use special target, which will start a web server:
```shell
$ npm run uidocs:view
```

After executing this tasks you'll be able to access the documentation at [http://localhost:8000/](http://localhost:8000/). If a different port is desired, it may be specified on as an option:
```shell
$ npm run uidocs:view -- --port=8002
```

## Git Commit Guidelines

PatternFly uses a semantic release process to automate npm and bower package publishing, based on the following commit message format.

Each commit message consists of a **header**, a **body** and a **footer**.  The header has a special
format that includes a **type**, a **scope** and a **subject** ([full explanation](https://github.com/stevemao/conventional-changelog-angular/blob/master/convention.md)):

```
<type>(<scope>): <subject>
<BLANK LINE>
<body>
<BLANK LINE>
<footer>
```

##### Patch Release

```
fix(pencil): stop graphite breaking when too much pressure applied
// Specific example using the component name as the scope
fix(pfEmptyState): add missing closing <span> tag
```

##### Feature Release

```
feat(pencil): add 'graphiteWidth' option
// Specific example using the component name as the scope
feat(pfNotificationDrawer): add empty state message to group without notifications
```

##### Breaking Release

```
perf(pencil): remove graphiteWidth option
// Specific example using the component name as the scope
perf(pfFakeComponent): remove pfFakeComponent
```

##### Non-Release

chore(pencil): rename internal graphiteDensity variable
chore(pfNotificationDrawer):  rename internal drawer expanded variable

## Contributing

We're always interested in contributions from the community.

Please ensure that your PR provides the following:

* Detailed description of the proposed changes
* Follows the style rules for [javascript](eslint.yaml) and [html](.htmlhintrc).
* Rebased onto the latest master commit
* If you would like to become a maintainer, please see our [Contributing Guide](contributing.md)

### Unit Testing Required

Applying a unit test, or an update to a unit test, is a contribution requirement.

If you're unfamiliar with Angular unit testing, or just need a refresher, here
are the overall [Angular 1x guidelines](https://docs.angularjs.org/guide/unit-testing).

You can access the Angular PatternFly unit test ```spec``` files under the ```test``` directory.

To get started, some basic guidelines:
* Provide a clear statement of what the component does. This encompasses what is expected, and what is produced.
* The component has features, be clear and concise on what is expected, and what is produced from each.

### Browser Support

Since PatternFly is based on Bootstrap, [PatternFly generally supports the same browsers](http://www.patternfly.org/get-started/frequently-asked-questions/), see also
[Bootstrap browser support](http://getbootstrap.com/getting-started/#support).
