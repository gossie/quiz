import React from 'react';
import Quiz from "../quiz-client-shared/quiz";
import './Question.css';
import Image from './Image';
import { useTranslation } from 'react-i18next';

interface QuestionProps {
    quiz: Quiz;
}

const Question: React.FC<QuestionProps> = (props: QuestionProps) => {
    const { t } = useTranslation();

    const pendingQuestion = props.quiz.openQuestions.find(question => question.pending)
    const hasImage = pendingQuestion?.imagePath !== '';

    return (
        <div>
            <div>
            { pendingQuestion &&
                <div>
                    <div data-testid="current-question">{pendingQuestion.question}</div>
                    { pendingQuestion.secondsLeft != null && <div data-testid="question-counter">{t('secondsLeft', { seconds: pendingQuestion.secondsLeft })}</div> }
                    { hasImage && <Image question={pendingQuestion} /> }
                </div>
            }
            </div>
        </div>
    )
}

export default Question;
