import React from 'react';
import Quiz, { Participant } from "../quiz";
import Answers from '../../Answers/Answers';

interface ParticipantProps {
    quiz: Quiz;
    participant: Participant;
    pointsAfterLastQuestion: number;
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
                {props.participant.turn ? <Answers quiz={props.quiz}></Answers> : ''}
            </div>;

}

export default ParticipantItem;