import React from 'react';
import { render, cleanup } from '@testing-library/react';
import Quiz from '../quiz';
import ParticipantItem from './ParticipantItem';

beforeEach(() => () => cleanup()); 
afterEach(() => cleanup());

test('displays participant after a correct answer', () => {
    const quiz: Quiz = {
        id: '1',
        name: 'Quiz',
        participants: [
            {
                id: '15',
                name: 'Erik',
                turn: false,
                points: 0,
                revealAllowed: true,
                links: []
            },
            {
                id: '16',
                name: 'Sandra',
                turn: false,
                points: 2,
                revealAllowed: true,
                links: []
            }
        ],
        openQuestions: [],
        playedQuestions: [],
        timestamp: 1234,
        links: []
    }

    const { getByTestId } = render(<ParticipantItem quiz={quiz} participant={quiz.participants[1]} pointsAfterLastQuestion={0} />);

    const participantWrapper = getByTestId('participant-wrapper') as HTMLDivElement;
    
    const participantName = getByTestId('participant-name') as HTMLSpanElement;
    expect(participantName.textContent).toEqual('Sandra ');

    const points = participantWrapper.querySelector('.points')!;

    expect(points.textContent).toEqual('(0 +2)');
});

test('displays participant after an incorrect answer', () => {
    const quiz: Quiz = {
        id: '1',
        name: 'Quiz',
        participants: [
            {
                id: '15',
                name: 'Erik',
                turn: false,
                points: 1,
                revealAllowed: true,
                links: []
            },
            {
                id: '16',
                name: 'Sandra',
                turn: false,
                points: 0,
                revealAllowed: true,
                links: []
            }
        ],
        openQuestions: [],
        playedQuestions: [],
        timestamp: 1234,
        links: []
    }

    const { getByTestId } = render(<ParticipantItem quiz={quiz} participant={quiz.participants[0]} pointsAfterLastQuestion={2} />);

    const participantWrapper = getByTestId('participant-wrapper') as HTMLDivElement;
    
    const participantName = getByTestId('participant-name') as HTMLSpanElement;
    expect(participantName.textContent).toEqual('Erik ');

    const points = participantWrapper.querySelector('.points')!;

    expect(points.textContent).toEqual('(2 -1)');
});

test('displays participant that did not answer', () => {
    const quiz: Quiz = {
        id: '1',
        name: 'Quiz',
        participants: [
            {
                id: '15',
                name: 'Erik',
                turn: false,
                points: 2,
                revealAllowed: true,
                links: []
            },
            {
                id: '16',
                name: 'Sandra',
                turn: false,
                points: 0,
                revealAllowed: true,
                links: []
            }
        ],
        openQuestions: [],
        playedQuestions: [],
        timestamp: 1234,
        links: []
    }

    const { getByTestId } = render(<ParticipantItem quiz={quiz} participant={quiz.participants[0]} pointsAfterLastQuestion={2} />);

    const participantWrapper = getByTestId('participant-wrapper') as HTMLDivElement;
    
    const participantName = getByTestId('participant-name') as HTMLSpanElement;
    expect(participantName.textContent).toEqual('Erik ');

    const points = participantWrapper.querySelector('.points')!;

    expect(points.textContent).toEqual('(2)');
});

test('displays icon to indicate that answer will not be revealed', () => {
    const quiz: Quiz = {
        id: '1',
        name: 'Quiz',
        participants: [
            {
                id: '15',
                name: 'Erik',
                turn: false,
                points: 0,
                revealAllowed: false,
                links: []
            }
        ],
        openQuestions: [],
        playedQuestions: [],
        timestamp: 1234,
        links: []
    }

    const { getByTestId } = render(<ParticipantItem quiz={quiz} participant={quiz.participants[0]} pointsAfterLastQuestion={0} />);

    expect(() => getByTestId('reveal-not-allowed')).not.toThrowError();
});

test('displays no icon because reveal is allowed', () => {
    const quiz: Quiz = {
        id: '1',
        name: 'Quiz',
        participants: [
            {
                id: '15',
                name: 'Erik',
                turn: false,
                points: 0,
                revealAllowed: true,
                links: []
            }
        ],
        openQuestions: [],
        playedQuestions: [],
        timestamp: 1234,
        links: []
    }

    const { getByTestId } = render(<ParticipantItem quiz={quiz} participant={quiz.participants[0]} pointsAfterLastQuestion={0} />);

    expect(() => getByTestId('reveal-not-allowed')).toThrowError();
});

test('deletes participant', () => {
    jest.spyOn(global, 'fetch').mockImplementation((url: string, request: object) => {
        expect(url).toEqual('http://localhost:5000/api/quiz/1/participants/15');
        expect(request).toEqual({
            method: 'DELETE'
        });
        Promise.resolve();
    });

    const quiz: Quiz = {
        id: '1',
        name: 'Quiz',
        participants: [
            {
                id: '15',
                name: 'Erik',
                turn: false,
                points: 0,
                revealAllowed: true,
                links: [
                    {
                        rel: 'delete',
                        href: '/api/quiz/1/participants/15'
                    }
                ]
            }
        ],
        openQuestions: [],
        playedQuestions: [],
        timestamp: 1234,
        links: []
    }

    const { getByTestId } = render(<ParticipantItem quiz={quiz} participant={quiz.participants[0]} pointsAfterLastQuestion={0} />);

    getByTestId('delete').click();

});
