import { ComponentFixture, TestBed } from '@angular/core/testing';

import { QuizHostComponent } from './quiz-host.component';

describe('QuizHostComponent', () => {
  let component: QuizHostComponent;
  let fixture: ComponentFixture<QuizHostComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ QuizHostComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(QuizHostComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
