import React from 'react';
import { Participant, Question } from "../quiz";
import './ParticipantItem.scss';

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

    const getEstimatedValue = () => {
        if (props.question) {
            if (props.question.revealed) {
                return props.question.estimates != null && props.question.estimates[props.participant.id] != null
                ? props.question.estimates[props.participant.id]
                : '';
            } else {
                return props.question.estimates != null && props.question.estimates[props.participant.id] != null
                ? '*****'
                : '';
            }
        }
        return null;
    }

    return <div data-testid="participant-wrapper" className="participant">
                <div className={"participant-header"}>             
                    <span data-testid="participant-name" className="participant-name">{props.participant.name} </span>
                    <div className="points">({props.pointsAfterLastQuestion}{pointDifference()})</div>
                </div>
                <div className={'participant-answer' + (props.participant.turn || getEstimatedValue() ? ' visible': '')}>
                    <div className="bubble">
                        {props.participant.turn ? 'I have buzzered!' : getEstimatedValue()}
                    </div>
                </div>
            </div>;

}

export default ParticipantItem;