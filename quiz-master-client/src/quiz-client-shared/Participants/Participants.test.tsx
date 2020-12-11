import React from 'react';
import { render, cleanup } from '../../test-utils';
import Participants from './Participants';
import Quiz from '../quiz';

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

jest.mock('react-i18next', () => ({
    useTranslation: () => {
        return {
            t: (str: string, keys: object) => str === 'secondsLeft' ? `${keys['seconds']} seconds left` : null,
            i18n: {
                changeLanguage: () => new Promise(() => {}),
            },
        };
        
    },
}));

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
        expirationDate: 1234,
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
        expirationDate: 1234,
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
        openQuestions: [
            {
                id: '117',
                category: 'history',
                publicVisible: false,
                question: 'What is happening?',
                pending: true,
                secondsLeft: 12,
                links: []
            }
        ],
        playedQuestions: [],
        timestamp: 1234,
        expirationDate: 1234,
        links: []
    }
    
    const { getByTestId } = render(<Participants quiz={quiz} />);
    const participants = getByTestId('participants').querySelectorAll('.participant');
    const countdown = getByTestId('question-counter') as HTMLSpanElement;
    
    expect(participants).toHaveLength(2);
    expect(countdown.textContent).toEqual('12 seconds left');
});
