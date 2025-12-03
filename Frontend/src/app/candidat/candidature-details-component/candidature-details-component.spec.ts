import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CandidatureDetailsComponent } from './candidature-details-component';

describe('CandidatureDetailsComponent', () => {
  let component: CandidatureDetailsComponent;
  let fixture: ComponentFixture<CandidatureDetailsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CandidatureDetailsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CandidatureDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
