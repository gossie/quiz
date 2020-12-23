import React, { useState } from 'react';
import { useTranslation } from 'react-i18next';

interface MultipleChoiceProps {
    choices: Array<string>;
    onChoiceAdd: (newChoice: string) => void;
    onChoiceDelete: (index: number) => void;
}

const MultipleChoices: React.FC<MultipleChoiceProps> = (props: MultipleChoiceProps) => {
    const [newChoice, setNewChoice] = useState('');

    const { t } = useTranslation();

    const addChoice = () => {
        props.onChoiceAdd(newChoice);
        setNewChoice('');
    }

    const choiceElements = props.choices.map(
        (choice, index) => 
            <div className="multiple-choice-option">
                <span className="text">{choice}</span>
                <span data-testid={`delete-multiple-choice-option-${index}`} className="icon clickable has-text-danger" title={t('titleDeleteMultipleChoiceOption')} onClick={() => props.onChoiceDelete(index)}><i className="fa fa-trash"></i></span>
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
                    <input data-testid="new-choice" value={newChoice} onChange={ev => setNewChoice(ev.target.value)} onKeyUp={ev => {if (ev.keyCode === 13) addChoice()}} className="input new-multiple-choice-option" type="text" />
                    <span data-testid="add-option" className="icon has-text-secondary clickable multiple-choice-add-option" title={t('titleAddMultipleChoiceOption')} onClick={addChoice}><i className="fas fa-plus"></i></span>
                </div>
            </div>
        </div>
    );
}

export default MultipleChoices;