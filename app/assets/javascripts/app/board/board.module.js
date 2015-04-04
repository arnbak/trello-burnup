angular.
    module('board', ['highcharts-ng', 'board.service']).
    run(['$location', '$window', function($location, $window) {
        console.log($location.path(), $window.location.href)

    }]);
