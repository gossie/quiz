import React, { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Question, Choice } from '../../quiz-client-shared/quiz';

interface MultipleChoiceProps {
    question: Question;
    participantId: string;
}

const MultipleChoice: React.FC<MultipleChoiceProps> = (props: MultipleChoiceProps) => {

    const [selectedIndex, setSelectedIndex] = useState(-1);

    useEffect(() => {
        setSelectedIndex(-1);
    }, [props.question.question]);
    
    const selectChoice = (choice: Choice, index: number) => {
        const selectHref = choice.links.find(link => link.rel === `${props.participantId}-selects-choice`).href;
        fetch(`${process.env.REACT_APP_BASE_URL}${selectHref}`, {
            method: 'PUT'
        })
        .then(() => setSelectedIndex(index));
    };

    const choices = props.question.choices.map((choice, index) => (
        <div key={`option-${index}`} data-testid={`multiple-choice-option-${index}`} className="clickable" onClick={() => selectChoice(choice, index)}>
            {choice.choice}
            { selectedIndex === index && <span className="icon"><i className="fas fa-arrow-left"></i></span>}
        </div>
    ));

    return (
        <div>
            { choices }
        </div>
    )
}

export default MultipleChoice;