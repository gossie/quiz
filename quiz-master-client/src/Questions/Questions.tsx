import React, { useState } from 'react';
import Quiz, { Question } from '../quiz-client-shared/quiz';
import './Questions.scss'
import QuestionElement from './Question/Question';
import QuestionForm from './QuestionForm/QuestionForm';
import QuestionPool from './QuestionPool/QuestionPool';

interface QuestionsProps {
    quiz: Quiz;
}

const Questions: React.FC<QuestionsProps> = (props: QuestionsProps) => {
    const [imageToDisplay, setImageToDisplay] = useState('');
    const [questionToAdd, setQuestionToAdd] = useState(false);
    const [tabIndex, setTabIndex] = useState(0);
    const [questionToEdit, setQuestionToEdit] = useState<Question | undefined>(undefined);

    const onEdit = (question: Question) => {
        setQuestionToEdit(question);
    };
    
    const playedQuestions = props.quiz.playedQuestions
            .map((q, index) => <li key={q.id} className="no-padding"><QuestionElement question={q} index={index} setImageToDisplay={setImageToDisplay}></QuestionElement></li>);

    const openQuestions = props.quiz.openQuestions
            .map((q, index) => <li key={q.id} className="no-padding"><QuestionElement question={q} index={index} setImageToDisplay={setImageToDisplay} enableOperations={true} onEdit={onEdit}></QuestionElement></li>);
    
    return (
        <div className="questions-column">
            <div className="level title">
                <h5 className="title is-5 no-margin">Questions</h5>
                <button className="button level-right is-primary" onClick={() => setQuestionToAdd(true)}>
                     <i className="fas fa-plus"></i>
                </button>
            </div>
            <div>
                <div data-testid="open-questions" className="block">
                    <ul className="block-list has-radius">
                        {openQuestions}
                    </ul>
                </div>

                
                <div data-testid="played-questions" className="block">
                    <h5 className="title is-5">Played questions</h5>
                    <ul className="block-list has-radius">
                        {playedQuestions}
                    </ul>
                </div>
            </div>
            
            { imageToDisplay.length > 0 &&
                <div data-testid="image-dialog" className="modal is-active">
                    <div className="modal-background"></div>
                    <div className="modal-card">
                        <img data-testid="image" src={imageToDisplay} alt="There should be something here" />
                    </div>
                    <button data-testid="close-button" className="modal-close is-large" aria-label="close" onClick={() => setImageToDisplay('')}></button>
                </div>
            }
            { questionToEdit &&
                <div data-testid="edit-dialog" className="modal is-active">
                    <div className="modal-background"></div>
                    <div className="modal-card fixed-height">
                        <header className="modal-card-head">
                            <p className="modal-card-title">Edit Question</p>
                            <button data-testid="close-button" className="delete" aria-label="close" onClick={() => setQuestionToEdit(undefined)}></button>
                        </header>
                        <section className="modal-card-body">
                            <QuestionForm quiz={props.quiz} questionToChange={questionToEdit} onSubmit={() => setQuestionToEdit(undefined)}></QuestionForm>
                         </section>
                    </div>
                </div>
            }
            { questionToAdd &&
                <div className="add-question-form modal is-active">
                    <div className="modal-background"></div>
                    <div className="modal-card fixed-height">
                        <header className="modal-card-head">
                            <p className="modal-card-title">Add Question</p>
                            <button data-testid="close-button" className="delete" aria-label="close" onClick={() => setQuestionToAdd(false)}></button>
                        </header>
                        <section className="modal-card-body">
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
                        </section>
                    </div>
                </div>
            }
        </div>  
    )
};

export default Questions;