import React from 'react';
import { render, fireEvent, wait, cleanup } from '@testing-library/react';
import Questions from './Questions';
import Quiz from '../quiz-client-shared/quiz';

beforeEach(() => () => cleanup()); 
afterEach(() => cleanup());

test('should display questions', () => {
    const quiz: Quiz = {
        id: '5',
        name: "Awesome Quiz",
        participants: [],
        playedQuestions: [
            {
                id: '1',
                question: 'Frage 1',
                category: 'other',
                publicVisible: false,
                pending: false,
                links: []
            },
        ],
        openQuestions: [
            {
                id: '2',
                question: 'Frage 2',
                category: 'other',
                publicVisible: false,
                pending: true,
                links: []
            },
            {
                id: '3',
                question: 'Frage 3',
                category: 'other',
                publicVisible: false,
                pending: false,
                links: []
            }
        ],
        timestamp: 1234,
        links: []
    }
    const { getByTestId } = render(<Questions quiz={quiz} />);

    const openQuestions = getByTestId('open-questions').querySelectorAll('.question-container li')

    expect(openQuestions.length).toBe(2);
    expect(openQuestions[0].textContent).toEqual('#1 Frage 2');
    expect(openQuestions[1].textContent).toEqual('#2 Frage 3');

    const playedQuestions = getByTestId('played-questions').querySelectorAll('.question-container li')

    expect(playedQuestions.length).toBe(1);
    expect(playedQuestions[0].textContent).toEqual('#1 Frage 1');
});

