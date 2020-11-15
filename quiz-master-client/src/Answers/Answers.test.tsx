import React from 'react';
import { render } from '@testing-library/react';
import Answers from './Answers';
import Quiz from '../quiz-client-shared/quiz';

test('should show buttons', () => {
    const quiz: Quiz = {
        id: '17',
        name: 'Test',
        participants: [
            {
                id: '12',
                name: 'Lena',
                turn: false,
                points: 13,
                links: []
            },
            {
                id: '13',
                name: 'Erik',
                turn: true,
                points: 13,
                links: []
            }
        ],
        openQuestions: [],
        playedQuestions: [],
        links: []
    }

    const { getByTestId } = render(<Answers quiz={quiz} participant={quiz.participants[1]} />);

    expect(getByTestId('correct-button')).toBeDefined();
    expect(getByTestId('incorrect-button')).toBeDefined();
});

test('should answer correctly', () => {
    jest.spyOn(global, 'fetch').mockImplementation((url: string, request: object) => {
        expect(url).toEqual('http://localhost:5000/api/quiz/17/participants/13/answers');
        expect(request).toEqual({
            method: 'POST',
            body: 'true',
            headers: {
                'Content-Type': 'text/plain',
                Accept: 'application/json'
            }
        });
        Promise.resolve();
    });

    const quiz: Quiz = {
        id: '17',
        name: 'Test',
        participants: [
            {
                id: '12',
                name: 'Lena',
                turn: false,
                points: 13,
                links: [],
                revealAllowed: true
            },
            {
                id: '13',
                name: 'Erik',
                turn: true,
                points: 13,
                links: [],
                revealAllowed: true
            }
        ],
        openQuestions: [],
        playedQuestions: [],
        links: []
    }

    const { getByTestId } = render(<Answers quiz={quiz} participant={quiz.participants[1]} />);

    getByTestId('correct-button').click();
});

test('should answer correctly', () => {
    jest.spyOn(global, 'fetch').mockImplementation((url: string, request: object) => {
        expect(url).toEqual('http://localhost:5000/api/quiz/17/participants/13/answers');
        expect(request).toEqual({
            method: 'POST',
            body: 'false',
            headers: {
                'Content-Type': 'text/plain',
                Accept: 'application/json'
            }
        });
        Promise.resolve();
    });

    const quiz: Quiz = {
        id: '17',
        name: 'Test',
        participants: [
            {
                id: '12',
                name: 'Lena',
                turn: false,
                points: 13,
                links: [],
                revealAllowed: true
            },
            {
                id: '13',
                name: 'Erik',
                turn: true,
                points: 13,
                links: [],
                revealAllowed: true
            }
        ],
        playedQuestions: [],
        openQuestions: [],
        links: []
    }

    const { getByTestId } = render(<Answers quiz={quiz} participant={quiz.participants[1]} />);

    getByTestId('incorrect-button').click();
});
