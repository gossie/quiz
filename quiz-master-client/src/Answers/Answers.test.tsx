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

    const { getByTestId } = render(<Answers quiz={quiz} />);

    expect(getByTestId('correct-button')).toBeDefined();
    expect(getByTestId('incorrect-button')).toBeDefined();
    expect(getByTestId('reopen-button')).toBeDefined();
});

test('should answer correctly', () => {
    jest.spyOn(global, 'fetch').mockImplementation((url: string, request: object) => {
        expect(url).toEqual('http://localhost:5000/api/answer');
        expect(request).toEqual({
            method: 'PATCH',
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
                links: []
            },
            {
                id: 13,
                name: 'Erik',
                turn: true,
                points: 13,
                links: []
            }
        ],
        openQuestions: [],
        playedQuestions: [],
        links: [{ rel: 'answer', href: '/api/answer' }]
    }

    const { getByTestId } = render(<Answers quiz={quiz} />);

    getByTestId('correct-button').click();
});

test('should answer correctly', () => {
    jest.spyOn(global, 'fetch').mockImplementation((url: string, request: object) => {
        expect(url).toEqual('http://localhost:5000/api/answer');
        expect(request).toEqual({
            method: 'PATCH',
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
        playedQuestions: [],
        openQuestions: [],
        links: [{ rel: 'answer', href: '/api/answer' }]
    }

    const { getByTestId } = render(<Answers quiz={quiz} />);

    getByTestId('incorrect-button').click();
});

test('should reopen question', () => {
    jest.spyOn(global, 'fetch').mockImplementation((url: string, request: object) => {
        expect(url).toEqual('http://localhost:5000/api/reopen');
        expect(request).toEqual({
            method: 'PUT',
            headers: {
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
        playedQuestions: [],
        openQuestions: [],
        links: [{ rel: 'reopenQuestion', href: '/api/reopen' }]
    }

    const { getByTestId } = render(<Answers quiz={quiz} />);

    getByTestId('reopen-button').click();
});
