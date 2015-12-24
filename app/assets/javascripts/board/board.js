(function () {
    'use strict';

    require(['../main'], function () {

        require(['jquery', 'd3', 'nv'], function ($, d3, nv) {
            console.log('D3', d3);

            console.log('NVD3', nv);

            //console.log('data', sinAndCos());

            var path = window.location.pathname; //.indexOf('/board/')

            var index = path.indexOf('/board/');

            var boardId = {};

            if(index !== -1) {
                boardId = path.split('/').pop();
                console.log(typeof boardId)
            }

            var seriesUrl = '/series/' + boardId;

            console.log("seriesUrl",seriesUrl)

            var scopeLine = [],bestLine = [],finishedLine = [];

            $.ajax( { url : seriesUrl }).then(function(result) {

                scopeLine = result.scopeLine;
                bestLine = result.bestLine;
                finishedLine = result.finishedLine;
            });



            var newScopeLine = {
                values: scopeLine,      //values - represents the array of {x,y} data points
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

            var theData = [];
            theData.push(newScopeLine);
            theData.push(newFinishedLine);
            theData.push(newBestLine);

            nv.addGraph(function () {

                var chart = nv.models.lineChart()
                    .useInteractiveGuideline(true)
                    .showLegend(true)
                    .showYAxis(true)
                    .showXAxis(true);

                //chart.xAxis     //Chart x-axis settings
                //    .axisLabel('Time (ms)')
                //    .tickFormat(d3.format(',r'));
                //
                //chart.yAxis     //Chart y-axis settings
                //    .axisLabel('Voltage (v)')
                //    .tickFormat(d3.format('.02f'));

                chart.xAxis.axisLabel('Period (datetime)').tickFormat(function(d) {
                    return d3.time.format('%d-%m-%y')(new Date(d))
                }).showMaxMin(false);

                chart.yAxis.axisLabel('Scope').showMaxMin(false);

                chart.x = function(d) {
                    return d[0];
                };

                chart.y = function(d) {
                    return d[1];
                };

                /* Done setting the chart up? Time to render it!*/
                //var myData = sinAndCos();   //You need data...

                d3.select('#chart svg').datum(theData).call(chart);    //Select the <svg> element you want to render the chart in.
                    //.datum(theData)         //Populate the <svg> element with chart data...
                    //.call(chart);          //Finally, render the chart!

                //Update the chart when window resizes.
                nv.utils.windowResize(function () {
                    chart.update()
                });
                return chart;
            });



        });
    });

})();