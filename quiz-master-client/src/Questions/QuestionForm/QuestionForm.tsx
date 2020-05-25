import React, { useState } from 'react';
import Quiz from '../../quiz-client-shared/quiz';

interface QuestionFormProps {
    quiz: Quiz;
}

const QuestionForm: React.FC<QuestionFormProps> = (props: QuestionFormProps) => {
    const [newQuestion, setNewQuestion] = useState('');
    const [imagePath, setImagePath] = useState('');
    const [questionButtonCssClasses, setQuestionButtonCssClasses] = useState('button is-link');
    
    const createQuestion = async () => {
        setQuestionButtonCssClasses('button is-link is-loading');
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
        setQuestionButtonCssClasses('button is-link');
    };

    return (
        <div>
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
                    <button data-testid="create-question-button" onClick={createQuestion} className={questionButtonCssClasses}>Add question</button>
                </div>
            </div>
        </div>
    )
};

export default QuestionForm;