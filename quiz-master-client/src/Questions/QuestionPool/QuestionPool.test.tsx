import React from 'react';
import { cleanup, render, waitFor } from '../../test-utils';
import Quiz from '../../quiz-client-shared/quiz';
import QuestionPool from './QuestionPool';

beforeEach(() => cleanup());
afterEach(() => cleanup());

jest.mock('react-i18next', () => ({
    useTranslation: () => {
        return {
            t: (str: string, keys: object) => str,
            i18n: {
                changeLanguage: () => new Promise(() => {}),
            },
        };
        
    },
}));

test('should determine question pool', async () => {
    jest.spyOn(global, 'fetch').mockImplementation((url: string, request: object) => {
        if (url === 'http://localhost:5000/api/questionPool?category=other') {
            expect(request).toEqual({
                method: 'GET',
                headers: {
                    Accept: 'application/json'
                }
            });
            return Promise.resolve({
                json: () => Promise.resolve([
                    {
                        id: '1',
                        question: 'Frage 1',
                        links: []
                    },
                    {
                        id: '2',
                        question: 'Frage 2',
                        links: []
                    }
                ])
            });
        } else if (url === 'http://localhost:5000/api/createQuestion') {
            expect(request).toEqual({
                method: 'POST',
                body: JSON.stringify({
                    question: 'Frage 1'
                }),
                headers: {
                    'Content-Type': 'application/json',
                    Accept: 'application/json'
                }
            });
            return Promise.resolve({ status: 201 });
        } else {
            return Promise.reject();
        }
        
    });

    const quiz: Quiz = {
        id: '5',
        name: "Awesome Quiz",
        points: 2,
        participants: [],
        playedQuestions: [],
        openQuestions: [],
        expirationDate: 1234,
        timestamp: 1234,
        links: [{href: '/api/createQuestion', rel: 'createQuestion'}]
    }

    const { getByTestId } = render(<QuestionPool quiz={quiz} setImageToDisplay={(path) => {}} />);

    await waitFor(() => {
        try {
            getByTestId('add-question-0')
            return true;
        } catch (e) {
            return false;
        }
    });
    const addQuestionButton = getByTestId('add-question-0') as HTMLButtonElement;

    addQuestionButton!.click();
});
