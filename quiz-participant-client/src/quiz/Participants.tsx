import React, { useState } from 'react';
import Quiz from "./quiz";
import '@gossie/array-pipe'

import './Participants.css';

interface ParticipantsProps {
    quiz: Quiz;
}

const Participants: React.FC<ParticipantsProps> = (props: ParticipantsProps) => {
    const [name, setName] = useState('');

    const participate = () => {
        console.log('in participate');
        const href = props.quiz.links.find(link => link.rel === 'createParticipant')!.href;

        fetch(`http://localhost:8080${href}`, {
            method: 'POST',
            body: name,
            headers: {
                'Content-Type': 'text/plain',
                Accept: 'application/json'
            }
        })
        .then(() => setName(''))
        .catch(e => console.error(e));
    };

    const elements = props.quiz.participants?.map(p => <span key={p} className={"participant " + (p === props.quiz.turn ? 'turn' : '')}>{p}</span>)

    return (
        <div>
            <div>
                <span>Ich möchte mitmachen und heiße</span>
                <input data-testid="participant-name" type="text" value={name} onChange={ev => setName(ev.target.value)}/>
                <button data-testid="add-button" onClick={participate}>Los!</button>
            </div>
            <h3>Teilnehmer</h3>
            <p data-testid="participants">
                {elements}
            </p>
        </div>
    )
};

export default Participants;