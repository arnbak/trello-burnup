(function(angular) { 'use strict';

	angular.
	module('burnupApp').
	run(function($state, $location, $window) {
		//if(!AuthService.isAuthed()) {
		//	$state.go('login.form');
		//} else if($state.current.name === '') {
		//	$state.go('dashboard.index');
		//}
		console.log($state, $location);

		console.log('hello world');

			$window.location("/")
		//$state.go('dashboard.index')
	});

})(angular);