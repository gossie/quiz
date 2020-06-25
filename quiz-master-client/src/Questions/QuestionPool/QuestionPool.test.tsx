import React from 'react';
import { render, unmountComponentAtNode } from "react-dom";
// import { render, wait } from '@testing-library/react';
import Quiz from '../../quiz-client-shared/quiz';
import QuestionPool from './QuestionPool';
import { act } from 'react-dom/test-utils';

let container: HTMLDivElement | null;
beforeEach(() => {
  container = document.createElement("div");
  document.body.appendChild(container);
});

afterEach(() => {
  unmountComponentAtNode(container!);
  container!.remove();
  container = null;
});

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
            return Promise.resolve();
        } else {
            return Promise.reject();
        }
        
    });

    const quiz: Quiz = {
        id: '5',
        name: "Awesome Quiz",
        participants: [],
        playedQuestions: [],
        openQuestions: [],
        timestamp: 1234,
        links: [{href: '/api/createQuestion', rel: 'createQuestion'}]
    }

    await act(async () => {
        render(<QuestionPool quiz={quiz} setImageToDisplay={(path) => {}} />, container);
    });

    const addQuestionButton = container!.querySelector('span[data-testid="add-question-0"]') as HTMLButtonElement;
    addQuestionButton!.click();
});
