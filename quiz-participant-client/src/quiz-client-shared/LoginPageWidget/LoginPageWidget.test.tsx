import React from 'react';
import { render, fireEvent, wait } from '@testing-library/react';
import LoginPageWidget from './LoginPageWidget';

test('should submit on submit button click', (done) => {
    const submitFunction = (value: string) => new Promise<void>(async (resolve) => {
        await wait(() => expect(getByTestId('submit-button').classList.contains('is-loading')).toBeTruthy());
        resolve();
        await wait(() => expect(value).toEqual({'Login': 'Name'}));
        await wait(() => expect(getByTestId('submit-button').classList.contains('is-loading')).toBeFalsy());
        done();
    });
    const { getByTestId } = render(<LoginPageWidget onSubmit={submitFunction} title="Login" inputLabels={['Login']} buttonLabel="Login" />);

    const inputField = getByTestId('field-0') as HTMLInputElement;
    const submitButton = getByTestId('submit-button') as HTMLButtonElement;

    expect(submitButton.classList.contains('is-loading')).toBeFalsy();

    fireEvent.change(inputField, { target: { value: 'Name' } });

    submitButton.click();
});

xtest('should submit on enter', (done) => {
    const submitFunction = (value: string) => new Promise<void>(async (resolve) => {
        await wait(() => expect(getByTestId('submit-button').classList.contains('is-loading')).toBeTruthy());
        resolve();
        await wait(() => expect(value).toEqual({'Login': 'Name'}));
        await wait(() => expect(getByTestId('submit-button').classList.contains('is-loading')).toBeFalsy());
        done();
    });
    const { getByTestId } = render(<LoginPageWidget onSubmit={submitFunction} title="Login" inputLabels={['Login']} buttonLabel="Login" />);

    const inputField = getByTestId('field-0') as HTMLInputElement;

    fireEvent.change(inputField, { target: { value: 'Name' } });
    // await wait(() => expect(inputField.value).toEqual('Name'));

    fireEvent.keyUp(inputField, { key: 'Enter', keyCode: 13 });
});
