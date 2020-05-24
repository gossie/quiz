import React, { useEffect, useState } from 'react';
import Quiz, { Question } from '../../quiz-client-shared/quiz';
import QuestionElement from '../Question/Question';

interface QuestionPoolProps {
    quiz: Quiz;
    setImageToDisplay: (path: string) => void;
}

const QuestionPool: React.FC<QuestionPoolProps> = (props: QuestionPoolProps) => {
    const [questions, setQuestions] = useState(undefined as any);
    
    useEffect(() => {
        fetch(`${process.env.REACT_APP_BASE_URL}/api/questionPool`, {
            method: 'GET',
            headers: {
                Accept: 'application/json'
            }
        })
        .then(response => response.json())
        .then((pooledQuestions: Array<Question>) => {
            setQuestions(pooledQuestions.map((q, index) => <QuestionElement key={q.id} question={q} index={index} setImageToDisplay={props.setImageToDisplay}></QuestionElement>));
        })
    })
    

    return (
        <div>
            <ul>
                { questions !== undefined && questions }
            </ul>
        </div>
    )
};

export default QuestionPool;
