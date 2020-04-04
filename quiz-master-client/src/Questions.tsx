import React, { useState } from 'react';
import Quiz from './quiz-client-shared/quiz';

interface QuestionsProps {
    quiz: Quiz;
}

const Questions: React.FC<QuestionsProps> = (props: QuestionsProps) => {
    const [newQuestion, setNewQuestion] = useState('');

    const startQuestion = async () => {
        const questioinLink = props.quiz.links.find(link => link.rel === 'createQuestion')?.href;
        await fetch(`${process.env.REACT_APP_BASE_URL}${questioinLink}`, {
            method: 'POST',
            body: newQuestion,
            headers: {
                'Content-Type': 'text/plain',
                Accept: 'application/json'
            }
        });
        setNewQuestion('');
    };

    const elements = props.quiz.questions.map((q, index) => <div key={index}>#{index + 1} {q}</div>);
    return (
        <div>
            <h3>Questions</h3>
            <p>
                <input value={newQuestion} onChange={ev => setNewQuestion(ev.target.value)} />
                <button onClick={startQuestion}>Start question</button>
            </p>
            <div data-testid="questions">
                {elements}
            </div>
        </div>
    )
};

export default Questions;