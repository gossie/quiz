import { TestBed } from '@angular/core/testing';
import { CommonElementsModule } from './common-elements.module';

import { QuizService } from './quiz.service';

describe('QuizService', () => {
    let service: QuizService;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports:[CommonElementsModule]
        });
        service = TestBed.inject(QuizService);
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });
});
