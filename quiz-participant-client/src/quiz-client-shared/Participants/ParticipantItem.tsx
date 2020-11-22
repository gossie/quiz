import React from 'react';
import { useTranslation } from 'react-i18next';
import { Participant, Question } from "../quiz";
import './ParticipantItem.scss';

interface ParticipantProps {
    participant: Participant;
    pointsAfterLastQuestion: number;
    question?: Question;
}

const ParticipantItem: React.FC<ParticipantProps> = (props: ParticipantProps) => {

    const { t } = useTranslation();

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
                <div data-testid="participant-answer" className={'participant-answer' + (props.participant.turn || getEstimatedValue() ? ' visible': '')}>
                    <div className="bubble">
                        {props.participant.turn ? t('buzzerHint') : getEstimatedValue()}
                    </div>
                </div>
            </div>;

}

export default ParticipantItem;