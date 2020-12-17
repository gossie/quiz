import React, { useEffect, useState, useCallback } from 'react';
import Quiz, { Question } from '../../quiz-client-shared/quiz';
import QuestionElement from '../Question/Question';

import './QuestionPool.css';

interface QuestionPoolProps {
    quiz: Quiz;
    setImageToDisplay: (path: string) => void;
}

const QuestionPool: React.FC<QuestionPoolProps> = (props: QuestionPoolProps) => {
    const [category, setCategory] = useState('other');
    const [questions, setQuestions] = useState(undefined as any);
    
    const createQuestion = useCallback((question: Question) => {
        const questionLink = props.quiz.links.find(link => link.rel === 'createQuestion')?.href;
        fetch(`${process.env.REACT_APP_BASE_URL}${questionLink}`, {
            method: 'POST',
            body: JSON.stringify({
                question: question.question,
                correctAnswer: question.correctAnswer,
                category: question.category,
                timeToAnswer: question.timeToAnswer,
                imagePath: question.imagePath,
                publicVisible: question.publicVisible,
                estimates: question.estimates,
                choices: question.choices
            }),
            headers: {
                'Content-Type': 'application/json',
                Accept: 'application/json'
            }
        });
    }, [props.quiz.links]);
    
    useEffect(() => {
        fetch(`${process.env.REACT_APP_BASE_URL}/api/questionPool?category=${category}`, {
            method: 'GET',
            headers: {
                Accept: 'application/json'
            }
        })
        .then(response => response.json())
        .then(pooledQuestions => {
            setQuestions(pooledQuestions.map((q: Question, index: number) =>
                <li key={q.id} >
                    <QuestionElement question={q} index={index} setImageToDisplay={props.setImageToDisplay}></QuestionElement>
                    <span data-testid={`add-question-${index}`} title="Add question" className="icon has-text-primary clickable" onClick={() => createQuestion(q)}><i className="fas fa-save"></i></span>
                </li>
            ));
        });
    }, [category, createQuestion, props.setImageToDisplay]);

    return (
        <div className="question-list">
            <div id="categories">
                <span onClick={() => setCategory('other')} className="category">Other</span>
                <span onClick={() => setCategory('history')} className="category">History</span>
                <span onClick={() => setCategory('science')} className="category">Science</span>
                <span onClick={() => setCategory('politics')} className="category">Politics</span>
                <span onClick={() => setCategory('geography')} className="category">Geography</span>
                <span onClick={() => setCategory('literature')} className="category">Literature</span>
                <span onClick={() => setCategory('music')} className="category">Music</span>
                <span onClick={() => setCategory('movies')} className="category">Movies / TV</span>
                <span onClick={() => setCategory('sport')} className="category">Sport</span>
            </div>
            <ul>
                { questions !== undefined && questions }
            </ul>
        </div>
    )
};

export default QuestionPool;
