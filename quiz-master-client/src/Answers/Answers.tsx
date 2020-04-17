import React from 'react';
import Quiz from '../quiz-client-shared/quiz';
import './Answers.css'

interface AnswersProps {
    quiz: Quiz;
}

const Answers: React.FC<AnswersProps> = (props: AnswersProps) => {
    const answer = async (correct: string) => {
        const answerHref = props.quiz
                .links
                .find(link => link.rel === 'answer')
                ?.href;

        await fetch(`${process.env.REACT_APP_BASE_URL}${answerHref}`, {
            method: 'PATCH',
            body: correct,
            headers: {
                'Content-Type': 'text/plain',
                Accept: 'application/json'
            }
        });
    };

    const reopenQuestion = async () => {
        const reopenHref = props.quiz
                .links
                .find(link => link.rel === 'reopenQuestion')
                ?.href;

        await fetch(`${process.env.REACT_APP_BASE_URL}${reopenHref}`, {
            method: 'PUT',
            headers: {
                Accept: 'application/json'
            }
        });
    };

    return (
        <div>
            { props.quiz.participants.some(p => p.turn) &&
                <div>
                    <div className="field is-grouped is-grouped-centered">
                        <span data-testid="correct-button" className="answers-icon has-text-success" onClick={() => answer('true')} title='Correct Answer'> 
                            <i className="fas fa-check-square"></i>
                        </span>
                    
        
                        <span data-testid="incorrect-button" className="answers-icon has-text-danger" onClick={() => answer('false')} title='Incorrect Answer'> 
                            <i className="fas fa-times-circle"></i>
                        </span>
                        <span className="spacer">|</span>
                        <span data-testid="reopen-button" className="answers-icon has-text-warning" onClick={() => reopenQuestion()} title='Reopen Question'> 
                            <i className="fas fa-lock-open"></i>
                        </span>
                    </div>              
                </div>
            }
        </div>
    )
};

export default Answers;