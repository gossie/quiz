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
        console.debug('register for server sent events');
        const evtSource = new EventSource(`${process.env.REACT_APP_BASE_URL}/api/quiz/${quiz.id}/stream`);

        evtSource.onerror = (e) => console.error('sse error', e);

        evtSource.addEventListener("ping", (ev: any) => console.debug('received heartbeat', ev));

        evtSource.addEventListener("quiz", (ev: any) => {
            console.debug('event', ev);
            setQuiz(JSON.parse(ev['data']));
        });

        return () => {
            console.debug('closing event stream');
            evtSource.close();
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