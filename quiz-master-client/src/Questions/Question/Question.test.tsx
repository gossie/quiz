import React from 'react';
import { render, cleanup } from '@testing-library/react';
import Quiz, { Question } from '../../quiz-client-shared/quiz';
import QuestionElement from './Question';

beforeEach(() => () => cleanup()); 
afterEach(() => cleanup());

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


test('should reopen question', () => {
    jest.spyOn(global, 'fetch').mockImplementation((url: string, request: object) => {
        expect(url).toEqual('http://localhost:5000/api/reopen');
        expect(request).toEqual({
            method: 'PUT',
            headers: {
                Accept: 'application/json'
            }
        });
        Promise.resolve();
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
        links: [{ rel: 'reopenQuestion', href: '/api/reopen' }]
    }

    const { getByTestId } = render(<QuestionElement enableOperations={true} index={0} quiz={quiz} question={quiz.openQuestions[0]} setImageToDisplay={(path) => {}} />);

    getByTestId('reopen-button').click();
});
