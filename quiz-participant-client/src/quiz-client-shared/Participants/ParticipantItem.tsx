import React from 'react';
import { Participant } from "../quiz";

interface ParticipantProps {
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
            </div>;

}

export default ParticipantItem;