import React from 'react';
import { render, fireEvent, waitFor } from '@testing-library/react';
import Quiz from '../../quiz-client-shared/quiz';
import MultipleChoice from './MultipleChoice';

jest.mock('react-i18next', () => ({
    useTranslation: () => {
        return {
            t: (str: string, keys: object) => {
                if (str === 'errorEstimation') {
                    return 'Something went wrong. Please send the data again.';
                } else {
                    return null;
                }
            },
            i18n: {
                changeLanguage: () => new Promise(() => {}),
            },
        };
        
    },
}));

test('should select choice', async () => {
    jest.spyOn(global, 'fetch').mockImplementation((url: string, request: object) => {
        expect(url).toEqual('http://localhost:5000/api/quiz/5/participants/4711/choices/17');
        expect(request).toEqual({
            method: 'PUT'
        });
        return Promise.resolve({
            status: 200
        });
    });

    const quiz: Quiz = {
        id: '5',
        name: "Awesome Quiz",
        participants: [{
            id: '4711',
            name: 'Erik',
            turn: false,
            revealAllowed: true,
            points: 12,
            links: [
                {
                    rel: 'buzzer',
                    href: '/api/participants/4711/buzzer'
                }
            ]
        }],
        openQuestions: [
            {
                id: '1',
                question: 'Frage 1',
                pending: false,
                revealed: false,
                links: []
            },
            {
                id: '2',
                question: 'Frage 2',
                estimates: {},
                choices: [
                    {
                        choice: 'Option 1',
                        links: [
                            {
                                rel: '4711-selects-choice',
                                href: '/api/quiz/5/participants/4711/choices/17'
                            }
                        ]
                    }
                ],
                pending: true,
                revealed: false,
                links: []
            }
        ],
        links: [{href: '/api/createQuestion', rel: 'createQuestion'}]
    }
    const { getByTestId } = render(<MultipleChoice question={quiz.openQuestions[1]} participantId="4711" />);
 
    getByTestId('multiple-choice-option-0').click();
});