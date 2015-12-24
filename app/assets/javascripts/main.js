(function() {
    'use strict';

    require.config({
        baseUrl : "/assets/javascripts",
        shim: {
            'bootstrap': ['jquery'],
            'nv': {
                exports: 'nv',
                deps: ['d3']
            }
        },
        paths: {
            'requirejs': ['../lib/requirejs/require'],
            'jquery' : ['../lib/jquery/jquery'],
            'bootstrap' : ['../lib/bootstrap/js/bootstrap'],
            'd3' : ['../lib/d3js/d3'],
            'nv' : ['../lib/nvd3/nv.d3']
        }
    });



    require(['jquery', 'bootstrap'],function() {
    });

    require.onError = function (err) {
        console.log(err);
    };
})();



