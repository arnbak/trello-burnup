angular.
    module('board', ['nvd3', 'board.service']).
    run(['$location', '$window', function($location, $window) {
        console.log($location.path(), $window.location.href)

    }]);
