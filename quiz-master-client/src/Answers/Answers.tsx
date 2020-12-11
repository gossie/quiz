import React from 'react';
import { useTranslation } from 'react-i18next';
import Quiz, { Participant } from '../quiz-client-shared/quiz';
import './Answers.css'

interface AnswersProps {
    quiz: Quiz;
    participant: Participant;
}

const Answers: React.FC<AnswersProps> = (props: AnswersProps) => {
    const { t } = useTranslation();

    const answer = async (correct: string) => {
        await fetch(`${process.env.REACT_APP_BASE_URL}/api/quiz/${props.quiz.id}/participants/${props.participant.id}/answers`, {
            method: 'POST',
            body: correct,
            headers: {
                'Content-Type': 'text/plain',
                Accept: 'application/json'
            }
        });
    };


    return (
        <div className="answer-icons">
            <span data-testid="correct-button" className="icon clickable has-text-link" onClick={() => answer('true')} title={t('titleAnswerCorrect')}> 
                <i className="fas fa-check-square"></i>
            </span>
            <span data-testid="incorrect-button" className="icon clickable has-text-danger" onClick={() => answer('false')} title={t('titleAnswerIncorrect')}> 
                <i className="fas fa-times-circle"></i>
            </span>      
        </div>
    )
};

export default Answers;