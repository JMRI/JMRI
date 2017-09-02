# Angular svg base fix

A small directive for fixing SVG ``xlink:href`` within a document with a base tag

Description of the problem — [#8934](https://github.com/angular/angular.js/issues/8934)

### Installation

via npm:

```
npm install angular-svg-base-fix
```

via bower:

```
bower install angular-svg-base-fix
```

### How to use

Just add this module to your angular app module declaration

```javascript
angular
  .module('myApp', ['svgBaseFix'])
  .config(function($locationProvider) {
    $locationProvider.html5Mode(true);
  });
```

Use svg like you normally would:

```html
<svg>
  <use xlink:href="#icon-name"></use>
</svg>
```
