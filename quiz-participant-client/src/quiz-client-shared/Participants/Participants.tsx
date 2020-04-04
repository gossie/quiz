import React from 'react';
import Quiz, { Participant } from "../quiz";
import FlipMove from "react-flip-move"

import './Participants.css';

interface ParticipantsProps {
    quiz: Quiz;
}

const Participants: React.FC<ParticipantsProps> = (props: ParticipantsProps) => {
    
    const comparePoints = (a: Participant, b: Participant) => {
        return b.points - a.points;
    }

    const elements = props.quiz.participants?.sort(comparePoints).map(p => <span key={p.name} className={"participant " + (p.turn ? 'turn' : '')}>{p.name} ({p.points})</span>)

    return (
        <div>
            <h5 className="title is-5">Participants</h5>
            <p data-testid="participants" className="participants-list">
            <FlipMove>
                 {elements}
            </FlipMove>     
            </p>
        </div>
    )
};

export default Participants;