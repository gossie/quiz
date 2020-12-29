import React from 'react';
import { render, fireEvent, waitFor, cleanup } from '../../test-utils';
import Quiz from '../../quiz-client-shared/quiz';
import QuestionForm from './QuestionForm';
import { ErrorAction } from '../../redux/actions';
import { ActionType } from '../../redux/action-types';

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
                correctAnswer: 'Antwort 3',
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
        return Promise.resolve({status: 201});
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
                previousQuestionId: null,
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
        timestamp: 12345678,
        links: [{href: '/api/createQuestion', rel: 'createQuestion'}]
    }
    const { getByTestId } = render(<QuestionForm quiz={quiz} />);
    const questionButton = getByTestId('create-question-button');
    const questionField = getByTestId('new-question')  as HTMLInputElement;
    const answerField = getByTestId('new-correct-answer')  as HTMLInputElement;
    const categoryField = getByTestId('category')  as HTMLSelectElement;
    const timeToAnswerField = getByTestId('time-to-answer')  as HTMLInputElement;
    const imagePathField = getByTestId('image-path')  as HTMLInputElement;
    const estimationField = getByTestId('type-estimation')  as HTMLInputElement;

    fireEvent.change(questionField, { target: { value: 'Frage 3' } });
    fireEvent.change(answerField, { target: { value: 'Antwort 3' } })
    fireEvent.change(categoryField, { target: { value: 'science' } });
    fireEvent.change(timeToAnswerField, { target: { value: '30' } });
    estimationField.click();

    expect(questionField.value).toBe('Frage 3');
    expect(answerField.value).toBe('Antwort 3');
    expect(categoryField.value).toBe('science');
    expect(timeToAnswerField.value).toBe('30');
    expect(imagePathField.value).toBe('');

    questionButton.click();

    await waitFor(() =>{
        expect(questionField.value).toBe('');
        expect(answerField.value).toBe('');
        expect(categoryField.value).toBe('other');
        expect(imagePathField.value).toBe('');
        expect(timeToAnswerField.value).toBe('');
    });
});

test('should not add new question, because quiz is finished', (done) => {
    jest.spyOn(global, 'fetch').mockImplementation((url: string, request: object) => {
        expect(url).toEqual('http://localhost:5000/api/createQuestion');
        expect(request).toEqual({
            method: 'POST',
            body: JSON.stringify({
                question: 'Frage 3',
                category: 'science',
                timeToAnswer: 30,
                publicVisible: false
            }),
            headers: {
                'Content-Type': 'application/json',
                Accept: 'application/json'
            }
        });
        return Promise.resolve({
            status: 409,
            json: () => Promise.resolve({message: 'errorMessageConflict'})
        });
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
                previousQuestionId: null,
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
        timestamp: 12345678,
        links: [{href: '/api/createQuestion', rel: 'createQuestion'}]
    }

    const onError = (state = {}, action: ErrorAction) => {
        if (action.type === ActionType.SHOW_ERROR) {
            expect(action.payload.errorMessage).toBe('errorMessageConflict');
            done();
        }
        return {};
    }

    const { getByTestId } = render(<QuestionForm quiz={quiz} />, { reducer: onError });

    const questionButton = getByTestId('create-question-button');
    const questionField = getByTestId('new-question')  as HTMLInputElement;
    const categoryField = getByTestId('category')  as HTMLSelectElement;
    const timeToAnswerField = getByTestId('time-to-answer')  as HTMLInputElement;
    const imagePathField = getByTestId('image-path')  as HTMLInputElement;
    const estimationField = getByTestId('type-estimation')  as HTMLInputElement;

    fireEvent.change(questionField, { target: { value: 'Frage 3' } });
    fireEvent.change(categoryField, { target: { value: 'science' } });
    fireEvent.change(timeToAnswerField, { target: { value: '30' } });

    questionButton.click();

});

test('should create multiple choice question', async () => {
    jest.spyOn(global, 'fetch').mockImplementation((url: string, request: object) => {
        expect(url).toEqual('http://localhost:5000/api/createQuestion');
        expect(request).toEqual({
            method: 'POST',
            body: JSON.stringify({
                question: 'Frage 3',
                category: 'science',
                timeToAnswer: 30,
                publicVisible: false,
                estimates: {},
                choices: [{choice: 'Option 1'}]
            }),
            headers: {
                'Content-Type': 'application/json',
                Accept: 'application/json'
            }
        });
        return Promise.resolve({status: 201});
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
                previousQuestionId: null,
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
        timestamp: 12345678,
        links: [{href: '/api/createQuestion', rel: 'createQuestion'}]
    }
    const { getByTestId } = render(<QuestionForm quiz={quiz} />);
    const questionButton = getByTestId('create-question-button');
    const questionField = getByTestId('new-question')  as HTMLInputElement;
    const categoryField = getByTestId('category')  as HTMLSelectElement;
    const timeToAnswerField = getByTestId('time-to-answer')  as HTMLInputElement;
    const imagePathField = getByTestId('image-path')  as HTMLInputElement;
    const multipleChoiceField = getByTestId('type-multiple-choice')  as HTMLInputElement;

    fireEvent.change(questionField, { target: { value: 'Frage 3' } });
    fireEvent.change(categoryField, { target: { value: 'science' } });
    fireEvent.change(timeToAnswerField, { target: { value: '30' } });

    expect(() => getByTestId('choices')).toThrowError();
    multipleChoiceField.click();

    const choicesWrapper = getByTestId('choices') as HTMLDivElement;
    const newChoiceField = getByTestId('new-choice') as HTMLInputElement;
    const addOption = getByTestId('add-option');

    fireEvent.change(newChoiceField, { target: { value: 'Option 1' } });
    addOption.click();

    expect(choicesWrapper.querySelectorAll('.multiple-choice-option').length).toBe(1);
    expect(choicesWrapper.querySelectorAll('.multiple-choice-option')[0].textContent).toBe('Option 1');
    expect(newChoiceField.value).toBe('');

    fireEvent.change(newChoiceField, { target: { value: 'Option 2' } });
    addOption.click();

    expect(choicesWrapper.querySelectorAll('.multiple-choice-option').length).toBe(2);
    expect(choicesWrapper.querySelectorAll('.multiple-choice-option')[0].textContent).toBe('Option 1');
    expect(choicesWrapper.querySelectorAll('.multiple-choice-option')[1].textContent).toBe('Option 2');
    expect(newChoiceField.value).toBe('');

    getByTestId('edit-multiple-choice-option-1').click();
    const editInput = getByTestId('edit-muliple-choice-option-input-1') as HTMLInputElement;
    expect(editInput.value).toEqual('Option 2');
    fireEvent.change(editInput, { target: { value: 'Option 2 (changed)' } });
    getByTestId('edit-multiple-choice-option-save-1').click();
    expect(choicesWrapper.querySelectorAll('.multiple-choice-option')[1].textContent).toBe('Option 2 (changed)');

    getByTestId('delete-multiple-choice-option-1').click();

    expect(choicesWrapper.querySelectorAll('.multiple-choice-option').length).toBe(1);
    expect(choicesWrapper.querySelectorAll('.multiple-choice-option')[0].textContent).toBe('Option 1');
    expect(newChoiceField.value).toBe('');

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
        expect(choicesWrapper.querySelectorAll('.multiple-choice-option').length).toBe(0); 
    });

});

