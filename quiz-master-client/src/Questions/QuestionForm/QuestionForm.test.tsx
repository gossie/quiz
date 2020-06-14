import React from 'react';
import { render, fireEvent, wait } from '@testing-library/react';
import Quiz from '../../quiz-client-shared/quiz';
import QuestionForm from './QuestionForm';

test('should add new estimation question', async () => {
    jest.spyOn(global, 'fetch').mockImplementation((url: string, request: object) => {
        expect(url).toEqual('http://localhost:5000/api/createQuestion');
        expect(request).toEqual({
            method: 'POST',
            body: JSON.stringify({
                question: 'Frage 3',
                publicVisible: false,
                estimates: {}
            }),
            headers: {
                'Content-Type': 'application/json',
                Accept: 'application/json'
            }
        });
        Promise.resolve();
    });

    const quiz: Quiz = {
        id: '5',
        name: "Awesome Quiz",
        participants: [],
        playedQuestions: [],
        openQuestions: [
            {
                id: '1',
                question: 'Frage 1',
                publicVisible: false,
                pending: false,
                links: []
            },
            {
                id: '2',
                question: 'Frage 2',
                publicVisible: false,
                pending: true,
                links: []
            }
        ],
        links: [{href: '/api/createQuestion', rel: 'createQuestion'}]
    }
    const { getByTestId } = render(<QuestionForm quiz={quiz} />);

    const questionButton = getByTestId('create-question-button');
    const questionField = getByTestId('new-question')  as HTMLInputElement;
    const imagePathField = getByTestId('image-path')  as HTMLInputElement;
    const estimationField = getByTestId('estimation')  as HTMLInputElement;

    fireEvent.change(questionField, { target: { value: 'Frage 3' } });
    estimationField.click();

    expect(questionField.value).toBe('Frage 3');
    expect(imagePathField.value).toBe('');

    questionButton.click();

    await wait(() =>{
        expect(questionField.value).toBe('');
        expect(imagePathField.value).toBe('');
    });
});