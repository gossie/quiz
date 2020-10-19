import React, { useState } from 'react';
import Quiz, { Question } from '../quiz-client-shared/quiz';
import './Questions.css'
import QuestionElement from './Question/Question';
import QuestionForm from './QuestionForm/QuestionForm';
import QuestionPool from './QuestionPool/QuestionPool';

interface QuestionsProps {
    quiz: Quiz;
}

const Questions: React.FC<QuestionsProps> = (props: QuestionsProps) => {
    const [imageToDisplay, setImageToDisplay] = useState('');
    const [tabIndex, setTabIndex] = useState(0);
    const [questionToEdit, setQuestionToEdit] = useState<Question | undefined>(undefined);

    const onEdit = (question: Question) => {
        setQuestionToEdit(question);
    };
    
    const playedQuestions = props.quiz.playedQuestions
            .map((q, index) => <li key={q.id}><QuestionElement question={q} index={index} setImageToDisplay={setImageToDisplay}></QuestionElement></li>);

    const openQuestions = props.quiz.openQuestions
            .map((q, index) => <li key={q.id}><QuestionElement question={q} index={index} setImageToDisplay={setImageToDisplay} enableOperations={true} onEdit={onEdit}></QuestionElement></li>);
    
    return (
        <div>
            <h4 className="title is-4">Questions</h4>
            <div className="tabs is-boxed">
                <ul>
                    <li className={tabIndex === 0 ? "is-active" : ""}>
                        <a onClick={() => setTabIndex(0)}>
                            <span className="icon is-small"><i className="far fa-lightbulb" aria-hidden="true"></i></span>
                            <span>Create a new question</span>
                        </a>
                    </li>
                    <li className={tabIndex === 1 ? "is-active" : ""}>
                        <a onClick={() => setTabIndex(1)}>
                            <span className="icon is-small"><i className="fas fa-cart-arrow-down" aria-hidden="true"></i></span>
                            <span>Pick from played questions</span>
                        </a>
                    </li>
                </ul>
            </div>
            
            { tabIndex === 0 && <QuestionForm quiz={props.quiz}></QuestionForm> }
            { tabIndex === 1 && <QuestionPool quiz={props.quiz} setImageToDisplay={setImageToDisplay}></QuestionPool> }

            <div className="columns question-columns">
                <div data-testid="open-questions" className="column">
                    <h5 className="title is-5">Open questions</h5>
                    <div className="question-container">
                        <ul>
                            {openQuestions}
                        </ul>
                    </div>
                </div>

                <div data-testid="played-questions" className="column">
                    <h5 className="title is-5">Played questions</h5>
                    <div className="question-container">
                        <ul>
                            {playedQuestions}
                        </ul>
                    </div>
                </div>
            </div>
            { imageToDisplay.length > 0 &&
                <div data-testid="image-dialog" className="modal is-active">
                    <div className="modal-background"></div>
                    <div className="modal-content">
                        <img data-testid="image" src={imageToDisplay} alt="There should be something here" />
                    </div>
                    <button data-testid="close-button" className="modal-close is-large" aria-label="close" onClick={() => setImageToDisplay('')}></button>
                </div>
            }
            { questionToEdit &&
                <div data-testid="edit-dialog" className="modal is-active">
                    <div className="modal-background"></div>
                    <div className="modal-content">
                        <QuestionForm quiz={props.quiz} questionToChange={questionToEdit} onSubmit={() => setQuestionToEdit(undefined)}></QuestionForm>
                    </div>
                    <button data-testid="close-button" className="modal-close is-large" aria-label="close" onClick={() => setQuestionToEdit(undefined)}></button>
                </div>
            }
        </div>  
    )
};

export default Questions;