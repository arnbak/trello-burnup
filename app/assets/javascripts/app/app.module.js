(function(angular) { 'use strict';
	angular.module('burnupApp', [
		'ui.router',
		'board'
		]);
	//]).config(['$stateProvider', '$urlRouterProvider', function($stateProvider, $urlRouterProvider) {
    //
	//	var dashboard = {
	//		name: 'dashboard',
	//		url: '',
	//		abstract: true,
	//		// template: '<div ui-view></div>'
	//		views: {
	//			'': {
	//				templateUrl: '../assets/javascripts/app/app.tpl.html'
	//			}
	//		}
    //
	//	};
    //
	//	var index = {
	//		name: 'dashboard.index',
	//		url: '/dashboard',
	//		// templateUrl: 'app/dashboard/dashboard.tpl.html',
	//		// controller: 'DashboardController'
	//		views: {
	//			'Content': {
	//				controller: 'DashboardController as dashboardCtrl',
	//				templateUrl: '../assets/javascripts/app/dashboard/dashboard.tpl.html'
	//			}
	//		}
	//	};
    //
	//	var board = {
	//		name: 'dashboard.board',
	//		url: '/board/:id',
	//		// templateUrl: 'app/dashboard/board.tpl.html',
	//		// controller: 'BoardCtrl',
	//		views: {
	//			'Content': {
	//				controller: 'BoardController as boardCtrl',
	//				templateUrl: '../assets/javascripts/app/dashboard/board.tpl.html'
	//			}
	//		}
	//	};
    //
	//	$stateProvider.state(dashboard);
	//	$stateProvider.state(index);
	//	$stateProvider.state(board);
	//	$urlRouterProvider.otherwise('/dashboard');
	//}]);
	

})(angular);

