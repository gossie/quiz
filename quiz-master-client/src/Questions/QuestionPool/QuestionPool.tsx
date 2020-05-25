import React, { useEffect, useState } from 'react';
import Quiz, { Question } from '../../quiz-client-shared/quiz';
import QuestionElement from '../Question/Question';

import './QuestionPool.css';

interface QuestionPoolProps {
    quiz: Quiz;
    setImageToDisplay: (path: string) => void;
}

const QuestionPool: React.FC<QuestionPoolProps> = (props: QuestionPoolProps) => {
    const [questions, setQuestions] = useState(undefined as any);
    
    const createQuestion = (question: Question) => {
        const questionLink = props.quiz.links.find(link => link.rel === 'createQuestion')?.href;
        fetch(`${process.env.REACT_APP_BASE_URL}${questionLink}`, {
            method: 'POST',
            body: JSON.stringify({
                question: question.question,
                imagePath: question.imagePath
            }),
            headers: {
                'Content-Type': 'application/json',
                Accept: 'application/json'
            }
        });
    };
    
    useEffect(() => {
        fetch(`${process.env.REACT_APP_BASE_URL}/api/questionPool`, {
            method: 'GET',
            headers: {
                Accept: 'application/json'
            }
        })
        .then(response => response.json())
        .then((pooledQuestions: Array<Question>) => {
            setQuestions(pooledQuestions.map((q, index) => 
                <li>
                    <QuestionElement key={q.id} question={q} index={index} setImageToDisplay={props.setImageToDisplay}></QuestionElement>
                    <span data-testid={'create-question'} title="Add question" className="icon has-text-primary" onClick={() => createQuestion(q)}><i className="fas fa-save"></i></span>
                </li>
            ));
        })
    });

    return (
        <div className="question-list">
            <ul>
                { questions !== undefined && questions }
            </ul>
        </div>
    )
};

export default QuestionPool;
