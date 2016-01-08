class ADexamples1:

# DEFAULT DATA FOR EXAMPLE PANELS ==========================
    # Used if no setting file is found
    # Settings are chosen on the basis of layout Title
    examples = {}
    exampleTrains = {}

    examples["JavaOne remake"] = (
                                  '2.0 Beta', ('CCW', 'CW'), 'N1', 'W', 1000, 0, 1, 0, 1, 2, 2, 30000
                                  , 1, ['BLACK', 'BLUE', 'RED', 'YELLOW', 'ORANGE', 'MAGENTA', 'CYAN']
                                  , 1, [['E', '', 'CCW-CW', 'HEccw', 'HEcw', '', ''], ['N1', '', ''
                                  , 'HN1ccw', 'HN1cw', '', ''], ['N2', '', '', 'HN2ccw', 'HN2cw', ''
                                  , ''], ['S1', '', '', 'HS1ccw', 'HS1cw', '', ''], ['S2', '', ''
                                  , 'HS2ccw', 'HS2cw', '', ''], ['W', '', 'CCW-CW', 'HWccw', 'HWcw', ''
                                  , '']], [['Ea', 'STOP', '', 'SAFE', '', '', '', '', 'ALLOCATE', ''
                                  , '', '', ''], ['Eb', '', '', '', '', 'BRAKE', '', '', '', '', '', ''
                                  , ''], ['Ec', '', '', '', '', '', '', '', '', '', '', 'BRAKE', ''], [
                                  'Ed', '', 'ALLOCATE', '', '', '', '', 'STOP', '', 'SAFE', '', '', '']
                                  , ['N1a', 'STOP', '', 'SAFE', '', '', '', '', 'ALLOCATE', '', '', ''
                                  , ''], ['N1b', '', '', '', '', 'BRAKE', '', '', '', '', '', '', '']
                                  , ['N1c', '', '', '', '', '', '', '', '', '', '', 'BRAKE', ''], [
                                  'N1d', '', 'ALLOCATE', '', '', '', '', 'STOP', '', 'SAFE', '', '', ''
                                  ], ['N2a', 'STOP', '', 'SAFE', '', '', '', '', 'ALLOCATE', '', '', ''
                                  , ''], ['N2b', '', '', '', '', 'BRAKE', '', '', '', '', '', '', '']
                                  , ['N2c', '', '', '', '', '', '', '', '', '', '', 'BRAKE', ''], [
                                  'N2d', '', 'ALLOCATE', '', '', '', '', 'STOP', '', 'SAFE', '', '', ''
                                  ], ['S1a', 'STOP', '', 'SAFE', '', '', '', '', 'ALLOCATE', '', '', ''
                                  , ''], ['S1b', '', '', '', '', 'BRAKE', '', '', '', '', '', '', '']
                                  , ['S1c', '', '', '', '', '', '', '', '', '', '', 'BRAKE', ''], [
                                  'S1d', '', 'ALLOCATE', '', '', '', '', 'STOP', '', 'SAFE', '', '', ''
                                  ], ['S2a', 'STOP', '', 'SAFE', '', '', '', '', 'ALLOCATE', '', '', ''
                                  , ''], ['S2b', '', '', '', '', 'BRAKE', '', '', '', '', '', '', '']
                                  , ['S2c', '', '', '', '', '', '', '', '', '', '', 'BRAKE', ''], [
                                  'S2d', '', 'ALLOCATE', '', '', '', '', 'STOP', '', 'SAFE', '', '', ''
                                  ], ['Wa', 'STOP', '', 'SAFE', '', '', '', '', 'ALLOCATE', '', '', ''
                                  , ''], ['Wb', '', '', '', '', 'BRAKE', '', '', '', '', '', '', ''], [
                                  'Wc', '', '', '', '', '', '', '', '', '', '', 'BRAKE', ''], ['Wd', ''
                                  , 'ALLOCATE', '', '', '', '', 'STOP', '', 'SAFE', '', '', '']], 1, 1
                                  , 0, 25.4, 0, 1, 0, ['Min.', 'Low', 'Med.', 'High', 'Max.'], 2, 10, 0
                                  , 2, 1, [['Stop', -1, -1, 0], ['Clear', -1, -1, 3]], 1, [['Single '
                                  'Head', [['Red'], ['Green']], [-1, -1]]], [['HXbccw', 'Single Head'
                                  , ['HXbccw']], ['HWcw', 'Single Head', ['HWcw']], ['HEcw', 'Single '
                                  'Head', ['HEcw']], ['HS1cw', 'Single Head', ['HS1cw']], ['HSWcw'
                                  , 'Single Head', ['HSWcw']], ['HSEcw', 'Single Head', ['HSEcw']], [
                                  'HNWccw', 'Single Head', ['HNWccw']], ['HSWccw', 'Single Head', [
                                  'HSWccw']], ['HWccw', 'Single Head', ['HWccw']], ['HN1cw', 'Single '
                                  'Head', ['HN1cw']], ['HNWcw', 'Single Head', ['HNWcw']], ['HXacw'
                                  , 'Single Head', ['HXacw']], ['HNEcw', 'Single Head', ['HNEcw']], [
                                  'HNEccw', 'Single Head', ['HNEccw']], ['HN1ccw', 'Single Head', [
                                  'HN1ccw']], ['HSEccw', 'Single Head', ['HSEccw']], ['HS2cw', 'Single '
                                  'Head', ['HS2cw']], ['HS1ccw', 'Single Head', ['HS1ccw']], ['HEccw'
                                  , 'Single Head', ['HEccw']], ['HN2ccw', 'Single Head', ['HN2ccw']], [
                                  'HS2ccw', 'Single Head', ['HS2ccw']], ['HXaccw', 'Single Head', [
                                  'HXaccw']], ['HN2cw', 'Single Head', ['HN2cw']], ['HXbcw', 'Single '
                                  'Head', ['HXbcw']]], 0, 0, 0, 1, 20000, 0, 1, 1, 1270, 3, 0, [['Bell'
                                  , 'resources/sounds/bell.wav']], [1, 1, 1, 1, 1, 1]
                                  , '', 12.0, 0.0, 87.0, [], 1.0)

    exampleTrains["JavaOne remake"] = [
        ['T1017', 'N1', 'CCW', 0, '(3([N1 N2] [S1 S2]) $P25)', 0, 889.0, [0, 1
        , 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0, 2, 0, 1, 0, 0, 0, 1, 3
        , 1, 7, 0, 1, 0, 10, 1, 0, 3, 1, 9], '1017', 0, [], 'Auto', 'N1', [1
        , 2, 3, 4, 5], {}], ['T1019', 'S1', 'CW', 0, '(2([S1 S2] [N1 N2]) '
        '$P15)', 0, 508.0, [0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0
        , 2, 0, 1, 0, 0, 0, 1, 2, 1, 7, 0, 1, 0, 10, 1, 0, 2, 1, 9], '1019'
        , 0, [], 'Auto', 'S1', [1, 2, 3, 4, 5], {}]]

    examples["Operations Example"] = (
                                      '2.0 Beta', ['EAST', 'WEST'], 'Sv1', 'SvFr', 1000, 0, 2, 0, 1, 2, 2
                                      , 0, 1, ['BLACK', 'BLUE', 'RED', 'YELLOW', 'ORANGE', 'MAGENTA'
                                      , 'CYAN'], 1, [['Bf1', '', '', 'HBf1west', 'HBf1east', '', 'BFman']
                                      , ['Bf2', '', '', 'HBf2west', 'HBf2east', '', 'BFman'], ['BfPa', ''
                                      , 'EAST-WEST', 'HBfPawest', 'HBfPaeast', '', ''], ['Dv1', '', ''
                                      , 'HDv1west', 'HDv1east', '', 'DVman'], ['Dv2', '', '', 'HDv2west'
                                      , 'HDv2east', '', 'DVman'], ['DvHb', '', 'EAST-WEST', 'HDvHbwest'
                                      , 'HDvHbeast', '', ''], ['Fa1', '', '', 'HFa1west', 'HFa1east', ''
                                      , 'FAman'], ['Fa2', '', '', 'HFa2west', 'HFa2east', '', 'FAman'], [
                                      'FaLv1', '', 'EAST-WEST+', 'HFaLv1west', 'HFaLv1east', '', ''], [
                                      'FaLv2', '', 'EAST-WEST+', 'HFaLv2west', 'HFaLv2east', '', ''], [
                                      'FaLv3', '', 'EAST-WEST+', 'HFaLv3west', 'HFaLv3east', '', ''], [
                                      'Fr1', '', '', 'HFr1west', 'HFr1east', '', 'FRman'], ['Fr2', '', ''
                                      , 'HFr2west', 'HFr2east', '', 'FRman'], ['FrBf', '', 'EAST-WEST'
                                      , 'HFrBfwest', 'HFrBfeast', '', ''], ['Hb1', '', '', 'HHb1west'
                                      , 'HHb1east', '', 'HBman'], ['Hb2', '', '', 'HHb2west', 'HHb2east'
                                      , '', 'HBman'], ['HbFa', '', 'EAST-WEST', 'HHbFawest', 'HHbFaeast'
                                      , '', ''], ['Lv1', '', '', 'HLv1west', 'HLv1cw', '', 'LVman'], ['Lv2'
                                      , '', '', 'HLv2west', 'HLv2cw', '', 'LVman'], ['Pa1', '', ''
                                      , 'HPa1west', 'HPa1east', '', 'PAman'], ['Pa2', '', '', 'HPa2west'
                                      , 'HPa2east', '', 'PAman'], ['PaDv', '', 'EAST-WEST', 'HPaDvwest'
                                      , 'HPaDveast', '', ''], ['Sv1', '', '', 'HSv1ccw', 'HSv1east', ''
                                      , 'SVman'], ['Sv2', '', '', 'HSv2ccw', 'HSv2east', '', 'SVman'], [
                                      'SvFr', '', 'EAST-WEST', 'HSvFrwest', 'HSvFreast', '', '']], [['Bf1a'
                                      , 'STOP', '', 'SAFE', '', '', '', '', 'ALLOCATE', '', '', '', ''], [
                                      'Bf1b', '', '', '', '', 'BRAKE', '', '', '', '', '', '', ''], ['Bf1c'
                                      , '', '', '', '', '', '', '', '', '', '', 'BRAKE', ''], ['Bf1d', ''
                                      , 'ALLOCATE', '', '', '', '', 'STOP', '', 'SAFE', '', '', ''], [
                                      'Bf2a', 'STOP', '', 'SAFE', '', '', '', '', 'ALLOCATE', '', '', ''
                                      , ''], ['Bf2b', '', '', '', '', 'BRAKE', '', '', '', '', '', '', '']
                                      , ['Bf2c', '', '', '', '', '', '', '', '', '', '', 'BRAKE', ''], [
                                      'Bf2d', '', 'ALLOCATE', '', '', '', '', 'STOP', '', 'SAFE', '', ''
                                      , ''], ['BfPa1', 'STOP', '', 'SAFE', '', '', '', '', 'ALLOCATE', ''
                                      , '', '', ''], ['BfPa2', '', '', '', '', 'BRAKE', '', '', '', '', ''
                                      , '', ''], ['BfPa3', '', '', '', '', '', '', '', '', '', '', '', '']
                                      , ['BfPa4', '', '', '', '', '', '', '', '', '', '', 'BRAKE', ''], [
                                      'BfPa5', '', 'ALLOCATE', '', '', '', '', 'STOP', '', 'SAFE', '', ''
                                      , ''], ['Dv1a', 'STOP', '', 'SAFE', '', '', '', '', 'ALLOCATE', ''
                                      , '', '', ''], ['Dv1b', '', '', '', '', 'BRAKE', '', '', '', '', ''
                                      , '', ''], ['Dv1c', '', '', '', '', '', '', '', '', '', '', 'BRAKE'
                                      , ''], ['Dv1d', '', 'ALLOCATE', '', '', '', '', 'STOP', '', 'SAFE'
                                      , '', '', ''], ['Dv2a', 'STOP', '', 'SAFE', '', '', '', ''
                                      , 'ALLOCATE', '', '', '', ''], ['Dv2b', '', '', '', '', 'BRAKE', ''
                                      , '', '', '', '', '', ''], ['Dv2c', '', '', '', '', '', '', '', ''
                                      , '', '', 'BRAKE', ''], ['Dv2d', '', 'ALLOCATE', '', '', '', ''
                                      , 'STOP', '', 'SAFE', '', '', ''], ['DvHb1', 'STOP', '', 'SAFE', ''
                                      , '', '', '', 'ALLOCATE', '', '', '', ''], ['DvHb2', '', '', '', ''
                                      , 'BRAKE', '', '', '', '', '', '', ''], ['DvHb3', '', '', '', '', ''
                                      , '', '', '', '', '', '', ''], ['DvHb4', '', '', '', '', '', '', ''
                                      , '', '', '', 'BRAKE', ''], ['DvHb5', '', 'ALLOCATE', '', '', '', ''
                                      , 'STOP', '', 'SAFE', '', '', ''], ['Fa1a', 'STOP', '', 'SAFE', ''
                                      , '', '', '', 'ALLOCATE', '', '', '', ''], ['Fa1b', '', '', '', ''
                                      , 'BRAKE', '', '', '', '', '', '', ''], ['Fa1c', '', '', '', '', ''
                                      , '', '', '', '', '', 'BRAKE', ''], ['Fa1d', '', 'ALLOCATE', '', ''
                                      , '', '', 'STOP', '', 'SAFE', '', '', ''], ['Fa2a', 'STOP', ''
                                      , 'SAFE', '', '', '', '', 'ALLOCATE', '', '', '', ''], ['Fa2b', ''
                                      , '', '', '', 'BRAKE', '', '', '', '', '', '', ''], ['Fa2c', '', ''
                                      , '', '', '', '', '', '', '', '', 'BRAKE', ''], ['Fa2d', ''
                                      , 'ALLOCATE', '', '', '', '', 'STOP', '', 'SAFE', '', '', ''], [
                                      'FaLv1', 'STOP', '', 'SAFE', '', '', '', '', 'ALLOCATE', '', '', ''
                                      , ''], ['FaLv2', '', '', '', '', 'BRAKE', '', '', '', '', '', '', '']
                                      , ['FaLv3', '', '', '', '', '', '', '', '', '', '', '', ''], ['FaLv4'
                                      , '', '', '', '', '', '', '', '', '', '', 'BRAKE', ''], ['FaLv5', ''
                                      , 'ALLOCATE', '', '', '', '', 'STOP', '', 'SAFE', '', '', ''], [
                                      'LaFv6', 'STOP', '', 'SAFE', '', '', '', '', 'ALLOCATE', '', '', ''
                                      , ''], ['FaLv7', '', '', '', '', 'BRAKE', '', '', '', '', '', '', '']
                                      , ['FaLv8', '', '', '', '', '', '', '', '', '', '', '', ''], ['FaLv9'
                                      , '', '', '', '', '', '', '', '', '', '', 'BRAKE', ''], ['FaLv10', ''
                                      , 'ALLOCATE', '', '', '', '', 'STOP', '', 'SAFE', '', '', ''], [
                                      'FaLv11', 'STOP', '', 'SAFE', '', '', '', '', 'ALLOCATE', '', '', ''
                                      , ''], ['FaLv12', '', '', '', '', 'BRAKE', '', '', '', '', '', '', ''
                                      ], ['FaLv13', '', '', '', '', '', '', '', '', '', '', '', ''], [
                                      'FaLv14', '', '', '', '', '', '', '', '', '', '', 'BRAKE', ''], [
                                      'FaLv15', '', 'ALLOCATE', '', '', '', '', 'STOP', '', 'SAFE', '', ''
                                      , ''], ['Fr1a', 'STOP', '', 'SAFE', '', '', '', '', 'ALLOCATE', ''
                                      , '', '', ''], ['Fr1b', '', '', '', '', 'BRAKE', '', '', '', '', ''
                                      , '', ''], ['Fr1c', '', '', '', '', '', '', '', '', '', '', 'BRAKE'
                                      , ''], ['Fr1d', '', 'ALLOCATE', '', '', '', '', 'STOP', '', 'SAFE'
                                      , '', '', ''], ['Fr2a', 'STOP', '', 'SAFE', '', '', '', ''
                                      , 'ALLOCATE', '', '', '', ''], ['Fr2b', '', '', '', '', 'BRAKE', ''
                                      , '', '', '', '', '', ''], ['Fr2c', '', '', '', '', '', '', '', ''
                                      , '', '', 'BRAKE', ''], ['Fr2d', '', 'ALLOCATE', '', '', '', ''
                                      , 'STOP', '', 'SAFE', '', '', ''], ['FrBf1', 'STOP', '', 'SAFE', ''
                                      , '', '', '', 'ALLOCATE', '', '', '', ''], ['FrBf2', '', '', '', ''
                                      , 'BRAKE', '', '', '', '', '', '', ''], ['FrBf3', '', '', '', '', ''
                                      , '', '', '', '', '', '', ''], ['FrBf4', '', '', '', '', '', '', ''
                                      , '', '', '', 'BRAKE', ''], ['FrBf5', '', 'ALLOCATE', '', '', '', ''
                                      , 'STOP', '', 'SAFE', '', '', ''], ['Hb1a', 'STOP', '', 'SAFE', ''
                                      , '', '', '', 'ALLOCATE', '', '', '', ''], ['Hb1b', '', '', '', ''
                                      , 'BRAKE', '', '', '', '', '', '', ''], ['Hb1c', '', '', '', '', ''
                                      , '', '', '', '', '', 'BRAKE', ''], ['Hb1d', '', 'ALLOCATE', '', ''
                                      , '', '', 'STOP', '', 'SAFE', '', '', ''], ['Hb2a', 'STOP', ''
                                      , 'SAFE', '', '', '', '', 'ALLOCATE', '', '', '', ''], ['Hb2b', ''
                                      , '', '', '', 'BRAKE', '', '', '', '', '', '', ''], ['Hb2c', '', ''
                                      , '', '', '', '', '', '', '', '', 'BRAKE', ''], ['Hb2d', ''
                                      , 'ALLOCATE', '', '', '', '', 'STOP', '', 'SAFE', '', '', ''], [
                                      'HbFa1', 'STOP', '', 'SAFE', '', '', '', '', 'ALLOCATE', '', '', ''
                                      , ''], ['HbFa2', '', '', '', '', 'BRAKE', '', '', '', '', '', '', '']
                                      , ['HbFa3', '', '', '', '', '', '', '', '', '', '', '', ''], ['HbFa4'
                                      , '', '', '', '', '', '', '', '', '', '', 'BRAKE', ''], ['HbFa5', ''
                                      , 'ALLOCATE', '', '', '', '', 'STOP', '', 'SAFE', '', '', ''], [
                                      'Lv1a', 'STOP', '', 'SAFE', '', '', '', '', 'ALLOCATE', '', '', ''
                                      , ''], ['Lv1b', '', '', '', '', 'BRAKE', '', '', '', '', '', '', '']
                                      , ['Lv1c', '', '', '', '', '', '', '', '', '', '', 'BRAKE', ''], [
                                      'Lv1d', '', 'ALLOCATE', '', '', '', '', 'STOP', '', 'SAFE', '', ''
                                      , ''], ['Lv2a', 'STOP', '', 'SAFE', '', '', '', '', 'ALLOCATE', ''
                                      , '', '', ''], ['Lv2b', '', '', '', '', 'BRAKE', '', '', '', '', ''
                                      , '', ''], ['Lv2c', '', '', '', '', '', '', '', '', '', '', 'BRAKE'
                                      , ''], ['Lv2d', '', 'ALLOCATE', '', '', '', '', 'STOP', '', 'SAFE'
                                      , '', '', ''], ['Pa1a', 'STOP', '', 'SAFE', '', '', '', ''
                                      , 'ALLOCATE', '', '', '', ''], ['Pa1b', '', '', '', '', 'BRAKE', ''
                                      , '', '', '', '', '', ''], ['Pa1c', '', '', '', '', '', '', '', ''
                                      , '', '', 'BRAKE', ''], ['Pa1d', '', 'ALLOCATE', '', '', '', ''
                                      , 'STOP', '', 'SAFE', '', '', ''], ['Pa2a', 'STOP', '', 'SAFE', ''
                                      , '', '', '', 'ALLOCATE', '', '', '', ''], ['Pa2b', '', '', '', ''
                                      , 'BRAKE', '', '', '', '', '', '', ''], ['Pa2c', '', '', '', '', ''
                                      , '', '', '', '', '', 'BRAKE', ''], ['Pa2d', '', 'ALLOCATE', '', ''
                                      , '', '', 'STOP', '', 'SAFE', '', '', ''], ['PaDv1', 'STOP', ''
                                      , 'SAFE', '', '', '', '', 'ALLOCATE', '', '', '', ''], ['PaDv2', ''
                                      , '', '', '', 'BRAKE', '', '', '', '', '', '', ''], ['PaDv3', '', ''
                                      , '', '', '', '', '', '', '', '', '', ''], ['PaDv4', '', '', '', ''
                                      , '', '', '', '', '', '', 'BRAKE', ''], ['PaDv5', '', 'ALLOCATE', ''
                                      , '', '', '', 'STOP', '', 'SAFE', '', '', ''], ['Sv1a', 'STOP', ''
                                      , 'SAFE', '', '', '', '', 'ALLOCATE', '', '', '', ''], ['Sv1b', ''
                                      , '', '', '', 'BRAKE', '', '', '', '', '', '', ''], ['Sv1c', '', ''
                                      , '', '', '', '', '', '', '', '', 'BRAKE', ''], ['Sv1d', ''
                                      , 'ALLOCATE', '', '', '', '', 'STOP', '', 'SAFE', '', '', ''], [
                                      'Sv2a', 'STOP', '', 'SAFE', '', '', '', '', 'ALLOCATE', '', '', ''
                                      , ''], ['Sv2b', '', '', '', '', 'BRAKE', '', '', '', '', '', '', '']
                                      , ['Sv2c', '', '', '', '', '', '', '', '', '', '', 'BRAKE', ''], [
                                      'Sv2d', '', 'ALLOCATE', '', '', '', '', 'STOP', '', 'SAFE', '', ''
                                      , ''], ['SvFr1', 'STOP', '', 'SAFE', '', '', '', '', 'ALLOCATE', ''
                                      , '', '', ''], ['SvFr2', '', '', '', '', 'BRAKE', '', '', '', '', ''
                                      , '', ''], ['SvFr3', '', '', '', '', '', '', '', '', '', '', '', '']
                                      , ['SvFr4', '', '', '', '', '', '', '', '', '', '', 'BRAKE', ''], [
                                      'SvFr5', '', 'ALLOCATE', '', '', '', '', 'STOP', '', 'SAFE', '', ''
                                      , '']], 1, 1, 0, 25.4, 0, 0, 0, ['Min.', 'Low', 'Med.', 'High'
                                      , 'Max.'], 3, 10, 0, 2, 1, [['Stop', -1, -1, 0], ['Clear', -1, -1, 5]
                                      , ['Approach', 0, -1, 3]], 1, [['Single Head', [['Red'], ['Green'], [
                                      'Yellow']], [-1, -1, -1]]], [['HFaLv3west', 'Single Head', [
                                      'HFaLv3west']], ['HDvHbccw', 'Single Head', ['HDvHbccw']], [
                                      'HFr1east', 'Single Head', ['HFr1east']], ['HFaLveast', 'Single Head'
                                      , ['HFaLveast']], ['HHbFacw', 'Single Head', ['HHbFacw']], [
                                      'HFaLv1east', 'Single Head', ['HFaLv1east']], ['HSv2east', 'Single '
                                      'Head', ['HSv2east']], ['HDv2west', 'Single Head', ['HDv2west']], [
                                      'HBf1west', 'Single Head', ['HBf1west']], ['HHb1east', 'Single Head'
                                      , ['HHb1east']], ['HHbFawest', 'Single Head', ['HHbFawest']], [
                                      'HSvFrccw', 'Single Head', ['HSvFrccw']], ['HFa2east', 'Single Head'
                                      , ['HFa2east']], ['HFr2west', 'Single Head', ['HFr2west']], [
                                      'HBfPawest', 'Single Head', ['HBfPawest']], ['HSvFreast', 'Single '
                                      'Head', ['HSvFreast']], ['HFaLv2west', 'Single Head', ['HFaLv2west']]
                                      , ['HFaLvccw', 'Single Head', ['HFaLvccw']], ['HSv1east', 'Single '
                                      'Head', ['HSv1east']], ['HDv1west', 'Single Head', ['HDv1west']], [
                                      'HHb2west', 'Single Head', ['HHb2west']], ['HLv2east', 'Single Head'
                                      , ['HLv2east']], ['HBfPaccw', 'Single Head', ['HBfPaccw']], [
                                      'HFa1east', 'Single Head', ['HFa1east']], ['HFrBfccw', 'Single Head'
                                      , ['HFrBfccw']], ['HFr1west', 'Single Head', ['HFr1west']], [
                                      'HFaLvwest', 'Single Head', ['HFaLvwest']], ['HSv1ccw', 'Single Head'
                                      , ['HSv1ccw']], ['HPa2east', 'Single Head', ['HPa2east']], [
                                      'HFaLv1west', 'Single Head', ['HFaLv1west']], ['HFaLvcw', 'Single '
                                      'Head', ['HFaLvcw']], ['HSv2west', 'Single Head', ['HSv2west']], [
                                      'HSv2ccw', 'Single Head', ['HSv2ccw']], ['HFrBfeast', 'Single Head'
                                      , ['HFrBfeast']], ['HFrBfcw', 'Single Head', ['HFrBfcw']], [
                                      'HPaDveast', 'Single Head', ['HPaDveast']], ['HSvFrcw', 'Single Head'
                                      , ['HSvFrcw']], ['HHb1west', 'Single Head', ['HHb1west']], ['HSv2cw'
                                      , 'Single Head', ['HSv2cw']], ['HDvHbeast', 'Single Head', [
                                      'HDvHbeast']], ['HFa2west', 'Single Head', ['HFa2west']], ['HSv1cw'
                                      , 'Single Head', ['HSv1cw']], ['HLv1east', 'Single Head', ['HLv1east'
                                      ]], ['HSvFrwest', 'Single Head', ['HSvFrwest']], ['HPa1east', 'Single'
                                      ' Head', ['HPa1east']], ['HBf2east', 'Single Head', ['HBf2east']], [
                                      'HHbFaccw', 'Single Head', ['HHbFaccw']], ['HSv1west', 'Single Head'
                                      , ['HSv1west']], ['HBfPacw', 'Single Head', ['HBfPacw']], ['HDvHbcw'
                                      , 'Single Head', ['HDvHbcw']], ['HFaLv3east', 'Single Head', [
                                      'HFaLv3east']], ['HLv2west', 'Single Head', ['HLv2west']], [
                                      'HFa1west', 'Single Head', ['HFa1west']], ['HDv2east', 'Single Head'
                                      , ['HDv2east']], ['HPa2west', 'Single Head', ['HPa2west']], [
                                      'HPaDvcw', 'Single Head', ['HPaDvcw']], ['HBf1east', 'Single Head', [
                                      'HBf1east']], ['HFrBfwest', 'Single Head', ['HFrBfwest']], ['HLv2cw'
                                      , 'Single Head', ['HLv2cw']], ['HPaDvwest', 'Single Head', [
                                      'HPaDvwest']], ['HHbFaeast', 'Single Head', ['HHbFaeast']], ['HLv1cw'
                                      , 'Single Head', ['HLv1cw']], ['HFr2east', 'Single Head', ['HFr2east'
                                      ]], ['HBfPaeast', 'Single Head', ['HBfPaeast']], ['HDvHbwest'
                                      , 'Single Head', ['HDvHbwest']], ['HFaLv2east', 'Single Head', [
                                      'HFaLv2east']], ['HLv1west', 'Single Head', ['HLv1west']], [
                                      'HDv1east', 'Single Head', ['HDv1east']], ['HPa1west', 'Single Head'
                                      , ['HPa1west']], ['HBf2west', 'Single Head', ['HBf2west']], [
                                      'HHb2east', 'Single Head', ['HHb2east']], ['HPaDvccw', 'Single Head'
                                      , ['HPaDvccw']]], 0, 0, 0, 1, 60000, 0, 1, 1, 2032, 3, 0, [['Bell'
                                      , 'resources/sounds/bell.wav']], [1, 1, 1, 1, 1, 1]
                                      , '', 0.0, 0.0, 87.0, [['Lakeview', 'Lv1 Lv2'], ['Hillsboro',
                                      'Hb1 Hb2'], ['Fremont', 'Fr1 Fr2'], ['Port Arthur', 'Pa1 Pa2'],
                                      ['Danville', 'Dv1 Dv2'], ['Farmington', 'Fa1 Fa2'], ['Bakersfield',
                                      'Bf1 Bf2'], ['Susanville', 'Sv1 Sv2']], 1.0)

    exampleTrains["Operations Example"] = []

    examples["Reversing Track Example"] = (
                                           '2.0 Beta', ('CCW', 'CW'), 'B1', 'B4', 1000, 0, 1, 0, 1, 2, 2, 0, 1
                                           , ['BLACK', 'BLUE', 'RED', 'YELLOW', 'ORANGE', 'MAGENTA', 'CYAN'], 1
                                           , [['B1', '', '', 'HB1cw', 'HB1ccw', '', 'B1man'], ['B2', '', ''
                                           , 'HB2cw', 'HB2ccw', '', 'B2man'], ['B3', '', 'CCW-CW', 'HB3cw'
                                           , 'HB3ccw', '', ''], ['B4', '', 'CCW-CW', 'HB4cw', 'HB4ccw', '', '']
                                           , ['B5', '', '', 'HB5cw', 'HB5ccw', 'INVERTED', 'B5man'], ['B6', ''
                                           , '', 'HB6cw', 'HB6ccw', '', ''], ['B7', '', '', 'HB7cw', 'HB7ccw'
                                           , '', 'B7man']], [['B1a', 'STOP', '', 'SAFE', '', '', '', ''
                                           , 'ALLOCATE', '', '', '', ''], ['B1b', '', '', '', '', 'BRAKE', ''
                                           , '', '', '', '', '', ''], ['B1c', '', '', '', '', '', '', '', '', ''
                                           , '', 'BRAKE', ''], ['B1d', '', 'ALLOCATE', '', '', '', '', 'STOP'
                                           , '', 'SAFE', '', '', ''], ['B2a', 'STOP', '', 'SAFE', '', '', '', ''
                                           , 'ALLOCATE', '', '', '', ''], ['B2b', '', '', '', '', 'BRAKE', ''
                                           , '', '', '', '', '', ''], ['B2c', '', '', '', '', '', '', '', '', ''
                                           , '', 'BRAKE', ''], ['B2d', '', 'ALLOCATE', '', '', '', '', 'STOP'
                                           , '', 'SAFE', '', '', ''], ['B3a', 'STOP', '', 'SAFE', '', '', '', ''
                                           , 'ALLOCATE', '', '', '', ''], ['B3b', '', '', '', '', 'BRAKE', ''
                                           , '', '', '', '', '', ''], ['B3c', '', '', '', '', '', '', '', '', ''
                                           , '', '', ''], ['B3d', '', '', '', '', '', '', '', '', '', ''
                                           , 'BRAKE', ''], ['B3e', '', 'ALLOCATE', '', '', '', '', 'STOP', ''
                                           , 'SAFE', '', '', ''], ['B4a', 'STOP', '', 'SAFE', '', '', '', ''
                                           , 'ALLOCATE', '', '', '', ''], ['B4b', '', '', '', '', 'BRAKE', ''
                                           , '', '', '', '', 'BRAKE', ''], ['B4c', '', 'ALLOCATE', '', '', ''
                                           , '', 'STOP', '', 'SAFE', '', '', ''], ['B5a', 'STOP', '', 'SAFE', ''
                                           , '', '', '', 'ALLOCATE', '', '', '', ''], ['B5b', '', '', '', ''
                                           , 'BRAKE', '', '', '', '', '', 'BRAKE', ''], ['B5c', '', 'ALLOCATE'
                                           , '', '', '', '', 'STOP', '', 'SAFE', '', '', ''], ['B6a', 'STOP', ''
                                           , 'SAFE', '', '', '', '', 'ALLOCATE', '', '', '', ''], ['B6b', '', ''
                                           , '', '', 'BRAKE', '', '', '', '', '', 'BRAKE', ''], ['B6c', ''
                                           , 'ALLOCATE', '', '', '', '', 'STOP', '', 'SAFE', '', '', ''], ['B7e'
                                           , 'STOP', '', 'SAFE', '', '', '', '', 'ALLOCATE', '', '', '', ''], [
                                           'B7d', '', '', '', '', 'BRAKE', '', '', '', '', '', '', ''], ['B7c'
                                           , '', '', '', '', '', '', '', '', '', '', '', ''], ['B7b', '', '', ''
                                           , '', '', '', '', '', '', '', 'BRAKE', ''], ['B7a', '', 'ALLOCATE'
                                           , '', '', '', '', 'STOP', '', 'SAFE', '', '', '']], 1, 1, 0, 25.4, 0
                                           , 1, 0, ['Min.', 'Low', 'Med.', 'High', 'Max.'], 3, 10, 0, 2, 1, [[
                                           'Stop', -1, -1, 0], ['Clear', -1, -1, 5]], 1, [['Single Head', [[
                                           'Red'], ['Green']], [-1, -1]]], [['HB2ccw', 'Single Head', ['HB2ccw']
                                           ], ['HB4cw', 'Single Head', ['']], ['HB5ccw', 'Single Head', [
                                           'HB5ccw']], ['HB1ccw', 'Single Head', ['HB1ccw']], ['HB5cw', 'Single '
                                           'Head', ['HB5cw']], ['HB4ccw', 'Single Head', ['']], ['HB1cw'
                                           , 'Single Head', ['HB1cw']], ['HB6cw', 'Single Head', ['HB6cw']], [
                                           'HB7ccw', 'Single Head', ['HB7ccw']], ['HB2cw', 'Single Head', [
                                           'HB2cw']], ['HB3ccw', 'Single Head', ['']], ['HB7cw', 'Single Head'
                                           , ['HB7cw']], ['HB3cw', 'Single Head', ['']], ['HB6ccw', 'Single '
                                           'Head', ['HB6ccw']]], 0, 0, 0, 1, 60000, 0, 1, 1, 2032, 3, 0, [[
                                           'Bell', 'resources/sounds/bell.wav']], [1, 1, 1, 1, 1, 1]
                                           , '', 0.0, 0.0, 87.0, [], 1.0)

    exampleTrains["Reversing Track Example"] = [
        ['T1017', 'B1', 'CW', 0, '(2(B6 [B1 B2]) $P10)', 0, 508.0, [0, 1, 0
        , 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0, 2, 0, 1, 0, 0, 0, 1, 2, 1
        , 3], '1017', 0, [], 'Auto', 'B1', [1, 2, 3, 4, 5], {}], ['T1633'
        , 'B2', 'CCW', 0, '(2(B6 [B1 B2]) $P15)', 0, 304.79999999999995, [0
        , 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0, 2, 0, 1, 0, 0, 0, 1
        , 2, 0, 3], '1633', 0, [], 'Auto', 'B2', [1, 2, 3, 4, 5], {}], [
        'T3023', 'B5', 'CW', 0, '(2( B7 $P10 [B1 B2] B5) $P20)', 0, 762.0, [0
        , 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0, 2, 0, 1, 0, 0, 0, 1
        , 2, 0, 3], '3023', 0, [], 'Auto', 'B5', [1, 2, 3, 4, 5], {}]]
        
