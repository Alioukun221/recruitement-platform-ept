import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CommissionCandidatDetailComponents } from './commission-candidat-detail-components';

describe('CommissionCandidatDetailComponents', () => {
  let component: CommissionCandidatDetailComponents;
  let fixture: ComponentFixture<CommissionCandidatDetailComponents>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CommissionCandidatDetailComponents]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CommissionCandidatDetailComponents);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
