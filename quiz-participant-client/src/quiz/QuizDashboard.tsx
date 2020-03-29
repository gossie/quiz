import React, { useState, useEffect } from 'react';

import './QuizDashboard.css';
import Participants from './Participants';
import Quiz from './quiz';
import Question from './Question';

interface QuizDashboardProps {
    quiz: Quiz;
    participantId: number;
}

const QuizDashboard: React.FC<QuizDashboardProps> = (props: QuizDashboardProps) => {

    const [quiz, setQuiz] = useState(props.quiz);

    const buzzer = () => {
        const buzzerHref = quiz.participants
                .find(p => p.id === props.participantId)
                .links
                .find(link => link.rel === 'buzzer')
                .href;

        fetch(`${process.env.REACT_APP_BASE_URL}${buzzerHref}`, {
            method: 'PUT',
            headers: {
                Accept: 'application/json'
            }
        })
        .then(response => response.json())
        .then(json => setQuiz(json))
        .catch(e => console.error(e));
    };

    useEffect(() => {
        console.log('websocket wird erstellt');
        const clientWebSocket = new WebSocket(`${process.env.REACT_APP_WS_BASE_URL}/event-emitter`);
        clientWebSocket.onmessage = ev => {
            console.log('event', ev);
            setQuiz(JSON.parse(ev.data));
        };
        
        return () => {
            console.log('websocket wird geschlossen');
            clientWebSocket.close();
        };
    }, []);

    return (
        <header className="App-header">
            <Participants quiz={quiz}></Participants>
            <Question quiz={quiz}></Question>
            <button disabled={quiz.participants.some(p => p.turn)} className="buzzer" onClick={buzzer}>
                Buzzer
            </button>
        </header>
    )
};

export default QuizDashboard;