test('should add new private question', async () => {
    jest.spyOn(global, 'fetch').mockImplementation((url: string, request: object) => {
        expect(url).toEqual('http://localhost:5000/api/createQuestion');
        expect(request).toEqual({
            method: 'POST',
            body: JSON.stringify({
                question: 'Frage 3',
                category: 'other',
                timeToAnswer: null,
                imagePath: 'https://pathToImage',
                publicVisible: false
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
        timestamp: 1234,
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

test('should add new public question', async () => {
    jest.spyOn(global, 'fetch').mockImplementation((url: string, request: object) => {
        expect(url).toEqual('http://localhost:5000/api/createQuestion');
        expect(request).toEqual({
            method: 'POST',
            body: JSON.stringify({
                question: 'Frage 3',
                category: 'other',
                timeToAnswer: null,
                imagePath: 'https://pathToImage',
                publicVisible: true
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
        timestamp: 1234,
        links: [{href: '/api/createQuestion', rel: 'createQuestion'}]
    }
    const { getByTestId } = render(<Questions quiz={quiz} />);

    const questionButton = getByTestId('create-question-button');
    const questionField = getByTestId('new-question')  as HTMLInputElement;
    const imagePathField = getByTestId('image-path')  as HTMLInputElement;
    const visibilityField = getByTestId('visibility')  as HTMLInputElement;

    fireEvent.change(questionField, { target: { value: 'Frage 3' } });
    fireEvent.change(imagePathField, { target: { value: 'https://pathToImage' } });
    visibilityField.click();

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
            method: 'PATCH',
            headers: {
                Accept: 'application/json'
            }
        });
        Promise.resolve();
    });

    const quiz: Quiz = {
        id: '5',
        name: "Awesome Quiz",
        participants: [],
        playedQuestions: [
            {
                id: '1',
                question: 'Frage 1',
                category: 'other',
                publicVisible: false,
                pending: false,
                links: []
            },
        ],
        openQuestions: [
            {
                id: '2',
                question: 'Frage 2',
                category: 'other',
                publicVisible: false,
                pending: false,
                links: [{ href: '/api/quiz/5/questions/11', rel: 'self' }]
            },
            {
                id: '3',
                question: 'Frage 3',
                category: 'other',
                publicVisible: false,
                pending: false,
                links: []
            }
        ],
        timestamp: 1234,
        links: []
    }
    const { getByTestId } = render(<Questions quiz={quiz} />);

    getByTestId('start-question-0').click();
});

test('should revert question', () => {
    jest.spyOn(global, 'fetch').mockImplementation((url: string, request: object) => {
        expect(url).toEqual('http://localhost:5000/api/quiz/5/questions/11');
        expect(request).toEqual({
            method: 'PATCH',
            headers: {
                Accept: 'application/json'
            }
        });
        Promise.resolve();
    });

    const quiz: Quiz = {
        id: '5',
        name: "Awesome Quiz",
        participants: [],
        playedQuestions: [
            {
                id: '1',
                question: 'Frage 1',
                category: 'other',
                publicVisible: false,
                pending: false,
                links: []
            },
        ],
        openQuestions: [
            {
                id: '2',
                question: 'Frage 2',
                category: 'other',
                publicVisible: false,
                pending: true,
                links: [{ href: '/api/quiz/5/questions/11', rel: 'self' }]
            },
            {
                id: '3',
                question: 'Frage 3',
                category: 'other',
                publicVisible: false,
                pending: false,
                links: []
            }
        ],
        timestamp: 1234,
        links: []
    }
    const { getByTestId } = render(<Questions quiz={quiz} />);

    getByTestId('revert-question-0').click();
});

test('should delete question', () => {
    jest.spyOn(global, 'fetch').mockImplementation((url: string, request: object) => {
        expect(url).toEqual('http://localhost:5000/api/quiz/5/questions/11');
        expect(request).toEqual({
            method: 'DELETE'
        });
        Promise.resolve();
    });

    const quiz: Quiz = {
        id: '5',
        name: "Awesome Quiz",
        participants: [],
        playedQuestions: [
            {
                id: '1',
                question: 'Frage 1',
                category: 'other',
                publicVisible: false,
                pending: false,
                links: []
            },
        ],
        openQuestions: [
            {
                id: '2',
                question: 'Frage 2',
                category: 'other',
                publicVisible: false,
                pending: false,
                links: [{ href: '/api/quiz/5/questions/11', rel: 'self' }]
            },
            {
                id: '3',
                question: 'Frage 3',
                category: 'other',
                publicVisible: false,
                pending: false,
                links: []
            }
        ],
        timestamp: 1234,
        links: []
    }
    const { getByTestId } = render(<Questions quiz={quiz} />);

    getByTestId('delete-question-0').click();
});

test('should open and close image modal', () => {
    const quiz: Quiz = {
        id: '5',
        name: "Awesome Quiz",
        participants: [],
        openQuestions: [
            {
                id: '2',
                question: 'Frage 2',
                category: 'other',
                publicVisible: false,
                pending: true,
                links: []
            },
            {
                id: '3',
                question: 'Frage 3',
                category: 'other',
                pending: false,
                imagePath: 'https://path_to_image/',
                publicVisible: false,
                links: []
            }
        ],
        playedQuestions: [],
        timestamp: 1234,
        links: []
    }

    const { getByTestId } = render(<Questions quiz={quiz} />);

    expect(() => getByTestId('image-dialog')).toThrowError();

    const imageIcon = getByTestId('image-icon-1');
    imageIcon.click();
    expect(() => getByTestId('image-dialog')).not.toThrowError();

    const image = getByTestId('image') as HTMLImageElement;
    expect(image.src).toEqual('https://path_to_image/');

    const closeButton = getByTestId('close-button');
    closeButton.click();
    expect(() => getByTestId('image-dialog')).toThrowError();
});

test('should edit question', async () => {
    jest.spyOn(global, 'fetch').mockImplementation((url: string, request: object) => {
        expect(url).toEqual('http://localhost:5000/api/quiz/5/questions/3');
        expect(request).toEqual({
            method: 'PUT',
            body: JSON.stringify({
                question: 'Frage 4',
                category: 'history',
                timeToAnswer: 45,
                imagePath: 'https://path_to_image_changed/',
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
        openQuestions: [
            {
                id: '2',
                question: 'Frage 2',
                category: 'other',
                publicVisible: false,
                pending: true,
                links: []
            },
            {
                id: '3',
                question: 'Frage 3',
                category: 'other',
                pending: false,
                imagePath: 'https://path_to_image/',
                publicVisible: true,
                links: [{ href: '/api/quiz/5/questions/3', rel: 'self' }]
            }
        ],
        playedQuestions: [],
        timestamp: 1234,
        links: []
    }

    const { getByTestId } = render(<Questions quiz={quiz} />);

    expect(() => getByTestId('edit-dialog')).toThrowError();

    const editIcon = getByTestId('edit-question-1');
    editIcon.click();
    expect(() => getByTestId('edit-dialog')).not.toThrowError();

    const questionButton = getByTestId('edit-question-button');
    const questionField = getByTestId('question-to-edit')  as HTMLInputElement;
    const categoryField = getByTestId('category-to-edit')  as HTMLSelectElement;
    const timeToAnswerField = getByTestId('time-to-answer-to-edit')  as HTMLInputElement;
    const imagePathField = getByTestId('image-path-to-edit')  as HTMLInputElement;
    const estimationField = getByTestId('estimation-to-edit')  as HTMLInputElement;
    const visibilityField = getByTestId('visibility-to-edit')  as HTMLInputElement;

    expect(questionField.value).toBe('Frage 3');
    expect(categoryField.value).toBe('other');
    expect(imagePathField.value).toBe('https://path_to_image/');
    expect(timeToAnswerField.value).toBe('');
    expect(estimationField.checked).toBe(false);
    expect(visibilityField.checked).toBe(true);

    fireEvent.change(questionField, { target: { value: 'Frage 4' } });
    fireEvent.change(categoryField, { target: { value: 'history' } });
    fireEvent.change(imagePathField, { target: { value: 'https://path_to_image_changed/' } });
    fireEvent.change(timeToAnswerField, { target: { value: '45' } });
    estimationField.click();
    visibilityField.click();

    expect(questionField.value).toBe('Frage 4');
    expect(categoryField.value).toBe('history');
    expect(imagePathField.value).toBe('https://path_to_image_changed/');
    expect(timeToAnswerField.value).toBe('45');
    expect(estimationField.checked).toBe(true);
    expect(visibilityField.checked).toBe(false);

    questionButton.click();

    await wait(() =>{
        expect(questionField.value).toBe('');
        expect(categoryField.value).toBe('other');
        expect(imagePathField.value).toBe('');
        expect(timeToAnswerField.value).toBe('');
    });
});
