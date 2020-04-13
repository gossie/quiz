import React, { useEffect, useState } from 'react';
import Participants from '../quiz-client-shared/Participants/Participants';
import Quiz from '../quiz-client-shared/quiz';
import Buzzer from './Buzzer';
import Question from './Question';
import './QuizDashboard.css';

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

    const isCurrentQuestionOpen = () => {
        return !quiz.participants.some(p => p.turn);
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
    }, [quiz.id]);

    return (
        <div className="Quiz-dashboard">
            <h4 className="title is-4">{quiz.name}</h4>
            <div className="columns Dashboard-content">
                <div className="column participants">
                    <Participants quiz={quiz}></Participants>
                </div>
                <div className="column question">
                    <h5 className="title is-5">Current question</h5>
                    <Buzzer isCurrentQuestionOpen={isCurrentQuestionOpen()} isParticipantActive={isParticipantActive()} onBuzzer={buzzer}></Buzzer>
                    {<Question quiz={quiz}></Question>}
                </div>
            </div>
        </div>
    )
};

export default QuizDashboard;