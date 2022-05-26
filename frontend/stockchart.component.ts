import { Component, Input, Output, OnInit, SimpleChanges} from '@angular/core';
import { AppService } from '../app.service';

import * as Highcharts_stock from "highcharts/highstock";

import indicators from 'highcharts/indicators/indicators';
indicators(Highcharts_stock);
import vbp from 'highcharts/indicators/volume-by-price';
vbp(Highcharts_stock);
import IndicatorsCore from "highcharts/indicators/indicators";
IndicatorsCore(Highcharts_stock);

@Component({
  selector: 'app-stockchart',
  templateUrl: './stockchart.component.html',
  styleUrls: ['./stockchart.component.css']
})
export class StockchartComponent implements OnInit {

  constructor(private postsService : AppService) { }
  @Input() ticker;
  chart_data: any=[];
  stock_option = Object();
  stock_chart = Highcharts_stock;
  chartConstructor: string = 'stockChart';
  vbp = Object()

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.getChartData();
  }
  getChartData() {
    this.postsService.getChart(this.ticker).subscribe(chartdata=>{
      this.chart_data = chartdata
      var ohlc = [];
      var volume = [];
      for (let i = 0; i < this.chart_data.c.length; i++) {
        var datapoint = []
        datapoint.push(this.chart_data.t[i])
        datapoint.push(this.chart_data.o[i])
        datapoint.push(this.chart_data.h[i])
        datapoint.push(this.chart_data.l[i])
        datapoint.push(this.chart_data.c[i])
        ohlc.push(datapoint)
    }
    for (let i = 0; i < this.chart_data.c.length; i++) {
      var datapoint = []
      datapoint.push(this.chart_data.t[i])
      datapoint.push(this.chart_data.v[i])
      volume.push(datapoint)
    }
    this.vbp = {
      type: 'vbp',
      linkedTo: 'ohlc',
      params: {
        volumeSeriesID: 'volume'
      },
      dataLabels: {
          enabled: false
      },
      zoneLines: {
          enabled: false
      },
  }
    this.stock_chart = Highcharts_stock;
    this.stock_option = Object();
    this.stock_option = {
      rangeSelector: {
        enabled: true,
        allButtonsEnabled: true,
        selected: 2
    },
      title: {
          text: this.ticker + ' Historical'
      },
      subtitle: {
          text: 'With SMA and Volume by Price technical indicators'
      },
      xAxis:{
        type: 'datetime',
        dateTimeLabelFormats: {
          millisecond: '%b %e',
          second: '%b %e',
          minute: '%b %e',
          hour: '%b %e',
          day: '%b %e',
          week: '%b %e',
          month: '%b %e',
          year: '%b %e'
        }
      },
      yAxis: [{
          opposite: true,
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
      }, 
      {
          opposite: true,
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
                  units: [[
                    'week',                         // unit name
                    [1]                             // allowed multiples
                ], [
                    'month',
                    [1, 2, 3, 4, 6]
                ]]
              }
          },
          column: {
            pointWidth: 2,
            pointPlacement: 'on'
        }
      },
    navigator: {
        enabled: true
    },
    scrollbar: {
        enabled: true
    },
    series: 
      [ {
        id: 'ohlc',
        type: 'candlestick',
        name: this.ticker,
        zIndex: 2,
        showInLegend: false,
        data: ohlc,
        threshold: null,
        tooltip: {
          valueDecimals: 2
        }
      }, {
        id: 'volume',
        type: 'column',
        name: "Volume",
        showInLegend: false,
        data: volume,
        yAxis: 1
      }
      ,this.vbp
      , 
      {
          type: 'sma',
          zIndex: 1,
          marker: {
              enabled: false
          },
          linkedTo: 'ohlc'
      }
    ]
  }
    })
  }

}
