import React, { useState, useEffect } from 'react';
import Quiz from './quiz';
import Participants from './Participants';
import Questions from './Questions';
import Answers from './Answers';

interface QuizMasterProps {
    quiz: Quiz;
}

const QuizMaster: React.FC<QuizMasterProps> = (props: QuizMasterProps) => {
    const [quiz, setQuiz] = useState(props.quiz);

    useEffect(() => {
        console.log('websocket wird erstellt');
        const clientWebSocket = new WebSocket("ws://localhost:8080/event-emitter");
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
        <div>
            <Participants quiz={quiz}></Participants>
            <Answers quiz={quiz}></Answers>
            <Questions quiz={quiz}></Questions>
        </div>
    )
}

export default QuizMaster;