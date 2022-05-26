import { BrowserModule } from '@angular/platform-browser';
import { Component, NgModule } from '@angular/core';

import { AppComponent } from './app.component';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

import { MatInputModule } from '@angular/material/input';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
// import { BasicInfoComponent } from './basic-info/basic-info.component';

// import { Routes, RouterModule } from '@angular/router';
import { AppRoutingModule, routingComponents } from './app-routing.module';

import { HttpClientModule } from '@angular/common/http';

import {MatTabsModule} from '@angular/material/tabs';

import { HighchartsChartModule } from 'highcharts-angular';


import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { WatchlistComponent } from './watchlist/watchlist.component';
import { StockchartComponent } from './stockchart/stockchart.component';
import { PortfolioComponent } from './portfolio/portfolio.component';
// import { SearhAreaComponent } from './searh-area/searh-area.component';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';



@NgModule({
  declarations: [
    AppComponent,
    // BasicInfoComponent
    routingComponents,
    WatchlistComponent,
    StockchartComponent,
    PortfolioComponent
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    NgbModule,
    BrowserAnimationsModule,
    MatInputModule,
    MatAutocompleteModule,
    MatTabsModule,
    HighchartsChartModule,
    FormsModule,
    AppRoutingModule,
    ReactiveFormsModule,
    MatProgressSpinnerModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
