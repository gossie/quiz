import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { QuizHostRoutingModule } from './quiz-host-routing.module';
import { QuizHostComponent } from './quiz-host.component';
import { LoginPageComponent } from './login-page/login-page.component';
import { CommonElementsModule } from '../common-elements/common-elements.module';


@NgModule({
    declarations: [
        QuizHostComponent,
        LoginPageComponent
    ],
    imports: [
        CommonModule,
        CommonElementsModule,
        QuizHostRoutingModule
    ]
})
export class QuizHostModule { }
