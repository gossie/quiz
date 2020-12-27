import React from 'react';
import { render, fireEvent, waitFor } from '@testing-library/react';
import LoginPageWidget from './LoginPageWidget';

test('should submit on submit button click', (done) => {
    const submitFunction = (value: string) => new Promise<void>(async (resolve) => {
        await waitFor(() => expect(getByTestId('submit-button').classList.contains('is-loading')).toBeTruthy());
        resolve();
        await waitFor(() => expect(value).toEqual({'Login': 'Name'}));
        await waitFor(() => expect(getByTestId('submit-button').classList.contains('is-loading')).toBeFalsy());
        done();
    });
    const { getByTestId } = render(<LoginPageWidget onSubmit={submitFunction} title="Login" inputInformation={[{label: 'Login'}]} buttonLabel="Login" />);

    const inputField = getByTestId('field-0') as HTMLInputElement;
    const submitButton = getByTestId('submit-button') as HTMLButtonElement;

    expect(submitButton.classList.contains('is-loading')).toBeFalsy();

    fireEvent.change(inputField, { target: { value: 'Name' } });

    submitButton.click();
});

test('should submit on enter', (done) => {
    const submitFunction = (value: string) => new Promise<void>(async (resolve) => {
        await waitFor(() => expect(getByTestId('submit-button').classList.contains('is-loading')).toBeTruthy());
        resolve();
        await waitFor(() => expect(value).toEqual({'Login': 'Name'}));
        await waitFor(() => expect(getByTestId('submit-button').classList.contains('is-loading')).toBeFalsy());
        done();
    });
    const { getByTestId } = render(<LoginPageWidget onSubmit={submitFunction} title="Login" inputInformation={[{label: 'Login'}]} buttonLabel="Login" />);

    const inputField = getByTestId('field-0') as HTMLInputElement;

    fireEvent.change(inputField, { target: { value: 'Name' } });

    fireEvent.keyUp(inputField, { key: 'Enter', keyCode: 13 });
});
