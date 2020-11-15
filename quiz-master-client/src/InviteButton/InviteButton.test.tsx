import React from 'react';
import { render } from '@testing-library/react';
import InviteButton from './InviteButton';

test('should show participant URL when clicked', () => {

    const { getByTestId } = render(<InviteButton quizId={'18'} />);

    const button = getByTestId('invite-button');
    expect(button).toBeDefined();

    button.click();

    const input = getByTestId('participant-url-input');
    expect(input).toBeDefined();

    expect((input as HTMLInputElement).value).toBe("http://localhost:3001?quiz_id=18")
});

