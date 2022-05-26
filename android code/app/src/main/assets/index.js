var rec_data;
var rec_x = [];
var strong_buy = [];
var buy = [];
var hold = [];
var sell = [];
var strong_sel = [];

function loadData(data, string, string1, string2, string3){
    rec_data = JSON.parse(data);
    strong_buy = rec_data[0];
    buy = rec_data[1];
    hold = rec_data[2];
    sell = rec_data[3];
    strong_sel = rec_data[4];
    rec_x.push(string);
    rec_x.push(string1);
    rec_x.push(string2);
    rec_x.push(string3);
    Highcharts.chart('container', {
        chart: {
            type: 'column'
        },
        title: {
            text: 'Recommendation Trends'
        },
        xAxis: {
            categories: rec_x,
        },
        yAxis: {
            min: 0,
            title: {
                text: '#Analysis',
                align: 'high'
            },
            stackLabels: {
                enabled: true
            }
        },
        legend: {
            verticalAlign: 'bottom',
            x: 0,
            y: 0,
            backgroundColor:
                Highcharts.defaultOptions.legend.backgroundColor || 'white',
            shadow: false
        },
        tooltip: {
            headerFormat: '<b>{point.x}</b><br/>',
            pointFormat: 
            '{series.name}: {point.y}<br/>Total: {point.stackTotal}'
        },
        plotOptions: {
            column: {
                stacking: 'normal',
                dataLabels: {
                    enabled: false
                }
            }
        },
        series: [{
            name: 'Strong Buy',
            data: strong_buy,
            color: '#0d9f3f'
        }, {
            name: 'Buy',
            data: buy,
            color: '#88f113'
        }, {
            name: 'Hold',
            data:  hold,
            color: '#d7a61b'
        },{
          name: 'Sell',
          data: sell,
          color: '#d10374'
      },{
        name: 'Strong Sell',
        data: strong_sel,
        color: '#8c4959'
      }
      ]
      });
};

var earn_data;
var earn_x = [];
var earn_act = [11, 22, 33, 22];
var earn_est = [10, 20, 30, 20];
var surp = [];

function loadEarnData(data, string, string1, string2, string3){
    earn_data = JSON.parse(data);
    earn_act = earn_data[0];
    earn_est = earn_data[1];
    surp = earn_data[2];
    rec_x.push(string + '<br>' + 'Surprise: '+ surp[0]);
    rec_x.push(string1 + '<br>' + 'Surprise: '+ surp[1]);
    rec_x.push(string2 + '<br>' + 'Surprise: '+ surp[2]);
    rec_x.push(string3 + '<br>' + 'Surprise: '+ surp[3]);
    Highcharts.chart('container_eps', {  
        chart: {
           type: "spline"
        },
        title: {
           text: "Historical EPS Surprises"
        },
        xAxis:{
          categories: rec_x
        },
        yAxis: {          
          title: {
            text: 'Quarterly EPS'
          }
        }
        ,
        tooltip: {
          shared: true
        },
        plotOptions: {
            series: {
                marker: {
                    enabled: true
                }
            }
        },
        series: [
           {
              name: 'Actual',
              data: earn_act
           },
           {
            name: 'Estimate',
            data: earn_est
          }
        ]
     });
};


var ticker;
var color;
var sixHourdata;

function loadHourlyData(data, string, string1){
    ticker = string;
    color = string1;
    sixHourdata = JSON.parse(data);
    Highcharts.chart('container_hour', {  
        chart: {
           type: "spline"
        },
        title: {
           text: ticker + " Hourly Price Variation"
        },
        xAxis:{
          type: 'datetime',
          tickInterval: 1000 * 60 * 60,
          dateTimeLabelFormats: {
            dminute: '%H:%M'
          },
          scrollbar: {
            enabled: true
          }
        },
        yAxis: {          
           opposite: true,
           title: false
        },
        tooltip: {
          split: true,
          dateTimeLabelFormats: {
            millisecond: "%A, %b %e, %H:%M"
          },
          borderColor: color
        },
        series: [
           {
              name: ticker,
              data: sixHourdata,
              color: color,
              showInLegend: false,
              states: {
                hover: {
                    enabled: false
                }
              }
           }
        ]
     });
}



     var ohlc;
     var volume;
     var timeArray_here;
     var volumeArray_here;
     var alltimeArray_here;
     var all_time_ticker;
     all_time_ticker = 'test';

    function loadALllData(timeArray, volumeArray, alltimeArray, ticker){
        timeArray_here = JSON.parse(timeArray);
        volumeArray_here = JSON.parse(volumeArray);
        alltimeArray_here = JSON.parse(alltimeArray);
        all_time_ticker = ticker;
        ohlc = []
        volume = []
        var groupingUnits = [[
        'week',                         // unit name
        [1]                             // allowed multiples
    ], [
        'month',
        [1, 2, 3, 4, 6]
    ]]
       for (var i = 0; i < timeArray_here.length; i += 1) {
           ohlc.push([
               timeArray_here[i], // the date
               alltimeArray_here[i][0], // open
               alltimeArray_here[i][1], // high
               alltimeArray_here[i][2], // low
               alltimeArray_here[i][3] // close
           ]);

           volume.push([
               timeArray_here[i], // the date
               volumeArray_here[i] // the volume
           ]);
       }

        // create the chart
    Highcharts.stockChart('container_all', {

        rangeSelector: {
            selected: 4
        },

        title: {
            text: all_time_ticker + ' Historical'
        },

        subtitle: {
            text: 'With SMA and Volume by Price technical indicators'
        },

        yAxis: [{
            startOnTick: false,
            endOnTick: false,
            labels: {
                align: 'right',
                x: -3
            },
            title: {
                text: 'OHLC'
            },
            height: '60%',
            lineWidth: 2,
            resize: {
                enabled: true
            }
        }, {
            labels: {
                align: 'right',
                x: -3
            },
            title: {
                text: 'Volume'
            },
            top: '65%',
            height: '35%',
            offset: 0,
            lineWidth: 2
        }],

        tooltip: {
            split: true
        },

        plotOptions: {
            series: {
                dataGrouping: {
                    units: groupingUnits
                }
            }
        },

        series: [{
            type: 'candlestick',
            name: ticker,
            id: 'aapl',
            zIndex: 2,
            data: ohlc
        }, {
            type: 'column',
            name: 'Volume',
            id: 'volume',
            data: volume,
            yAxis: 1
        }, {
            type: 'vbp',
            linkedTo: 'aapl',
            params: {
                volumeSeriesID: 'volume'
            },
            dataLabels: {
                enabled: false
            },
            zoneLines: {
                enabled: false
            }
        }, {
            type: 'sma',
            linkedTo: 'aapl',
            zIndex: 1,
            marker: {
                enabled: false
            }
        }]
        });
    }