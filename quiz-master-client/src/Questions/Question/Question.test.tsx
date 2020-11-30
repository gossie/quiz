import React from 'react';
import { render, cleanup } from '../../test-utils';
import Quiz, { Question } from '../../quiz-client-shared/quiz';
import QuestionElement from './Question';
import { ErrorAction } from '../../redux/actions';
import { ActionType } from '../../redux/action-types';

beforeEach(() => () => cleanup()); 
afterEach(() => cleanup());

jest.mock('react-i18next', () => ({
    useTranslation: () => {
        return {
            t: (str: string, keys: object) => {
                switch (str) {
                    case 'titleSecondsToAnswer': return `${keys['seconds']} seconds to answer`;
                    case 'titleCorrectAnswer': return `The answer is '${keys['answer']}'`;
                    default: return str;
                }
            },
            i18n: {
                changeLanguage: () => new Promise(() => {}),
            },
        };
        
    },
}));

test('should start editing', done => {
    const question: Question = {
        id: '1',
        question: 'Warum?',
        category: 'other',
        imagePath: 'pathToImage',
        publicVisible: false,
        pending: false,
        links: []
    }

    const onEdit = (questionToEdit: Question) => {
        expect(questionToEdit).toEqual(question);
        done();
    };

    const { getByTestId } = render(<QuestionElement enableOperations={true} question={question} index={0} setImageToDisplay={(path) => {}} onEdit={onEdit} />);

    const editModeButton = getByTestId('edit-question-0') as HTMLButtonElement;
    editModeButton.click();
});

test('should be a freetext question', async () => {
    const question: Question = {
        id: '1',
        question: 'Warum?',
        estimates: {},
        category: 'other',
        imagePath: 'pathToImage',
        publicVisible: false,
        pending: false,
        links: []
    }

    const { getByTestId } = render(<QuestionElement enableOperations={true} question={question} index={0} setImageToDisplay={(path) => {}} />);
    expect(getByTestId('index').textContent).toEqual('1');
    expect(getByTestId('question').textContent).toEqual('Warum?');
    expect(() => getByTestId('freetext-question-0')).not.toThrowError();
    expect(() => getByTestId('buzzer-question-0')).toThrowError();
});

test('should be a buzzer question', async () => {
    const question: Question = {
        id: '1',
        question: 'Warum?',
        category: 'other',
        imagePath: 'pathToImage',
        publicVisible: false,
        pending: false,
        links: []
    }

    const { getByTestId } = render(<QuestionElement enableOperations={true} question={question} index={0} setImageToDisplay={(path) => {}} />);

    expect(getByTestId('index').textContent).toEqual('1');
    expect(getByTestId('question').textContent).toEqual('Warum?');
    expect(() => getByTestId('freetext-question-0')).toThrowError();
    expect(() => getByTestId('buzzer-question-0')).not.toThrowError();
});

test('that stop watch is shown', () => {
    const question: Question = {
        id: '1',
        question: 'Warum?',
        timeToAnswer: 30,
        category: 'other',
        imagePath: 'pathToImage',
        publicVisible: false,
        pending: false,
        links: []
    }

    const { getByTestId } = render(<QuestionElement enableOperations={true} question={question} index={0} setImageToDisplay={(path) => {}} />);

    const stopWatchIcon = getByTestId('stop-watch-0') as HTMLSpanElement;

    expect(stopWatchIcon.title).toEqual('30 seconds to answer');
});

test('that stop watch is not shown', () => {
    const question: Question = {
        id: '1',
        question: 'Warum?',
        category: 'other',
        imagePath: 'pathToImage',
        publicVisible: false,
        pending: false,
        links: []
    }

    const { getByTestId } = render(<QuestionElement enableOperations={true} question={question} index={0} setImageToDisplay={(path) => {}} />);

    expect(() => getByTestId('stop-watch-0')).toThrowError();
});


