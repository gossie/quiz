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

    const isParticipantActive = () => {
       return quiz.participants.some(p => p.turn && p.id === props.participantId)
    }

    useEffect(() => {
        console.log('websocket wird erstellt');
        const clientWebSocket = new WebSocket(`${process.env.REACT_APP_WS_BASE_URL}/event-emitter/${quiz.id}`);
        clientWebSocket.onmessage = ev => {
            console.log('event', ev);
            setQuiz(JSON.parse(ev.data));
        };

        const i = setInterval(() => clientWebSocket.send('heartBeat'), 10000);

        return () => {
            console.log('websocket wird geschlossen');
            clientWebSocket.close();
            clearInterval(i);
        };
    }, []);

    return (
        <div className="Quiz-dashboard">
            <h4 className="title is-4">{quiz.name}</h4>
            <div className="container Dashboard-content">
                <div className="container participants">
                    <Participants quiz={quiz}></Participants>
                </div>
                <div className="container question">
                    <Question quiz={quiz}></Question>
                    <button disabled={quiz.participants.some(p => p.turn)} className={isParticipantActive() ? 'buzzer active' : 'buzzer'} onClick={buzzer}>
                        {!quiz.participants.some(p => p.turn) ? 
                            "I know it!" :
                            (!isParticipantActive() ?
                            "Too late!"
                            :
                            "Your turn!")
                        }
                            
                    </button>
                </div>
            </div>
        </div>
    )
};

export default QuizDashboard;