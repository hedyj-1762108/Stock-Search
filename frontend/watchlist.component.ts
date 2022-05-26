import { Component, OnInit, SimpleChanges} from '@angular/core';
import { isEmpty } from 'rxjs-compat/operator/isEmpty';
import { AppService } from '../app.service';

@Component({
  selector: 'app-watchlist',
  templateUrl: './watchlist.component.html',
  styleUrls: ['./watchlist.component.css']
})
export class WatchlistComponent implements OnInit {
  latestPrice: any= [];
  profile: any= [];
  storageData: any=[];
  prices: any=[];
  // item: any=[];

  all_data = [];

  upOrDown = 1;
  

  constructor(private postsService : AppService) { }


  ngOnInit(): void {
    this.getStorageData()
  }

  ngOnChanges(changes: SimpleChanges): void {
  }

  getStorageData() {
    // this.all_data = []
    // if (localStorage.getItem("tickers") != null) {
    //   this.storageData = eval(localStorage.getItem("tickers"))
    //   for (let i = 0; i < this.storageData.length; i++) {
    //     this.getLatestPriceData(this.storageData[i]);
    //   }
    // }
    if (localStorage.getItem("tickers") == null) {
      this.all_data = []
    } else {
      this.storageData = eval(localStorage.getItem("tickers"))
      for (var i = 0; i < this.storageData.length; i++) {
        this.getLatestPriceData(this.storageData[i]);
      }
    }
  }

  getLatestPriceData(ticker: string) {
    var item = {}
    this.postsService.getLatestPrice(ticker).subscribe(lp=>{
      this.latestPrice = lp;
      var color;
      if (this.latestPrice[0].d > 0) {
        this.upOrDown = 1;
        color = 'green'
      } else {
        this.upOrDown = 2;
        color = 'red'
      }
      this.latestPrice[0].dp = +this.latestPrice[0].dp.toFixed(2)

      item['c'] = this.latestPrice[0].c
      item['upOrDown'] = this.upOrDown
      item['color'] = color
      item['d'] = this.latestPrice[0].d
      item['dp'] = this.latestPrice[0].dp
    })

    this.postsService.getBasicInfo(ticker).subscribe((response: any)=>{
      this.profile = response[0];
      item['name'] = this.profile.name
      item['ticker'] = this.profile.ticker
    })

    this.all_data.push(item)
  }

  remove(ticker: string) {
    var storagelist = eval(localStorage.getItem('tickers'))
    let index = storagelist.indexOf(ticker);
    if (index > -1) {
      storagelist.splice(index, 1);
      this.all_data.splice(index, 1);
    }
    localStorage.setItem('tickers', JSON.stringify(storagelist))
    // this.getStorageData()
  }

}
