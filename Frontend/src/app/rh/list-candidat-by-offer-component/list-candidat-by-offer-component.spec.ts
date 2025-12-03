import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListCandidatByOfferComponent } from './list-candidat-by-offer-component';

describe('ListCandidatByOfferComponent', () => {
  let component: ListCandidatByOfferComponent;
  let fixture: ComponentFixture<ListCandidatByOfferComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ListCandidatByOfferComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ListCandidatByOfferComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
