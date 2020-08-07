import React from 'react';
import { render, cleanup } from '@testing-library/react';
import Participants from './Participants';
import Quiz from '../quiz';

beforeEach(() => () => cleanup()); 
afterEach(() => cleanup());

test('has two participants', () => {
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
                points: 0,
                links: []
            }
        ],
        openQuestions: [],
        playedQuestions: [],
        timestamp: 1234,
        links: []
    }
    
    const { getByTestId } = render(<Participants quiz={quiz} />);
    const participants = getByTestId('participants').querySelectorAll('.participant');
    
    expect(participants).toHaveLength(2);
    expect(() => getByTestId('question-counter')).toThrowError();
});

test('has no participants', () => {
    const quiz: Quiz = {
        id: '1',
        name: 'Quiz',
        participants: [],
        openQuestions: [],
        playedQuestions: [],
        timestamp: 1234,
        links: []
    }
    const { getByTestId } = render(<Participants quiz={quiz} />);
    const participants = getByTestId('participants').querySelectorAll('ParticipantItem');
    
    expect(participants).toHaveLength(0);
});

test('shows question counter', () => {
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
                points: 0,
                links: []
            }
        ],
        openQuestions: [
            {
                question: 'What is happening?',
                pending: true,
                secondsLeft: 12,
                links: []
            }
        ],
        playedQuestions: [],
        timestamp: 1234,
        links: []
    }
    
    const { getByTestId } = render(<Participants quiz={quiz} />);
    const participants = getByTestId('participants').querySelectorAll('.participant');
    
    expect(participants).toHaveLength(2);
    expect(() => getByTestId('question-counter')).not.toThrowError();
});
