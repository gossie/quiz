import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

const routes: Routes = [
    { path: '', redirectTo: 'quiz-host', pathMatch: 'full' },
    { path: 'quiz-host', loadChildren: () => import('./quiz-host/quiz-host.module').then(m => m.QuizHostModule) },
    { path: 'quiz-participant', loadChildren: () => import('./quiz-participant/quiz-participant.module').then(m => m.QuizParticipantModule) }
];

@NgModule({
    imports: [RouterModule.forRoot(routes, { useHash: true })],
    exports: [RouterModule]
})
export class AppRoutingModule { }
