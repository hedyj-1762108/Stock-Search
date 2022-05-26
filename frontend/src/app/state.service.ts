import { Injectable } from '@angular/core';
import { Subject, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class StateService {
  ticker = new Subject<string>();

  constructor() { }

  public getMessage(): Observable<string> {
    return this.ticker.asObservable();
  }

  public updateMessage(newticker: string): void {
    this.ticker.next(newticker);
  }
   
}
