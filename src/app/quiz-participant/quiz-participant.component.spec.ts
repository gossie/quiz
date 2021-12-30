import { ComponentFixture, TestBed } from '@angular/core/testing';

import { QuizParticipantComponent } from './quiz-participant.component';

describe('QuizParticipantComponent', () => {
  let component: QuizParticipantComponent;
  let fixture: ComponentFixture<QuizParticipantComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ QuizParticipantComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(QuizParticipantComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
