import { Component, Input, Output, OnInit, SimpleChanges} from '@angular/core';

import { StringLiteralLike } from 'typescript';
import { PostService } from './basic-info.service';

import { AppService } from '../app.service';

import * as Highcharts from 'highcharts';
// import { StockChart } from 'angular-highcharts';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

import * as Highcharts_Stock from "highcharts/highstock";
import {Options} from "highcharts/highstock";

import indicators from 'highcharts/indicators/indicators';
indicators(Highcharts_Stock);
// import vbp from 'highcharts/indicators/volume-by-price';
// vbp(Highcharts_Stock);

import IndicatorsCore from "highcharts/indicators/indicators";
import IndicatorVBP from "highcharts/indicators/volume-by-price";
import { FormControl } from '@angular/forms';
IndicatorsCore(Highcharts_Stock);
IndicatorVBP(Highcharts_Stock);

import { ActivatedRoute } from '@angular/router';
import { Router } from '@angular/router'; 

import { StateService } from '../state.service';


IndicatorsCore(Highcharts_Stock);


@Component({
  selector: 'app-basic-info',
  templateUrl: './basic-info.component.html',
  styleUrls: ['./basic-info.component.css'],
  providers: [PostService]
})
export class BasicInfoComponent implements OnInit {
  isHighcharts = typeof Highcharts === 'object';
  posts: any= [];
  latestPrice: any= [];
  upOrDown = 0;
  openOrClose = 0;
  message: string;
  highPrice: string;
  lowPrice: string;
  openPrice: string;
  prevClose: string;
  closePrice: string;
  d: string;
  dp: string;
  t: string;
  peers: any=[];
  sixHourdata: any=[];
  highcharts: any=[];
  options: any=[];
  news: any=[];
  color: string='green';

  // stockchart
  ohlc: any=[];
  volume: any=[];
  chart_data: any=[];
  stock_chart: any=[];
  stock_option: any=[];

  sentiment: any=[];
  reddit_pos = 0;
  mention = 0;
  reddit_neg = 0;
  twitter_pos = 0;
  twitter_mention = 0;
  twitter_neg = 0;

  rec_chart: any=[];
  rec_options: any=[];
  rec_data: any=[];
  rec_x = [];
  strong_buy = [];
  buy = [];
  hold = [];
  sell = [];
  strong_sel = [];

  earn: any=[];
  earn_chart: any=[];
  earn_options: any=[];

  earn_x = [];
  earn_act = [];
  earn_est = [];


  @Input() ticker;

 
  collect = false;
  discollect = false;

  star_status: boolean; // true is fill false is not 

  money: number
  moneyNeeded: any;
  input_q: number;
  exceed: boolean = false;
  // bought: boolean = false;
  buy_for_sell_button = false; //not button

  sellToMuch = false;
  input_q_sell: number;
  moneyback: number;

  // sold = false;

  foundAPI = 0;

  updateFlag = false;

  Bought_ALERTS: any[] = []
  Sell_ALERTS: any[] = []
  Collect_ALERTS: any[] = []
  disCollect_ALERTS: any[] = []


   notFound = false;

   empty = false;


  constructor(private postsService : AppService, private modalService: NgbModal, private router: Router, private service: StateService) { }

  ngOnInit(): void {
  }
  


  ngOnChanges(changes: SimpleChanges): void {
    

    this.empty = false;
    this.notFound = false;
    if (this.ticker == '') {
      this.empty = true;
    } else {
      this.empty = false;
      this.foundAPI = 0;
      this.posts = [];
      this.getProfileData();
      this.sendToSibling();
   }
  }


  sendToSibling(): void {
    this.service.updateMessage(this.ticker);
  }

  peerTo(peer: string){
    this.ticker = peer;
    this.foundAPI = 0;
    this.posts = [];
    this.getProfileData();
  }

  sellToMuchorNot() {
    this.moneyback = this.input_q_sell * +this.closePrice
    var list = eval(localStorage.getItem('bought'))
    var quantity = 0;
    for(let i = 0; i < list.length; i++) {
      if (list[i]['ticker'] == this.ticker) {
        quantity = list[i]['quantity']
      }
    }
    if (quantity < this.input_q_sell) {
        this.sellToMuch = true;
    } else {
        this.sellToMuch = false;
    }
  }

