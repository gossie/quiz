import React from 'react';
import { render, fireEvent, waitFor, cleanup } from '../test-utils';
import Questions from './Questions';
import Quiz from '../quiz-client-shared/quiz';
import {verticalDrag} from 'react-beautiful-dnd-tester';
import { ErrorAction } from '../redux/actions';
import { ActionType } from '../redux/action-types';

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
                links: [], 
                previousQuestionId: null
            },
        ],
        openQuestions: [
            {
                id: '2',
                question: 'Frage 2',
                category: 'other',
                publicVisible: false,
                pending: true,
                links: [],
                previousQuestionId: null
            },
            {
                id: '3',
                question: 'Frage 3',
                category: 'other',
                publicVisible: false,
                pending: false,
                links: [],
                previousQuestionId: '2'
            }
        ],
        expirationDate: 1234,
        timestamp: 1234,
        links: []
    }
    const { getByTestId } = render(<Questions quiz={quiz} />);

    const openQuestions = getByTestId('open-questions').querySelectorAll('li')

    expect(openQuestions.length).toBe(2);
    expect(openQuestions[0].textContent).toEqual('2Frage 2');
    expect(openQuestions[1].textContent).toEqual('3Frage 3');

    const playedQuestions = getByTestId('played-questions').querySelectorAll('li')

    expect(playedQuestions.length).toBe(1);
    expect(playedQuestions[0].textContent).toEqual('1Frage 1');
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
        return Promise.resolve({ status: 201 });
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
                links: [],
                previousQuestionId: null
            },
            {
                id: '2',
                question: 'Frage 2',
                category: 'other',
                publicVisible: false,
                pending: true,
                links: [],
                previousQuestionId: '1'
            }
        ],
        expirationDate: 1234,
        timestamp: 1234,
        links: [{href: '/api/createQuestion', rel: 'createQuestion'}]
    }
    const { getByTestId } = render(<Questions quiz={quiz} />);
    const addQuestionButton = getByTestId('add-question-button');
    addQuestionButton.click();
    const questionButton = getByTestId('create-question-button');
    const questionField = getByTestId('new-question')  as HTMLInputElement;
    const imagePathField = getByTestId('image-path')  as HTMLInputElement;

    fireEvent.change(questionField, { target: { value: 'Frage 3' } });
    fireEvent.change(imagePathField, { target: { value: 'https://pathToImage' } });

    expect(questionField.value).toBe('Frage 3');
    expect(imagePathField.value).toBe('https://pathToImage');

    questionButton.click();

    await waitFor(() =>{
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
        return Promise.resolve({ status: 201 });
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
                links: [],
                previousQuestionId: null
            },
            {
                id: '2',
                question: 'Frage 2',
                category: 'other',
                publicVisible: false,
                pending: true,
                links: [],
                previousQuestionId: '2'
            }
        ],
        expirationDate: 1234,
        timestamp: 1234,
        links: [{href: '/api/createQuestion', rel: 'createQuestion'}]
    }
    const { getByTestId } = render(<Questions quiz={quiz} />);
    const addQuestionButton = getByTestId('add-question-button');
    addQuestionButton.click();
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

    await waitFor(() =>{
        expect(questionField.value).toBe('');
        expect(imagePathField.value).toBe('');
    });
});

test('should start question', () => {
    jest.spyOn(global, 'fetch').mockImplementation((url: string, request: object) => {
        expect(url).toEqual('http://localhost:5000/api/quiz/5/questions/11');
        expect(request).toEqual({
            method: 'PATCH'
        });
        return Promise.resolve({ status: 200 });
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
                links: [],
                previousQuestionId: null
            },
        ],
        openQuestions: [
            {
                id: '2',
                question: 'Frage 2',
                category: 'other',
                publicVisible: false,
                pending: false,
                links: [{ href: '/api/quiz/5/questions/11', rel: 'self' }],
                previousQuestionId: null
            },
            {
                id: '3',
                question: 'Frage 3',
                category: 'other',
                publicVisible: false,
                pending: false,
                links: [],
                previousQuestionId:'2'
            }
        ],
        expirationDate: 1234,
        timestamp: 1234,
        links: []
    }
    const { getByTestId } = render(<Questions quiz={quiz} />);

    getByTestId('start-question-1').click();
});

