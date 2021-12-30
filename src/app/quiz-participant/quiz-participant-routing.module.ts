import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { QuizParticipantComponent } from './quiz-participant.component';

const routes: Routes = [{ path: '', component: QuizParticipantComponent }];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class QuizParticipantRoutingModule { }
