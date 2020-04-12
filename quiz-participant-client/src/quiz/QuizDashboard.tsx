import React, { useState, useEffect, useRef } from 'react';

import './QuizDashboard.css';
import Quiz from '../quiz-client-shared/quiz';
import Question from './Question';
import Participants from '../quiz-client-shared/Participants/Participants';
const buzzerfile = require('./../assets/buzzer.mp3');

interface QuizDashboardProps {
    quiz: Quiz;
    participantId: number;
}

const QuizDashboard: React.FC<QuizDashboardProps> = (props: QuizDashboardProps) => {

    const [quiz, setQuiz] = useState(props.quiz);
    const buzzerAudio = useRef(null);

    const buzzer = () => {
       
        buzzerAudio.current.muted = false;
        buzzerAudio.current.play();
        
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

    const isCurrentQuestionOpen = () => {
        return !quiz.participants.some(p => p.turn);
    }

    useEffect(() => {
        // Trigger preloading of audio to prevent delays when buzzer is pressed
        buzzerAudio.current.muted = true;
        buzzerAudio.current.play();
    }, [])

    useEffect(() => {
        const buzzerOnKeydown = (event) => {
            if ((event.keyCode === 32 || event.keyCode === 13) && isCurrentQuestionOpen()) {
                buzzer();
            }
        }
        document.addEventListener('keydown', buzzerOnKeydown);
        return () => {
            document.removeEventListener('keydown', buzzerOnKeydown);
        }
    }, []);

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
    });

    return (
        <div className="Quiz-dashboard">
            <h4 className="title is-4">{quiz.name}</h4>
            <div className="container Dashboard-content">
                <div className="container participants">
                    <Participants quiz={quiz}></Participants>
                </div>
                <div className="container question">
                    <h5 className="title is-5">Current question</h5>
                    <audio src={buzzerfile} ref={buzzerAudio} preload='auto'></audio>
                    <button disabled={quiz.participants.some(p => p.turn)} className={isParticipantActive() ? 'buzzer active' : 'buzzer'} onMouseDown={buzzer}>
                        {isCurrentQuestionOpen() ? 
                            "I know it!" :
                            (!isParticipantActive() ?
                            "Too late!"
                            :
                            "Your turn!")
                        }
                            
                    </button>
                    {<Question quiz={quiz}></Question>}
                </div>
            </div>
        </div>
    )
};

export default QuizDashboard;