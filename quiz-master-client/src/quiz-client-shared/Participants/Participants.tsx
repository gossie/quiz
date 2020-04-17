import React from 'react';
import Quiz, { Participant } from "../quiz";
import FlipMove from "react-flip-move"

import './Participants.css';
import Answers from '../../Answers/Answers';

interface ParticipantsProps {
    quiz: Quiz;
}

const Participants: React.FC<ParticipantsProps> = (props: ParticipantsProps) => {
    
    const comparePoints = (a: Participant, b: Participant) => {
        return b.points - a.points;
    }

    const elements = props.quiz.participants?.sort(comparePoints).map(p => 
        <div key={p.name} className={"participant " + (p.turn ? 'turn' : '')}>
            <div className="participant-name">{p.name} ({p.points})</div>
            {p.turn ? <Answers quiz={props.quiz}></Answers> : ''}
        </div>)

    return (
        <div>
            <h5 className="title is-5">Participants</h5>
            <div data-testid="participants" className="participants-list">
                <FlipMove>
                    {elements}
                </FlipMove>     
            </div>
        </div>
    )
};

export default Participants;