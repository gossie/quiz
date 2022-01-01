import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { Observable } from 'rxjs';
import { CommonElementsModule } from './common-elements.module';
import { QuizService } from './quiz.service';

@Injectable({
    providedIn: CommonElementsModule
})
export class AuthGuard implements CanActivate {

    constructor(private quizService: QuizService, private router: Router) {}

    canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
        console.log('AuthGuard#canActivate called');
        if (this.quizService.hasQuiz()) {
            return true;
        }
        return this.router.parseUrl("/quiz-host/login");
    }
  
}
