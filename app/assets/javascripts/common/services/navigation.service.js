(function(angular){ 'use strict';
	
	angular.
	module('services.navigation', []).
	factory('NavigationService', ['$rootScope', '$state', function($rootScope, $state){
	
		var currentState = $state.current;
		var previousState = {};

		$rootScope.$on('$stateChangeSuccess', function(event, toState, toParams, fromState, fromParams) {

			service._currentState = toState;			

			service._previousState = fromState;
		});		

		var service = {

			_currentState: currentState,
			_previousState: previousState,

			isActive: function(e) {
				return this._currentState.name.indexOf(e) > -1;		
			},

			isNestedItemActive: function(e) {
				return angular.equals(this._currentState.name, e);
			},

			previousState: function() {
				return this._previousState.name;
			},

			goBack: function() {
				$state.go(this._previousState.name);
			},
			login: function() {
				$state.go('login.form');
			}

		};

		return service;
	}]);
})(angular);