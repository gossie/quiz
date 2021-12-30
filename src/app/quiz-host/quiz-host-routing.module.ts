import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { QuizHostComponent } from './quiz-host.component';

const routes: Routes = [{ path: '', component: QuizHostComponent }];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class QuizHostRoutingModule { }
