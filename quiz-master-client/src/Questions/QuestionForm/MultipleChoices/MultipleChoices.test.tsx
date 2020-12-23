import React from 'react';
import { render, fireEvent, waitFor, cleanup } from '../../../test-utils';
import MultipleChoices from './MultipleChoices';

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

test('should add choice', (done) => {

    const choiceAdded = (choice: string) => {
        expect(choice).toEqual('Option 1');
        done();
    }

    const { getByTestId } = render(<MultipleChoices choices={[]} onChoiceAdd={choiceAdded} onChoiceDelete={() => {}} />)

    const newChoiceField = getByTestId('new-choice') as HTMLInputElement;
    const addOption = getByTestId('add-option');

    fireEvent.change(newChoiceField, { target: { value: 'Option 1' } });
    addOption.click();
});

test('should delete choice', (done) => {

    const choiceDeleted = (index: number) => {
        expect(index).toEqual(1);
        done();
    }

    const { getByTestId } = render(<MultipleChoices choices={['Option 1', 'Option 2', 'Option 3']} onChoiceAdd={() => {}} onChoiceDelete={choiceDeleted} />)

    getByTestId('delete-multiple-choice-option-1').click();
});
