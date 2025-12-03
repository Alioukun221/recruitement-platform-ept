import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ContentHeader } from './content-header';

describe('ContentHeader', () => {
  let component: ContentHeader;
  let fixture: ComponentFixture<ContentHeader>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ContentHeader]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ContentHeader);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
