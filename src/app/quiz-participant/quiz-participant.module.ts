import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { QuizParticipantRoutingModule } from './quiz-participant-routing.module';
import { QuizParticipantComponent } from './quiz-participant.component';


@NgModule({
  declarations: [
    QuizParticipantComponent
  ],
  imports: [
    CommonModule,
    QuizParticipantRoutingModule
  ]
})
export class QuizParticipantModule { }
