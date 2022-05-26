import { Component, OnInit, SimpleChanges, Input} from '@angular/core';
// import { FormBuilder } from '@angular/forms';
import { AppService } from './app.service';
import { HttpClient } from '@angular/common/http';

import { StateService } from './state.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit{
  public isMenuCollapsed = true;
  options: any= [];
  
  ticker:string='home';

  ticker_link:string = ''
  public subscription: Subscription;

  input_ticker: string = '';

  
  constructor(private appService : AppService, private service: StateService){}

  public ngOnDestroy(): void {
    this.subscription.unsubscribe(); // onDestroy cancels the subscribe request
  }

  ngOnInit(): void {
    this.subscription = this.service.getMessage().subscribe(msg =>{ 
      this.ticker = msg
      this.ticker_link = 'search/' + msg
    });
  }

  ngOnChanges(changes: SimpleChanges): void {

  }

  

  handleClear() {
    this.input_ticker = '';
  }

  

}
