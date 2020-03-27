import React, { useState, useEffect } from 'react';

import './QuizDashboard.css';
import Participants from './Participants';
import Quiz from './quiz';

interface QuizProps {
    quiz: Quiz;
}

const QuizDashboard: React.FC<QuizProps> = (props: QuizProps) => {

    const [quiz, setQuiz] = useState(props.quiz);

    const buzzer = () => {
        fetch('http://localhost:8080/api/quiz/1/participants/Erik/buzzer', {
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
        const clientWebSocket = new WebSocket("ws://localhost:8080/event-emitter");
        clientWebSocket.onmessage = ev => {
            console.log('event', ev);
            setQuiz(JSON.parse(ev.data));
        };

        fetch('http://localhost:8080/api/quiz/1/', {
            method: 'GET',
            headers: {
                Accept: 'application/json'
            }
        })
        .then(response => response.json())
        .then(json => setQuiz(json))
        .catch(e => console.error(e));
        
        return () => {
            console.log('websocket wird geschlossen');
            clientWebSocket.close();
        };
    }, []);

    return (
        <header className="App-header">
            <Participants quiz={quiz}></Participants>
            <p>
                <span>{quiz?.turn} ist dran</span>
            </p>
            <button className="App-link" onClick={buzzer}>
                Buzzer
            </button>
        </header>
    )
};

export default QuizDashboard;