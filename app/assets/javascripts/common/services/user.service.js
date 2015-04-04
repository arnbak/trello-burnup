(function() {'use strict';
	angular.module('services.users', []).
	factory('UserService', ['$http', '$q', '$window', function($http, $q, $window){
		

		var baseUrl = 'http://localhost:9000';

		if(angular.equals($window.location.host.indexOf('localhost'), -1)) {
			baseUrl = '';
		}

		var userService = {
			url: baseUrl,

			list: function() {
				return $http.get(this.url + '/api/users/list');
			}

		};		

		return userService;
	}]);
})();