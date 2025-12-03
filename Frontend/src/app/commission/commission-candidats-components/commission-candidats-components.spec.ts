import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CommissionCandidatsComponents } from './commission-candidats-components';

describe('CommissionCandidatsComponents', () => {
  let component: CommissionCandidatsComponents;
  let fixture: ComponentFixture<CommissionCandidatsComponents>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CommissionCandidatsComponents]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CommissionCandidatsComponents);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
