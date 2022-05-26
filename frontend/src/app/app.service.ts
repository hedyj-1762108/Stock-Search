import { Injectable } from '@angular/core';
import { HttpClient, HttpClientModule, HttpHeaders } from '@angular/common/http';
import {Observable} from 'rxjs';
import {map, tap} from 'rxjs/operators';

const options = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json'})
};


@Injectable({
  providedIn: 'root'
})
export class AppService {
  // message: any;
  private hostname = "https://hw-8-csci571.wl.r.appspot.com/";

  constructor(private http : HttpClient) { }
    getData(ticker: string){
      return this.http.get(this.hostname + '/autoC?ticker=' + ticker, options);
    }    

    getBasicInfo(ticker: string){
      return this.http.get(this.hostname + '/posts?ticker=' + ticker, options);
    }

    getLatestPrice(ticker: string){
      // return this.http.get('/latestPrice');
      return this.http.get(this.hostname + '/latestPrice?ticker=' + ticker, options);
    }

    getPeerInfo(ticker: string){
      // return this.http.get('/peer');
      return this.http.get(this.hostname + '/peer?ticker=' + ticker, options);
    }

    // getSixHourPrice(ticker: string) {
    //   // return this.http.get('/sixHourPrice');
    //   return this.http.get(this.hostname + '/sixHourPrice?ticker=' + ticker, options);
    // }

    getSixHourPrice(ticker: string, timestamp: number) {
      // return this.http.get('/sixHourPrice');
      return this.http.get(this.hostname + '/sixHourPrice?ticker=' + ticker + '&to=' + timestamp, options);      
    }

    getNews(ticker: string) {
      // return this.http.get('/news');
      return this.http.get(this.hostname + '/news?ticker=' + ticker, options);
    }

    getChart(ticker: string) {
      // return this.http.get('/chart');
      return this.http.get(this.hostname + '/chart?ticker=' + ticker, options);
    }

    getSentiment(ticker: string) {
      // return this.http.get('/sentiment');
      return this.http.get(this.hostname + '/sentiment?ticker=' + ticker, options);
    }

    getRec(ticker: string) {
      // return this.http.get('/rec');
      return this.http.get(this.hostname + '/rec?ticker=' + ticker, options);
    }

    getEarn(ticker: string) {
      // return this.http.get('/earn');
      return this.http.get(this.hostname + '/earn?ticker=' + ticker, options);
    }
    
}
