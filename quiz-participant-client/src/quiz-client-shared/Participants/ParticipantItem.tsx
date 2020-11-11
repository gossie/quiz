import React from 'react';
import { Participant, Question } from "../quiz";

interface ParticipantProps {
    participant: Participant;
    pointsAfterLastQuestion: number;
    question?: Question;
}

const ParticipantItem: React.FC<ParticipantProps> = (props: ParticipantProps) => {

    const pointDifference = () => {
        if (props.pointsAfterLastQuestion !== props.participant.points) {
            return (props.participant.points - props.pointsAfterLastQuestion > 0 ? ' +': ' ') + (props.participant.points - props.pointsAfterLastQuestion);
        }
    }

    return <div data-testid="participant-wrapper" className={"participant " + (props.participant.turn ? 'turn' : '')}>
                <span data-testid="participant-name">{props.participant.name} </span>
                <div className="points">({props.pointsAfterLastQuestion}{pointDifference()})</div>
                { props.question && !props.question.revealed && props.question.estimates && props.question.estimates[props.participant.id] && <span data-testid="answer-hint" className="icon"><i className="far fa-comment"></i></span> }
                { props.question && props.question.revealed && props.question.estimates && props.question.estimates[props.participant.id] && <span>{props.question.estimates[props.participant.id]}</span> }
            </div>;

}

export default ParticipantItem;