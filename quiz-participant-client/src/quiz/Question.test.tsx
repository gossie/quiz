import React from 'react';
import { render } from '@testing-library/react';
import Quiz from './quiz';
import Question from './Question';

test('has no questions', () => {
    const quiz: Quiz = {
        id: 5,
        name: "Awesome Quiz",
        participants: [],
        openQuestions: [],
        links: []
    }
    const { getByTestId } = render(<Question quiz={quiz} />);
    
    expect(() => getByTestId('current-question')).toThrowError('Unable to find an element by: [data-testid="current-question"]');
});

test('has no pending question', () => {
    const quiz: Quiz = {
        id: 5,
        name: "Awesome Quiz",
        participants: [],
        openQuestions: [
            {
                question: "What is happening?",
                pending: false,
                links: []
            }
        ],
        links: []
    }
    const { getByTestId } = render(<Question quiz={quiz} />);
    
    expect(() => getByTestId('current-question')).toThrowError('Unable to find an element by: [data-testid="current-question"]');
});

test('has pending question', () => {
    const quiz: Quiz = {
        id: 5,
        name: "Awesome Quiz",
        participants: [],
        openQuestions: [
            {
                question: 'What is happening?',
                pending: true,
                links: []
            }
        ],
        links: []
    }
    const { getByTestId } = render(<Question quiz={quiz} />);

    const currentQuestion = getByTestId('current-question');
    
    expect(currentQuestion.textContent).toEqual('What is happening?');
    expect(() => getByTestId('question-counter')).toThrowError();
});

test('has pending question with counter', () => {
    const quiz: Quiz = {
        id: 5,
        name: "Awesome Quiz",
        participants: [],
        openQuestions: [
            {
                question: 'What is happening?',
                secondsLeft: 17,
                pending: true,
                links: []
            }
        ],
        links: []
    }
    const { getByTestId } = render(<Question quiz={quiz} />);

    const currentQuestion = getByTestId('current-question');
    const countdown = getByTestId('question-counter') as HTMLSpanElement;
    
    expect(currentQuestion.textContent).toEqual('What is happening?');
    expect(countdown.textContent).toEqual('17 seconds left');
});