class ADexamples2:

# DEFAULT DATA FOR EXAMPLE PANELS ==========================
# Examples are divided into two different classes to avoid exceeding Jython 64K limit
    examples = {}
    exampleTrains = {}

    examples["SignalMasts Example"] = (
                                       '2.0 Beta', ('CCW', 'CW'), 'B30', 'B29', 1500, 0, 2, 0, 2, 2, 2
                                       , 30000, 1, ['BLACK', 'BLUE', 'RED', 'YELLOW', 'ORANGE', 'MAGENTA'
                                       , 'CYAN'], 1, [['B01', '', '', 'HB01cw', 'HB01ccw', '', 'B01man'], [
                                       'B02', '', '', 'HB02cw', 'HB02ccw', '', 'B02man'], ['B03', '', ''
                                       , 'HB03cw', 'HB03ccw', '', 'B03man'], ['B04', '', '', 'HB04cw'
                                       , 'HB04ccw', '', 'B04man'], ['B05', '', '', 'HB05cw', 'HB05ccw', ''
                                       , 'B05man'], ['B06', '', '', 'HB06cw', 'HB06ccw', '', 'B11man'], [
                                       'B07', '', '', 'HB07cw', 'HB07ccw', '', 'B11man'], ['B08', '', ''
                                       , 'HB08cw', 'HB08ccw', '', 'B11man'], ['B09', '', '', 'HB09cw'
                                       , 'HB09ccw', '', 'B09man'], ['B10', '', '', 'HB10cw', 'HB10ccw', ''
                                       , 'B10man'], ['B11', '', 'CCW-CW', 'HB11cw', 'HB11ccw', '', 'B11man']
                                       , ['B12', '', 'CCW-CW', 'HB12cw', 'HB12ccw', '', 'B12man'], ['B13'
                                       , '', '', 'HB13cw', 'HB13ccw', '', 'B13man'], ['B14', '', ''
                                       , 'HB14cw', 'HB14ccw', '', 'B13man'], ['B15', '', '', 'HB15cw'
                                       , 'HB15ccw', '', 'B13man'], ['B16', '', '', 'HB16cw', 'HB16ccw', ''
                                       , 'B13man'], ['B17', '', 'CCW-CW', 'HB17cw', 'HB17ccw', '', 'B26man']
                                       , ['B18', '', 'CCW-CW', 'HB18cw', 'HB18ccw', '', 'B28man'], ['B19'
                                       , 'CCW', '', 'HB19cw', 'HB19ccw', '', 'B19man'], ['B20', 'CW', ''
                                       , 'HB20cw', 'HB20ccw', '', 'B20man'], ['B21', 'CW', '', 'HB21cw'
                                       , 'HB21ccw', '', ''], ['B22', 'CCW', '', 'HB22cw', 'HB22ccw', '', '']
                                       , ['B23', 'CW', '', 'HB23cw', 'HB23ccw', '', ''], ['B24', '', ''
                                       , 'HB24cw', 'HB24ccw', '', ''], ['B25', '', 'CW+', 'HB25cw'
                                       , 'HB25ccw', '', ''], ['B26', 'CCW', '', 'HB26cw', 'HB26ccw', ''
                                       , 'B26man'], ['B27', '', 'CCW', 'HB27cw', 'HB27ccw', '', 'B27man'], [
                                       'B28', '', 'CCW+', 'HB28cw', 'HB28ccw', '', 'B28man'], ['B29', ''
                                       , 'CCW-CW+', 'HB29cw', 'HB29ccw', '', 'B29man'], ['B30', '', ''
                                       , 'HB30cw', 'HB30ccw', '', 'B30man'], ['B31', '', '', 'HB31cw'
                                       , 'HB31ccw', '', 'B31man'], ['B32', '', '', 'HB32cw', 'HB32ccw', ''
                                       , 'B32man'], ['B33', 'CCW', 'CCW', 'HB33cw', 'HB33ccw', '', ''], [
                                       'B34', 'CW', 'CW', 'HB34cw', 'HB34ccw', '', '']], [['B1a', 'STOP', ''
                                       , 'SAFE', '', '', '', '', 'ALLOCATE', '', '30 MPH', '', ''], ['B1b'
                                       , '', '', '', '', 'BRAKE', '', '', '', '', '', '', ''], ['B1c', ''
                                       , '', '', '', '', '', '', '', '', '', 'BRAKE', ''], ['B1d', ''
                                       , 'ALLOCATE', '', '30 MPH', '', '', 'STOP', '', 'SAFE', '', '', '']
                                       , ['B2a', 'STOP', '', 'SAFE', '', '', '', '', 'ALLOCATE', '', '30 '
                                       'MPH', '', ''], ['B2b', '', '', '', '', 'BRAKE', '', '', '', '', ''
                                       , '', ''], ['B2c', '', '', '', '', '', '', '', '', '', '', 'BRAKE'
                                       , ''], ['B2d', '', 'ALLOCATE', '', '30 MPH', '', '', 'STOP', ''
                                       , 'SAFE', '', '', ''], ['B3a', 'STOP', '', 'SAFE', '', '', '', ''
                                       , 'ALLOCATE', '', '30 MPH', '', ''], ['B3b', '', '', '', '', 'BRAKE'
                                       , '', '', '', '', '', '', ''], ['B3c', '', '', '', '', '', '', '', ''
                                       , '', '', 'BRAKE', ''], ['B3d', '', 'ALLOCATE', '', '30 MPH', '', ''
                                       , 'STOP', '', 'SAFE', '', '', ''], ['B4a', 'STOP', '', 'SAFE', '', ''
                                       , '', '', 'ALLOCATE', '', '30 MPH', '', ''], ['B4b', '', '', '', ''
                                       , 'BRAKE', '', '', '', '', '', '', ''], ['B4c', '', '', '', '', ''
                                       , '', '', '', '', '', 'BRAKE', ''], ['B4d', '', 'ALLOCATE', '', '30 '
                                       'MPH', '', '', 'STOP', '', 'SAFE', '', '', ''], ['B5a', 'STOP', ''
                                       , 'SAFE', '', '', '', '', 'ALLOCATE', '', '30 MPH', '', ''], ['B5b'
                                       , '', '', '', '', 'BRAKE', '', '', '', '', '', '', ''], ['B5c', ''
                                       , '', '', '', '', '', '', '', '', '', 'BRAKE', ''], ['B5d', ''
                                       , 'ALLOCATE', '', '30 MPH', '', '', 'STOP', '', 'SAFE', '', '', '']
                                       , ['B6a', 'STOP', '', 'SAFE', '', '', '', '', 'ALLOCATE', '', ''
                                       , 'BRAKE', ''], ['B6b', '', '', '', '', 'NO', '', '', '', '', ''
                                       , 'NO', ''], ['B6c', '', 'ALLOCATE', '', '', 'BRAKE', '', 'STOP', ''
                                       , 'SAFE', '', '', ''], ['B7a', 'STOP', '', 'SAFE', '', '', '', ''
                                       , 'ALLOCATE', '', '', 'BRAKE', ''], ['B7b', '', '', '', '', 'NO', ''
                                       , '', '', '', '', 'NO', ''], ['B7c', '', 'ALLOCATE', '', '', 'BRAKE'
                                       , '', 'STOP', '', 'SAFE', '', '', ''], ['B8a', 'STOP', '', 'SAFE', ''
                                       , '', '', '', 'ALLOCATE', '', '', 'BRAKE', ''], ['B8b', '', '', ''
                                       , '', 'NO', '', '', '', '', '', 'NO', ''], ['B8c', '', 'ALLOCATE', ''
                                       , '', 'BRAKE', '', 'STOP', '', 'SAFE', '', '', ''], ['B9b', 'STOP'
                                       , '', 'SAFE', '', '', '', '', 'ALLOCATE', '', '', 'BRAKE', ''], [
                                       'B9a', '', 'ALLOCATE', '', '', 'BRAKE', '', 'STOP', '', 'SAFE', ''
                                       , '', ''], ['B10a', 'NO', '', 'NO', '', '', '', '', 'NO', '', '', ''
                                       , ''], ['B10b', 'STOP', '', 'SAFE', '', 'NO', '', '', 'ALLOCATE', ''
                                       , '', 'BRAKE', ''], ['B10c', '', 'ALLOCATE', '', '', 'BRAKE', ''
                                       , 'STOP', '', 'SAFE', '', '', ''], ['B11a', 'STOP', '', 'NO', '', ''
                                       , '', '', 'ALLOCATE', '', '', 'BRAKE', ''], ['B11b', '', 'ALLOCATE'
                                       , '', '', 'BRAKE', '', 'STOP', '', 'NO', '', '', ''], ['B11c', '', ''
                                       , '', '', '', '', '', '', '', '', '', ''], ['B12a', '', '', '', ''
                                       , '', '', '', '', '', '', '', ''], ['B12b', 'STOP', '', 'NO', '', ''
                                       , '', '', 'ALLOCATE', '', '', 'BRAKE', ''], ['B12c', '', 'ALLOCATE'
                                       , '', '', 'BRAKE', '', 'STOP', '', 'NO', '', '', ''], ['B13a', 'STOP'
                                       , '', 'SAFE', '', '', '', '', 'ALLOCATE', '', '', 'BRAKE', ''], [
                                       'B13b', '', 'ALLOCATE', '', '', 'BRAKE', '', 'STOP', '', 'SAFE', ''
                                       , '', ''], ['B14a', 'STOP', '', 'SAFE', '', '', '', '', 'ALLOCATE'
                                       , '', '', 'BRAKE', ''], ['B14b', '', 'ALLOCATE', '', '', 'BRAKE', ''
                                       , 'STOP', '', 'SAFE', '', '', ''], ['B15a', 'STOP', '', 'SAFE', ''
                                       , '', '', '', 'ALLOCATE', '', '', 'BRAKE', ''], ['B15b', ''
                                       , 'ALLOCATE', '', '', 'BRAKE', '', 'STOP', '', 'SAFE', '', '', ''], [
                                       'B16a', 'STOP', '', 'SAFE', '', '', '', '', 'ALLOCATE', '', ''
                                       , 'BRAKE', ''], ['B16b', '', 'ALLOCATE', '', '', 'BRAKE', '', 'STOP'
                                       , '', 'SAFE', '', '', ''], ['B17a', 'STOP', 'ALLOCATE', '', '30 MPH'
                                       , '', '', 'STOP', 'ALLOCATE', '', '30 MPH', '', ''], ['B18a', 'STOP'
                                       , 'ALLOCATE', '', '30 MPH', 'BRAKE', '', 'STOP', 'ALLOCATE', '', '30 '
                                       'MPH', 'BRAKE', ''], ['B19a', 'STOP', '', 'SAFE', '', '', '', ''
                                       , 'ALLOCATE', '', '', '', ''], ['B19b', '', '', '', '', 'BRAKE', ''
                                       , '', '', '', '', 'BRAKE', ''], ['B19c', '', 'ALLOCATE', '', '', ''
                                       , '', 'STOP', '', 'SAFE', '40 MPH', '', ''], ['B20a', 'STOP', ''
                                       , 'SAFE', '', '', '', '', 'ALLOCATE', '', '', '', ''], ['B20b', ''
                                       , '', '', '', 'BRAKE', '', '', '', '', '40 MPH', '', ''], ['B20c', ''
                                       , '', '', '', '', '', '', '', '', '', 'BRAKE', ''], ['B20d', ''
                                       , 'ALLOCATE', '', '40 MPH', '', '', 'STOP', '', 'SAFE', '', '', '']
                                       , ['B21a', 'STOP', '', 'SAFE', '', '', '', '', 'ALLOCATE', '', '', ''
                                       , ''], ['B21b', '', '', '', '', 'BRAKE', '', '', '', '', '', 'BRAKE'
                                       , ''], ['B21c', '', 'ALLOCATE', '', '50 MPH', '', '', 'STOP', ''
                                       , 'SAFE', '', '', ''], ['B22a', 'STOP', '', 'SAFE', '', '', '', ''
                                       , 'ALLOCATE', '', '', '', ''], ['B22b', '', '', '', '', 'BRAKE', ''
                                       , '', '', '', '', 'BRAKE', ''], ['B22c', '', 'ALLOCATE', '', '', ''
                                       , '', 'STOP', '', 'SAFE', '50 MPH', '', ''], ['B23a', 'STOP', ''
                                       , 'SAFE', '', '', '', '', 'ALLOCATE', '', '', '', ''], ['B23b', ''
                                       , '', '', '', 'BRAKE', '', '', '', '', '', 'BRAKE', ''], ['B23c', ''
                                       , 'ALLOCATE', '', '50 MPH', '', '', 'STOP', '', 'SAFE', '', '', '']
                                       , ['B24a', 'STOP', '', 'NO', '', '', '', '', 'ALLOCATE', '', '', ''
                                       , ''], ['B24b', '', '', '', '', 'BRAKE', '', '', '', '', '', '', '']
                                       , ['B24c', '', '', 'SAFE', '', '', '', '', '', 'SAFE', '40 MPH', ''
                                       , ''], ['B24d', '', '', '', '40 MPH', '', '', '', '', '', '', 'BRAKE'
                                       , ''], ['B24e', '', 'ALLOCATE', '', '', '', '', 'STOP', '', 'NO', ''
                                       , '', ''], ['B25a', 'STOP', '', 'NO', '', '', '', '', 'NO', '', ''
                                       , '', ''], ['B25b', '', '', '', '', 'BRAKE', '', '', '', '', '', ''
                                       , ''], ['B25c', '', 'ALLOCATE', 'SAFE', 'Max.', '', '', ''
                                       , 'ALLOCATE', 'SAFE', '40 MPH', '', ''], ['B25d', '', '', '', '', ''
                                       , '', '', '', '', '', 'BRAKE', ''], ['B25e', '', 'NO', '', '40 MPH'
                                       , '', '', 'STOP', '', 'NO', '', '', ''], ['B26d', 'STOP', '', 'SAFE'
                                       , '', '', '', '', 'ALLOCATE', '', '30 MPH', '', ''], ['B26c', '', ''
                                       , '', '', 'BRAKE', '', '', '', '', '', '', ''], ['B26b', '', '', ''
                                       , '', '', '', '', '', '', '', 'BRAKE', ''], ['B26a', '', 'ALLOCATE'
                                       , '', '', '', '', 'STOP', '', 'SAFE', '', '', ''], ['B27d', 'STOP'
                                       , '', 'SAFE', '', '', '', '', 'ALLOCATE', '', 'Max.', '', ''], [
                                       'B27c', '', '', '', '', 'BRAKE', '', '', '', '', '', '', ''], ['B27b'
                                       , '', '', '', '', '', '', '', '', '', '', 'BRAKE', ''], ['B27a', ''
                                       , 'ALLOCATE', '', '', '', '', 'STOP', '', 'SAFE', '', '', ''], [
                                       'B28d', 'STOP', '', 'SAFE', '', '', '', '', 'ALLOCATE', '', '', ''
                                       , ''], ['B28c', '', '', '', '', 'BRAKE', '', '', '', '', '', '', '']
                                       , ['B28b', '', '', '', '', '', '', '', '', '', '', 'BRAKE', ''], [
                                       'B28a', '', 'ALLOCATE', '', 'Max.', '', '', 'STOP', '', 'SAFE', ''
                                       , '', ''], ['B29a', 'STOP', '', 'NO', '40 MPH', '', '', ''
                                       , 'ALLOCATE', '', '', '', ''], ['B29b', '', '', 'SAFE', '', 'BRAKE'
                                       , '', '', '', '', '', '', ''], ['B29c', '', '', '', '', '', '', ''
                                       , '', 'SAFE', '', 'BRAKE', ''], ['B29d', '', 'ALLOCATE', '', '', ''
                                       , '', 'STOP', '', 'NO', '', '', ''], ['B30a', 'STOP', '', 'SAFE', ''
                                       , '', '', '', 'ALLOCATE', '', '30 MPH', '', ''], ['B30b', '', '', ''
                                       , '', 'BRAKE', '', '', '', '', '', '', ''], ['B30c', '', '', '', ''
                                       , '', '', '', '', '', '', 'BRAKE', ''], ['B30d', '', 'ALLOCATE', ''
                                       , '30 MPH', '', '', 'STOP', '', 'SAFE', '', '', ''], ['B31a', 'STOP'
                                       , '', 'SAFE', '', '', '', '', 'ALLOCATE', '', '30 MPH', '', ''], [
                                       'B31b', '', '', '', '', 'BRAKE', '', '', '', '', '', '', ''], ['B31c'
                                       , '', '', '', '', '', '', '', '', '', '', 'BRAKE', ''], ['B31d', ''
                                       , 'ALLOCATE', '', '30 MPH', '', '', 'STOP', '', 'SAFE', '', '', '']
                                       , ['B32a', 'NO', '', 'NO', '', '', '', '', 'NO', '', '', '', ''], [
                                       'B32b', 'STOP', '', 'SAFE', '', 'NO', '', '', 'ALLOCATE', '', ''
                                       , 'BRAKE', ''], ['B32c', '', 'ALLOCATE', '', '', 'BRAKE', '', 'STOP'
                                       , '', 'SAFE', '', '', ''], ['B33a', 'STOP', 'ALLOCATE', '', '', ''
                                       , '', 'STOP', 'ALLOCATE', '', '50 MPH', '', ''], ['B34a', 'STOP'
                                       , 'ALLOCATE', '', '40 MPH', '', '', 'STOP', 'ALLOCATE', '', '', ''
                                       , '']], 1, 1, 0, 1.0, 0, 1, 0, ['Min.', '30 MPH', '40 MPH', '50 MPH'
                                       , 'Max.'], 2, 10, 0, 2, 2, [['Stop', -1, -1, 0], ['Clear', -1, -1, 5]
                                       , ['Diverging Clear Limited', -1, 1, 3], ['Approach', 0, 0, 2], [
                                       'Diverging Approach', 0, 1, 2], ['Approach Limited', 2, 0, 3], [
                                       'Diverging Approach Limited', 2, 1, 4]], 1, [['Single Head', [['Red']
                                       , ['Green'], ['Yellow'], ['Flash-Green'], ['Flash-Yellow'], ['Green']
                                       , ['Green']], [-1, -1, -1, -1, -1, -1, -1]], ['Double head', [['Red'
                                       , 'Red'], ['Green', 'Red'], ['Red', 'Flash-Green'], ['Yellow', 'Red']
                                       , ['Red', 'Yellow'], ['Flash-Yellow', 'Red'], ['Red', 'Flash-Yellow']
                                       ], [-1, -1, -1, -1, -1, -1, -1]]], [['HB03ccw', 'Double head', [
                                       'HB03ccw', 'HB03ccwL']], ['HB24cw', 'Double head', ['HB24cw'
                                       , 'HB24cwL']], ['HB19cw', 'Single Head', ['']], ['HB22ccw', 'Double '
                                       'head', ['HB22ccw', 'HB22ccwL']], ['HB08ccw', 'Single Head', ['']], [
                                       'HB10cw', 'Single Head', ['']], ['HB05cw', 'Double head', ['HB05cw'
                                       , 'HB05cwL']], ['HB27ccw', 'Double head', ['HB27ccw', 'HB27ccwL']], [
                                       'HB23cw', 'Double head', ['HB23cw', 'HB23cwL']], ['HB14ccw', 'Double '
                                       'head', ['HB14ccw', 'HB14ccwL']], ['HB18cw', 'Single Head', ['']], [
                                       'HB33ccw', 'Single Head', ['']], ['HB01ccw', 'Double head', [
                                       'HB01ccw', 'HB01ccwL']], ['HB04cw', 'Double head', ['HB04cw'
                                       , 'HB04cwL']], ['HB19ccw', 'Double head', ['HB19ccw', 'HB19ccwL']], [
                                       'HB20ccw', 'Single Head', ['']], ['HB06ccw', 'Single Head', ['']], [
                                       'HB22cw', 'Single Head', ['']], ['HB17cw', 'Single Head', ['']], [
                                       'HB25ccw', 'Double head', ['HB25ccw', 'HB25ccwL']], ['HB03cw'
                                       , 'Double head', ['HB03cw', 'HB03cwL']], ['HB12ccw', 'Single Head', [
                                       '']], ['HB21cw', 'Double head', ['HB21cw', 'HB21cwL']], ['HB31ccw'
                                       , 'Double head', ['HB31ccw', 'HB31ccwL']], ['HB16cw', 'Single Head'
                                       , ['']], ['HB17ccw', 'Single Head', ['']], ['HB04ccw', 'Double head'
                                       , ['HB04ccw', 'HB04ccwL']], ['HB02cw', 'Single Head', ['']], [
                                       'HB34cw', 'Single Head', ['']], ['HB29cw', 'Double head', ['HB29cw'
                                       , 'HB29cwL']], ['HB23ccw', 'Single Head', ['']], ['HB09ccw', 'Single '
                                       'Head', ['']], ['HB20cw', 'Double head', ['HB20cw', 'HB20cwL']], [
                                       'HB15cw', 'Single Head', ['']], ['HB10ccw', 'Double head', ['HB10ccw'
                                       , 'HB10ccwL']], ['HB28ccw', 'Single Head', ['']], ['HB33cw', 'Single '
                                       'Head', ['']], ['HB01cw', 'Single Head', ['']], ['HB28cw', 'Double '
                                       'head', ['HB28cw', 'HB28cwL']], ['HB15ccw', 'Double head', ['HB15ccw'
                                       , 'HB15ccwL']], ['HB02ccw', 'Double head', ['HB02ccw', 'HB02ccwL']]
                                       , ['HB34ccw', 'Single Head', ['']], ['HB14cw', 'Single Head', ['']]
                                       , ['HB09cw', 'Double head', ['HB09cw', 'HB09cwL']], ['HB21ccw'
                                       , 'Single Head', ['']], ['HB07ccw', 'Single Head', ['']], ['HB32cw'
                                       , 'Single Head', ['']], ['HB27cw', 'Double head', ['HB27cw'
                                       , 'HB27cwL']], ['HB26ccw', 'Double head', ['HB26ccw', 'HB26ccwL']], [
                                       'HB13cw', 'Single Head', ['']], ['HB08cw', 'Double head', ['HB08cw'
                                       , 'HB08cwL']], ['HB13ccw', 'Double head', ['HB13ccw', 'HB13ccwL']], [
                                       'HB32ccw', 'Double head', ['HB32ccw', 'HB32ccwL']], ['HB31cw'
                                       , 'Double head', ['HB31cw', 'HB31cwL']], ['HB26cw', 'Single Head', [
                                       '']], ['HB18ccw', 'Single Head', ['']], ['HB05ccw', 'Double head', [
                                       'HB05ccw', 'HB05ccwL']], ['HB12cw', 'Single Head', ['']], ['HB07cw'
                                       , 'Double head', ['HB07cw', 'HB07cwL']], ['HB24ccw', 'Double head', [
                                       'HB24ccw', 'HB24ccwL']], ['HB30cw', 'Double head', ['HB30cw'
                                       , 'HB30cwL']], ['HB25cw', 'Double head', ['HB25cw', 'HB25cwL']], [
                                       'HB11ccw', 'Single Head', ['']], ['HB29ccw', 'Double head', [
                                       'HB29ccw', 'HB29ccwL']], ['HB30ccw', 'Double head', ['HB30ccw'
                                       , 'HB30ccwL']], ['HB11cw', 'Single Head', ['']], ['HB16ccw', 'Double '
                                       'head', ['HB16ccw', 'HB16ccwL']], ['HB06cw', 'Double head', ['HB06cw'
                                       , 'HB06cwL']]], 0, 1, 1, 1, 60000, 1, 1, 1, 2000, 3, 0, [['Bell'
                                       , 'resources/sounds/bell.wav']], [1, 1, 1, 1, 1, 1]
                                       , '', 0.0, 0.0, 87.0, [], 1.0)

    exampleTrains["SignalMasts Example"] = [
        ['T1017', 'B01', 'CCW', 0, '(3(B24 [B01 B02]) $P30 [B30 B31] [B01 '
        'B02])', 0, 650.0, [0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0
        , 10, 0, 1, 0, 13, 1, 0, 0, 0, 12], '1017', 0, [], 'Auto', 'B01', [1
        , 2, 2, 3, 4], {}], ['T1019', 'B02', 'CCW', 0, '(3(B24 [B02 B01]) '
        '$P30) ', 0, 680.0, [0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0
        , 0, 2, 0, 1, 0, 0, 0, 1, 3, 0, 3], '1019', 0, [], 'Auto', 'B02', [1
        , 2, 3, 4, 5], {}], ['T1633', 'B05', 'CW', 0, '(2(B20 $H:HB26ccw B24 '
        '$R:HB26ccw [B05 B04 B03]) $P15 [B31 B30] $P10 [B05 B04 B03])', 0
        , 650.0, [0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0, 14, 0, 1
        , 0, 17, 1, 0, 0, 0, 16], '1633', 0, [], 'Auto', 'B05', [1, 2, 3, 4
        , 5], {}], ['T1641', 'B30', 'CCW', 0, '([B02 B03 B01] B24 2([B03 B02 '
        'B01] B30) $P10)', 0, 410.0, [0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0
        , 0, 1, 0, 0, 2, 0, 1, 0, 6, 1, 0, 0, 0, 4], '1641', 0, [], 'Auto'
        , 'B30', [1, 2, 3, 4, 5], {}], ['T3023', 'B31', 'CW', 0, '(2([B05 B04'
        ' B03] B31) $P25 [B05 B04 B03] B24)', 0, 490.0, [0, 1, 0, 0, 0, 0, 0
        , 0, 1, 0, 1, 0, 0, 0, 1, 0, 0, 2, 0, 1, 0, 0, 0, 1, 2, 1, 3, 0, 1, 0
        , 7, 1, 0, 2, 1, 5], '3023', 0, [], 'Auto', 'B31', [1, 2, 3, 4, 5], {
        }], ['T3029', 'B06', 'CW', 0, '($SWOFF $CW 2(B24 [B04 B03]) [B31 B30]'
        ' [B04 B03] B20 $SWON $H:HB06cw $R:HB07cw $CCW B06 $D3 $OFF:F0)', 0
        , 810.0, [0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0, 4, 0, 1
        , 0, 0, 0, 1, 2, 0, 6, 0, 1, 0, 9, 1, 0, 2, 0, 8], '3029', 0, []
        , 'Auto', 'B06', [1, 2, 3, 4, 5], {}], ['T4802', 'B07', 'CW', 0
        , '($SWOFF $CW 3(B24 [B05 B04 B03]) B20 $SWON $H:HB07cw $R:HB06cw '
        '$CCW B07 $D3 $OFF:F0)', 0, 250.0, [0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1
        , 0, 0, 0, 1, 0, 0, 4, 0, 1, 0, 0, 0, 1, 3, 0, 6, 0, 1, 0, 10, 1, 0
        , 3, 0, 8], '4802', 1, [], 'Auto', 'B07', [1, 2, 3, 4, 5], {}], [
        'T4805', 'B32', 'CCW', 0, '($SWOFF $CCW [B02 B03 B01] B24 [B02 B03 '
        'B01] [B31 B30] $SWON $H:HB32ccw B29 $CW B32 $D3 $OFF:F0)', 0, 380.0
        , [0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 1, 4, 0, 1, 0, 8
        , 1, 0, 0, 1, 6], '4805', 0, [], 'Auto', 'B32', [1, 2, 3, 4, 5], {}]]

    examples["Xing Example"] = (
                                '2.0 Beta', ('CCW', 'CW'), 'N1', 'W', 1000, 0, 1, 0, 1, 2, 2, 30000
                                , 1, ['BLACK', 'BLUE', 'RED', 'YELLOW', 'ORANGE', 'MAGENTA', 'CYAN']
                                , 1, [['E', '', 'CCW-CW', 'HEcw', 'HEccw', '', ''], ['N1', '', ''
                                , 'HN1cw', 'HN1ccw', '', ''], ['N2', '', '', 'HN2cw', 'HN2ccw', ''
                                , ''], ['NE', '', '', 'HNEcw', 'HNEccw', '', ''], ['NW', '', ''
                                , 'HNWcw', 'HNWccw', '', ''], ['S1', '', '', 'HS1cw', 'HS1ccw', ''
                                , ''], ['S2', '', '', 'HS2cw', 'HS2ccw', '', ''], ['SE', '', ''
                                , 'HSEcw', 'HSEccw', '', ''], ['SW', '', '', 'HSWcw', 'HSWccw'
                                , 'INVERTED', ''], ['W', '', 'CCW-CW', 'HWcw', 'HWccw', '', ''], [
                                'Xa', '', 'CCW-CW', 'HXacw', 'HXaccw', '', ''], ['Xb', '', 'CCW-CW'
                                , 'HXbcw', 'HXbccw', '', '']], [['Ed', 'STOP', '', 'SAFE', '', '', ''
                                , '', 'ALLOCATE', '', '', '', ''], ['Ec', '', '', '', '', 'BRAKE', ''
                                , '', '', '', '', '', ''], ['Eb', '', '', '', '', '', '', '', '', ''
                                , '', 'BRAKE', ''], ['Ea', '', 'ALLOCATE', '', '', '', '', 'STOP', ''
                                , 'SAFE', '', '', ''], ['N1d', 'STOP', '', 'SAFE', '', '', '', ''
                                , 'ALLOCATE', '', '', '', ''], ['N1c', '', '', '', '', 'BRAKE', ''
                                , '', '', '', '', '', ''], ['N1b', '', '', '', '', '', '', '', '', ''
                                , '', 'BRAKE', ''], ['N1a', '', 'ALLOCATE', '', '', '', '', 'STOP'
                                , '', 'SAFE', '', '', ''], ['N2d', 'STOP', '', 'SAFE', '', '', '', ''
                                , 'ALLOCATE', '', '', '', ''], ['N2c', '', '', '', '', 'BRAKE', ''
                                , '', '', '', '', '', ''], ['N2b', '', '', '', '', '', '', '', '', ''
                                , '', 'BRAKE', ''], ['N2a', '', 'ALLOCATE', '', '', '', '', 'STOP'
                                , '', 'SAFE', '', '', ''], ['NEc', 'STOP', '', 'NO', '', '', '', ''
                                , 'ALLOCATE', '', '', '', ''], ['NEb', '', '', '', '', 'BRAKE', ''
                                , '', '', '', '', 'BRAKE', ''], ['NEa', '', 'ALLOCATE', '', '', ''
                                , '', 'STOP', '', 'NO', '', '', ''], ['NWc', 'STOP', '', 'NO', '', ''
                                , '', '', 'ALLOCATE', '', '', '', ''], ['NWb', '', '', '', ''
                                , 'BRAKE', '', '', '', '', '', 'BRAKE', ''], ['NWa', '', 'ALLOCATE'
                                , '', '', '', '', 'STOP', '', 'NO', '', '', ''], ['S1d', 'STOP', ''
                                , 'SAFE', '', '', '', '', 'ALLOCATE', '', '', '', ''], ['S1c', '', ''
                                , '', '', 'BRAKE', '', '', '', '', '', '', ''], ['S1b', '', '', ''
                                , '', '', '', '', '', '', '', 'BRAKE', ''], ['S1a', '', 'ALLOCATE'
                                , '', '', '', '', 'STOP', '', 'SAFE', '', '', ''], ['S2d', 'STOP', ''
                                , 'SAFE', '', '', '', '', 'ALLOCATE', '', '', '', ''], ['S2c', '', ''
                                , '', '', 'BRAKE', '', '', '', '', '', '', ''], ['S2b', '', '', ''
                                , '', '', '', '', '', '', '', 'BRAKE', ''], ['S2a', '', 'ALLOCATE'
                                , '', '', '', '', 'STOP', '', 'SAFE', '', '', ''], ['SEa', 'STOP', ''
                                , 'NO', '', '', '', '', 'ALLOCATE', '', '', '', ''], ['SEb', '', ''
                                , '', '', 'BRAKE', '', '', '', '', '', 'BRAKE', ''], ['SEc', ''
                                , 'ALLOCATE', '', '', '', '', 'STOP', '', 'NO', '', '', ''], ['SWa'
                                , 'STOP', '', 'NO', '', '', '', '', 'ALLOCATE', '', '', '', ''], [
                                'SWb', '', '', '', '', 'BRAKE', '', '', '', '', '', 'BRAKE', ''], [
                                'SWc', '', 'ALLOCATE', '', '', '', '', 'STOP', '', 'NO', '', '', '']
                                , ['Wd', 'STOP', '', 'SAFE', '', '', '', '', 'ALLOCATE', '', '', ''
                                , ''], ['Wc', '', '', '', '', 'BRAKE', '', '', '', '', '', '', ''], [
                                'Wb', '', '', '', '', '', '', '', '', '', '', 'BRAKE', ''], ['Wa', ''
                                , 'ALLOCATE', '', '', '', '', 'STOP', '', 'SAFE', '', '', ''], ['Xa'
                                , 'STOP', 'ALLOCATE', '', '', '', '', 'STOP', 'ALLOCATE', '', '', ''
                                , ''], ['Xb', 'STOP', 'ALLOCATE', '', '', '', '', 'STOP', 'ALLOCATE'
                                , '', '', '', '']], 1, 1, 0, 25.4, 0, 1, 0, ['Min.', 'Low', 'Med.'
                                , 'High', 'Max.'], 3, 10, 0, 2, 1, [['Stop', -1, -1, 0], ['Clear', -1
                                , -1, 5]], 1, [['Single Head', [['Red'], ['Green']], [-1, -1]]], [[
                                'HXbccw', 'Single Head', ['HXbccw']], ['HWcw', 'Single Head', ['HWcw'
                                ]], ['HEcw', 'Single Head', ['HEcw']], ['HS1cw', 'Single Head', [
                                'HS1cw']], ['HSWcw', 'Single Head', ['HSWcw']], ['HSEcw', 'Single '
                                'Head', ['HSEcw']], ['HNWccw', 'Single Head', ['HNWccw']], ['HSWccw'
                                , 'Single Head', ['HSWccw']], ['HWccw', 'Single Head', ['HWccw']], [
                                'HN1cw', 'Single Head', ['HN1cw']], ['HNWcw', 'Single Head', ['HNWcw'
                                ]], ['HXacw', 'Single Head', ['HXacw']], ['HNEcw', 'Single Head', [
                                'HNEcw']], ['HNEccw', 'Single Head', ['HNEccw']], ['HN1ccw', 'Single '
                                'Head', ['HN1ccw']], ['HSEccw', 'Single Head', ['HSEccw']], ['HS2cw'
                                , 'Single Head', ['HS2cw']], ['HEccw', 'Single Head', ['HEccw']], [
                                'HS1ccw', 'Single Head', ['HS1ccw']], ['HN2ccw', 'Single Head', [
                                'HN2ccw']], ['HS2ccw', 'Single Head', ['HS2ccw']], ['HN2cw', 'Single '
                                'Head', ['HN2cw']], ['HXaccw', 'Single Head', ['HXaccw']], ['HXbcw'
                                , 'Single Head', ['HXbcw']]], 0, 0, 0, 1, 20000, 0, 1, 1, 1778, 3, 0
                                , [['Bell', 'resources/sounds/bell.wav']], [1, 1, 1, 1, 1, 1]
                                , '', 12.0, 0.0, 87.0, [])

    exampleTrains["Xing Example"] = [
        ['T1017', 'S1', 'CCW', 0, '([N1 N2] [S1 S2] $P10)', 0, 889.0, [0, 1, 0
        , 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0, 2, 0, 1, 0, 5, 1, 0, 0, 0
        , 4], '1017', 0, [], 'Auto', 'S1', [1, 2, 3, 4, 5], {}], ['T1019', 'N1'
        , 'CW', 0, '([S1 S2] $P15 [N1 N2])', 0, 1143.0, [0, 1, 0, 0, 0, 0, 0
        , 0, 1, 0, 1, 0, 0, 0, 1, 0, 0, 2, 0, 1, 0, 5, 1, 0, 0, 0, 4], '1019'
        , 0, [], 'Auto', 'N1', [1, 2, 3, 4, 5], {}], ['T1633', 'NE', 'CW', 0
        , '(SE NW [S1 S2] [N1 N2] SW NE [S1 S2] [N1 N2] )', 0
        , 355.6, [0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0
        , 0, 14, 0, 1, 0, 17, 1, 0, 0, 0, 16], '1633', 0, [], 'Auto', 'NE', [
        1, 2, 3, 4, 5], {}], ['T1641', 'SW', 'CW', 0, '(SW SE)', 0, 203.2, [0
        , 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0, 3], '1641', 0, []
        , 'Auto', 'SW', [1, 2, 3, 4, 5], {}]]

    examples["Xover Example"] = (
                                 '2.0 Beta', ('CCW', 'CW'), 'B1', 'B6', 1000, 0, 1, 1, 1, 2, 2, 30000
                                 , 1, ['BLACK', 'BLUE', 'RED', 'YELLOW', 'ORANGE', 'MAGENTA', 'CYAN']
                                 , 1, [['B1', '', '', 'HB1ccw', 'HB1cw', '', ''], ['B2', '', ''
                                 , 'HB2ccw', 'HB2cw', '', ''], ['B3', '', '', 'HB3ccw', 'HB3cw', ''
                                 , ''], ['B4', '', '', 'HB4ccw', 'HB4cw', '', ''], ['B5', ''
                                 , 'CCW-CW+', 'HB5ccw', 'HB5cw', '', ''], ['B6', '', 'CCW-CW+'
                                 , 'HB6ccw', 'HB6cw', '', '']], [['B1a', 'STOP', '', 'SAFE', '', ''
                                 , '', '', 'ALLOCATE', '', '', '', ''], ['B1b', '', '', '', ''
                                 , 'BRAKE', '', '', '', '', '', 'BRAKE', ''], ['B1c', '', 'ALLOCATE'
                                 , '', '', '', '', 'STOP', '', 'SAFE', '', '', ''], ['B2a', 'STOP', ''
                                 , 'SAFE', '', '', '', '', 'ALLOCATE', '', '', '', ''], ['B2b', '', ''
                                 , '', '', 'BRAKE', '', '', '', '', '', 'BRAKE', ''], ['B2c', ''
                                 , 'ALLOCATE', '', '', '', '', 'STOP', '', 'SAFE', '', '', ''], ['B3a'
                                 , 'STOP', '', 'SAFE', '', '', '', '', 'ALLOCATE', '', '', '', ''], [
                                 'B3b', '', '', '', '', 'BRAKE', '', '', '', '', '', 'BRAKE', ''], [
                                 'B3c', '', 'ALLOCATE', '', '', '', '', 'STOP', '', 'SAFE', '', '', ''
                                 ], ['B4a', 'STOP', '', 'SAFE', '', '', '', '', 'ALLOCATE', '', '', ''
                                 , ''], ['B4b', '', '', '', '', 'BRAKE', '', '', '', '', '', 'BRAKE'
                                 , ''], ['B4c', '', 'ALLOCATE', '', '', '', '', 'STOP', '', 'SAFE', ''
                                 , '', ''], ['B5a', 'STOP', '', 'SAFE', '', '', '', '', 'ALLOCATE', ''
                                 , '', '', ''], ['B5b', '', '', '', '', 'BRAKE', '', '', '', '', ''
                                 , '', ''], ['B5c', '', '', '', '', '', '', '', '', '', '', 'BRAKE'
                                 , ''], ['B5d', '', 'ALLOCATE', '', '', '', '', 'STOP', '', 'SAFE', ''
                                 , '', ''], ['B6a', 'STOP', '', 'SAFE', '', '', '', '', 'ALLOCATE', ''
                                 , '', '', ''], ['B6b', '', '', '', '', 'BRAKE', '', '', '', '', ''
                                 , '', ''], ['B6c', '', '', '', '', '', '', '', '', '', '', 'BRAKE'
                                 , ''], ['B6d', '', 'ALLOCATE', '', '', '', '', 'STOP', '', 'SAFE', ''
                                 , '', '']], 1, 1, 0, 25.4, 0, 1, 0, ['Min.', 'Low', 'Med.', 'High'
                                 , 'Max.'], 3, 10, 0, 2, 1, [['Stop', -1, -1, 0], ['Clear', -1, -1, 5]
                                 ], 1, [['Single Head', [['Red'], ['Green']], [-1, -1]]], [['HB2ccw'
                                 , 'Single Head', ['HB2ccw']], ['HB4cw', 'Single Head', ['HB4cw']], [
                                 'HB5ccw', 'Single Head', ['HB5ccw']], ['HB1ccw', 'Single Head', [
                                 'HB1ccw']], ['HB5cw', 'Single Head', ['HB5cw']], ['HB4ccw', 'Single '
                                 'Head', ['HB4ccw']], ['HB1cw', 'Single Head', ['HB1cw']], ['HB6cw'
                                 , 'Single Head', ['HB6cw']], ['HB3ccw', 'Single Head', ['HB3ccw']], [
                                 'HB2cw', 'Single Head', ['HB2cw']], ['HB3cw', 'Single Head', ['HB3cw'
                                 ]], ['HB6ccw', 'Single Head', ['HB6ccw']]], 0, 0, 0, 1, 60000, 0, 1
                                 , 1, 2032, 3, 0, [['Bell', 'resources/sounds/bell.wav']], [1, 1, 1, 1
                                 , 1, 1], '', 0.0, 0.0, 87.0, [], 1.0)

    exampleTrains["Xover Example"] = [
        ['T1017', 'B1', 'CCW', 0, '(2([B3 B4] [B1 B2]) $P15)', 0
        , 304.79999999999995, [0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0
        , 0, 2, 0, 1, 0, 0, 0, 1, 2, 1, 3, 0, 1, 0, 6, 1, 0, 2, 1, 5], '1017'
        , 0, [], 'Auto', 'B1', [1, 2, 3, 4, 5], {}], ['T1019', 'B2', 'CW', 0
        , '(2([B3 B4] [B1 B2]) $P20)', 0, 609.5999999999999, [0, 1, 0, 0, 0
        , 0, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0, 2, 0, 1, 0, 0, 0, 1, 2, 1, 3, 0
        , 1, 0, 6, 1, 0, 2, 1, 5], '1019', 0, [], 'Auto', 'B2', [1, 2, 3, 4
        , 5], {}]]
                        
# MAIN PROGRAMM ==============
    
