import React, { useEffect, useState, useCallback } from 'react';
import Quiz, { Question } from '../../quiz-client-shared/quiz';
import QuestionElement from '../Question/Question';

import './QuestionPool.css';

interface QuestionPoolProps {
    quiz: Quiz;
    setImageToDisplay: (path: string) => void;
}

const QuestionPool: React.FC<QuestionPoolProps> = (props: QuestionPoolProps) => {
    const [questions, setQuestions] = useState(undefined as any);
    
    const createQuestion = useCallback((question: Question) => {
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
    }, [props.quiz.links]);

    const fetchQuestionPool = useCallback(async () => {
        const response = await fetch(`${process.env.REACT_APP_BASE_URL}/api/questionPool`, {
            method: 'GET',
            headers: {
                Accept: 'application/json'
            }
        });
        const pooledQuestions = await response.json();
        setQuestions(pooledQuestions.map((q: Question, index: number) =>
            <li key={q.id} >
                <QuestionElement question={q} index={index} setImageToDisplay={props.setImageToDisplay}></QuestionElement>
                <span data-testid={`add-question-${index}`} title="Add question" className="icon has-text-primary" onClick={() => createQuestion(q)}><i className="fas fa-save"></i></span>
            </li>
        ));
    }, [props.setImageToDisplay, createQuestion])
    
    useEffect(() => {
        fetchQuestionPool();
    }, [fetchQuestionPool]);

    return (
        <div className="question-list">
            <ul>
                { questions !== undefined && questions }
            </ul>
        </div>
    )
};

export default QuestionPool;
