import React from 'react';
import { render, fireEvent, wait } from '@testing-library/react';
import Questions from './Questions';
import Quiz from '../quiz-client-shared/quiz';

test('should display questions', () => {
    const quiz: Quiz = {
        id: 5,
        name: "Awesome Quiz",
        participants: [],
        playedQuestions: [
            {
                question: 'Frage 1',
                pending: false,
                links: []
            },
        ],
        openQuestions: [
            {
                question: 'Frage 2',
                pending: true,
                links: []
            },
            {
                question: 'Frage 3',
                pending: false,
                links: []
            }
        ],
        links: []
    }
    const { getByTestId } = render(<Questions quiz={quiz} />);

    const openQuestions = getByTestId('open-questions').querySelectorAll('div')

    expect(openQuestions.length).toBe(2);
    expect(openQuestions[0].textContent).toEqual('#1 Frage 2');
    expect(openQuestions[1].textContent).toEqual('#2 Frage 3');

    const playedQuestions = getByTestId('played-questions').querySelectorAll('div')

    expect(playedQuestions.length).toBe(1);
    expect(playedQuestions[0].textContent).toEqual('#1 Frage 1');
});

test('should add new question', async () => {
    jest.spyOn(global, 'fetch').mockImplementation((url: string, request: object) => {
        expect(url).toEqual('http://localhost:5000/api/createQuestion');
        expect(request).toEqual({
            method: 'POST',
            body: JSON.stringify({
                question: 'Frage 3',
                imagePath: 'https://pathToImage'
            }),
            headers: {
                'Content-Type': 'application/json',
                Accept: 'application/json'
            }
        });
        Promise.resolve();
    });

    const quiz: Quiz = {
        id: 5,
        name: "Awesome Quiz",
        participants: [],
        playedQuestions: [],
        openQuestions: [
            {
                question: 'Frage 1',
                pending: false,
                links: []
            },
            {
                question: 'Frage 2',
                pending: true,
                links: []
            }
        ],
        links: [{href: '/api/createQuestion', rel: 'createQuestion'}]
    }
    const { getByTestId } = render(<Questions quiz={quiz} />);

    const questionButton = getByTestId('create-question-button');
    const questionField = getByTestId('new-question')  as HTMLInputElement;
    const imagePathField = getByTestId('image-path')  as HTMLInputElement;

    fireEvent.change(questionField, { target: { value: 'Frage 3' } });
    fireEvent.change(imagePathField, { target: { value: 'https://pathToImage' } });

    expect(questionField.value).toBe('Frage 3');
    expect(imagePathField.value).toBe('https://pathToImage');

    questionButton.click();

    await wait(() =>{
        expect(questionField.value).toBe('');
        expect(imagePathField.value).toBe('');
    });
});

test('should start question', () => {
    jest.spyOn(global, 'fetch').mockImplementation((url: string, request: object) => {
        expect(url).toEqual('http://localhost:5000/api/quiz/5/questions/11');
        expect(request).toEqual({
            method: 'PUT',
            headers: {
                Accept: 'application/json'
            }
        });
        Promise.resolve();
    });

    const quiz: Quiz = {
        id: 5,
        name: "Awesome Quiz",
        participants: [],
        playedQuestions: [
            {
                question: 'Frage 1',
                pending: false,
                links: []
            },
        ],
        openQuestions: [
            {
                question: 'Frage 2',
                pending: false,
                links: [{ href: '/api/quiz/5/questions/11', rel: 'self' }]
            },
            {
                question: 'Frage 3',
                pending: false,
                links: []
            }
        ],
        links: []
    }
    const { getByTestId } = render(<Questions quiz={quiz} />);

    getByTestId('start-question-0').click();
});