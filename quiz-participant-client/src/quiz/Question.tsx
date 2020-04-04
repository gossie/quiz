import React from 'react';
import Quiz from "./quiz";

interface QuestionProps {
    quiz: Quiz;
}

const Question: React.FC<QuestionProps> = (props: QuestionProps) => {
    const pendingQuestion = props.quiz.questions.find(question => question.pending)
    return (
        <div>
            <h5 className="title is-5">Current question</h5>
            { pendingQuestion && <span data-testid="current-question">{pendingQuestion.question}</span> }
        </div>
    )
};

export default Question;