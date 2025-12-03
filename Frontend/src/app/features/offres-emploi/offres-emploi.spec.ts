import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OffresEmploi } from './offres-emploi';

describe('OffresEmploi', () => {
  let component: OffresEmploi;
  let fixture: ComponentFixture<OffresEmploi>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OffresEmploi]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OffresEmploi);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
