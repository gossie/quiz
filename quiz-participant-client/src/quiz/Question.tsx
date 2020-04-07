import React from 'react';
import Quiz from "../quiz-client-shared/quiz";
import './Question.css';
import Image from './Image';

interface QuestionProps {
    quiz: Quiz;
}

const Question: React.FC<QuestionProps> = (props: QuestionProps) => {
    const pendingQuestion = props.quiz.openQuestions.find(question => question.pending)
    const hasImage = pendingQuestion?.imagePath !== '';

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