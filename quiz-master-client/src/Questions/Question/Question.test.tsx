import React from 'react';
import { render, cleanup } from '@testing-library/react';
import { Question } from '../../quiz-client-shared/quiz';
import QuestionElement from './Question';

beforeEach(() => () => cleanup()); 
afterEach(() => cleanup());

test('should start editing', async done => {
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
    
    expect(getByTestId('question').textContent).toEqual('#1 Warum?');
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

    expect(getByTestId('question').textContent).toEqual('#1 Warum?');
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
