import { TestBed } from '@angular/core/testing';

import { SearhAreaService } from './searh-area.service';

describe('SearhAreaService', () => {
  let service: SearhAreaService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SearhAreaService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
