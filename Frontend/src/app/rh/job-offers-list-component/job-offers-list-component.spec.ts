import { ComponentFixture, TestBed } from '@angular/core/testing';

import { JobOffersListComponent } from './job-offers-list-component';

describe('JobOffersListComponent', () => {
  let component: JobOffersListComponent;
  let fixture: ComponentFixture<JobOffersListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [JobOffersListComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(JobOffersListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
