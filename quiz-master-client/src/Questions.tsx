import React, { useState } from 'react';
import Quiz from './quiz-client-shared/quiz';

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
            <h4 className="title is-4">Questions</h4>
            <div className="field">
                <div className="control">
                    <input data-testid="new-question" value={newQuestion} onChange={ev => setNewQuestion(ev.target.value)} className="input" type="text" placeholder="Question" />
                </div>
            </div>
            
            <div className="field">
                <div className="control">
                    <input data-testid="image-path" value={imagePath} onChange={ev => setImagePath(ev.target.value)} className="input" type="text" placeholder="Image path" />
                </div>
            </div>
            <div className="field is-grouped">
                <div className="control">
                    <button data-testid="question-button" onClick={startQuestion} className="button is-link">Start question</button>
                </div>
            </div>
                    
            <div data-testid="questions">
                {elements}
            </div>
        </div>
    )
};

export default Questions;