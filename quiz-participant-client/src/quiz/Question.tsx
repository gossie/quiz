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
            <div>
            { pendingQuestion &&
                <div>
                    <div data-testid="current-question">{pendingQuestion.question}</div>
                    { pendingQuestion.secondsLeft && <div data-testid="question-counter">{pendingQuestion.secondsLeft} seconds left</div> }
                    { hasImage && <Image question={pendingQuestion} /> }
                </div>
            }
            </div>
        </div>
    )
}

export default Question;
