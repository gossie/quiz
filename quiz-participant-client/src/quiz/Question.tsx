import React, { useEffect, useState } from 'react';
import Quiz from "../quiz-client-shared/quiz";
import './Question.css';

interface QuestionProps {
    quiz: Quiz;
}

const Question: React.FC<QuestionProps> = (props: QuestionProps) => {
    const [cssClass, setCssClass] = useState('question-image invisible');
    const [time, setTime] = useState(3);

    const pendingQuestion = props.quiz.questions.find(question => question.pending)
    const hasImage = pendingQuestion?.imagePath !== '';

    useEffect(() => {
        if (hasImage && cssClass.includes('invisible')) {
            const timer = setTimeout(() => {
                setTime(oldTime => oldTime - 1);
                if (time === 0) {
                    setCssClass('question-image');
                    setTime(3);
                }
            }, 1000);
            return () => clearTimeout(timer);
        }
    });

    return (
        <div>
            <h5 className="title is-5">Current question</h5>
            <div>
            { pendingQuestion &&
                <div>
                    <span data-testid="current-question">{pendingQuestion.question}</span>
                    { hasImage &&
                        <div className="image-wrapper">
                            { cssClass.includes('invisible') && <span>{time}</span> }
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