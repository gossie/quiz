import React from 'react';
import { render, fireEvent, wait } from '@testing-library/react';
import Participants from './Participants';
import Quiz from './quiz';

test('has two participants', () => {
    const quiz: Quiz = {
        participants: ['Erik', 'Sandra'],
        links: []
    }
    const { getByTestId } = render(<Participants quiz={quiz} />);
    const participants = getByTestId('participants').querySelectorAll('span');
    
    expect(participants).toHaveLength(2);
    expect(participants[0].textContent).toBe('Erik');
    expect(participants[1].textContent).toBe('Sandra');
});

test('has no participants', () => {
    const quiz: Quiz = {
        participants: [],
        links: []
    }
    const { getByTestId } = render(<Participants quiz={quiz} />);
    const participants = getByTestId('participants').querySelectorAll('span');
    
    expect(participants).toHaveLength(0);
});

test('adds new participants', async () => {
    jest.spyOn(global, 'fetch').mockImplementation(() => Promise.resolve());

    const quiz: Quiz = {
        participants: [],
        links: [{
            rel: 'createParticipant',
            href: '/api/quiz/17/participants'
        }]
    }

    const { getByTestId } = render(<Participants quiz={quiz} />);

    const nameField = getByTestId('participant-name') as HTMLInputElement;
    const addButton = getByTestId('add-button') as HTMLButtonElement;

    fireEvent.change(nameField, { target: { value: 'Allli' } });
    
    expect(nameField.value).toBe('Allli');

    addButton.click();

    await wait(() => expect(nameField.value).toBe(''));
});
