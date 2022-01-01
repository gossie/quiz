import { Injectable } from '@angular/core';
import { CommonElementsModule } from './common-elements.module';

@Injectable({
  providedIn: CommonElementsModule
})
export class QuizService {

    constructor() { }

    public hasQuiz(): boolean {
        return false;
    }

}
