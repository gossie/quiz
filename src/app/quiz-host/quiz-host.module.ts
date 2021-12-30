import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { QuizHostRoutingModule } from './quiz-host-routing.module';
import { QuizHostComponent } from './quiz-host.component';


@NgModule({
  declarations: [
    QuizHostComponent
  ],
  imports: [
    CommonModule,
    QuizHostRoutingModule
  ]
})
export class QuizHostModule { }
