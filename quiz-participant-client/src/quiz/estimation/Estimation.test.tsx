import React from 'react';
import { render, fireEvent } from '@testing-library/react';
import Estimation from './Estimation';
import Quiz from '../../quiz-client-shared/quiz';

test('should estimate', async () => {
    jest.spyOn(global, 'fetch').mockImplementation((url: string, request: object) => {
        expect(url).toEqual('http://localhost:5000/api/participants/4711/buzzer');
        expect(request).toEqual({
            method: 'PUT',
            body: '1000',
            headers: {
                'Content-Type': 'text/plain'
            }
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
                links: []
            },
            {
                id: '2',
                question: 'Frage 2',
                estimates: {},
                pending: true,
                links: []
            }
        ],
        links: [{href: '/api/createQuestion', rel: 'createQuestion'}]
    }
    const { getByTestId } = render(<Estimation quiz={quiz} participantId="4711" />);

    const estimationField = getByTestId('estimation') as HTMLInputElement;
    const sendButton = getByTestId('send') as HTMLButtonElement;

    fireEvent.change(estimationField, { target: { value: '1000' } });
    expect(estimationField.value).toEqual('1000');

    sendButton.click();
});
