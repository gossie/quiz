import React from 'react';
import { render, fireEvent, wait } from '@testing-library/react';
import Participants from './Participants';
import Quiz from './quiz';

test('has two participants', () => {
    // const quiz: Quiz = {
    //     participants: [
    //         {
    //             id: 15,
    //             name: 'Erik',
    //             turn: false,
    //             points: 0,
    //             links: []
    //         },
    //         {
    //             id: 16,
    //             name: 'Sandra',
    //             turn: false,
    //             points: 0,
    //             links: []
    //         }
    //     ],
    //     links: []
    // }
    // const { getByTestId } = render(<Participants quiz={quiz} />);
    // const participants = getByTestId('participants').querySelectorAll('span');
    
    // expect(participants).toHaveLength(2);
    // expect(participants[0].textContent).toBe('Erik');
    // expect(participants[1].textContent).toBe('Sandra');
});

test('has no participants', () => {
    const quiz: Quiz = {
        participants: [],
        openQuestions: [],
        links: []
    }
    const { getByTestId } = render(<Participants quiz={quiz} />);
    const participants = getByTestId('participants').querySelectorAll('span');
    
    expect(participants).toHaveLength(0);
});
