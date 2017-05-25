describe('pf-utils', function () {

  var merged, config, defaultConfig, pfUtils;

  beforeEach(module('patternfly.utils'));
  beforeEach(inject(function ($injector) {
    pfUtils = $injector.get('pfUtils');
  }));

  beforeEach(function() {
    defaultConfig = {
      'units'    : 'GB',
      'legend'   : {'show':false},
      'color'    : {'pattern':['blue','green']},
      'tooltipFn': function () {return 'tooltip for defaultConfig'},
      'donutDeepCp'    : {
        "label": {"show":false}
      },
      'onlyInDefaultConfig': {
        'columns': {'used': 5}
      }
    };

    config = {
      'units': 'MHz',
      'legend'   : {'show':true},
      'color'    : {'pattern':['blue','red']},
      'tooltipFn': function () {return 'tooltip for config'},
      'donutDeepCp'    : {
        "label": {"show": true}
      },
      'onlyInConfig': {
        'columns': {'used': 50}
      }
    };
  });

  it('extendDeep should merge objects', function () {
    merged = pfUtils.mergeDeep(defaultConfig, config);
    runExpects();

  });

  it('angular merge should merge objects', function () {
    merged = pfUtils.angularMerge(defaultConfig, config);
    runExpects();
  });

  it('_merge should merge objects', function () {
    merged = pfUtils._merge(defaultConfig, config);
    runExpects();
  });

  it('$extend should merge objects', function () {
    merged = pfUtils.$extend(defaultConfig, config);
    runExpects();
  });

  var runExpects = function () {
    expect(merged.units).toBe(config.units);
    expect(config.units).toBe('MHz');
    expect(defaultConfig.units).toBe('GB');

    expect(merged.legend.show).toBe(config.legend.show);
    expect(config.legend.show).toBe(true);
    expect(defaultConfig.legend.show).toBe(false);

    expect(merged.color.pattern[1]).toBe(config.color.pattern[1]);
    expect(config.color.pattern[1]).toBe('red');
    expect(defaultConfig.color.pattern[1]).toBe('green');

    expect(merged.tooltipFn()).toBe('tooltip for config');
    expect(config.tooltipFn()).toBe('tooltip for config');
    expect(defaultConfig.tooltipFn()).toBe('tooltip for defaultConfig');

    expect(merged.tooltipFn()).toBe('tooltip for config');
    expect(config.tooltipFn()).toBe('tooltip for config');
    expect(defaultConfig.tooltipFn()).toBe('tooltip for defaultConfig');

    expect(merged.donutDeepCp.label.show).toBe(config.donutDeepCp.label.show);
    expect(config.donutDeepCp.label.show).toBe(true);
    expect(defaultConfig.donutDeepCp.label.show).toBe(false);

    expect(merged.onlyInDefaultConfig.columns.used).toBe(defaultConfig.onlyInDefaultConfig.columns.used);
    expect(merged.onlyInConfig.columns.used).toBe(config.onlyInConfig.columns.used);
    expect(config.onlyInDefaultConfig).not.toBeDefined();
    expect(config.onlyInConfig.columns.used).toBe(50);
    expect(defaultConfig.onlyInConfig).not.toBeDefined();
    expect(defaultConfig.onlyInDefaultConfig.columns.used).toBe(5);
  };

});
