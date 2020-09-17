import {autoinject, PLATFORM} from "aurelia-framework";
import {Router, RouterConfiguration} from 'aurelia-router';
import {StatisticValues} from "./services/statistic-values";

@autoinject()
export class App {

  router: Router;


  constructor(
    private statisticValues: StatisticValues
  ) {

  }

  attached() {
    this.statisticValues.createMergeClassStatistics().then(value => {
      console.log(value);
    })
  }


  configureRouter(config: RouterConfiguration, router: Router): void {
    this.router = router;
    config.title = 'Testerra Report';
    config.map([
      {
        route: '',
        name: 'Dashboard',
        moduleId: PLATFORM.moduleName('components/dashboard'),
        nav: true,
        title: 'Dashboard'
      },
      {
        route: 'classes',
        name: 'Classes',
        moduleId: PLATFORM.moduleName('components/classes'),
        nav: true,
        title: 'Classes'
      },
      {
        route: 'threads',
        name: 'Threads',
        moduleId: PLATFORM.moduleName('components/threads'),
        nav: true,
        title: 'Threads'
      },
      {
        route: 'exit-points',
        name: 'Exit Points',
        moduleId: PLATFORM.moduleName('components/exit'),
        nav: true,
        title: 'Exit Points'
      },
      {route: 'logs', name: 'Logs', moduleId: PLATFORM.moduleName('components/logs'), nav: true, title: 'Logs'},
      {
        route: 'timings',
        name: 'Timings',
        moduleId: PLATFORM.moduleName('components/timings'),
        nav: true,
        title: 'Timings'
      },
      {
        route: 'jvm-monitor',
        name: 'JVM Monitor',
        moduleId: PLATFORM.moduleName('components/jvm'),
        nav: true,
        title: 'JVM Monitor'
      },
      {
        route: 'testing',
        name: 'Testing',
        moduleId: PLATFORM.moduleName('components/testing'),
        nav: true,
        title: 'Testing'
      },

    ]);
  }

  navigateTo(route) {
    this.router.navigateToRoute(route)
  }

}


