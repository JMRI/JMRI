@webtest
Feature: JMRI Web Server 

Scenario Outline: Basic Http requests
   Given I am using <browser>
   When I ask for the url <PageURL>
   Then a page with title <PageTitle> is returned

   @firefox
   Examples: Firefox Tests
   | browser | PageURL | PageTitle |
   | firefox | http://localhost:12080/       | My JMRI Railroad |
   | firefox | http://localhost:12080/index.html | My JMRI Railroad |
   | firefox | http://localhost:12080/panel | Panels \| My JMRI Railroad |
   | firefox | http://localhost:12080/roster | Roster \| My JMRI Railroad |
   | firefox | http://localhost:12080/operations | Trains \| My JMRI Railroad |
   | firefox | http://localhost:12080/frame | Panels \| My JMRI Railroad |
   | firefox | http://localhost:12080/prefs/ | Directory: /prefs/ \| My JMRI Railroad |
   | firefox | http://localhost:12080/dist/ | Directory: /dist/ \| My JMRI Railroad |
   | firefox | http://localhost:12080/images/ | Directory: /images/ \| My JMRI Railroad |
   | firefox | http://localhost:12080/xml/signals/ | Directory: /xml/signals/ \| My JMRI Railroad |
   | firefox | http://localhost:12080/about/ | About JMRI |

   @chrome
   Examples: Chrome Tests
   | browser | PageURL | PageTitle |
   | chrome  | http://localhost:12080/       | My JMRI Railroad |
   | chrome  | http://localhost:12080/index.html | My JMRI Railroad |
   | chrome  | http://localhost:12080/panel | Panels \| My JMRI Railroad |
   | chrome  | http://localhost:12080/roster | Roster \| My JMRI Railroad |
   | chrome  | http://localhost:12080/operations | Trains \| My JMRI Railroad |
   | chrome  | http://localhost:12080/frame | Panels \| My JMRI Railroad |
   | chrome  | http://localhost:12080/prefs/ | Directory: /prefs/ \| My JMRI Railroad |
   | chrome  | http://localhost:12080/dist/ | Directory: /dist/ \| My JMRI Railroad |
   | chrome  | http://localhost:12080/images/ | Directory: /images/ \| My JMRI Railroad |
   | chrome  | http://localhost:12080/xml/signals/ | Directory: /xml/signals/ \| My JMRI Railroad |
   | chrome  | http://localhost:12080/about/ | About JMRI |
