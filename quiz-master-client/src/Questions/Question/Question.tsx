import React from 'react';
import './Question.scss'
import Quiz, { Question } from '../../quiz-client-shared/quiz';

interface QuestionElementProps {
    enableOperations?: boolean;
    question: Question;
    quiz?: Quiz;
    index: number;
    setImageToDisplay: (path: string) => void;
    onEdit?: (question: Question) => void;
}

const QuestionElement: React.FC<QuestionElementProps> = (props: QuestionElementProps) => {
    const toggleQuestion = async (question: Question) => {
        const questionLink = question.links.find(link => link.rel === 'self')?.href;
        await fetch(`${process.env.REACT_APP_BASE_URL}${questionLink}`, {
            method: 'PATCH',
            headers: {
                Accept: 'application/json'
            }
        });
    };

    const deleteQuestion = async (question: Question) => {
        const questionLink = question.links.find(link => link.rel === 'self')?.href;
        await fetch(`${process.env.REACT_APP_BASE_URL}${questionLink}`, {
            method: 'DELETE'
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

        await fetch(`${process.env.REACT_APP_BASE_URL}${reopenHref}`, {
            method: 'PUT',
            headers: {
                Accept: 'application/json'
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
                        { props.question.estimates != null && <span data-testid={`freetext-question-${props.index}`} className="icon" title="Freetext question"><i className="far fa-keyboard"></i></span> }
                        { props.question.estimates == null && <span data-testid={`buzzer-question-${props.index}`} className="icon" title="Buzzer question"><i className="fas fa-hockey-puck"></i></span> }
                        { props.question.timeToAnswer != null && <span data-testid={`stop-watch-${props.index}`} className="icon" title={`${props.question.timeToAnswer} seconds to answer`}><i className="fas fa-hourglass-half"></i></span> }
                        { props.question.imagePath && props.question.imagePath.length > 0 && <span data-testid={`image-icon-${props.index}`} title="Show image" className="icon" onClick={() => props.setImageToDisplay(props.question.imagePath!)}><i className="fas fa-images"></i></span>}
                    </div>
                    <div className="question-actions-column">
                        { props.enableOperations && !props.question.pending && 
                            <span data-testid={`start-question-${props.index}`} className="icon clickable has-text-link" title="Ask question" onClick={() => toggleQuestion(props.question)}><i className="fas fa-play"></i></span>
                        }
                        { props.enableOperations && !props.question.pending && props.onEdit && 
                            <span data-testid={`edit-question-${props.index}`} className="icon clickable has-text-warning" title="Edit question" onClick={() => props.onEdit!(props.question)}><i className="fas fa-pencil-alt"></i></span>
                        }
                        { props.enableOperations && props.question.pending && (isParticipantsTurn() || props.question.estimates != null) &&
                            <span data-testid="reopen-button" className="icon clickable has-text-warning" onClick={() => reopenQuestion()} title='Reopen Question'><i className="fas fa-lock-open"></i></span>
                        }
                        { props.enableOperations && !props.question.pending && 
                            <span data-testid={`delete-question-${props.index}`} className="icon clickable has-text-danger" title="Delete question" onClick={() => deleteQuestion(props.question)}><i className="fas fa-trash"></i></span>
                        }
                    </div>
                </div>
                <div className="has-text-left question-question-column">   
                    <span data-testid="question">{props.question.question}</span>
                </div>
            </div>
        </div>
    )
};

export default QuestionElement;
