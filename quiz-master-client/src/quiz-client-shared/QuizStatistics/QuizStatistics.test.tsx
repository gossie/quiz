import React from 'react';
import { render } from '@testing-library/react';
import Quiz from '../quiz';
import QuizStatistics from './QuizStatistics';

test('does not display statistics', () => {
    const quiz: Quiz = {
        id: '1',
        name: 'Quiz',
        participants: [
            {
                id: '15',
                name: 'Erik',
                turn: false,
                points: 0,
                links: []
            },
            {
                id: '16',
                name: 'Sandra',
                turn: false,
                points: 2,
                links: []
            }
        ],
        openQuestions: [],
        playedQuestions: [],
        links: []
    }

    const { getByTestId } = render(<QuizStatistics quiz={quiz} closeable={false} forceOpen={false} />);

    expect(() => getByTestId('quiz-statistics')).toThrowError();
});

test('displays statistics', () => {
    const quiz: Quiz = {
        id: '1',
        name: 'Quiz',
        participants: [
            {
                id: '15',
                name: 'Erik',
                turn: false,
                points: 0,
                links: []
            },
            {
                id: '16',
                name: 'Sandra',
                turn: false,
                points: 2,
                links: []
            }
        ],
        openQuestions: [],
        playedQuestions: [],
        quizStatistics: {
            questionStatistics: [
                {
                    question: {
                        id: '7',
                        question: "Warum?",
                        pending: false,
                        links: []
                    },
                    buzzerStatistics: []
                }
            ]
        },
        links: []
    }

    const { getByTestId } = render(<QuizStatistics quiz={quiz} closeable={false} forceOpen={false} />);

    expect(() => getByTestId('quiz-statistics')).not.toThrowError();
});

test('should close statistics', () => {
    const quiz: Quiz = {
        id: '1',
        name: 'Quiz',
        participants: [
            {
                id: '15',
                name: 'Erik',
                turn: false,
                points: 0,
                links: []
            },
            {
                id: '16',
                name: 'Sandra',
                turn: false,
                points: 2,
                links: []
            }
        ],
        openQuestions: [],
        playedQuestions: [],
        quizStatistics: {
            questionStatistics: [
                {
                    question: {
                        id: '7',
                        question: "Warum?",
                        pending: false,
                        links: []
                    },
                    buzzerStatistics: []
                }
            ]
        },
        links: []
    }

    const { getByTestId } = render(<QuizStatistics quiz={quiz} closeable={true} forceOpen={false} />);

    const closeButton = getByTestId('close-button');

    closeButton.click();

    expect(() => getByTestId('quiz-statistics')).toThrowError();
});

test('should not be closeable', () => {
    const quiz: Quiz = {
        id: '1',
        name: 'Quiz',
        participants: [
            {
                id: '15',
                name: 'Erik',
                turn: false,
                points: 0,
                links: []
            },
            {
                id: '16',
                name: 'Sandra',
                turn: false,
                points: 2,
                links: []
            }
        ],
        openQuestions: [],
        playedQuestions: [],
        quizStatistics: {
            questionStatistics: [
                {
                    question: {
                        id: '7',
                        question: "Warum?",
                        pending: false,
                        links: []
                    },
                    buzzerStatistics: []
                }
            ]
        },
        links: []
    }

    const { getByTestId } = render(<QuizStatistics quiz={quiz} closeable={false} forceOpen={false} />);

    expect(() => getByTestId('close-button')).toThrowError();
});
