import React from 'react';
import { render, fireEvent, waitFor } from '@testing-library/react';
import Estimation from './Estimation';
import Quiz from '../../quiz-client-shared/quiz';

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
        } as Response);
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
                category: 'eins',
                links: []
            },
            {
                id: '2',
                question: 'Frage 2',
                estimates: {},
                pending: true,
                revealed: false,
                category: 'eins',
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

    await waitFor(() => expect(estimationField.value).toEqual(''));
    await waitFor(() => expect(estimationField.placeholder).toEqual('1000'));
    await waitFor(() => expect(() => getByTestId('error-message')).toThrowError());
});

test('should send value with enter', async () => {
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
                estimates: {},
                pending: true,
                revealed: false,
                category: 'eins',
                links: []
            }
        ],
        links: [{href: '/api/createQuestion', rel: 'createQuestion'}]
    }
    const { getByTestId } = render(<Estimation quiz={quiz} participantId="4711" />);

    const estimationField = getByTestId('estimation') as HTMLInputElement;

    fireEvent.change(estimationField, { target: { value: '1000' } });
    expect(estimationField.value).toEqual('1000');

    fireEvent.keyUp(estimationField, { key: 'Enter', keyCode: 13 });

    await waitFor(() => expect(estimationField.value).toEqual(''));
    await waitFor(() => expect(() => getByTestId('error-message')).toThrowError());
});

test('should receive error when sending value', async () => {
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
            status: 400
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
                estimates: {},
                pending: true,
                revealed: false,
                category: 'eins',
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

    await waitFor(() => expect(estimationField.value).toEqual('1000'));
    await waitFor(() => expect(() => getByTestId('error-message')).not.toThrowError());
    await waitFor(() => expect(getByTestId('error-message').textContent).toEqual('Something went wrong. Please send the data again.'));
});

test('should disable button and field because time is up', async () => {
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
                estimates: {},
                pending: true,
                revealed: false,
                secondsLeft: 0,
                category: 'eins',
                links: []
            }
        ],
        links: [{href: '/api/createQuestion', rel: 'createQuestion'}]
    }
    const { getByTestId } = render(<Estimation quiz={quiz} participantId="4711" />);

    const estimationField = getByTestId('estimation') as HTMLInputElement;
    const sendButton = getByTestId('send') as HTMLButtonElement;

    expect(estimationField.disabled).toBeTruthy();
    expect(sendButton.disabled).toBeTruthy();
});

test('should disable button and field because answers were revealed', async () => {
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
                estimates: {},
                pending: true,
                revealed: true,
                category: 'eins',
                links: []
            }
        ],
        links: [{href: '/api/createQuestion', rel: 'createQuestion'}]
    }
    const { getByTestId } = render(<Estimation quiz={quiz} participantId="4711" />);

    const estimationField = getByTestId('estimation') as HTMLInputElement;
    const sendButton = getByTestId('send') as HTMLButtonElement;

    expect(estimationField.disabled).toBeTruthy();
    expect(sendButton.disabled).toBeTruthy();
});
