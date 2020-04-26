import React from 'react';
import { render } from '@testing-library/react';
import Participants from './Participants';
import Quiz from '../quiz';

test('has two participants', () => {
    const quiz: Quiz = {
        id: 1,
        name: 'Quiz',
        participants: [
            {
                id: 15,
                name: 'Erik',
                turn: false,
                points: 0,
                links: []
            },
            {
                id: 16,
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
    
    const { getByTestId } = render(<Participants quiz={quiz} />);
    const participants = getByTestId('participants').querySelectorAll('.participant');
    
    expect(participants).toHaveLength(2);
});

test('has no participants', () => {
    const quiz: Quiz = {
        id: 1,
        name: 'Quiz',
        participants: [],
        openQuestions: [],
        links: []
    }
    const { getByTestId } = render(<Participants quiz={quiz} />);
    const participants = getByTestId('participants').querySelectorAll('ParticipantItem');
    
    expect(participants).toHaveLength(0);
});
