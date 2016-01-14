(function () {
    'use strict';

    require(['../main'], function () {

        require(['jquery', 'd3', 'nv'], function ($, d3, nv) {

            var path = window.location.pathname;

            var index = path.indexOf('/board/');

            var boardId = {};

            if(index !== -1) {
                boardId = path.split('/').pop();
            }

            var seriesUrl = '/series/' + boardId;

            var scopeLine = [],bestLine = [],finishedLine = [];

            var theData = [];

            nv.addGraph(function () {

                var chart = nv.models.lineChart()
                    .useInteractiveGuideline(true)
                    .showLegend(true)
                    .showYAxis(true)
                    .showXAxis(true);

                chart.height(450);

                chart.xAxis
                    .axisLabel('Period (datetime)')
                    .tickFormat(function(d) {
                        return d3.time.format('%d-%m-%y')(new Date(d))
                    }).showMaxMin(false);

                chart.yAxis.axisLabel('Scope').showMaxMin(false);

                $.ajax( { url : seriesUrl }).then(function(result) {

                    scopeLine = result.scopeLine;
                    bestLine = result.bestLine;
                    finishedLine = result.finishedLine;

                    var newScopeLine = {
                        values: scopeLine, //values - represents the array of {x,y} data points
                        key: 'Scope Line', //key  - the name of the series.
                        color: '#45454A'  //color - optional: choose your own line color.
                    };

                    var newFinishedLine = {
                        values: finishedLine,
                        key: 'Finished Line',
                        color: '#AFEE7E'
                    };

                    var newBestLine = {
                        values: bestLine,
                        key: 'Projected Scope',
                        color: '#E4A25A'
                    };

                    theData.push(newScopeLine);
                    theData.push(newFinishedLine);
                    theData.push(newBestLine);

                    //Select the <svg> element you want to render the chart in.
                    d3.select('#chart svg').datum(theData).call(chart);
                });

                //Update the chart when window resizes.
                nv.utils.windowResize(function () {
                    chart.update()
                });

                return chart;
            });

        });
    });

})();