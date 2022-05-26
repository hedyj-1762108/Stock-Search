import { Component, OnInit} from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AppService } from '../app.service';
import { Observable } from 'rxjs';
import { FormControl } from '@angular/forms';
import { debounceTime, tap, switchMap, finalize, distinctUntilChanged, filter  } from 'rxjs/operators';
import {FormBuilder, FormGroup} from '@angular/forms';
// import { ActivatedRoute } from '@angular/router'

import {ThemePalette} from '@angular/material/core';
import {ProgressSpinnerMode} from '@angular/material/progress-spinner';

import { Router } from '@angular/router'; 

import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-searh-area',
  templateUrl: './searh-area.component.html',
  styleUrls: ['./searh-area.component.css']
})
export class SearhAreaComponent implements OnInit {

  options: any= [];
  input_ticker: string='';
  ticker : string = 'home';
  userInput: string;

  myControl = new FormControl();

  filteredOptions: any;
  minLengthTerm = 0;
  isLoading = false;

  color: ThemePalette = 'primary';
  mode: ProgressSpinnerMode = 'indeterminate';
  value = 5;


  constructor(private appService : AppService, private router: Router, private route: ActivatedRoute){}

  

  ngOnInit(): void {
    var routeP = this.route.snapshot.paramMap.get('ticker')?.toUpperCase()
    if (routeP != 'HOME') {
      this.ticker = this.route.snapshot.paramMap.get('ticker')?.toUpperCase()
    }

    this.myControl.valueChanges
      .pipe(
        filter(res => {
          return res !== null && res.length >= this.minLengthTerm
        }),
        distinctUntilChanged(),
        debounceTime(1000),
        tap(() => {
          // this.errorMsg = "";
          this.filteredOptions = [];
          this.isLoading = true;
        }),
        switchMap(value => this.appService.getData(value)
          .pipe(
            finalize(() => {
              this.isLoading = false
            }),
          )
        )
      )
      .subscribe((data: any) => {
        if (data == undefined) {
          // this.errorMsg = data['Error'];
          this.filteredOptions = [];
        } else {
          // this.errorMsg = "";
          this.filteredOptions = data;
        }
      });
  }


  handleClear() {
    this.input_ticker = '';
    this.router.navigate(['/search', 'home'])
    this.ticker = 'home'
  }

  handleSubmit() {
    this.ticker = this.input_ticker.toUpperCase();
    this.router.navigate(['/search', this.ticker])
  }

  handleSubmitAuto(ticker: string) {
    // if (ticker != 'home') {
      this.ticker = ticker.toUpperCase();
      this.router.navigate(['/search', this.ticker])
    //   console.log(ticker)
    // }
  }

}