test('should create multiple choice question with last choice still in the input field', async () => {
    jest.spyOn(global, 'fetch').mockImplementation((url: string, request: object) => {
        expect(url).toEqual('http://localhost:5000/api/createQuestion');
        expect(request).toEqual({
            method: 'POST',
            body: JSON.stringify({
                question: 'Frage 3',
                category: 'science',
                timeToAnswer: 30,
                publicVisible: false,
                estimates: {},
                choices: [{choice: 'Option 1'},{choice: 'Option 2'}]
            }),
            headers: {
                'Content-Type': 'application/json',
                Accept: 'application/json'
            }
        });
        return Promise.resolve({status: 201});
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
                previousQuestionId: null,
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
        timestamp: 12345678,
        links: [{href: '/api/createQuestion', rel: 'createQuestion'}]
    }
    const { getByTestId } = render(<QuestionForm quiz={quiz} />);
    const questionButton = getByTestId('create-question-button');
    const questionField = getByTestId('new-question')  as HTMLInputElement;
    const categoryField = getByTestId('category')  as HTMLSelectElement;
    const timeToAnswerField = getByTestId('time-to-answer')  as HTMLInputElement;
    const imagePathField = getByTestId('image-path')  as HTMLInputElement;
    const multipleChoiceField = getByTestId('type-multiple-choice')  as HTMLInputElement;

    fireEvent.change(questionField, { target: { value: 'Frage 3' } });
    fireEvent.change(categoryField, { target: { value: 'science' } });
    fireEvent.change(timeToAnswerField, { target: { value: '30' } });

    expect(() => getByTestId('choices')).toThrowError();
    multipleChoiceField.click();

    const choicesWrapper = getByTestId('choices') as HTMLDivElement;
    const newChoiceField = getByTestId('new-choice') as HTMLInputElement;
    const addOption = getByTestId('add-option');

    fireEvent.change(newChoiceField, { target: { value: 'Option 1' } });
    addOption.click();

    expect(choicesWrapper.querySelectorAll('.multiple-choice-option').length).toBe(1);
    expect(choicesWrapper.querySelectorAll('.multiple-choice-option')[0].textContent).toBe('Option 1');
    expect(newChoiceField.value).toBe('');

    fireEvent.change(newChoiceField, { target: { value: 'Option 2' } });
    expect(newChoiceField.value).toBe('Option 2');

    questionButton.click();

    await waitFor(() =>{
        expect(questionField.value).toBe('');
        expect(categoryField.value).toBe('other');
        expect(imagePathField.value).toBe('');
        expect(timeToAnswerField.value).toBe('');
        expect(choicesWrapper.querySelectorAll('.multiple-choice-option').length).toBe(0); 
    });

});

test('should edit multiple choice question', async () => {
    jest.spyOn(global, 'fetch').mockImplementation((url: string, request: object) => {
        console.log('bin da');
        expect(url).toEqual('http://localhost:5000/api/editQuestion');
        expect(request).toEqual({
            method: 'PUT',
            body: JSON.stringify({
                question: 'Frage 2',
                category: 'other',
                timeToAnswer: null,
                publicVisible: false,
                estimates: {},
                choices: [{choice: 'Option 1'}, {choice: 'Option 2'}],
                previousQuestionId: '1'
            }),
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            }
        });
        return Promise.resolve({status: 200});
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
                previousQuestionId: null,
            },
            {
                id: '2',
                question: 'Frage 2',
                category: 'other',
                publicVisible: false,
                estimates: {},
                choices: [
                    {
                        choice: 'Option 1'
                    }
                ],
                pending: false,
                previousQuestionId: '1',
                links: [{href: '/api/editQuestion', rel: 'self'}]
            }
        ],
        expirationDate: 1234,
        timestamp: 12345678,
        links: []
    }
    const { getByTestId } = render(<QuestionForm quiz={quiz} questionToChange={quiz.openQuestions[1]} />);
    const questionField = getByTestId('question-to-edit')  as HTMLInputElement;
    const categoryField = getByTestId('category-to-edit')  as HTMLSelectElement;
    const timeToAnswerField = getByTestId('time-to-answer-to-edit')  as HTMLInputElement;
    const imagePathField = getByTestId('image-path-to-edit')  as HTMLInputElement;

    const choicesWrapper = getByTestId('choices') as HTMLDivElement;
    const newChoiceField = getByTestId('new-choice') as HTMLInputElement;
    const addOption = getByTestId('add-option');

    expect(choicesWrapper.querySelectorAll('.multiple-choice-option').length).toBe(1);
    expect(choicesWrapper.querySelectorAll('.multiple-choice-option')[0].textContent).toBe('Option 1');
    expect(newChoiceField.value).toBe('');

    fireEvent.change(newChoiceField, { target: { value: 'Option 2' } });
    fireEvent.keyUp(newChoiceField, { key: 'Enter', keyCode: 13 });

    expect(choicesWrapper.querySelectorAll('.multiple-choice-option').length).toBe(2);
    expect(choicesWrapper.querySelectorAll('.multiple-choice-option')[0].textContent).toBe('Option 1');
    expect(choicesWrapper.querySelectorAll('.multiple-choice-option')[1].textContent).toBe('Option 2');
    expect(newChoiceField.value).toBe('');

    getByTestId('edit-question-button').click();

    await waitFor(() =>{
        expect(questionField.value).toBe('');
        expect(categoryField.value).toBe('other');
        expect(imagePathField.value).toBe('');
        expect(timeToAnswerField.value).toBe('');
        expect(choicesWrapper.querySelectorAll('.multiple-choice-option').length).toBe(0);
    });
});