test('should reopen question', (done) => {
    jest.spyOn(global, 'fetch').mockImplementation((url: string, request: object) => {
        expect(url).toEqual('http://localhost:5000/api/reopen');
        expect(request).toEqual({
            method: 'PUT',
            headers: {
                Accept: 'application/json'
            }
        });
        return new Promise((resolve) => {
            resolve({status: 200});
            done();
        });
    });

    const quiz: Quiz = {
        id: '17',
        name: 'Test',
        participants: [
            {
                id: '12',
                name: 'Lena',
                turn: false,
                points: 13,
                links: [],
                revealAllowed: false
            },
            {
                id: '13',
                name: 'Erik',
                turn: true,
                points: 13,
                links: [],
                revealAllowed: false
            }
        ],
        playedQuestions: [],
        openQuestions: [
            { 
                id: '123',
                question: 'Who is who?',
                pending: true,
                category: 'Other',
                publicVisible: true,
                imagePath: '',
                links: []
            }
        ],
        timestamp: 100,
        expirationDate: 1234,
        links: [{ rel: 'reopenQuestion', href: '/api/reopen' }]
    }

    const { getByTestId } = render(<QuestionElement enableOperations={true} index={0} quiz={quiz} question={quiz.openQuestions[0]} setImageToDisplay={(path) => {}} />);

    getByTestId('reopen-button').click();
});

test('should not reopen question, because the quiz is already finished', (done) => {

    jest.spyOn(global, 'fetch').mockImplementation((url: string, request: object) => {
        expect(url).toEqual('http://localhost:5000/api/reopen');
        expect(request).toEqual({
            method: 'PUT',
            headers: {
                Accept: 'application/json'
            }
        });
        return Promise.resolve({status: 409});
    });

    const quiz: Quiz = {
        id: '17',
        name: 'Test',
        participants: [
            {
                id: '12',
                name: 'Lena',
                turn: false,
                points: 13,
                links: [],
                revealAllowed: false
            },
            {
                id: '13',
                name: 'Erik',
                turn: true,
                points: 13,
                links: [],
                revealAllowed: false
            }
        ],
        playedQuestions: [],
        openQuestions: [
            { 
                id: '123',
                question: 'Who is who?',
                pending: true,
                category: 'Other',
                publicVisible: true,
                imagePath: '',
                links: []
            }
        ],
        timestamp: 100,
        expirationDate: 1234,
        links: [{ rel: 'reopenQuestion', href: '/api/reopen' }]
    }

    const onError = (state = {}, action: ErrorAction) => {
        if (action.type === ActionType.SHOW_ERROR) {
            expect(action.payload.errorMessage).toBe('errorMessageConflict');
            done();
        }
        return {};
    }

    const { getByTestId } = render(<QuestionElement enableOperations={true} index={0} quiz={quiz} question={quiz.openQuestions[0]} setImageToDisplay={(path) => {}} />, { reducer: onError });

    getByTestId('reopen-button').click();
});

test('should not delete question, because the quiz is already finished', (done) => {

    jest.spyOn(global, 'fetch').mockImplementation((url: string, request: object) => {
        expect(url).toEqual('http://localhost:5000/api/quiz/17/questions/123');
        expect(request).toEqual({
            method: 'DELETE'
        });
        return Promise.resolve({status: 409});
    });

    const quiz: Quiz = {
        id: '17',
        name: 'Test',
        participants: [
            {
                id: '12',
                name: 'Lena',
                turn: false,
                points: 13,
                links: [],
                revealAllowed: false
            },
            {
                id: '13',
                name: 'Erik',
                turn: true,
                points: 13,
                links: [],
                revealAllowed: false
            }
        ],
        playedQuestions: [],
        openQuestions: [
            { 
                id: '123',
                question: 'Who is who?',
                pending: false,
                category: 'Other',
                publicVisible: true,
                imagePath: '',
                links: [
                    {
                        rel: 'self',
                        href: '/api/quiz/17/questions/123'
                    }
                ]
            }
        ],
        timestamp: 100,
        expirationDate: 1234,
        links: [{ rel: 'reopenQuestion', href: '/api/reopen' }]
    }

    const onError = (state = {}, action: ErrorAction) => {
        if (action.type === ActionType.SHOW_ERROR) {
            expect(action.payload.errorMessage).toBe('errorMessageConflict');
            done();
        }
        return {};
    }

    const { getByTestId } = render(<QuestionElement enableOperations={true} index={0} quiz={quiz} question={quiz.openQuestions[0]} setImageToDisplay={(path) => {}} />, { reducer: onError });

    getByTestId('delete-question-0').click();
});

