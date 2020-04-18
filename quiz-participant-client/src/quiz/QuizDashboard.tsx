import React, { useEffect, useState } from 'react';
import Participants from '../quiz-client-shared/Participants/Participants';
import Quiz from '../quiz-client-shared/quiz';
import Buzzer from './Buzzer';
import Question from './Question';
import './QuizDashboard.css';

interface QuizDashboardProps {
    quizId: string;
    participantName: string;
}

const QuizDashboard: React.FC<QuizDashboardProps> = (props: QuizDashboardProps) => {

    const [quiz, setQuiz] = useState({} as Quiz);
    const [participantId, setParticipantId] = useState('');

    useEffect(() => {
        console.log('websocket wird erstellt');
        const clientWebSocket = new WebSocket(`${process.env.REACT_APP_WS_BASE_URL}/event-emitter/${props.quizId}`);
        clientWebSocket.onmessage = ev => {
            console.log('event', ev);
            if (Object.keys(JSON.parse(ev.data)).includes('id')) {
                const newQuiz = JSON.parse(ev.data);
                const pId = newQuiz.participants.find(p => p.name === props.participantName)?.id
                if (pId) {
                    setParticipantId(pId);
                }
                setQuiz(newQuiz);
            }
        };

        const i = setInterval(() => clientWebSocket.send('heartBeat'), 10000);

        return () => {
            console.log('websocket wird geschlossen');
            clientWebSocket.close();
            clearInterval(i);
        };
    }, [props.quizId, props.participantName]);

    useEffect(() => {
        if (participantId.length === 0 && Object.keys(quiz).length > 0) {
            const participantLink = quiz.links.find(link => link.rel === 'createParticipant').href;
            fetch(`${process.env.REACT_APP_BASE_URL}${participantLink}`, {
                method: 'POST',
                body: props.participantName,
                headers: {
                    'Content-Type': 'text/plain',
                    Accept: 'text/plain'
                }
            })
            .then(response => response.text())
            .then(text => setParticipantId(text))
        }
    });
    
    return (
        <div className="Quiz-dashboard">
            { Object.keys(quiz).length > 0 && 
                <div>
                    <h4 className="title is-4">{quiz.name}</h4>
                    <div className="columns Dashboard-content">
                        <div className="column participants">
                            <Participants quiz={quiz}></Participants>
                        </div>
                        <div className="column question">
                            <h5 className="title is-5">Current question</h5>
                            <Buzzer quiz={quiz} participantId={participantId}></Buzzer>
                            {<Question quiz={quiz}></Question>}
                        </div>
                    </div>
                </div>
            }
        </div>
    )
};

export default QuizDashboard;