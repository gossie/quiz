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

test('should render choices', () => {
    const { getByTestId } = render(<MultipleChoices choices={['Option 1', 'Option 2', 'Option 3']} onChoiceAdd={() => {}} onChoiceEdit={() => {}} onChoiceDelete={() => {}} />)
    expect(getByTestId('choices').querySelectorAll('.multiple-choice-option').length).toBe(3);
});

test('should add choice', (done) => {

    const choiceAdded = (choice: string) => {
        expect(choice).toEqual('Option 1');
        done();
    }

    const { getByTestId } = render(<MultipleChoices choices={[]} onChoiceAdd={choiceAdded} onChoiceEdit={() => {}} onChoiceDelete={() => {}} />)

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

    const { getByTestId } = render(<MultipleChoices choices={['Option 1', 'Option 2', 'Option 3']} onChoiceAdd={() => {}} onChoiceEdit={() => {}} onChoiceDelete={choiceDeleted} />)

    getByTestId('delete-multiple-choice-option-1').click();
});

test('should edit choice', (done) => {

    const onEdit = (value: string, index: number) => {
        expect(value).toEqual('Option 2 (changed)');
        expect(index).toEqual(1);

        setTimeout(() => {
            expect(() => getByTestId('edit-multiple-choice-option-1')).not.toThrowError();
            expect(() => getByTestId('edit-multiple-choice-option-save-1')).toThrowError();
            expect(() => getByTestId('edit-muliple-choice-option-input-1')).toThrowError();
            done();
        });
    }

    const { getByTestId } = render(<MultipleChoices choices={['Option 1', 'Option 2', 'Option 3']} onChoiceAdd={() => {}} onChoiceEdit={onEdit} onChoiceDelete={() => {}} />)

    getByTestId('edit-multiple-choice-option-1').click();

    const editInput = getByTestId('edit-muliple-choice-option-input-1') as HTMLInputElement;
    expect(() => getByTestId('edit-multiple-choice-option-1')).toThrowError();

    expect(editInput.value).toEqual('Option 2');
    fireEvent.change(editInput, { target: { value: 'Option 2 (changed)' } });

    getByTestId('edit-multiple-choice-option-save-1').click();
});
