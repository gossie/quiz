import React, { useEffect, useState } from 'react';
import Quiz from "./quiz";
import './Question.css';

interface QuestionProps {
    quiz: Quiz;
}

const Question: React.FC<QuestionProps> = (props: QuestionProps) => {
    const [cssClass, setCssClass] = useState('invisible');

    const pendingQuestion = props.quiz.questions.find(question => question.pending)

    useEffect(() => {
        const timer = setTimeout(() => {
            console.log('timer');
            setCssClass('');
        }, 3000);
        return () => clearTimeout(timer);
    });

    return (
        <div>
            <h5 className="title is-5">Current question</h5>
            <div>
            { pendingQuestion &&
                <div>
                    <span data-testid="current-question">{pendingQuestion.question}</span>
                    { pendingQuestion.imagePath !== '' &&
                        <div className="image-wrapper">
                            <img src={pendingQuestion.imagePath} alt="There should be something here" className={cssClass}></img>
                        </div>
                    }
                </div>
            }
            </div>
        </div>
    )
};

export default Question;