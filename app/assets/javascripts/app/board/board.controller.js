(function(angular) { 'use strict';

    angular.
        module('board').
        controller('BoardController', BoardController);

    BoardController.$inject =  ['$window','BoardService'];

    function BoardController($window, BoardService) {
        var pathname = $window.location.pathname.split('/');

        var vm = this;
        vm.boardid = pathname[2];

        vm.cards = {};
        vm.period = {};
        vm.loading = false;

        vm.options = {
            chart: {
                type: 'lineChart',
                height: 450,
                margin : {
                    top: 20,
                    right: 20,
                    bottom: 40,
                    left: 55
                },
                x: function(d) {
                    return d[0];
                },
                y: function(d) {
                    return d[1];
                },
                useInteractiveGuideline: true,
                dispatch: {
                    stateChange: function(e){ console.log("stateChange"); },
                    changeState: function(e){ console.log("changeState"); },
                    tooltipShow: function(e){ console.log("tooltipShow"); },
                    tooltipHide: function(e){ console.log("tooltipHide"); }
                },
                xAxis: {
                    axisLabel: 'Period (datetime)',
                    tickFormat: function(d) {
                        return d3.time.format('%d-%m-%y')(new Date(d))
                    },
                    showMaxMin: false
                },
                yAxis: {
                    axisLabel: 'Scope (v)'//,
                    //tickFormat: function(d){
                    //
                    //    return d3.format('.02f')(d);
                    //}
                    //axisLabelDistance: 30
                },
                callback: function(chart){
                    //console.log(chart);
                    //console.log("!!! lineChart callback !!!");
                }
            },
            title: {
                enable: true,
                text: 'Title for Line Chart'
            },
            subtitle: {
                enable: true,
                text: 'Subtitle for simple line chart. Lorem ipsum dolor sit amet, at eam blandit sadipscing, vim adhuc sanctus disputando ex, cu usu affert alienum urbanitas.',
                css: {
                    'text-align': 'center',
                    'margin': '10px 13px 0px 7px'
                }
            },
            caption: {
                enable: true,
                html: '<b>Figure 1.</b> Lorem ipsum dolor sit amet, at eam blandit sadipscing, <span style="text-decoration: underline;">vim adhuc sanctus disputando ex</span>, cu usu affert alienum urbanitas. <i>Cum in purto erat, mea ne nominavi persecuti reformidans.</i> Docendi blandit abhorreant ea has, minim tantas alterum pro eu. <span style="color: darkred;">Exerci graeci ad vix, elit tacimates ea duo</span>. Id mel eruditi fuisset. Stet vidit patrioque in pro, eum ex veri verterem abhorreant, id unum oportere intellegam nec<sup>[1, <a href="https://github.com/krispo/angular-nvd3" target="_blank">2</a>, 3]</sup>.',
                css: {
                    'text-align': 'justify',
                    'margin': '10px 13px 0px 7px'
                }
            }
        };

        vm.data = [];


        function loadBoard() {
            vm.loading = true;

            BoardService.period(vm.boardid).then(function(response) {
                vm.period = response.data;
                return BoardService.series(vm.boardid);
            }).then(function(response) {

                //vm.chartConfig.series = [];
                vm.scopeLine = response.data.scopeLine;
                vm.finishedLine = response.data.finishedLine;
                vm.bestLine = response.data.bestLine;



                var newScopeLine = {
                    values: vm.scopeLine,      //values - represents the array of {x,y} data points
                    key: 'Scope Line', //key  - the name of the series.
                    color: '#45454A'  //color - optional: choose your own line color.
                };

                var newFinishedLine = {
                    values: vm.finishedLine,
                    key: 'Finished Line',
                    color: '#AFEE7E'
                };

                var newBestLine = {
                    values: vm.bestLine,
                    key: 'Projected Scope',
                    color: '#E4A25A'
                }

                vm.data.push(newScopeLine);
                vm.data.push(newFinishedLine);
                vm.data.push(newBestLine);
            }).catch(function(e) {

                //if(angular.equals(e, 'no info for board, is there a configuration card in the board?')) {
                //    //eventually show an error message
                //    toastr.error('No information for choosen board');
                //} else if(angular.equals(e.status, 501)) {
                //    toastr.error('Error occoured!');
                //} else if(angular.equals(e.status, 404)) {
                //    toastr.error('No information for selected board');
                //} else if(angular.equals(e.status, 0)) {
                //    toastr.error('Unable to connect to the backend, is the backend started ??');
                //}

            }).finally(function() {
                vm.loading = false;
            });

        }

        loadBoard();

        function accumulate() {
            //$window.location.assign("/dashboard")
            vm.loading = true;
            BoardService.accumulateBoardInfo().then(function() {
                loadBoard();
            });
        }

        vm.accumulate = accumulate;
    }

})(angular);

