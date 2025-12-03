import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CommissionsListComponent } from './commissions-list-component';

describe('CommissionsListComponent', () => {
  let component: CommissionsListComponent;
  let fixture: ComponentFixture<CommissionsListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CommissionsListComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CommissionsListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
