import { Component, OnInit } from '@angular/core';
import { AppService } from '../app.service';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';


@Component({
  selector: 'app-portfolio',
  templateUrl: './portfolio.component.html',
  styleUrls: ['./portfolio.component.css']
})
export class PortfolioComponent implements OnInit {
  money: any
  boughtList: any[]
  boughtLength: number;
  latestPrice: any= [];
  moneyNeeded: any;
  moneyback: any;
  input_q: number;
  input_q_sell: number;
  all_data = []
  exceed = false;
  sellToMuch = false;
  // bought = false;

  Bought_ALERTS: any[] = []
  Sell_ALERTS: any[] = []



  constructor(private postsService : AppService, private modalService: NgbModal) { }
  
  ngOnInit(): void {
    this.checkMoney()
    this.getStorageData()
  }

  sellIt(ticker: string){
    this.Sell_ALERTS.push(ticker);
    var list = eval(localStorage.getItem('bought'))
      for(let i = 0; i < list.length; i++) {
        if (list[i]['ticker'] == ticker) {
          list[i]['quantity'] = list[i]['quantity'] - this.input_q_sell;
          list[i]['totalcost'] = list[i]['totalcost'] - this.moneyback;
          if (list[i]['quantity'] == 0) {
            list.splice(i, 1); 
            this.all_data.splice(i, 1)
            localStorage.setItem('bought', JSON.stringify(list))
            this.money = (+(this.money) + +(this.moneyback)).toFixed(2)
            localStorage.setItem('money', this.money.toString())
          } else {
            localStorage.setItem('bought', JSON.stringify(list))
            this.money = (+(this.money) + +(this.moneyback)).toFixed(2)
            localStorage.setItem('money', this.money.toString())
            for (var j = 0; i < this.all_data.length; j++) {
              if (this.all_data[j]['ticker'] == ticker) {
                this.all_data[j]['quantity'] = ((+(this.all_data[j]['quantity'])) - (+(this.input_q_sell))).toFixed(2)
                this.all_data[j]['totalCost'] = (+(this.all_data[j]['totalCost']) - +(this.moneyback)).toFixed(2)
                this.all_data[j]['aveCost'] = (this.all_data[j]['totalCost'] / this.all_data[j]['quantity']).toFixed(2)
                this.all_data[j]['marketVal'] = (+this.all_data[j]['curPrice'] * +this.all_data[j]['quantity']).toFixed(2)
              }
            }
          }
        }
      }
      // localStorage.setItem('bought', JSON.stringify(list))
      // this.money = (+(this.money) + +(this.moneyback)).toFixed(2)
      // localStorage.setItem('money', this.money.toString())
  }

  toggle_sell(ticker: string) {
    this.Sell_ALERTS.splice(this.Sell_ALERTS.indexOf(ticker), 1);
  }

  toggle_bought(ticker: string) {
    this.Bought_ALERTS.splice(this.Bought_ALERTS.indexOf(ticker), 1);
  }
  boughtIt(ticker: string) {
    // this.bought = true;
      this.Bought_ALERTS.push(ticker);
      var list = eval(localStorage.getItem('bought'))
      for(let i = 0; i < list.length; i++) {
        if (list[i]['ticker'] == ticker) {
          list[i]['quantity'] = list[i]['quantity'] + this.input_q;
          list[i]['totalcost'] = list[i]['totalcost'] + this.moneyNeeded;
        }
      }
      localStorage.setItem('bought', JSON.stringify(list))
      this.money = +(this.money) - +(this.moneyNeeded)
      localStorage.setItem('money', this.money.toString())

      for (var i = 0; i < this.all_data.length; i++) {
        if (this.all_data[i]['ticker'] == ticker) {
          this.all_data[i]['quantity'] = (+(this.all_data[i]['quantity']) + +(this.input_q)).toFixed(2)
          this.all_data[i]['totalCost'] = (+(this.all_data[i]['totalCost']) + +(this.moneyNeeded)).toFixed(2)
          this.all_data[i]['aveCost'] = +(this.all_data[i]['totalCost'] / this.all_data[i]['quantity']).toFixed(2)
          this.all_data[i]['marketVal'] = (+this.all_data[i]['curPrice'] * +this.all_data[i]['quantity']).toFixed(2)
        }
      }
  }

  sellToMuchorNot(closePrice: number, ticker: string) {
    this.moneyback = ((this.input_q_sell) * (+closePrice)).toFixed(2)
    var list = eval(localStorage.getItem('bought'))
    var quantity = 0;
    for(let i = 0; i < list.length; i++) {
      if (list[i]['ticker'] == ticker) {
        quantity = list[i]['quantity']
      }
    }
    if (quantity < this.input_q_sell) {
        this.sellToMuch = true;
    } else {
        this.sellToMuch = false;
    }

  }

  checkMoneyNeeded(closePrice: number) {
    this.moneyNeeded = this.input_q * closePrice
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
      this.money = (+localStorage.getItem("money")).toFixed(2)
    }
  }

  getStorageData() {
    if (localStorage.getItem("bought") == null) {
      this.boughtLength == 0;
    } else {
      this.boughtList = eval(localStorage.getItem("bought"))
      this.boughtLength = this.boughtList.length
      for (var i = 0; i < this.boughtLength; i++) {
        this.getSpecificData(i)
      }
    }
  }

  getSpecificData(index: number) {
        var item = {};
        item['quantity'] = (+this.boughtList[index].quantity).toFixed(2)
        item['totalCost'] = (+this.boughtList[index].totalcost).toFixed(2)
        item['aveCost'] = (item['totalCost'] / item['quantity']).toFixed(2)
        this.postsService.getLatestPrice(this.boughtList[index].ticker).subscribe(lp=>{
          this.latestPrice = lp;
          item['curPrice'] = +this.latestPrice[0].c.toFixed(2)
          item['change'] = (+item['curPrice'] - +item['aveCost']).toFixed(2)
          if (item['change'] > 0) {
            item['upOrDown'] = 1
          } else if(item['change'] < 0) {
            item['upOrDown'] = 0
          } else {
            item['upOrDown'] = 3
          }
          item['marketVal'] = (+item['curPrice'] * +item['quantity']).toFixed(2)
        })
        item['ticker'] = this.boughtList[index].ticker;
        this.postsService.getBasicInfo(this.boughtList[index].ticker).subscribe((response: any)=>{
          var profile = response[0];
          item['name'] = profile.name
          item['ticker'] = this.boughtList[index].ticker
        })
        this.all_data.push(item)
  }

  openBackDropCustomClass(content) {
    this.modalService.open(content, {backdropClass: 'light-blue-backdrop'});
  }

}