  sellIt() {
    this.Sell_ALERTS.push(this.ticker);
    var list = eval(localStorage.getItem('bought'))
      for(let i = 0; i < list.length; i++) {
        if (list[i]['ticker'] == this.ticker) {
          list[i]['quantity'] = list[i]['quantity'] - this.input_q_sell;
          list[i]['totalcost'] = list[i]['totalcost'] - this.moneyback;
          if (list[i]['quantity'] == 0) {
            list.splice(i, 1); 
            this.buy_for_sell_button = false;
          }
        }
      }
      localStorage.setItem('bought', JSON.stringify(list))
      this.money = +(this.money + this.moneyback).toFixed(2)
      localStorage.setItem('money', this.money.toString())
  }

  close_sell_alert(ticker: string) {
    this.Sell_ALERTS.splice(this.Sell_ALERTS.indexOf(ticker), 1);
  }

  check_load_sell_or_not() {
    if (localStorage.getItem('bought') == null) {
      this.buy_for_sell_button = false;
    } else {
      var list = eval(localStorage.getItem('bought'))
      var found = false;
      for(let i = 0; i < list.length; i++) {
        if (list[i]['ticker'] == this.ticker) {
          this.buy_for_sell_button = true;
          found = true;
        }
      }
      if (found == false) {
        this.buy_for_sell_button = false;
      }   
    }
  }

  boughtIt() {
    this.Bought_ALERTS.push(this.ticker);
    this.buy_for_sell_button = true;
    if (localStorage.getItem('bought') == null) {
      var bought = []
      var boughtDetail = {}
      boughtDetail['ticker'] = this.ticker
      boughtDetail['quantity'] = this.input_q
      boughtDetail['totalcost'] = this.moneyNeeded
      bought.push(boughtDetail)
      localStorage.setItem('bought', JSON.stringify(bought))
    } else {
      var list = eval(localStorage.getItem('bought'))
      var found = false;
      for(let i = 0; i < list.length; i++) {
        if (list[i]['ticker'] == this.ticker) {
          list[i]['quantity'] = list[i]['quantity'] + this.input_q;
          list[i]['totalcost'] = list[i]['totalcost'] + this.moneyNeeded;
          found = true;
        }
      }
      if (found == false) {
        var boughtDetail = {}
        boughtDetail['ticker'] = this.ticker
        boughtDetail['quantity'] = this.input_q
        boughtDetail['totalcost'] = this.moneyNeeded
        list.push(boughtDetail)
      }
      localStorage.setItem('bought', JSON.stringify(list))
    }
    this.money = +(this.money - this.moneyNeeded).toFixed(2)
    localStorage.setItem('money', this.money.toString())
  }

  close(ticker: string) {
    this.Bought_ALERTS.splice(this.Bought_ALERTS.indexOf(ticker), 1);
  }

  checkMoneyNeeded() {
    this.moneyNeeded = this.input_q * +this.closePrice
    if (this.moneyNeeded > this.money) {
      this.exceed = true;
    } else {
      this.exceed = false;
    }
  }

  checkMoney() {
    if (localStorage.getItem("money") == null) {
      localStorage.setItem("money", '25000');
      this.money = 25000;
    } else {
      this.money = +localStorage.getItem("money");
    }
  }

  

  getStarStatus() {
    var collected = eval(localStorage.getItem("tickers"));
      this.star_status = false;
      if (collected != null ) {
        for (let i = 0; i < collected.length; i++) {
          if (collected[i] == this.ticker) {
            this.star_status = true;
          }
        }
      }
  }
  starClickEvent() {
    this.star_status = !this.star_status; // true is fill false is not 
    if (this.star_status) {
      this.Collect_ALERTS.push(this.ticker)
      if (localStorage.getItem('tickers') == null) {
        var tickers = []
        tickers.push(this.ticker)
        localStorage.setItem('tickers', JSON.stringify(tickers))
      } else {
        var the_tickers = []
        the_tickers = eval(localStorage.getItem('tickers'))
        the_tickers.push(this.ticker)
        localStorage.setItem('tickers', JSON.stringify(the_tickers))
      }
    } 
    if (!this.star_status) {
      this.disCollect_ALERTS.push(this.ticker)

      var the_tickers = []
        the_tickers = eval(localStorage.getItem('tickers'))
        let index = the_tickers.indexOf(this.ticker);
        if (index > -1) {
          the_tickers.splice(index, 1); 
        }
        localStorage.setItem('tickers', JSON.stringify(the_tickers))
    } 
  }

  toggle_collect(ticker: string) {
    this.Collect_ALERTS.splice(this.Collect_ALERTS.indexOf(ticker), 1);
  }

  toggle_discollect(ticker: string) {
    this.disCollect_ALERTS.splice(this.disCollect_ALERTS.indexOf(ticker), 1);
  }

  