test('that form is not valid when question is missing', async () => {

    const fetchSpy = jest.spyOn(global, 'fetch');

    const quiz: Quiz = {
        id: '5',
        name: "Awesome Quiz",
        participants: [],
        playedQuestions: [],
        openQuestions: [],
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

    fireEvent.change(categoryField, { target: { value: 'science' } });
    fireEvent.change(timeToAnswerField, { target: { value: '30' } });

    expect(questionField.value).toBe('');
    expect(categoryField.value).toBe('science');
    expect(timeToAnswerField.value).toBe('30');
    expect(imagePathField.value).toBe('');
    expect(() => getByTestId('question-error')).toThrowError();

    questionButton.click();

    await waitFor(() =>{
        expect(questionField.value).toBe('');
        expect(getByTestId('question-error').textContent).toBe('errorQuestionMandatory');
        expect(categoryField.value).toBe('science');
        expect(imagePathField.value).toBe('');
        expect(timeToAnswerField.value).toBe('30');
    });

    expect(fetchSpy).not.toHaveBeenCalled();
});

test('that error message shown and removed depending on the question', () => {

    const quiz: Quiz = {
        id: '5',
        name: "Awesome Quiz",
        participants: [],
        playedQuestions: [],
        openQuestions: [],
        expirationDate: 1234,
        timestamp: 12345678,
        links: [{href: '/api/createQuestion', rel: 'createQuestion'}]
    }

    const { getByTestId } = render(<QuestionForm quiz={quiz} />);
    
    const questionField = getByTestId('new-question')  as HTMLInputElement;

    expect(questionField.value).toBe('');
    expect(() => getByTestId('question-error')).toThrowError();

    fireEvent.change(questionField, { target: { value: 'Frage 1' } });

    expect(questionField.value).toBe('Frage 1');
    expect(() => getByTestId('question-error')).toThrowError();

    fireEvent.change(questionField, { target: { value: '' } });

    expect(questionField.value).toBe('');
    expect(getByTestId('question-error').textContent).toBe('errorQuestionMandatory');

    fireEvent.change(questionField, { target: { value: 'Frage 1' } });

    expect(questionField.value).toBe('Frage 1');
    expect(() => getByTestId('question-error')).toThrowError();
});
