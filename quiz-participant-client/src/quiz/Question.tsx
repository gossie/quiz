import React from 'react';
import Quiz from "./quiz";

interface QuestionProps {
    quiz: Quiz;
}

const Question: React.FC<QuestionProps> = (props: QuestionProps) => {
    const lastQuestion = props.quiz.questions.length > 0 ? props.quiz.questions[props.quiz.questions.length - 1] : undefined;
    return (
        <div>
            <h5 className="title is-5">Current question</h5>
            { lastQuestion && <span>{lastQuestion}</span> }
        </div>
    )
};

export default Question;