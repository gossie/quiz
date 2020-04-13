import React, { useState } from 'react';
import Quiz, { Question } from '../quiz-client-shared/quiz';
import './Questions.css'

interface QuestionsProps {
    quiz: Quiz;
}

const Questions: React.FC<QuestionsProps> = (props: QuestionsProps) => {
    const [newQuestion, setNewQuestion] = useState('');
    const [imagePath, setImagePath] = useState('');

    const createQuestion = async () => {
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

    const startQuestion = async (question: Question) => {
        const questionLink = question.links.find(link => link.rel === 'self')?.href;
        await fetch(`${process.env.REACT_APP_BASE_URL}${questionLink}`, {
            method: 'PUT',
            headers: {
                Accept: 'application/json'
            }
        });
    };

    const playedQuestions = props.quiz.playedQuestions
            .sort((q1, q2) => q2.id - q1.id)
            .map((q, index) => <div key={index}>#{index + 1} {q.question}</div>);

    const openQuestions = props.quiz.openQuestions
            .sort((q1, q2) => q1.id - q2.id)
            .map((q, index) => <div key={index}>#{index + 1} {q.question}{!q.pending && <span data-testid={`start-question-${index}`} className="icon has-text-primary" onClick={() => startQuestion(q)}><i className="fas fa-share-square"></i></span>}</div>);
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
                    <button data-testid="create-question-button" onClick={createQuestion} className="button is-link">Add question</button>
                </div>
            </div>

            <div className="columns">
                <div data-testid="open-questions" className="column">
                    <h5 className="title is-5">Open questions</h5>
                    {openQuestions}
                </div>

                <div data-testid="played-questions" className="column">
                    <h5 className="title is-5">Played questions</h5>
                    {playedQuestions}
                </div>
            </div>
        </div>
    )
};

export default Questions;