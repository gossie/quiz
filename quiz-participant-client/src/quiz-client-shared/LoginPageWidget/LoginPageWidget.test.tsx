import React from 'react';
import { render, fireEvent } from '@testing-library/react';
import LoginPageWidget from './LoginPageWidget';

test('should submit on submit button click', (done) => {
    const submitFunction = (value: string) => {
        expect(value).toEqual({'Login': 'Name'});
        done();
    };
    const { getByTestId } = render(<LoginPageWidget onSubmit={submitFunction} title="Login" inputLabels={['Login']} buttonLabel="Login" />);

    const inputField = getByTestId('field-0') as HTMLInputElement;
    const submitButton = getByTestId('submit-button');

    fireEvent.change(inputField, { target: { value: 'Name' } });
    submitButton.click();
});

test('should submit on enter', (done) => {
    const submitFunction = (value) => {
        expect(value).toEqual({'Login': 'Name'});
        done();
    };
    const { getByTestId } = render(<LoginPageWidget onSubmit={submitFunction} title="Login" inputLabels={['Login']} buttonLabel="Login" />);

    const inputField = getByTestId('field-0') as HTMLInputElement;

    fireEvent.change(inputField, { target: { value: 'Name' } });
    fireEvent.keyUp(inputField, { key: 'Enter', keyCode: 13 });
});
