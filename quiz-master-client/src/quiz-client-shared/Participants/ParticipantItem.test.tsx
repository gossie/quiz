import React from 'react';
import { render } from '@testing-library/react';
import Quiz from '../quiz';
import ParticipantItem from './ParticipantItem';

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
                links: []
            },
            {
                id: '16',
                name: 'Sandra',
                turn: false,
                points: 2,
                links: []
            }
        ],
        openQuestions: [],
        playedQuestions: [],
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
                links: []
            },
            {
                id: '16',
                name: 'Sandra',
                turn: false,
                points: 0,
                links: []
            }
        ],
        openQuestions: [],
        playedQuestions: [],
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
                links: []
            },
            {
                id: '16',
                name: 'Sandra',
                turn: false,
                points: 0,
                links: []
            }
        ],
        openQuestions: [],
        playedQuestions: [],
        links: []
    }

    const { getByTestId } = render(<ParticipantItem quiz={quiz} participant={quiz.participants[0]} pointsAfterLastQuestion={2} />);

    const participantWrapper = getByTestId('participant-wrapper') as HTMLDivElement;
    
    const participantName = getByTestId('participant-name') as HTMLSpanElement;
    expect(participantName.textContent).toEqual('Erik ');

    const points = participantWrapper.querySelector('.points')!;

    expect(points.textContent).toEqual('(2)');
});
