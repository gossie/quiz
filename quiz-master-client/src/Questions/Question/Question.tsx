import React from 'react';
import './Question.css'
import { Question } from '../../quiz-client-shared/quiz';

interface QuestionElementProps {
    enableOperations?: boolean;
    question: Question;
    index: number;
    setImageToDisplay: (path: string) => void;
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
            #{props.index + 1} {props.question.question}
            { props.enableOperations && !props.question.pending && <span data-testid={`start-question-${props.index}`} className="icon has-text-primary" onClick={() => toggleQuestion(props.question)}><i className="fas fa-share-square"></i></span>}
            { props.enableOperations && props.question.pending && <span data-testid={`revert-question-${props.index}`} className="icon has-text-primary" onClick={() => toggleQuestion(props.question)}><i className="fas fa-undo"></i></span> }
            { props.question.imagePath && props.question.imagePath.length > 0 && <span data-testid={`image-icon-${props.index}`} title="Show image" className="icon" onClick={() => props.setImageToDisplay(props.question.imagePath!)}><i className="fas fa-images"></i></span>}
            { props.enableOperations && !props.question.pending && <span data-testid={`delete-question-${props.index}`} className="icon has-text-danger" onClick={() => deleteQuestion(props.question)}><i className="fas fa-trash"></i></span>}
        </span>
    )
};

export default QuestionElement;