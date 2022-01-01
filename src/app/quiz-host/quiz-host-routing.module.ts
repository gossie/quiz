import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from '../common-elements/auth.guard';
import { LoginPageComponent } from './login-page/login-page.component';
import { QuizHostComponent } from './quiz-host.component';

const routes: Routes = [
    { path: 'quiz', component: QuizHostComponent, canActivate: [AuthGuard] },
    { path: 'login', component: LoginPageComponent }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class QuizHostRoutingModule { }
