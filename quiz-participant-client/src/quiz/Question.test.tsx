import React from 'react';
import { render } from '@testing-library/react';
import Quiz from '../quiz-client-shared/quiz';
import Question from './Question';

jest.mock('react-i18next', () => ({
    useTranslation: () => {
        return {
            t: (str: string, keys: object) => str === 'secondsLeft' ? `${keys['seconds']} seconds left` : null,
            i18n: {
                changeLanguage: () => new Promise(() => {}),
            },
        };
        
    },
}));

test('has no questions', () => {
    const quiz: Quiz = {
        id: '5',
        name: "Awesome Quiz",
        participants: [
            {
                id: '1234',
                name: 'Max',
                points: 0,
                turn: false,
                revealAllowed: true,
                links: []
            }
        ],
        openQuestions: [],
        links: []
    }
    const { getByTestId } = render(<Question quiz={quiz} participantId="1234" />);
    
    expect(() => getByTestId('current-question')).toThrowError('Unable to find an element by: [data-testid="current-question"]');
});

test('has no pending question', () => {
    const quiz: Quiz = {
        id: '5',
        name: "Awesome Quiz",
        participants: [
            {
                id: '1234',
                name: 'Max',
                points: 0,
                turn: false,
                revealAllowed: true,
                links: []
            }
        ],
        openQuestions: [
            {
                id: '1235',
                question: "What is happening?",
                pending: false,
                revealed: false,
                links: []
            }
        ],
        links: []
    }
    const { getByTestId } = render(<Question quiz={quiz} participantId="1234" />);
    
    expect(() => getByTestId('current-question')).toThrowError('Unable to find an element by: [data-testid="current-question"]');
});

test('has pending question', () => {
    const quiz: Quiz = {
        id: '5',
        name: "Awesome Quiz",
        participants: [
            {
                id: '1234',
                name: 'Max',
                points: 0,
                turn: false,
                revealAllowed: true,
                links: []
            }
        ],
        openQuestions: [
            {
                id: '1235',
                question: 'What is happening?',
                pending: true,
                revealed: false,
                links: []
            }
        ],
        links: []
    }
    const { getByTestId } = render(<Question quiz={quiz} participantId="1234" />);

    const currentQuestion = getByTestId('current-question');
    
    expect(currentQuestion.textContent).toEqual('What is happening?');
    expect(() => getByTestId('question-counter')).toThrowError();
});

test('has pending question with counter', () => {
    const quiz: Quiz = {
        id: '5',
        name: "Awesome Quiz",
        participants: [
            {
                id: '1234',
                name: 'Max',
                points: 0,
                turn: false,
                revealAllowed: true,
                links: []
            }
        ],
        openQuestions: [
            {
                id: '1235',
                question: 'What is happening?',
                secondsLeft: 17,
                pending: true,
                revealed: false,
                links: []
            }
        ],
        links: []
    }
    const { getByTestId } = render(<Question quiz={quiz} participantId="1234" />);

    const currentQuestion = getByTestId('current-question');
    const countdown = getByTestId('question-counter') as HTMLSpanElement;
    
    expect(currentQuestion.textContent).toEqual('What is happening?');
    expect(countdown.textContent).toEqual('17 seconds left');
});
