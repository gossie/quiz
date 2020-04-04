import React from 'react';
import { render, fireEvent, wait } from '@testing-library/react';
import Quiz from './quiz';
import Questions from './Questions';

test('should display questions', () => {
    const quiz: Quiz = {
        id: 5,
        name: "Awesome Quiz",
        participants: [],
        questions: [
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
        links: []
    }
    const { getByTestId } = render(<Questions quiz={quiz} />);

    const questions = getByTestId('questions').querySelectorAll('div')

    expect(questions.length).toBe(2);
    expect(questions[0].textContent).toEqual('#1 Frage 1');
    expect(questions[1].textContent).toEqual('#2 Frage 2');
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
        questions: [
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

    const questionButton = getByTestId('question-button');
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
