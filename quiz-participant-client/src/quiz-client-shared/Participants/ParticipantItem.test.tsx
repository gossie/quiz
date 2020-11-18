import React from 'react';
import { render } from '@testing-library/react';
import Quiz, { Participant, Question } from '../quiz';
import ParticipantItem from './ParticipantItem';

test('displays participant after a correct answer', () => {
    const participant: Participant = {
        id: '16',
        name: 'Sandra',
        turn: false,
        points: 2,
        links: []
    };

    const { getByTestId } = render(<ParticipantItem participant={participant} pointsAfterLastQuestion={0} />);

    const participantWrapper = getByTestId('participant-wrapper') as HTMLDivElement;
    
    const participantName = getByTestId('participant-name') as HTMLSpanElement;
    expect(participantName.textContent).toEqual('Sandra ');

    const points = participantWrapper.querySelector('.points')!;

    expect(points.textContent).toEqual('(0 +2)');
});

test('displays participant after an incorrect answer', () => {
    const participant: Participant = {
        id: '15',
        name: 'Erik',
        turn: false,
        points: 1,
        links: []
    };

    const { getByTestId } = render(<ParticipantItem participant={participant} pointsAfterLastQuestion={2} />);

    const participantWrapper = getByTestId('participant-wrapper') as HTMLDivElement;
    
    const participantName = getByTestId('participant-name') as HTMLSpanElement;
    expect(participantName.textContent).toEqual('Erik ');

    const points = participantWrapper.querySelector('.points')!;

    expect(points.textContent).toEqual('(2 -1)');
});

test('displays participant that did not answer', () => {
    const participant: Participant = {
        id: '15',
        name: 'Erik',
        turn: false,
        points: 2,
        links: []
    };

    const { getByTestId } = render(<ParticipantItem participant={participant} pointsAfterLastQuestion={2} />);

    const participantWrapper = getByTestId('participant-wrapper') as HTMLDivElement;
    
    const participantName = getByTestId('participant-name') as HTMLSpanElement;
    expect(participantName.textContent).toEqual('Erik ');

    const points = participantWrapper.querySelector('.points')!;

    expect(points.textContent).toEqual('(2)');
});

test('displays answer hint for freetext questions', () => {
    const participant: Participant = {
        id: '15',
        name: 'Erik',
        turn: false,
        points: 2,
        links: []
    };

    const question: Question = {
        id: '17',
        question: 'Was ist das?',
        estimates: {
            '15': '*****'
        },
        pending: true,
        links: []
    }

    const { getByTestId } = render(<ParticipantItem participant={participant} pointsAfterLastQuestion={2} question={question} />);

    expect(getByTestId('participant-answer')).toHaveClass("visible");
});

test('displays no answer hint because participant has not answered', () => {
    const participant: Participant = {
        id: '15',
        name: 'Erik',
        turn: false,
        points: 2,
        links: []
    };

    const question: Question = {
        id: '17',
        question: 'Was ist das?',
        estimates: {
            '19': '*****'
        },
        pending: true,
        links: []
    }

    const { getByTestId } = render(<ParticipantItem participant={participant} pointsAfterLastQuestion={2} question={question} />);

    expect(getByTestId('participant-answer')).not.toHaveClass("visible");
});

test('displays no answer hint because it is no freetext question', () => {
    const participant: Participant = {
        id: '15',
        name: 'Erik',
        turn: false,
        points: 2,
        links: []
    };

    const question: Question = {
        id: '17',
        question: 'Was ist das?',
        pending: true,
        links: []
    }

    const { getByTestId } = render(<ParticipantItem participant={participant} pointsAfterLastQuestion={2} question={question} />);

    expect(() => getByTestId('answer-hint')).toThrowError();
});

test('displays no answer hint because no question is pending', () => {
    const participant: Participant = {
        id: '15',
        name: 'Erik',
        turn: false,
        points: 2,
        links: []
    };

    const { getByTestId } = render(<ParticipantItem participant={participant} pointsAfterLastQuestion={2} />);

    expect(() => getByTestId('answer-hint')).toThrowError();
});
