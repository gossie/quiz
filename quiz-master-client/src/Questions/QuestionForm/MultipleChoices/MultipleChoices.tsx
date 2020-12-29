import React, { useState } from 'react';
import { useTranslation } from 'react-i18next';

interface MultipleChoiceProps {
    choices: Array<string>;
    onOptionChange: (option: string) => void;
    onChoiceAdd: (newChoice: string) => void;
    onChoiceEdit: (editedChoide: string, index: number) => void;
    onChoiceDelete: (index: number) => void;
}

const MultipleChoices: React.FC<MultipleChoiceProps> = (props: MultipleChoiceProps) => {
    const [newChoice, setNewChoice] = useState('');
    const [editedChoice, setEditedChoice] = useState('');
    const [indexToEdit, setIndexToEdit] = useState(-1);

    const { t } = useTranslation();

    const optionChanged = (option: string) => {
        props.onOptionChange(option);
        setNewChoice(option);
    }

    const addChoice = () => {
        props.onOptionChange('')
        props.onChoiceAdd(newChoice);
        setNewChoice('');
    }

    const startEdit = (index: number) => {
        setIndexToEdit(index);
        setEditedChoice(props.choices[index]);
    }

    const edit = () => {
        props.onChoiceEdit(editedChoice, indexToEdit);
        setEditedChoice('');
        setIndexToEdit(-1);
    };

    const choiceElements = props.choices.map(
        (choice, index) => 
            <div key={`choice-wrapper-${index}`} className="multiple-choice-option">
                { indexToEdit === index
                    ? <input data-testid={`edit-muliple-choice-option-input-${index}`} value={editedChoice} onChange={ev => setEditedChoice(ev.target.value)} onKeyUp={ev => {if (ev.keyCode === 13) edit()}} className="input new-multiple-choice-option" type="text" />
                    : <span className="text">{choice}</span>
                }
                { indexToEdit === index
                    ? <span key={`save-edit-${index}`} data-testid={`edit-multiple-choice-option-save-${index}`} className="icon clickable " title={t('titleEditMultipleChoiceOption')} onClick={() => edit()}><i className="fas fa-save"></i></span>
                    : <span key={`start-edit-${index}`} data-testid={`edit-multiple-choice-option-${index}`} className="icon clickable has-text-warning" title={t('titleEditMultipleChoiceOption')} onClick={() => startEdit(index)}><i className="fa fa-pencil"></i></span>
                }
                { indexToEdit !== index && <span data-testid={`delete-multiple-choice-option-${index}`} className="icon clickable has-text-danger" title={t('titleDeleteMultipleChoiceOption')} onClick={() => props.onChoiceDelete(index)}><i className="fa fa-trash"></i></span> }
            </div>
    );

    return (
        <div data-testid="choices" className="field">
            <div className="field">
                <label className="label">{t('labelOption')}</label>
                <div className="multiple-choice-options">
                    {choiceElements}
                </div>
                <div className="control">
                    <input data-testid="new-choice" value={newChoice} onChange={ev => optionChanged(ev.target.value)} onKeyUp={ev => {if (ev.keyCode === 13) addChoice()}} className="input new-multiple-choice-option" type="text" />
                    <span data-testid="add-option" className="icon has-text-secondary clickable multiple-choice-add-option" title={t('titleAddMultipleChoiceOption')} onClick={addChoice}><i className="fas fa-plus"></i></span>
                </div>
            </div>
        </div>
    );
}

export default MultipleChoices;