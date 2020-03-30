import React from 'react';
import Quiz from "./quiz";

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

    return (
        <div>
            { props.quiz.participants.some(p => p.turn) &&
                <div>
                    <button onClick={() => answer('true')}>Correct</button>
                    <button onClick={() => answer('false')}>Incorrect</button>
                </div>
            }
        </div>
    )
};

export default Answers;