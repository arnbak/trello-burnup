(function(angular) { 'use strict';

    angular.
        module('board').
        controller('BoardController', BoardController);

    BoardController.$inject =  ['$state','$q','$window','$location','BoardService'];


//
    function BoardController($state, $q, $window, $location, BoardService) {
        var pathname = $window.location.pathname.split('/');

        var vm = this;
        vm.boardid = pathname[2];

        vm.cards = {};
        vm.period = {};


        function loadBoard() {
            vm.loading = true;

            //BoardService.boardInfo(vm.boardid).then(function(board) {
            //    vm.board = board;
            //    return BoardService.period(vm.boardid);
            //}).
            BoardService.period(vm.boardid).then(function(response) {
                vm.period = response.data;
                return BoardService.series(vm.boardid);
            }).then(function(response) {

                vm.chartConfig.series = [];
                vm.scopeLine = response.data.scopeLine;
                vm.finishedLine = response.data.finishedLine;
                vm.bestLine = response.data.bestLine;

                var _scopeLine = {
                    type: 'line',
                    name: 'Scope Line',
                    //pointStart: $scope.period.startDate,
                    data: vm.scopeLine,
                    //pointInterval: 24*3600*1000,
                    //connectNulls: true,
                    //black
                    color: '#45454A',
                    marker: {
                        radius: 3
                    }
                };

                var _bestLine = {
                    type: 'line',
                    name: 'Projected Scope',
                    //pointStart: $scope.period.startDate,
                    data: vm.bestLine,
                    //pointInterval: 24*3600*1000,
                    //connectNulls: true,
                    //red
                    color: '#E4A25A',
                    marker: {
                        radius: 2
                    }
                };

                var _finishedLine = {
                    type: 'line',
                    name: 'Done',
                    //pointStart: $scope.period.startDate,
                    data: vm.finishedLine,
                    //pointInterval: 24*3600*1000,
                    //green
                    color: '#AFEE7E',
                    marker: {
                        radius: 3
                    }
                    //useUTC: false
                };

                console.log(_scopeLine);
                //console.log(_progressLine);
                console.log(_finishedLine);
                console.log(_bestLine);

                vm.chartConfig.series.push(_scopeLine);
                //$scope.chartConfig.series.push(_progressLine);
                vm.chartConfig.series.push(_finishedLine);
                vm.chartConfig.series.push(_bestLine);


            }).catch(function(e) {

                if(angular.equals(e, 'no info for board, is there a configuration card in the board?')) {
                    //eventually show an error message
                    toastr.error('No information for choosen board');
                } else if(angular.equals(e.status, 501)) {
                    toastr.error('Error occoured!');
                } else if(angular.equals(e.status, 404)) {
                    toastr.error('No information for selected board');
                } else if(angular.equals(e.status, 0)) {
                    toastr.error('Unable to connect to the backend, is the backend started ??');
                }

            }).finally(function() {
                vm.loading = false;
            });

        }

        loadBoard();

        function accumulate() {
            //$window.location.assign("/dashboard")

            BoardService.accumulateBoardInfo().then(function() {
                loadBoard();
            });
        }

        vm.chartConfig = {
            chart: {
                zoomType: 'xy'
            },
            yAxis: [{
                title: {
                    text: 'Scope'
                }
            },{
                title: {
                    text: 'Scope'
                },
                opposite: true
            }],
            xAxis: {
                type: 'datetime',
                dateTimeLabelFormats: { // don't display the dummy year
                    month: '%e. %b',
                    year: '%b'
                },
                title: {
                    text: 'Periode'
                }
            },
            series: [ ],
            title: {
                text: 'Burn Up Chart'
            },
            legend: {
                layout: 'vertical',
                align: 'left',
                x: 120,
                verticalAlign: 'top',
                y: 100,
                floating: true,
                backgroundColor: (Highcharts.theme && Highcharts.theme.legendBackgroundColor) || '#FFFFFF'
            }
        };

        vm.accumulate = accumulate;
    }

})(angular);

