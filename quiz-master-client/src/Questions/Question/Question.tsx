import React from 'react';
import './Question.css'
import { Question } from '../../quiz-client-shared/quiz';

interface QuestionElementProps {
    enableOperations?: boolean;
    question: Question;
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

    return (
        <span>
            <div>
                <span data-testid="question">#{props.index + 1} {props.question.question}</span>
                { props.question.estimates != null && <span data-testid={`freetext-question-${props.index}`} className="icon" title="Freetext question"><i className="far fa-keyboard"></i></span> }
                { props.question.estimates == null && <span data-testid={`buzzer-question-${props.index}`} className="icon has-text-danger" title="Buzzer question"><i className="fas fa-circle"></i></span> }
                { props.question.timeToAnswer != null && <span data-testid={`stop-watch-${props.index}`} className="icon" title={`${props.question.timeToAnswer} seconds to answer`}><i className="fas fa-hourglass-half"></i></span> }
            </div>
            <div>
                { props.enableOperations && !props.question.pending && <span data-testid={`start-question-${props.index}`} className="icon clickable has-text-primary" title="Ask question" onClick={() => toggleQuestion(props.question)}><i className="fas fa-share-square"></i></span>}
                { props.enableOperations && !props.question.pending && props.onEdit && <span data-testid={`edit-question-${props.index}`} className="icon clickable has-text-link" title="Edit question" onClick={() => props.onEdit!(props.question)}><i className="fas fa-edit"></i></span>}
                { props.enableOperations && props.question.pending && <span data-testid={`revert-question-${props.index}`} className="icon clickable has-text-primary"  onClick={() => toggleQuestion(props.question)}><i className="fas fa-undo"></i></span> }
                { props.question.imagePath && props.question.imagePath.length > 0 && <span data-testid={`image-icon-${props.index}`} title="Show image" className="icon" onClick={() => props.setImageToDisplay(props.question.imagePath!)}><i className="fas fa-images"></i></span>}
                { props.enableOperations && !props.question.pending && <span data-testid={`delete-question-${props.index}`} className="icon clickable has-text-danger" title="Delete question" onClick={() => deleteQuestion(props.question)}><i className="fas fa-trash"></i></span>}
            </div>
        </span>
    )
};

export default QuestionElement;
