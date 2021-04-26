import React from 'react';
import Quiz from "../quiz-client-shared/quiz";
import './Question.css';
import Buzzer from './buzzer/Buzzer';
import Estimation from './estimation/Estimation';
import MultipleChoice from './multiple-choice/MultipleChoice';
import Image from './Image';
import Countdown from '../quiz-client-shared/Countdown/Countdown';

interface QuestionProps {
    quiz: Quiz;
    participantId: string;
}

const Question: React.FC<QuestionProps> = (props: QuestionProps) => {

    const pendingQuestion = props.quiz.openQuestions.find(question => question.pending)
    const hasImage = pendingQuestion?.imagePath !== '';

    const questionInteraction = () => {
        if (pendingQuestion.choices) {
            return <MultipleChoice question={pendingQuestion} participantId={props.participantId} />
        } else if (pendingQuestion.estimates) {
            return <Estimation quiz={props.quiz} participantId={props.participantId} />
        } else {
            return <Buzzer quiz={props.quiz} participantId={props.participantId} />
        }                 
    };

    return (
        <div>
            <div>
            { pendingQuestion &&
                <div>
                    <div data-testid="current-question" className="current-question">{pendingQuestion.question}</div>
                    { pendingQuestion.secondsLeft != null &&
                        <Countdown question={pendingQuestion}></Countdown>
                    }
                    { questionInteraction() }
                    { hasImage && <Image question={pendingQuestion} /> }
                </div>
            }
            </div>
        </div>
    )
}

export default Question;
