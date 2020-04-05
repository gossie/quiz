import React from 'react';
import { render } from '@testing-library/react';
import Answers from './Answers';
import Quiz from './quiz-client-shared/quiz';

test('should not show buttons because there are no participants', () => {
    const quiz: Quiz = {
        id: 17,
        name: 'Test',
        participants: [],
        questions: [],
        links: []
    }

    const { getByTestId } = render(<Answers quiz={quiz} />);

    expect(() => getByTestId('correct-button')).toThrowError('');
    expect(() => getByTestId('incorrect-button')).toThrowError('');
});

test('should not show buttons because it is nobodys turn', () => {
    const quiz: Quiz = {
        id: 17,
        name: 'Test',
        participants: [{
            id: 12,
            name: 'Lena',
            turn: false,
            points: 13,
            links: []
        }],
        questions: [],
        links: []
    }

    const { getByTestId } = render(<Answers quiz={quiz} />);

    expect(() => getByTestId('correct-button')).toThrowError('');
    expect(() => getByTestId('incorrect-button')).toThrowError('');
});

test('should show buttons', () => {
    const quiz: Quiz = {
        id: 17,
        name: 'Test',
        participants: [
            {
                id: 12,
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
        questions: [],
        links: []
    }

    const { getByTestId } = render(<Answers quiz={quiz} />);

    expect(getByTestId('correct-button')).toBeDefined();
    expect(getByTestId('incorrect-button')).toBeDefined();
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
        id: 17,
        name: 'Test',
        participants: [
            {
                id: 12,
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
        questions: [],
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
        id: 17,
        name: 'Test',
        participants: [
            {
                id: 12,
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
        questions: [],
        links: [{ rel: 'answer', href: '/api/answer' }]
    }

    const { getByTestId } = render(<Answers quiz={quiz} />);

    getByTestId('incorrect-button').click();
});
