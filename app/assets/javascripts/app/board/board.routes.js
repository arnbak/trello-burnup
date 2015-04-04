angular.
    module('board').
    config(['$stateProvider', '$locationProvider', function($stateProvider, $locationProvider) {

        //$locationProvider.html5Mode(true);

        var board = {
            name: 'board',
            url: '',
            controller: 'BoardController',
            controllerAs: 'boardCtrl',
            templateUrl: '../assets/javascripts/app/board/board.tpl.html'
        };

        $stateProvider.state(board);

    }]);