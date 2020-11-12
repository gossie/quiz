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

    const isEstimationQuestion = () => {
        const pendingQuestion = props.quiz.openQuestions.find(q => q.pending);
        return pendingQuestion !== undefined && pendingQuestion.estimates !== null;
    }

    const getEstimatedValue = () => {
        const pendingQuestion = props.quiz.openQuestions.find(q => q.pending);
        return pendingQuestion !== undefined && pendingQuestion.estimates !== undefined
                ? pendingQuestion.estimates[props.participant.id]
                : '';
    }

    const deleteParticipant = () => {
        const deleteUrl = props.participant.links.find(link => link.rel === 'delete').href;
        fetch(`${process.env.REACT_APP_BASE_URL}${deleteUrl}`, {
            method: 'DELETE'
        })
    }

    return <div className="participant-answer">
                <div data-testid="participant-wrapper" className={"participant " + (props.participant.turn ? 'turn' : '')}>
                    <span data-testid="participant-name">{props.participant.name} </span>
                    <div className="points">({props.pointsAfterLastQuestion}{pointDifference()})</div>
                    { !props.participant.revealAllowed && <span data-testid="reveal-not-allowed" className="icon" title="The participant does not want the answer to be shown"><i className="fas fa-eye-slash"></i></span> }
                    <span data-testid="delete" className="icon clickable has-text-danger" title="Delete participant" onClick={() => deleteParticipant()}><i className="fa fa-trash"></i></span>
                    {(props.participant.turn || isEstimationQuestion()) ? <Answers quiz={props.quiz} participant={props.participant}></Answers> : ''}
                </div>
                { isEstimationQuestion() &&
                    <div><b>{props.participant.name}'s answer:</b> {getEstimatedValue()}</div>
                }
            </div>
}

export default ParticipantItem;