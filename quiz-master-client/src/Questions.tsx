import React, { useState } from 'react';
import Quiz from "./quiz";

interface QuestionsProps {
    quiz: Quiz;
}

const Questions: React.FC<QuestionsProps> = (props: QuestionsProps) => {
    const [newQuestion, setNewQuestion] = useState('');
    const [imagePath, setImagePath] = useState('');

    const startQuestion = async () => {
        const questionLink = props.quiz.links.find(link => link.rel === 'createQuestion')?.href;
        await fetch(`${process.env.REACT_APP_BASE_URL}${questionLink}`, {
            method: 'POST',
            body: JSON.stringify({
                question: newQuestion,
                imagePath: imagePath
            }),
            headers: {
                'Content-Type': 'application/json',
                Accept: 'application/json'
            }
        });
        setNewQuestion('');
        setImagePath('');
    };

    const elements = props.quiz.questions.map((q, index) => <div key={index}>#{index + 1} {q.question}</div>);
    return (
        <div>
            <h3>Questions</h3>
            <p>
                <input data-testid="new-question" value={newQuestion} onChange={ev => setNewQuestion(ev.target.value)} />
                <input data-testid="image-path" value={imagePath} onChange={ev => setImagePath(ev.target.value)} />
                <button data-testid="question-button" onClick={startQuestion}>Start question</button>
            </p>
            <div data-testid="questions">
                {elements}
            </div>
        </div>
    )
};

export default Questions;