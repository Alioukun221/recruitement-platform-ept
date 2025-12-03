import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EvaluateDetailsComponent } from './evaluate-details-component';

describe('EvaluateDetailsComponent', () => {
  let component: EvaluateDetailsComponent;
  let fixture: ComponentFixture<EvaluateDetailsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EvaluateDetailsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EvaluateDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
