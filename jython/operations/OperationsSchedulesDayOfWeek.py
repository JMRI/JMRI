# Sample script showing how to active the actual day of the week for OperationsPro
#
# Part of the JMRI distribution
#
# Author: Daniel Boudreau copyright 2025
#

import jmri

class trainScheduleDayOfWeek(jmri.jmrit.automat.AbstractAutomaton) : 
  def init(self):
    # Used to convert the number to a day name, must match OperationsPro train schedule names
    self.days = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
    # get the train schedule manager
    self.tsm = jmri.InstanceManager.getDefault(jmri.jmrit.operations.trains.schedules.TrainScheduleManager)
    
    return
    

  def handle(self):
    from datetime import date
    today = date.today()
    day_of_week_number = today.weekday()

    day_of_week_name = self.days[day_of_week_number]

    print('Today''s date: {}'.format(today))
    print('Day of the week number: {}'.format(day_of_week_number))
    print('Day of the week name: {}'.format(day_of_week_name))
    
    schedule = self.tsm.getScheduleByName(day_of_week_name)
    if not schedule == None:
      self.tsm.setTrainScheduleActiveId(schedule.getId())
      active = self.tsm.getActiveSchedule()
      print('Active schedule name: {}'.format(active.getName()))
    else:
      print('Train schedule {} not found'.format(day_of_week_name))
    
    return False              # all done, don't repeat again

trainScheduleDayOfWeek().start() # create one of these, and start it running
