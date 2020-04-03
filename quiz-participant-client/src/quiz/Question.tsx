import React from 'react';
import Quiz from "./quiz";

interface QuestionProps {
    quiz: Quiz;
}

const Question: React.FC<QuestionProps> = (props: QuestionProps) => {
    const pendingQuestion = props.quiz.questions.find(question => question.pending)
    return (
        <div>
            <h3>Current question</h3>
            { pendingQuestion && <span data-testid="current-question">{pendingQuestion.question}</span> }
        </div>
    )
};

export default Question;