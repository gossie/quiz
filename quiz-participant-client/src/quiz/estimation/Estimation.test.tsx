import React from 'react';
import { render, fireEvent, wait } from '@testing-library/react';
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

    await wait(() => expect(estimationField.value).toEqual(''));
    await wait(() => expect(() => getByTestId('error-message')).toThrowError());
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

    fireEvent.change(estimationField, { target: { value: '1000' } });
    expect(estimationField.value).toEqual('1000');

    fireEvent.keyUp(estimationField, { key: 'Enter', keyCode: 13 });

    await wait(() => expect(estimationField.value).toEqual(''));
    await wait(() => expect(() => getByTestId('error-message')).toThrowError());
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

    await wait(() => expect(estimationField.value).toEqual('1000'));
    await wait(() => expect(() => getByTestId('error-message')).not.toThrowError());
    await wait(() => expect(getByTestId('error-message').textContent).toEqual('Something went wrong. Please send the data again.'));
});