  openBackDropCustomClass(content) {
    this.modalService.open(content, {backdropClass: 'light-blue-backdrop'});
  }

  getProfileData() {
    this.postsService.getBasicInfo(this.ticker).subscribe((response: any)=>{
      this.posts = response;
      if (Object.keys(this.posts[0]).length == 0) {
        this.notFound = true;
      } else if(Object.keys(this.posts[0]).length > 0) {
        this.notFound = false;
        this.getLatestPriceData();
        this.getPeerData();
        // this.getsixHourData();
        this.getNewsData();
        this.getChartData();
        this.getSentimentData();
        this.getRecData();
        this.getEarnData();
        this.getStarStatus();
        this.checkMoney();
        this.check_load_sell_or_not();
      }
    })
    this.foundAPI = this.foundAPI + 1;
  }

  getLatestPriceData() {
    this.postsService.getLatestPrice(this.ticker).subscribe(lp=>{
      this.latestPrice = lp;

      this.getsixHourData(this.latestPrice[0].t)


      if (this.latestPrice[0].d > 0) {
        this.upOrDown = 1;
        this.color = 'green'
      } else {
        this.upOrDown = 2;
        this.color = 'red'
      }
      let raw_time = this.latestPrice[0].t
      var date = new Date(raw_time * 1000)
      var yyyy = date.getFullYear().toString();
      var mm = (date.getMonth()+1).toString();
      var dd  = date.getDate().toString();
      var hours = date.getHours();
      var minutes = "0" + date.getMinutes()
      var seconds = "0" + date.getSeconds()
      var formattedTime = yyyy +'-'+("0"+(mm)).slice(-2)+'-'+("0"+dd).slice(-2) + " " + hours + ':' + minutes.substr(-2) + ':' + seconds.substr(-2);
      this.latestPrice[0].t = formattedTime
      this.latestPrice[0].dp = +this.latestPrice[0].dp.toFixed(2)

      var cur_time = new Date();
      var diff = Math.abs(cur_time.getTime() - date.getTime());
      var minDiff = diff / 60 / 1000; 
      if (minDiff > 5) {
        this.openOrClose = 1; //close
      } else {
        this.openOrClose = 2; //open
        setInterval(() => {         
          this.postsService.getBasicInfo(this.ticker).subscribe((response: any)=>{
            this.posts = response;
            this.getLatestPriceData();
            this.getPeerData();
            // this.getsixHourData();
            this.getStarStatus();
            this.checkMoney();
            this.check_load_sell_or_not();
        })
        }, 15000);
      }

      var cur_yyyy = cur_time.getFullYear().toString();
      var cur_mm = (cur_time.getMonth()+1).toString();
      var cur_dd  = cur_time.getDate().toString();
      var cur_hours = cur_time.getHours();
      var cur_minutes = "0" + cur_time.getMinutes()
      var cur_seconds = "0" + cur_time.getSeconds()
      var cur_formattedTime = cur_yyyy +'-'+("0"+(cur_mm)).slice(-2)+'-'+("0"+cur_dd).slice(-2) + " " +cur_hours + ':' + cur_minutes.substr(-2) + ':' + cur_seconds.substr(-2);
      this.message = cur_formattedTime

     this.highPrice = this.latestPrice[0].h
     this.lowPrice = this.latestPrice[0].l
     this.openPrice = this.latestPrice[0].o
     this.prevClose = this.latestPrice[0].pc
     this.closePrice = this.latestPrice[0].c
     this.d = this.latestPrice[0].d
     this.dp = this.latestPrice[0].dp
     this.t = this.latestPrice[0].t
    })
    this.foundAPI = this.foundAPI + 1;
  }

  getPeerData() {
    this.postsService.getPeerInfo(this.ticker).subscribe(p=>{
      this.peers = p;
    })
    this.foundAPI = this.foundAPI + 1;
  }

  getEarnData() {
    this.postsService.getEarn(this.ticker).subscribe(earn=>{
      this.earn = earn;
      this.earn_x = [];
      this.earn_act = [];
      this.earn_est = [];
      for (let i = 0; i < this.earn.length; i++) {
        this.earn_x.push(this.earn[i].period + '<br>' + 'Surprise: '+this.earn[i].surprise);
        this.earn_act.push(this.earn[i].actual);
        this.earn_est.push(this.earn[i].estimate);
      }
      this.earn_chart = Highcharts;
      this.earn_options = {  
      chart: {
         type: "spline"
      },
      title: {
         text: "Historical EPS Surprises"
      },
      xAxis:{
        categories: this.earn_x
      },
      yAxis: {          
        //  opposite: true,
        title: {
          text: 'Quarterly EPS'
        }
      }
      ,
      tooltip: {
        shared: true
      },
      series: [
         {
            name: 'Actual',
            data: this.earn_act
            // states: {
            //   hover: {
            //       enabled: false
            //   }
            // }
         },
         {
          name: 'Estimate',
          data: this.earn_est
          // states: {
          //   hover: {
          //       enabled: false
          //   }
          // }
        }
      ]
   };
    })
    this.foundAPI = this.foundAPI + 1;
  }