test('should delete question', () => {
    jest.spyOn(global, 'fetch').mockImplementation((url: string, request: object) => {
        expect(url).toEqual('http://localhost:5000/api/quiz/5/questions/11');
        expect(request).toEqual({
            method: 'DELETE'
        });
        return Promise.resolve({ status: 200 });
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
                links: [],
                previousQuestionId: null
            },
        ],
        openQuestions: [
            {
                id: '2',
                question: 'Frage 2',
                category: 'other',
                publicVisible: false,
                pending: false,
                links: [{ href: '/api/quiz/5/questions/11', rel: 'self' }],
                previousQuestionId: null
            },
            {
                id: '3',
                question: 'Frage 3',
                category: 'other',
                publicVisible: false,
                pending: false,
                links: [],
                previousQuestionId: '2'
            }
        ],
        expirationDate: 1234,
        timestamp: 1234,
        links: []
    }
    const { getByTestId } = render(<Questions quiz={quiz} />);

    getByTestId('delete-question-1').click();
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
                links: [],
                previousQuestionId: null
            },
            {
                id: '3',
                question: 'Frage 3',
                category: 'other',
                pending: false,
                imagePath: 'https://path_to_image/',
                publicVisible: false,
                links: [],
                previousQuestionId: '2'
            }
        ],
        playedQuestions: [],
        expirationDate: 1234,
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

test('should move question to any position', async () => {
    jest.spyOn(global, 'fetch').mockImplementation((url: string, request: object) => {
        expect(url).toEqual('http://localhost:5000/api/quiz/5/questions/3');
        expect(request).toEqual({
            method: 'PUT',
            body: JSON.stringify({
                id: '3',
                question: 'Frage 3',
                category: 'other',
                pending: false,
                imagePath: 'https://path_to_image/',
                publicVisible: true,
                links: [{ href: '/api/quiz/5/questions/3', rel: 'self' }],
                previousQuestionId: '1'
            }),
            headers: {
                'Content-Type': 'application/json',
                Accept: 'application/json'
            }
        });
        return Promise.resolve({status: 200});
    });

    const quiz: Quiz = {
        id: '5',
        name: "Awesome Quiz",
        participants: [],
        openQuestions: [
            {
                id: '1',
                question: 'Frage 1',
                category: 'other',
                pending: false,
                imagePath: 'https://path_to_image/',
                publicVisible: true,
                links: [{ href: '/api/quiz/5/questions/1', rel: 'self' }],
                previousQuestionId: null
            },
            {
                id: '2',
                question: 'Frage 2',
                category: 'other',
                pending: false,
                imagePath: 'https://path_to_image/',
                publicVisible: true,
                links: [{ href: '/api/quiz/5/questions/2', rel: 'self' }],
                previousQuestionId: '1'
            },
            {
                id: '3',
                question: 'Frage 3',
                category: 'other',
                pending: false,
                imagePath: 'https://path_to_image/',
                publicVisible: true,
                links: [{ href: '/api/quiz/5/questions/3', rel: 'self' }],
                previousQuestionId: '2'
            }
        ],
        playedQuestions: [],
        timestamp: 1234,
        expirationDate: 1234,
        links: []
    }
    const {getAllByTestId} = render(<Questions quiz={quiz}/>);
    let second = getAllByTestId(/dragquestion/i)[1];      
    let first = getAllByTestId(/dragquestion/i)[0];   
    let third = getAllByTestId(/dragquestion/i)[2];    

    verticalDrag(third).inFrontOf(second);

   const newSecond = getAllByTestId(/dragquestion/i)[1];
   expect(newSecond.textContent).toBe(third.textContent);
});

