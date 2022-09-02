import React from 'react';
import { render } from '@testing-library/react';
import Buzzer from './Buzzer';
import Quiz from '../../quiz-client-shared/quiz';

test('should buzzer', async () => {
    jest.spyOn(global, 'fetch').mockImplementation((url: string, request: object) => {
        expect(url).toEqual('http://localhost:5000/api/participants/4711/buzzer');
        expect(request).toEqual({
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        return Promise.resolve({
            status: 200
        } as Response);
    });

    const quiz: Quiz = {
        id: '5',
        name: "Awesome Quiz",
        participants: [{
            id: '4711',
            name: 'Erik',
            turn: false,
            points: 12,
            revealAllowed: true,
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
                category: 'eins',
                links: []
            },
            {
                id: '2',
                question: 'Frage 2',
                pending: true,
                revealed: false,
                category: 'zwei',
                links: []
            }
        ],
        links: [{href: '/api/createQuestion', rel: 'createQuestion'}]
    }
    const { getByTestId } = render(<Buzzer quiz={quiz} participantId="4711" />);

    const buzzer = getByTestId('buzzer') as HTMLButtonElement;

    buzzer.click();
});
