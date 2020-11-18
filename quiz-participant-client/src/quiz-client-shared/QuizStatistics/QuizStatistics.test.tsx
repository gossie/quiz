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
                revealAllowed: true,
                links: []
            },
            {
                id: '16',
                name: 'Sandra',
                turn: false,
                points: 2,
                revealAllowed: true,
                links: []
            }
        ],
        openQuestions: [],
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
                revealAllowed: true,
                links: []
            },
            {
                id: '16',
                name: 'Sandra',
                turn: false,
                points: 2,
                revealAllowed: true,
                links: []
            }
        ],
        openQuestions: [],
        quizStatistics: {
            questionStatistics: [
                {
                    question: {
                        id: '7',
                        question: "Warum?",
                        pending: false,
                        revealed: true,
                        links: []
                    },
                    answerStatistics: []
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
                revealAllowed: true,
                links: []
            },
            {
                id: '16',
                name: 'Sandra',
                turn: false,
                points: 2,
                revealAllowed: true,
                links: []
            }
        ],
        openQuestions: [],
        quizStatistics: {
            questionStatistics: [
                {
                    question: {
                        id: '7',
                        question: "Warum?",
                        pending: false,
                        revealed: true,
                        links: []
                    },
                    answerStatistics: []
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
                revealAllowed: true,
                links: []
            },
            {
                id: '16',
                name: 'Sandra',
                turn: false,
                points: 2,
                revealAllowed: true,
                links: []
            }
        ],
        openQuestions: [],
        quizStatistics: {
            questionStatistics: [
                {
                    question: {
                        id: '7',
                        question: "Warum?",
                        pending: false,
                        revealed: true,
                        links: []
                    },
                    answerStatistics: []
                }
            ]
        },
        links: []
    }

    const { getByTestId } = render(<QuizStatistics quiz={quiz} closeable={false} forceOpen={false} />);

    expect(() => getByTestId('close-button')).toThrowError();
});

test('displays buzzer answer', () => {
    const quiz: Quiz = {
        id: '1',
        name: 'Quiz',
        participants: [
            {
                id: '15',
                name: 'Erik',
                turn: false,
                points: 0,
                revealAllowed: true,
                links: []
            },
            {
                id: '16',
                name: 'Sandra',
                turn: false,
                points: 2,
                revealAllowed: true,
                links: []
            }
        ],
        openQuestions: [],
        quizStatistics: {
            questionStatistics: [
                {
                    question: {
                        id: '7',
                        question: "Warum?",
                        pending: false,
                        revealed: true,
                        links: []
                    },
                    answerStatistics: [
                        {
                            duration: 12576,
                            rating: 'CORRECT',
                            participant: {
                                id: '16',
                                name: 'Sandra',
                                turn: false,
                                points: 2,
                                revealAllowed: true,
                                links: []
                            }
                        }
                    ]
                }
            ]
        },
        links: []
    }

    const { getByTestId } = render(<QuizStatistics quiz={quiz} closeable={false} forceOpen={false} />);

    expect(getByTestId('answer-statistic-0').textContent).toBe('Sandra has buzzered after 12.576 seconds and it was CORRECT');
});

test('displays freetext answer', () => {
    const quiz: Quiz = {
        id: '1',
        name: 'Quiz',
        participants: [
            {
                id: '15',
                name: 'Erik',
                turn: false,
                points: 0,
                revealAllowed: true,
                links: []
            },
            {
                id: '16',
                name: 'Sandra',
                turn: false,
                points: 2,
                revealAllowed: true,
                links: []
            }
        ],
        openQuestions: [],
        quizStatistics: {
            questionStatistics: [
                {
                    question: {
                        id: '7',
                        question: "Warum?",
                        pending: false,
                        revealed: true,
                        links: []
                    },
                    answerStatistics: [
                        {
                            duration: 12576,
                            rating: 'CORRECT',
                            answer: 'Darum!',
                            participant: {
                                id: '16',
                                name: 'Sandra',
                                turn: false,
                                points: 2,
                                revealAllowed: true,
                                links: []
                            }
                        }
                    ]
                }
            ]
        },
        links: []
    }

    const { getByTestId } = render(<QuizStatistics quiz={quiz} closeable={false} forceOpen={false} />);

    expect(getByTestId('answer-statistic-0').textContent).toBe('Sandra has answered "Darum!" after 12.576 seconds and it was CORRECT');
});
