import React from 'react';
import Quiz, { Participant } from '../quiz-client-shared/quiz';
import './Answers.css'

interface AnswersProps {
    quiz: Quiz;
    participant: Participant;
}

const Answers: React.FC<AnswersProps> = (props: AnswersProps) => {
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
            <div className="field is-grouped is-grouped-centered">
                <span data-testid="correct-button" className="answers-icon has-text-success" onClick={() => answer('true')} title='Correct Answer'> 
                    <i className="fas fa-check-square"></i>
                </span>
            

                <span data-testid="incorrect-button" className="answers-icon has-text-danger" onClick={() => answer('false')} title='Incorrect Answer'> 
                    <i className="fas fa-times-circle"></i>
                </span>
            </div>        
        </div>
    )
};

export default Answers;