import React from 'react';
import { connect } from 'react-redux';
import './Question.scss'
import Quiz, { Question } from '../../quiz-client-shared/quiz';
import { useTranslation } from 'react-i18next';
import { showError } from '../../redux/actions';

interface StateProps {}

interface DispatchProps {
    showError: (errorMessage: string) => void;
}

interface OwnProps {
    enableOperations?: boolean;
    question: Question;
    quiz?: Quiz;
    index: number;
    setImageToDisplay: (path: string) => void;
    onEdit?: (question: Question) => void;
}

type QuestionElementProps = StateProps & DispatchProps & OwnProps;

const QuestionElement: React.FC<QuestionElementProps> = (props: QuestionElementProps) => {
    const { t } = useTranslation();

    const toggleQuestion = (question: Question) => {
        const questionLink = question.links.find(link => link.rel === 'self')?.href;
        fetch(`${process.env.REACT_APP_BASE_URL}${questionLink}`, {
            method: 'PATCH'
        })
        .then(response => {
            if (response.status === 409) {
                response.json().then(json => props.showError(t(json.message)));
            }
        });
    };

    const deleteQuestion = async (question: Question) => {
        const questionLink = question.links.find(link => link.rel === 'self')?.href;
        fetch(`${process.env.REACT_APP_BASE_URL}${questionLink}`, {
            method: 'DELETE'
        })
        .then(response => {
            if (response.status === 409) {
                response.json().then(json => props.showError(t(json.message)));
            }
        });
    };

    const isParticipantsTurn = () => {
        return props.quiz && props.quiz.participants.some(p => p.turn);
    }

    const reopenQuestion = async () => {
        const reopenHref = props.quiz
                .links
                .find(link => link.rel === 'reopenQuestion')
                ?.href;

        fetch(`${process.env.REACT_APP_BASE_URL}${reopenHref}`, {
            method: 'PUT',
            headers: {
                Accept: 'application/json'
            }
        })
        .then(response => {
            if (response.status === 409) {
                response.json().then(json => props.showError(t(json.message)));
            }
        });
    };

    return (
        <div className="quiz-master-question">
            <div data-testid="index" className={'question-index-column' + (props.question.pending ? ' is-pending-question' : '')}>
                <span>{props.index + 1}</span>    
            </div>
            <div className="question-main-column">
                <div className="question-top-row">   
                    <div className={'question-type-column has-text-weight-semibold' + (props.question.pending ? ' is-pending-question' : '')}>
                        { props.question.estimates != null && props.question.choices == null && <span data-testid={`freetext-question-${props.index}`} className="icon" title={t('titleFreetextQuestion')}><i className="far fa-keyboard"></i></span> }
                        { props.question.estimates == null && <span data-testid={`buzzer-question-${props.index}`} className="icon" title={t('titleBuzzerQuestion')}><i className="fas fa-hockey-puck"></i></span> }
                        { props.question.choices != null && <span data-testid={`multiple-choice-question-${props.index}`} className="icon" title={t('titleMultipleChoiceQuestion')}><i className="fas fa-list"></i></span> }
                        { props.question.timeToAnswer != null && <span data-testid={`stop-watch-${props.index}`} className="icon" title={t('titleSecondsToAnswer', { seconds: props.question.timeToAnswer })}><i className="fas fa-hourglass-half"></i></span> }
                        { props.question.imagePath?.length > 0 && <span data-testid={`image-icon-${props.index}`} title={t('titleShowImage')} className="icon clickable" onClick={() => props.setImageToDisplay(props.question.imagePath!)}><i className="fas fa-images"></i></span>}
                        { props.question.correctAnswer?.length > 0 && <span data-testid={`answer-hint-${props.index}`} title={t('titleCorrectAnswer', { answer: props.question.correctAnswer })} className="icon"><i className="far fa-comment-dots"></i></span>}
                    </div>
                    <div className="question-actions-column">
                        { props.enableOperations && !props.question.pending && 
                            <span data-testid={`start-question-${props.index}`} className="icon clickable has-text-link" title={t('titleAskQuestion')} onClick={() => toggleQuestion(props.question)}><i className="fas fa-play"></i></span>
                        }
                        { props.enableOperations && !props.question.pending && props.onEdit && 
                            <span data-testid={`edit-question-${props.index}`} className="icon clickable has-text-warning" title={t('titleEditQuestion')} onClick={() => props.onEdit!(props.question)}><i className="fas fa-pencil-alt"></i></span>
                        }
                        { props.enableOperations && props.question.pending && (isParticipantsTurn() || props.question.estimates != null) &&
                            <span data-testid="reopen-button" className="icon clickable has-text-warning" onClick={() => reopenQuestion()} title={t('titleReopenQuestion')}><i className="fas fa-lock-open"></i></span>
                        }
                        { props.enableOperations && !props.question.pending && 
                            <span data-testid={`delete-question-${props.index}`} className="icon clickable has-text-danger" title={t('titleDeleteQuestion')} onClick={() => deleteQuestion(props.question)}><i className="fas fa-trash"></i></span>
                        }
                    </div>
                </div>
                <div className="has-text-left question-question-column">   
                    <span data-testid="question">{props.question.question}</span>
                    { props.question.pending && props.question.choices &&
                        <div className="question-choices">
                            { props.question.choices.map(choice => <div>{choice.choice}</div>) }
                        </div>
                    }
                </div>
            </div>
        </div>
    )
};

export default connect<StateProps, DispatchProps, OwnProps>(
    null,
    {showError}
)(QuestionElement);
