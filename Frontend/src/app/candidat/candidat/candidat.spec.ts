import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Candidat } from './candidat';

describe('Candidat', () => {
  let component: Candidat;
  let fixture: ComponentFixture<Candidat>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Candidat]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Candidat);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
