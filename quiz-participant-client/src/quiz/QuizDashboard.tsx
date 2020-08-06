import React, { useEffect, useState } from 'react';
import Participants from '../quiz-client-shared/Participants/Participants';
import Quiz from '../quiz-client-shared/quiz';
import Buzzer from './buzzer/Buzzer';
import Question from './Question';
import './QuizDashboard.css';
import QuizStatistics from '../quiz-client-shared/QuizStatistics/QuizStatistics';
import Estimation from './estimation/Estimation';

interface QuizDashboardProps {
    quizId: string;
    participantName: string;
}

const QuizDashboard: React.FC<QuizDashboardProps> = (props: QuizDashboardProps) => {

    const [quiz, setQuiz] = useState({} as Quiz);
    const [participantId, setParticipantId] = useState('');

    useEffect(() => {
        console.debug('register for server sent events');
        const evtSource = new EventSource(`${process.env.REACT_APP_BASE_URL}/api/quiz/${props.quizId}/quiz-participant`);

        evtSource.onerror = (e) => console.error('sse error', e);

        evtSource.addEventListener("ping", (ev: any) => console.debug('received heartbeat', ev));

        evtSource.addEventListener("quiz", (ev: any) => {
            console.log('event', ev);
            if (Object.keys(JSON.parse(ev.data)).includes('id')) {
                const newQuiz: Quiz = JSON.parse(ev.data);
                const pId = newQuiz.participants.find(p => p.name === props.participantName)?.id
                if (pId) {
                    setParticipantId(pId);
                }
                setQuiz(newQuiz);
            }
        });

        return () => {
            console.debug('closing event stream');
            evtSource.close();
        };
    }, [props.quizId, props.participantName]);

    useEffect(() => {
        if (participantId.length === 0) {
            // const participantLink = quiz.links.find(link => link.rel === 'createParticipant').href;
            fetch(`${process.env.REACT_APP_BASE_URL}/api/quiz/${props.quizId}/participants`, {
                method: 'POST',
                body: props.participantName,
                headers: {
                    'Content-Type': 'text/plain',
                }
            });
        }
    });

    const isEstimationQuestion = (quiz: Quiz) => {
        const pendingQuestion = quiz.openQuestions.find(q => q.pending);
        return pendingQuestion !== undefined && pendingQuestion.estimates !== null;
    }
    
    return (
        <div className="Quiz-dashboard">
            { Object.keys(quiz).length > 0
            ?
                <div>
                    <h4 className="title is-4">{quiz.name}</h4>
                    <div className="columns Dashboard-content">
                        <div className="column participants">
                            <Participants quiz={quiz}></Participants>
                            <QuizStatistics quiz={quiz} closeable={false}></QuizStatistics>
                        </div>
                        <div className="column question">
                            <h5 className="title is-5">Current question</h5>
                            { isEstimationQuestion(quiz)
                            ?
                                <Estimation quiz={quiz} participantId={participantId}></Estimation>
                            :
                                <Buzzer quiz={quiz} participantId={participantId}></Buzzer>
                            }
                            {<Question quiz={quiz}></Question>}
                        </div>
                    </div>
                </div>
            :
                <div>
                   The quiz is being loaded. This might take a moment if the application has to be woken up.
                </div>
            }
        </div>
    )
};

export default QuizDashboard;
