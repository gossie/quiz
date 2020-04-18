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
            .map((q, index) => <div key={index}>#{index + 1} {q.question}</div>);

    const openQuestions = props.quiz.openQuestions
            .map((q, index) =>
                    <div key={index}>
                        #{index + 1} {q.question}
                        { q.imagePath && q.imagePath.length > 0 && <span className="icon"><i className="fas fa-images"></i></span>}
                        {!q.pending && <span data-testid={`start-question-${index}`} className="icon has-text-primary" onClick={() => startQuestion(q)}><i className="fas fa-share-square"></i></span>}
                    </div>);
    
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
                    <div className="question-container">
                        {openQuestions}
                    </div>
                </div>

                <div data-testid="played-questions" className="column">
                    <h5 className="title is-5">Played questions</h5>
                    <div className="question-container">
                        {playedQuestions}
                    </div>
                </div>
            </div>
        </div>
    )
};

export default Questions;