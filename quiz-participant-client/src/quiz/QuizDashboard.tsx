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
    quizNotFoundHandler: () => void;
}

const QuizDashboard: React.FC<QuizDashboardProps> = (props: QuizDashboardProps) => {

    const [quiz, setQuiz] = useState({} as Quiz);
    const [participantId, setParticipantId] = useState('');
    const [revealAllowed, setRevealAllowed] = useState(false);

    let evtSource: EventSource = undefined;

    useEffect(() => {
        console.debug('register for server sent events');
        evtSource = new EventSource(`${process.env.REACT_APP_BASE_URL}/api/quiz/${props.quizId}/quiz-participant`);

        evtSource.onerror = (e) => console.error('sse error', e);

        evtSource.addEventListener("ping", (ev: any) => console.debug('received heartbeat', ev));

        evtSource.addEventListener("quiz", (ev: any) => {
            console.log('event', ev);
            if (Object.keys(JSON.parse(ev.data)).includes('id')) {
                const newQuiz: Quiz = JSON.parse(ev.data);
                const participant = newQuiz.participants.find(p => p.name === props.participantName)
                if (participant) {
                    setParticipantId(participant.id);
                    setRevealAllowed(participant.revealAllowed);
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
            })
            .then(response => {
                if (response.status > 400) {
                    console.debug(`There is no quiz with id ${props.quizId}. The EventSource is closes.`);
                    if (evtSource) {
                        evtSource.close();
                    }
                    props.quizNotFoundHandler();
                }
            });
        }
    });

    const isEstimationQuestion = (quiz: Quiz) => {
        const pendingQuestion = quiz.openQuestions.find(q => q.pending);
        return pendingQuestion !== undefined && pendingQuestion.estimates !== null;
    }

    const toggleRevealAllowed = (allowed: boolean) => {
        setRevealAllowed(allowed);
        const href = quiz.participants.find(p => p.id === participantId).links.find(link => link.rel === 'toggleRevealAllowed').href;
        fetch(`${process.env.REACT_APP_BASE_URL}${href}`, {
            method: 'PUT'
        })
        .then(response => {
            if (response.status !== 200) {
                throw Error('error when performing toggle of reveal allowed');
            }
        })
        .catch(e => console.error(e));
    };
    
    return (
        <div className="Quiz-dashboard">
            { Object.keys(quiz).length > 0 &&
                <div>
                    <h4 className="title is-4">{quiz.name}</h4>
                    <div className="columns Dashboard-content">
                        <div className="column participants box">
                            <div className="field">
                                <div className="control">
                                    <label className="checkbox">
                                        <input type="checkbox" checked={revealAllowed} onChange={ev => toggleRevealAllowed(ev.target.checked)} />
                                        If checked, other participants can see your answer when the quiz master reveals them
                                    </label>
                                </div>
                            </div>
                            <Participants quiz={quiz}></Participants>
                            <QuizStatistics quiz={quiz} closeable={false}></QuizStatistics>
                        </div>
                        <div className="column question box">
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
            }
        </div>
    )
};

export default QuizDashboard;
