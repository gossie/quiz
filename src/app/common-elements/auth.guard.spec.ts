import { TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';

import { AuthGuard } from './auth.guard';
import { CommonElementsModule } from './common-elements.module';
import { QuizService } from './quiz.service';

describe('AuthGuard', () => {
    let guard: AuthGuard;

    beforeEach(() => {  
        TestBed.configureTestingModule({
            imports: [CommonElementsModule, RouterTestingModule]
        });
        guard = TestBed.inject(AuthGuard);
    });

    it('should be created', () => {
        expect(guard).toBeTruthy();
    });
});
