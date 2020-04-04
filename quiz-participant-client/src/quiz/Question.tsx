import React, { useEffect, useState } from 'react';
import Quiz from "../quiz-client-shared/quiz";
import './Question.css';
import Image from './Image';

interface QuestionProps {
    quiz: Quiz;
}

const Question: React.FC<QuestionProps> = (props: QuestionProps) => {
    const [imageCssClass, setImageCssClass] = useState('question-image invisible');
    const [timerCssClass, setTimerCssClass] = useState('');
    const [time, setTime] = useState(3);

    const pendingQuestion = props.quiz.questions.find(question => question.pending)
    const hasImage = pendingQuestion?.imagePath !== '';

    useEffect(() => {
        console.debug('hasName', hasImage);
        if (hasImage) {
            const timer = setTimeout(() => {
                console.debug('counting down');
                setTime(oldTime => oldTime - 1);
                if (time === 0) {
                    console.debug('render image');
                    setImageCssClass('question-image');
                    setTimerCssClass('invisible');
                    setTime(3);
                }
            }, 1000);
            return () => clearTimeout(timer);
        }
    }, [hasImage, time]);

    return (
        <div>
            <h5 className="title is-5">Current question</h5>
            <div>
            { pendingQuestion &&
                <div>
                    <span data-testid="current-question">{pendingQuestion.question}</span>
                    { hasImage && <Image question={pendingQuestion} /> }
                </div>
            }
            </div>
        </div>
    )
};

export default Question;