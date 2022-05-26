import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SearhAreaComponent } from './searh-area.component';

describe('SearhAreaComponent', () => {
  let component: SearhAreaComponent;
  let fixture: ComponentFixture<SearhAreaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SearhAreaComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SearhAreaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
