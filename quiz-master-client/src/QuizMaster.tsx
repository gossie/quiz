import React, { useEffect, useState } from 'react';
import Questions from './Questions/Questions';
import Participants from './quiz-client-shared/Participants/Participants';
import Quiz from './quiz-client-shared/quiz';

interface QuizMasterProps {
    quiz: Quiz;
}

const QuizMaster: React.FC<QuizMasterProps> = (props: QuizMasterProps) => {
    const [quiz, setQuiz] = useState(props.quiz);

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
            <h4 className="title is-4">{quiz.name} (ID: {quiz.id})</h4>
            <div className="columns">
                <div className="column participants">
                    <Participants quiz={quiz}></Participants>
                </div>
                <div className="column question">
                    <Questions quiz={quiz}></Questions>
                </div>
            </div>
        </div>
    )
}

export default QuizMaster;