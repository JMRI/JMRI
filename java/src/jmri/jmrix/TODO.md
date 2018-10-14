Work still to be done on multi-connection support

(June 2016, reviewed Oct 2017)
- [ ] Follow the deprecation report on the various instance() methods and convert calls to those to multi-connection versions

- [ ] Once not invoked, remove the instance() methods themselves

- These systems might have non-deprecated instance() methods, which means they aren't migrated to multi-connection:
  - [x] easydcc (migrated 4.9.6)
  - [ ] grapevine
  - [ ] maple
  - [ ] oaktree
  - [ ] pricom
  - [ ] qsi
  - [ ] rps
  - [ ] secsi
  - [ ] tams (because under construction when the instance() methods were deprecated
  
- [ ] remote setInstance from AbstractMRTrafficController, but first set it 'final' to flush out and remove implementations


