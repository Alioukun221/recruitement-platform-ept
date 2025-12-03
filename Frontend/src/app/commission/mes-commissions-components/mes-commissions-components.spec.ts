import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MesCommissionsComponents } from './mes-commissions-components';

describe('MesCommissionsComponents', () => {
  let component: MesCommissionsComponents;
  let fixture: ComponentFixture<MesCommissionsComponents>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MesCommissionsComponents]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MesCommissionsComponents);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
