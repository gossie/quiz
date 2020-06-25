import React, { useState } from 'react';
import Quiz, { Question } from '../../quiz-client-shared/quiz';

import './QuestionForm.css';

interface QuestionFormProps {
    quiz: Quiz;
    questionToChange?: Question;
    onSubmit?: () => void;
}

const QuestionForm: React.FC<QuestionFormProps> = (props: QuestionFormProps) => {
    const [newQuestion, setNewQuestion] = useState(props.questionToChange?.question);
    const [imagePath, setImagePath] = useState(props.questionToChange?.imagePath);
    const [questionButtonCssClasses, setQuestionButtonCssClasses] = useState('button is-link');
    const [visibility, setVisibility] = useState(props.questionToChange ? props.questionToChange.publicVisible : false);
    const [estimation, setEstimation] = useState(props.questionToChange ? (props.questionToChange.estimates !== null && props.questionToChange.estimates !== undefined) : false);
    
    const createQuestion = async () => {
        setQuestionButtonCssClasses('button is-link is-loading');
        let questionLink: string;
        let method: string;
        if (props.questionToChange) {
            questionLink = props.questionToChange.links.find(link => link.rel === 'self')?.href;
            method = 'PUT';
        } else {
            questionLink = props.quiz.links.find(link => link.rel === 'createQuestion')?.href;
            method = 'POST';
        }

        await fetch(`${process.env.REACT_APP_BASE_URL}${questionLink}`, {
            method: method,
            body: JSON.stringify({
                question: newQuestion,
                imagePath: imagePath,
                publicVisible: visibility,
                estimates: estimation ? {} : undefined
            }),
            headers: {
                'Content-Type': 'application/json',
                Accept: 'application/json'
            }
        });
        
        setNewQuestion('');
        setImagePath('');
        setQuestionButtonCssClasses('button is-link');

        props.onSubmit && props.onSubmit();
    };

    return (
        <div>
            <div className="field">
                <div className="control">
                    <input data-testid={props.questionToChange ? 'question-to-edit' : 'new-question'} value={newQuestion} onChange={ev => setNewQuestion(ev.target.value)} className="input" type="text" placeholder="Question" />
                </div>
            </div>
            
            <div className="field">
                <div className="control">
                    <input data-testid={props.questionToChange ? 'image-path-to-edit' : 'image-path'} value={imagePath} onChange={ev => setImagePath(ev.target.value)} className="input" type="text" placeholder="Image path" />
                </div>
            </div>
            <div className="field">
                <div className="control">
                    <label className="checkbox">
                        <input data-testid={props.questionToChange ? 'estimation-to-edit' : 'estimation'} type="checkbox" checked={estimation} onChange={ev => setEstimation(ev.target.checked)} />
                        If checked, participants won't see the buzzer, but a textfield where each participant can submit an answer
                    </label>
                </div>
            </div>
            <div className="field">
                <div className="control">
                    <label className="checkbox">
                        <input data-testid={props.questionToChange ? 'visibility-to-edit' : 'visibility'} type="checkbox" checked={visibility} onChange={ev => setVisibility(ev.target.checked)} />
                        Question can be used by others after it is played
                    </label>
                </div>
            </div>
            <div className="field is-grouped">
                <div className="control">
                    <button data-testid={props.questionToChange ? 'edit-question-button' : 'create-question-button'} onClick={createQuestion} className={questionButtonCssClasses}>
                        {props.questionToChange ? 'Edit question' : 'Add question'}
                    </button>
                </div>
            </div>
        </div>
    )
};

export default QuestionForm;
