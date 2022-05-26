import { Injectable } from '@angular/core';
import { HttpClient, HttpClientModule, HttpHeaders } from '@angular/common/http';
// import { map } from "rxjs/operators"; 

const options = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json'})
};

@Injectable({
  providedIn: 'root'
})
export class PostService {

  private hostname = "http://localhost:4600";

  constructor(private http : HttpClient) { }
    // getBasicInfo(ticker: string){
    //   // return this.http.get('/posts');
    //   return this.http.get(this.hostname + '/posts', options);
    //   // return this.http.get(this.hostname + '/movievideo?movie_id=' + id, options);
    // }

    // getLatestPrice(){
    //   // return this.http.get('/latestPrice');
    //   return this.http.get(this.hostname + '/latestPrice', options);
    // }

    // getPeerInfo(){
    //   // return this.http.get('/peer');
    //   return this.http.get(this.hostname + '/peer', options);
    // }

    // getSixHourPrice() {
    //   // return this.http.get('/sixHourPrice');
    //   return this.http.get(this.hostname + '/sixHourPrice', options);
    // }

    // getNews() {
    //   // return this.http.get('/news');
    //   return this.http.get(this.hostname + '/news', options);
    // }

    // getChart() {
    //   // return this.http.get('/chart');
    //   return this.http.get(this.hostname + '/chart', options);
    // }

    // getSentiment() {
    //   // return this.http.get('/sentiment');
    //   return this.http.get(this.hostname + '/sentiment', options);
    // }

    // getRec() {
    //   // return this.http.get('/rec');
    //   return this.http.get(this.hostname + '/rec', options);
    // }

    // getEarn() {
    //   // return this.http.get('/earn');
    //   return this.http.get(this.hostname + '/earn', options);
    // }
}