import React, { useEffect, useState } from 'react';
import { connect } from 'react-redux';
import Quiz, { Question } from '../quiz-client-shared/quiz';
import './Questions.scss'
import QuestionElement from './Question/Question';
import QuestionForm from './QuestionForm/QuestionForm';
import QuestionPool from './QuestionPool/QuestionPool';
import { DragDropContext, Droppable, Draggable } from 'react-beautiful-dnd';
import { useTranslation } from 'react-i18next';
import { showError } from '../redux/actions';
import FinishQuiz from './FinishQuiz/FinishQuiz';

interface StateProps {
    globalErrorMessage: string;
}

interface DispatchProps {
    showError: (errorMessage: string) => void;
}

interface OwnProps {
    quiz: Quiz;
}

type QuestionsProps = StateProps & DispatchProps & OwnProps;

const Questions: React.FC<QuestionsProps> = (props: QuestionsProps) => {
    const [imageToDisplay, setImageToDisplay] = useState('');
    const [sortedOpenQuestions, setSortedOpenQuestions] = useState(props.quiz.openQuestions);
    const [questionToAdd, setQuestionToAdd] = useState(false);
    const [tabIndex, setTabIndex] = useState(0);
    const [questionToEdit, setQuestionToEdit] = useState<Question | undefined>(undefined);

    const { t } = useTranslation();

    const onEdit = (question: Question) => {
        setQuestionToEdit(question);
    };

    useEffect(() => {
        setSortedOpenQuestions(props.quiz.openQuestions);
    }, [props.quiz.openQuestions]);

    const updatePreviousQuestionId = async (question, newPreviewQuestionId) => {
        let questionLink = question.links.find(link => link.rel === 'self')?.href;      
        let newQuestion = Object.assign(question, {previousQuestionId: newPreviewQuestionId})
        fetch(`${process.env.REACT_APP_BASE_URL}${questionLink}`, {
            method: 'PUT',
            body: JSON.stringify(newQuestion),
            headers: {
                'Content-Type': 'application/json',
                Accept: 'application/json'
            }
        })
        .then(response => {
            if (response.status === 409) {
                props.showError(t('errorMessageConflict'));
            }
        });
    };


    const onDragEnd = (result) => {
        if (!result.destination) {
            return;
        }
        let newSortedOpenQuestions: Question[] = [...sortedOpenQuestions];
        const removed: Question[] = newSortedOpenQuestions.splice(result.source.index, 1);
        newSortedOpenQuestions.splice(result.destination.index, 0, ...removed);

        if (result.destination.index === 0) {
            updatePreviousQuestionId(removed[0], null);
        } else {
            updatePreviousQuestionId(removed[0], newSortedOpenQuestions[result.destination.index - 1].id)
        }

        setSortedOpenQuestions(newSortedOpenQuestions);

    };

    const playedQuestions = props.quiz.playedQuestions.map((q, index) => 
        <li key={q.id} className="no-padding"><QuestionElement question={q} quiz={props.quiz} index={index} setImageToDisplay={setImageToDisplay}></QuestionElement></li>);
  
    const openQuestions = sortedOpenQuestions.map((item, index) => (
        <Draggable key={item.id} draggableId={item.id} index={index}>
          {(provided, snapshot) => (
            <li key={item.id}
                data-testid='dragquestion'
                className="no-padding"
                ref={provided.innerRef}
                {...provided.draggableProps}
                {...provided.dragHandleProps}>
             <QuestionElement question={item} quiz={props.quiz} index={props.quiz.playedQuestions.length + index} setImageToDisplay={setImageToDisplay} enableOperations={true} onEdit={onEdit}></QuestionElement>
            </li>
          )}
        </Draggable>
    ));

    return (
        <div className="questions-column">
            <div className="level not-responsive title">
                <h4 className="title is-4 no-margin">{t('headlingQuestions')}</h4>
                <button data-testid="add-question-button" className="button level-right is-primary" onClick={() => setQuestionToAdd(true)}>
                     <i className="fas fa-plus"></i>
                </button>
            </div>
            <div>
                <div data-testid="open-questions" className="block">
                  
                    <DragDropContext onDragEnd={onDragEnd}>
                        <Droppable droppableId="droppable">
                        {(provided, snapshot) => (
                            <ul className="block-list has-radius is-question-list"
                                {...provided.droppableProps}
                                ref={provided.innerRef}>
                                {openQuestions}
                                {provided.placeholder}
                            </ul>
                        )}
                        </Droppable>
                    </DragDropContext>

                </div>
               <FinishQuiz quiz={props.quiz}></FinishQuiz>
                { props.quiz.playedQuestions.length > 0 &&
                    <div data-testid="played-questions" className="block">
                        <hr/>
                        <ul className="block-list has-radius is-question-list">
                            {playedQuestions}
                        </ul>
                    </div>
                }
            </div>
            
            { imageToDisplay.length > 0 &&
                <div data-testid="image-dialog" className="modal is-active">
                    <div className="modal-background"></div>
                    <div className="modal-card">
                        <img data-testid="image" src={imageToDisplay} alt={t('imageAlt')} />
                    </div>
                    <button data-testid="close-button" className="modal-close is-large" aria-label="close" onClick={() => setImageToDisplay('')}></button>
                </div>
            }
            { questionToEdit && !props.globalErrorMessage &&
                <div data-testid="edit-dialog" className="modal is-active">
                    <div className="modal-background"></div>
                    <div className="modal-card fixed-height">
                        <header className="modal-card-head">
                            <p className="modal-card-title">{t('headlineEditQuestion')}</p>
                            <button data-testid="close-button" className="delete" aria-label="close" onClick={() => setQuestionToEdit(undefined)}></button>
                        </header>
                        <section className="modal-card-body">
                            <QuestionForm quiz={props.quiz} questionToChange={questionToEdit} onSubmit={() => setQuestionToEdit(undefined)}></QuestionForm>
                         </section>
                    </div>
                </div>
            }
            { questionToAdd && !props.globalErrorMessage &&
                <div className="add-question-form modal is-active">
                    <div className="modal-background"></div>
                    <div className="modal-card fixed-height">
                        <header className="modal-card-head">
                            <p className="modal-card-title">{t('headlineAddQuestion')}</p>
                            <button data-testid="close-button" className="delete" aria-label="close" onClick={() => setQuestionToAdd(false)}></button>
                        </header>
                        <section className="modal-card-body">
                            <div className="tabs is-centered is-boxed">
                                <ul>
                                    <li className={tabIndex === 0 ? "is-active" : ""}>
                                        <button onClick={() => setTabIndex(0)}>
                                            <span className="icon is-small"><i className="far fa-lightbulb" aria-hidden="true"></i></span>
                                            <span>{t('tabCreateNewQuestion')}</span>
                                        </button>
                                    </li>
                                    <li className={tabIndex === 1 ? "is-active" : ""}>
                                        <button onClick={() => setTabIndex(1)}>
                                            <span className="icon is-small"><i className="fas fa-cart-arrow-down" aria-hidden="true"></i></span>
                                            <span>{t('tabPickExistingQuestion')}</span>
                                        </button>
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

const mapStateToProps = state => {
    return { globalErrorMessage: state.errorMessage };
};

export default connect<StateProps, DispatchProps, OwnProps>(
    mapStateToProps,
    {showError}
)(Questions);