test('should move question to first position', async () => {
    jest.spyOn(global, 'fetch').mockImplementation((url: string, request: object) => {
        expect(url).toEqual('http://localhost:5000/api/quiz/5/questions/3');
        expect(request).toEqual({
            method: 'PUT',
            body: JSON.stringify({
                id: '3',
                question: 'Frage 3',
                category: 'other',
                pending: false,
                imagePath: 'https://path_to_image/',
                publicVisible: true,
                links: [{ href: '/api/quiz/5/questions/3', rel: 'self' }],
                previousQuestionId: null
            }),
            headers: {
                'Content-Type': 'application/json',
                Accept: 'application/json'
            }
        });
        return Promise.resolve({status: 200});
    });

    const quiz: Quiz = {
        id: '5',
        name: "Awesome Quiz",
        participants: [],
        openQuestions: [
            {
                id: '1',
                question: 'Frage 1',
                category: 'other',
                pending: false,
                imagePath: 'https://path_to_image/',
                publicVisible: true,
                links: [{ href: '/api/quiz/5/questions/1', rel: 'self' }],
                previousQuestionId: null
            },
            {
                id: '2',
                question: 'Frage 2',
                category: 'other',
                pending: false,
                imagePath: 'https://path_to_image/',
                publicVisible: true,
                links: [{ href: '/api/quiz/5/questions/2', rel: 'self' }],
                previousQuestionId: '1'
            },
            {
                id: '3',
                question: 'Frage 3',
                category: 'other',
                pending: false,
                imagePath: 'https://path_to_image/',
                publicVisible: true,
                links: [{ href: '/api/quiz/5/questions/3', rel: 'self' }],
                previousQuestionId: '2'
            }
        ],
        playedQuestions: [],
        timestamp: 1234,
        expirationDate: 1234,
        links: []
    }
    const {getAllByTestId} = render(<Questions quiz={quiz}/>);
    let second = getAllByTestId(/dragquestion/i)[1];      
    let first = getAllByTestId(/dragquestion/i)[0];   
    let third = getAllByTestId(/dragquestion/i)[2];    

    verticalDrag(third).inFrontOf(first);

   const newFirst = getAllByTestId(/dragquestion/i)[0];
   expect(newFirst.textContent).toBe(third.textContent);
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
                estimates: {},
                previousQuestionId: '2'
            }),
            headers: {
                'Content-Type': 'application/json',
                Accept: 'application/json'
            }
        });
        return Promise.resolve({ status: 200 });
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
                links: [],
                previousQuestionId: null
            },
            {
                id: '3',
                question: 'Frage 3',
                category: 'other',
                pending: false,
                imagePath: 'https://path_to_image/',
                publicVisible: true,
                links: [{ href: '/api/quiz/5/questions/3', rel: 'self' }],
                previousQuestionId: '2'
            }
        ],
        playedQuestions: [],
        expirationDate: 1234,
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
    const estimationField = getByTestId('type-estimation-to-edit')  as HTMLInputElement;
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

    await waitFor(() =>{
        expect(questionField.value).toBe('');
        expect(categoryField.value).toBe('other');
        expect(imagePathField.value).toBe('');
        expect(timeToAnswerField.value).toBe('');
    });
});

test('should not move question, because the quiz is finished', (done) => {
    jest.spyOn(global, 'fetch').mockImplementation((url: string, request: object) => {
        expect(url).toEqual('http://localhost:5000/api/quiz/5/questions/3');
        expect(request).toEqual({
            method: 'PUT',
            body: JSON.stringify({
                id: '3',
                question: 'Frage 3',
                category: 'other',
                pending: false,
                imagePath: 'https://path_to_image/',
                publicVisible: true,
                links: [{ href: '/api/quiz/5/questions/3', rel: 'self' }],
                previousQuestionId: '1'
            }),
            headers: {
                'Content-Type': 'application/json',
                Accept: 'application/json'
            }
        });
        return Promise.resolve({status: 409});
    });

    const quiz: Quiz = {
        id: '5',
        name: "Awesome Quiz",
        participants: [],
        openQuestions: [
            {
                id: '1',
                question: 'Frage 1',
                category: 'other',
                pending: false,
                imagePath: 'https://path_to_image/',
                publicVisible: true,
                links: [{ href: '/api/quiz/5/questions/1', rel: 'self' }],
                previousQuestionId: null
            },
            {
                id: '2',
                question: 'Frage 2',
                category: 'other',
                pending: false,
                imagePath: 'https://path_to_image/',
                publicVisible: true,
                links: [{ href: '/api/quiz/5/questions/2', rel: 'self' }],
                previousQuestionId: '1'
            },
            {
                id: '3',
                question: 'Frage 3',
                category: 'other',
                pending: false,
                imagePath: 'https://path_to_image/',
                publicVisible: true,
                links: [{ href: '/api/quiz/5/questions/3', rel: 'self' }],
                previousQuestionId: '2'
            }
        ],
        playedQuestions: [],
        timestamp: 1234,
        expirationDate: 1234,
        links: []
    }

    const onError = (state = {}, action: ErrorAction) => {
        if (action.type === ActionType.SHOW_ERROR) {
            expect(action.payload.errorMessage).toBe('errorMessageConflict');
            done();
        }
    }

    const {getAllByTestId} = render(<Questions quiz={quiz}/>, { reducer: onError });
    
    let second = getAllByTestId(/dragquestion/i)[1];      
    let first = getAllByTestId(/dragquestion/i)[0];   
    let third = getAllByTestId(/dragquestion/i)[2];    

    verticalDrag(third).inFrontOf(second);

    const newSecond = getAllByTestId(/dragquestion/i)[1];
    expect(newSecond.textContent).toBe(third.textContent);
});