  getRecData() {
    this.postsService.getRec(this.ticker).subscribe(rec=>{
    this.rec_data = rec;
    this.rec_x = [];
    this.strong_buy = [];
    this.buy = [];
    this.hold = [];
    this.sell = [];
    this.strong_sel = [];
    for (let i = 0; i < this.rec_data.length; i++) {
      this.rec_x.push(this.rec_data[i].period.substring(0, 7));
      this.strong_buy.push(this.rec_data[i].strongBuy);
      this.buy.push(this.rec_data[i].buy);
      this.hold.push(this.rec_data[i].hold);
      this.sell.push(this.rec_data[i].sell);
      this.strong_sel.push(this.rec_data[i].strongSell);
    }
    this.rec_chart = Highcharts;
    this.rec_options = {
      chart: {
          type: 'column'
      },
      title: {
          text: 'Recommendation Trends'
      },
      xAxis: {
          categories: this.rec_x
      },
      yAxis: {
          min: 0,
          title: {
              text: '#Analysis',
              align: 'high'
          },
          stackLabels: {
              enabled: false
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
          pointFormat: '{series.name}: {point.y}'
      },
      plotOptions: {
          column: {
              stacking: 'normal',
              dataLabels: {
                  enabled: true
              }
          }
      },
      series: [{
          name: 'Strong Buy',
          data: this.strong_buy,
          color: '#0d9f3f'
      }, {
          name: 'Buy',
          data: this.buy,
          color: '#88f113'
      }, {
          name: 'Hold',
          data: this.hold,
          color: '#d7a61b'
      },{
        name: 'Sell',
        data: this.sell,
        color: '#d10374'
    },{
      name: 'Strong Sell',
      data: this.strong_sel,
      color: '#8c4959'
    }
    ]
    }
  })
  this.foundAPI = this.foundAPI + 1;
  }

  getsixHourData(timestamp: number) {
    this.postsService.getSixHourPrice(this.ticker, timestamp).subscribe(data=>{
      this.sixHourdata = data;
      this.highcharts = Highcharts;
      this.options = {  
      chart: {
         type: "spline"
      },
      title: {
         text: this.ticker + " Hourly Price Variation"
      },
      xAxis:{
        type: 'datetime'
        ,
        tickInterval: 1000 * 60 * 60,
        dateTimeLabelFormats: {
          dminute: '%H:%M'
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
        borderColor: this.color
      },
      series: [
         {
            name: this.ticker,
            data: this.sixHourdata,
            color: this.color,
            showInLegend: false,
            states: {
              hover: {
                  enabled: false
              }
            }
         }
      ]
   };
    })
    this.foundAPI = this.foundAPI + 1;
  }

  getNewsData() {
    this.postsService.getNews(this.ticker).subscribe(news=>{
      this.news = news;
    })
    this.foundAPI = this.foundAPI + 1;
  }

  getSentimentData() {
    this.postsService.getSentiment(this.ticker).subscribe(sentiment=>{
      this.sentiment = sentiment;
      for (let i = 0; i < this.sentiment.reddit.length; i++) {
          this.reddit_pos = this.reddit_pos + this.sentiment.reddit[i].positiveMention 
          this.mention = this.mention + this.sentiment.reddit[i].mention 
          this.reddit_neg = this.reddit_neg + this.sentiment.reddit[i].negativeMention
      }
      for (let i = 0; i < this.sentiment.twitter.length; i++) {
        this.twitter_pos = this.twitter_pos + this.sentiment.twitter[i].positiveMention 
        this.twitter_mention = this.twitter_mention + this.sentiment.twitter[i].mention 
        this.twitter_neg = this.twitter_neg + this.sentiment.twitter[i].negativeMention
      }
    })
    this.foundAPI = this.foundAPI + 1;
  }

  getChartData() {
    this.updateFlag = false;
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
    this.stock_chart = Highcharts_Stock;
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
      ,
      {
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
    this.updateFlag = true;
    this.foundAPI = this.foundAPI + 1;
  }
}


