import React, { useEffect, useState } from 'react';
import { Question, Choice } from '../../quiz-client-shared/quiz';

import './MultipleChoice.scss';

interface MultipleChoiceProps {
    question: Question;
    participantId: string;
}

const MultipleChoice: React.FC<MultipleChoiceProps> = (props: MultipleChoiceProps) => {

    const [selectedIndex, setSelectedIndex] = useState(-1);

    const disabled = props.question.revealed || (props.question.secondsLeft != null && props.question.secondsLeft <= 0);

    useEffect(() => {
        setSelectedIndex(-1);
    }, [props.question.id]);
    
    const selectChoice = (choice: Choice, index: number) => {
        if (!disabled) {
            const selectHref = choice.links.find(link => link.rel === `${props.participantId}-selects-choice`).href;
            fetch(`${process.env.REACT_APP_BASE_URL}${selectHref}`, {
                method: 'PUT'
            })
            .then(() => setSelectedIndex(index));
        }
    };

    const choices = props.question.choices.map((choice, index) => (
        <div key={`option-${index}`} data-testid={`multiple-choice-option-${index}`} className={`clickable option ${selectedIndex === index ? 'selected' : ''} ${disabled ? 'disabled' : ''}`} onClick={() => selectChoice(choice, index)}>
            {choice.choice}
        </div>
    ));

    return (
        <div>
            { choices }
        </div>
    )
}

export default MultipleChoice;