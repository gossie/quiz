import React from 'react';
import Quiz from '../quiz-client-shared/quiz';

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
                        <div className="control">
                            <button data-testid="correct-button" onClick={() => answer('true')} className="button is-primary">Correct</button>
                        </div>
                        <div className="control">
                            <button data-testid="incorrect-button" onClick={() => answer('false')} className="button is-link is-light">Incorrect</button>
                        </div>
                    </div>
                    <div className="field is-grouped is-grouped-centered">
                        <div className="control">
                            <button data-testid="reopen-button" onClick={() => reopenQuestion()} className="button is-link">Reopen question</button>
                        </div>
                    </div>
                </div>
            }
        </div>
    )
};

export default Answers;