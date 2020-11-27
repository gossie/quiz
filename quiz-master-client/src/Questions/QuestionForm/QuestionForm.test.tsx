import React from 'react';
import { render, fireEvent, waitFor, cleanup } from '@testing-library/react';
import Quiz from '../../quiz-client-shared/quiz';
import QuestionForm from './QuestionForm';

beforeEach(() => () => cleanup()); 
afterEach(() => cleanup());

jest.mock('react-i18next', () => ({
    useTranslation: () => {
        return {
            t: (str: string, keys: object) => str,
            i18n: {
                changeLanguage: () => new Promise(() => {}),
            },
        };
    },
}));

test('should add new estimation question', async () => {
    jest.spyOn(global, 'fetch').mockImplementation((url: string, request: object) => {
        expect(url).toEqual('http://localhost:5000/api/createQuestion');
        expect(request).toEqual({
            method: 'POST',
            body: JSON.stringify({
                question: 'Frage 3',
                category: 'science',
                timeToAnswer: 30,
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
                category: 'other',
                publicVisible: false,
                pending: false,
                links: []
            },
            {
                id: '2',
                question: 'Frage 2',
                category: 'other',
                publicVisible: false,
                pending: true,
                links: []
            }
        ],
        expirationDate: 1234,
        timestamp: 12345678,
        links: [{href: '/api/createQuestion', rel: 'createQuestion'}]
    }
    const { getByTestId } = render(<QuestionForm quiz={quiz} />);
    const questionButton = getByTestId('create-question-button');
    const questionField = getByTestId('new-question')  as HTMLInputElement;
    const categoryField = getByTestId('category')  as HTMLSelectElement;
    const timeToAnswerField = getByTestId('time-to-answer')  as HTMLInputElement;
    const imagePathField = getByTestId('image-path')  as HTMLInputElement;
    const estimationField = getByTestId('type-estimation')  as HTMLInputElement;

    fireEvent.change(questionField, { target: { value: 'Frage 3' } });
    fireEvent.change(categoryField, { target: { value: 'science' } });
    fireEvent.change(timeToAnswerField, { target: { value: '30' } });
    estimationField.click();

    expect(questionField.value).toBe('Frage 3');
    expect(categoryField.value).toBe('science');
    expect(timeToAnswerField.value).toBe('30');
    expect(imagePathField.value).toBe('');

    questionButton.click();

    await waitFor(() =>{
        expect(questionField.value).toBe('');
        expect(categoryField.value).toBe('other');
        expect(imagePathField.value).toBe('');
        expect(timeToAnswerField.value).toBe('');
    });
});