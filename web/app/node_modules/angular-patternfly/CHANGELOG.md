## 4.0.0 (YYYY-MM-DD) *not released yet*

Enhancements
- Updated lodash dependency to 4.x
- Updated angular-bootstrap dependency to 2.3.x
- Dropped 1.3.x support for dependencies: angular, angular-animate, angular-sanitize, and angular-mocks
- Removed jQuery dependency
  - Switched to using "lib/patternfly/dist/js/patternfly-settings.js"
  - Removed directives: pfSelect, pfDatepicker, pfDateTimepicker

Bug Fixes
- Update layout for sort, filter, and toolbar to match patternfly markup


Breaking Changes
- pfInlineNotification - pfNotificationRemove function added which ties the click event of the close button to a user specified function.  Previously, this used to be hardcoded to use the Notifications service, this is now optional.
- pfListView - If defined, actionButton class will replace 'btn-default'. Previously it was appended.  (Issue #434) 
