(function(angular) { 'use strict';

    angular.
        module('board').
        controller('BoardController', BoardController);

    BoardController.$inject =  ['$state','$q','$window','$location','BoardService'];

    function BoardController($state, $q, $window, $location, BoardService) {
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
                x: function(d){ return d.x; },
                y: function(d){ return d.y; },
                useInteractiveGuideline: true,
                dispatch: {
                    stateChange: function(e){ console.log("stateChange"); },
                    changeState: function(e){ console.log("changeState"); },
                    tooltipShow: function(e){ console.log("tooltipShow"); },
                    tooltipHide: function(e){ console.log("tooltipHide"); }
                },
                xAxis: {
                    axisLabel: 'Time (ms)'
                },
                yAxis: {
                    axisLabel: 'Voltage (v)',
                    tickFormat: function(d){
                        return d3.format('.02f')(d);
                    },
                    axisLabelDistance: 30
                },
                callback: function(chart){
                    console.log("!!! lineChart callback !!!");
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
        vm.data = sinAndCos();

        /*Random Data Generator */
        function sinAndCos() {
            var sin = [],sin2 = [],
                cos = [];

            //Data is represented as an array of {x,y} pairs.
            for (var i = 0; i < 100; i++) {
                sin.push({x: i, y: Math.sin(i/10)});
                sin2.push({x: i, y: i % 10 == 5 ? null : Math.sin(i/10) *0.25 + 0.5});
                cos.push({x: i, y: .5 * Math.cos(i/10+ 2) + Math.random() / 10});
            }

            //Line chart data should be sent as an array of series objects.
            return [
                {
                    values: sin,      //values - represents the array of {x,y} data points
                    key: 'Sine Wave', //key  - the name of the series.
                    color: '#ff7f0e'  //color - optional: choose your own line color.
                },
                {
                    values: cos,
                    key: 'Cosine Wave',
                    color: '#2ca02c'
                },
                {
                    values: sin2,
                    key: 'Another sine wave',
                    color: '#7777ff',
                    area: true      //area - set to true if you want this line to turn into a filled area chart.
                }
            ];
        };

        function loadBoard() {
            vm.loading = true;

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