test('should not ask question, because the quiz is already finished', (done) => {

    jest.spyOn(global, 'fetch').mockImplementation((url: string, request: object) => {
        expect(url).toEqual('http://localhost:5000/api/quiz/17/questions/123');
        expect(request).toEqual({
            method: 'PATCH'
        });
        return Promise.resolve({status: 409});
    });

    const quiz: Quiz = {
        id: '17',
        name: 'Test',
        participants: [
            {
                id: '12',
                name: 'Lena',
                turn: false,
                points: 13,
                links: [],
                revealAllowed: false
            },
            {
                id: '13',
                name: 'Erik',
                turn: true,
                points: 13,
                links: [],
                revealAllowed: false
            }
        ],
        playedQuestions: [],
        openQuestions: [
            { 
                id: '123',
                question: 'Who is who?',
                pending: false,
                category: 'Other',
                publicVisible: true,
                imagePath: '',
                links: [
                    {
                        rel: 'self',
                        href: '/api/quiz/17/questions/123'
                    }
                ]
            }
        ],
        timestamp: 100,
        expirationDate: 1234,
        links: [{ rel: 'reopenQuestion', href: '/api/reopen' }]
    }

    const onError = (state = {}, action: ErrorAction) => {
        if (action.type === ActionType.SHOW_ERROR) {
            expect(action.payload.errorMessage).toBe('errorMessageConflict');
            done();
        }
        return {};
    }

    const { getByTestId } = render(<QuestionElement enableOperations={true} index={0} quiz={quiz} question={quiz.openQuestions[0]} setImageToDisplay={() => {}} />, { reducer: onError });

    getByTestId('start-question-0').click();
});

test('should display answer hint', () => {

    const quiz: Quiz = {
        id: '17',
        name: 'Test',
        participants: [
            {
                id: '12',
                name: 'Lena',
                turn: false,
                points: 13,
                links: [],
                revealAllowed: false
            },
            {
                id: '13',
                name: 'Erik',
                turn: true,
                points: 13,
                links: [],
                revealAllowed: false
            }
        ],
        playedQuestions: [],
        openQuestions: [
            { 
                id: '123',
                question: 'Who is who?',
                correctAnswer: 'That is who',
                pending: false,
                category: 'Other',
                publicVisible: true,
                imagePath: '',
                links: [
                    {
                        rel: 'self',
                        href: '/api/quiz/17/questions/123'
                    }
                ]
            }
        ],
        timestamp: 100,
        expirationDate: 1234,
        links: [{ rel: 'reopenQuestion', href: '/api/reopen' }]
    }

    const { getByTestId } = render(<QuestionElement enableOperations={true} index={0} quiz={quiz} question={quiz.openQuestions[0]} setImageToDisplay={() => {}} />);

    expect(getByTestId('answer-hint-0').title).toBe('The answer is \'That is who\'');

});

test('should not display answer hint', () => {

    const quiz: Quiz = {
        id: '17',
        name: 'Test',
        participants: [
            {
                id: '12',
                name: 'Lena',
                turn: false,
                points: 13,
                links: [],
                revealAllowed: false
            },
            {
                id: '13',
                name: 'Erik',
                turn: true,
                points: 13,
                links: [],
                revealAllowed: false
            }
        ],
        playedQuestions: [],
        openQuestions: [
            { 
                id: '123',
                question: 'Who is who?',
                pending: false,
                category: 'Other',
                publicVisible: true,
                imagePath: '',
                links: [
                    {
                        rel: 'self',
                        href: '/api/quiz/17/questions/123'
                    }
                ]
            }
        ],
        timestamp: 100,
        expirationDate: 1234,
        links: [{ rel: 'reopenQuestion', href: '/api/reopen' }]
    }

    const { getByTestId } = render(<QuestionElement enableOperations={true} index={0} quiz={quiz} question={quiz.openQuestions[0]} setImageToDisplay={() => {}} />);

    expect(() => getByTestId('answer-hint-0')).toThrowError();

});
