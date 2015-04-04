(function(angular) { 'use strict';

    /**
     *	@ngdoc service
     *	@name TrelloService
     *	@requires $rootScope, $http, $q, $window, AuthService
     */
    angular.module('board.service', []).
        factory('BoardService', ['$rootScope', '$http', '$q', function($rootScope,$http, $q) {

            var service = {

                currentBoardInfo: {},
                currentBoard: {},

                setCurrentBoard: function(board) {
                    this.currentBoard = board;
                },
                getCurrentBoard: function() {
                    return this.currentBoard;
                },
                accumulateBoardInfo: function() {
                    var d = $q.defer();

                    $http.get('/accumulate').then(function(response) {
                        d.resolve(response);
                    }).catch(function(error) {
                        d.reject(error);
                    });

                    return d.promise;
                },
                /**
                 * 	@ngdoc method
                 * 	@name TrelloService#boards
                 * 	@kind function
                 * 	@description queries all boards from all organizations
                 * 	@return {Promise} the callback promise of the resolved info
                 */
                boards: function() {
                    var d = $q.defer();

                    //Trello.setToken($cookies.trelloToken);
                    //
                    //Trello.get('/members/me', {boards:'all',organizations:'all'}, function(me) {
                    //	d.resolve(me.boards);
                    //}, function(e) {
                    //	d.reject(e.responseText);
                    //});

                    return d.promise;

                },
                /**
                 * 	@ngdoc method
                 * 	@name TrelloService#info
                 * 	@kind function
                 * 	@description fetches the whole member object from the trello api
                 * 	@return {Promise} the callback promise of the resolved info
                 */
                info: function() {
                    var d = $q.defer();

                    //Trello.setToken($cookies.trelloToken);
                    //
                    //Trello.get('/members/me', {boards:'all',organizations:'all'},
                    //	function(me) {
                    //		d.resolve(me);
                    //	},
                    //	function(e) {
                    //		d.reject(e.responseText);
                    //	}
                    //);

                    return d.promise;
                },
                /**
                 * 	@ngdoc method
                 * 	@name TrelloService#period
                 * 	@kind function
                 * 	@description returns the configured period of a sprint in a given board
                 * 	@return {Promise} the callback promise of the resolved data
                 */
                period: function(boardid) {
                    //var d = $q.defer();



                    return $http.get('/period/' + boardid).then(function(period) {
                        //d.resolve(period);
                        return period;
                    });
                    // .catch(function(e) {
                    // 	console.log(e);
                    // 	return d.reject(e);
                    // });

                    //return d.promise;
                },
                series: function(boardid) {
                    // var d = $q.defer();

                    return $http.get('/series/' + boardid).then(function(series) {
                        return series;
                        //d.resolve(series);
                    });
                    // .catch(function(e) {
                    // 	return d.reject(e);
                    // });

                    // return d.promise;
                },
                /**
                 * 	@ngdoc method
                 * 	@name TrelloService#boardInfo
                 * 	@kind function
                 * 	@description return the specific info for a given board such as
                 *	name, id, organization id.
                 *
                 * 	@return {Promise} the callback promise of the resolved data
                 */
                boardInfo: function(boardid) {
                    var d = $q.defer();

                    this.info().then(function(me) {
                        angular.forEach(me.boards, function(board) {
                            if(angular.equals(board.id, boardid)) {
                                d.resolve(board);
                            }
                        });

                    }).catch(function(e) {
                        console.log(e);
                        d.reject(e);
                    });

                    return d.promise;
                }


            };

            return service;
        }]);
})(angular);