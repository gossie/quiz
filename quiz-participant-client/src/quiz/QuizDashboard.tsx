import React, { useEffect, useRef, useState } from 'react';
import Participants from '../quiz-client-shared/Participants/Participants';
import Quiz from '../quiz-client-shared/quiz';
import Buzzer from './buzzer/Buzzer';
import Question from './Question';
import './QuizDashboard.css';
import QuizStatistics from '../quiz-client-shared/QuizStatistics/QuizStatistics';
import Estimation from './estimation/Estimation';
import { useTranslation } from 'react-i18next';
import MultipleChoice from './multiple-choice/MultipleChoice';

interface QuizDashboardProps {
    quizId: string;
    participantName: string;
    errorHandler: (errorMessage: string) => void;
}

const QuizDashboard: React.FC<QuizDashboardProps> = (props: QuizDashboardProps) => {

    const [quiz, setQuiz] = useState({} as Quiz);
    const [participantId, setParticipantId] = useState('');
    const [revealAllowed, setRevealAllowed] = useState(false);
    const evtSource = useRef(undefined as EventSource);

    const { t } = useTranslation();

    useEffect(() => {
        console.debug('register for server sent events');
        evtSource.current = new EventSource(`${process.env.REACT_APP_BASE_URL}/api/quiz/${props.quizId}/quiz-participant`);

        evtSource.current.onerror = (e) => console.error('sse error', e);

        evtSource.current.addEventListener("ping", (ev: any) => console.debug('received heartbeat', ev));

        evtSource.current.addEventListener("quiz", (ev: any) => {
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
            evtSource.current.close();
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
                if (response.status === 404) {
                    if (evtSource.current) {
                        evtSource.current.close();
                    }
                    props.errorHandler(t('errorQuizNotFound', { participantName: props.participantName, quizId: props.quizId }));
                } else if (response.status === 400) {
                    if (evtSource.current) {
                        evtSource.current.close();
                    }
                    props.errorHandler(t('errorQuizIdNotValid', { participantName: props.participantName, quizId: props.quizId }));
                }
            });
        }
    });

    const hasPendingQuestion = () => quiz.openQuestions.find(q => q.pending) !== undefined;

    const questionInteraction = () => {
        const pendingQuestion = quiz.openQuestions.find(q => q.pending);
        if (pendingQuestion.choices) {
            return <MultipleChoice question={pendingQuestion} participantId={participantId} />
        } else if (pendingQuestion.estimates) {
            return <Estimation quiz={quiz} participantId={participantId} />
        } else {
            return <Buzzer quiz={quiz} participantId={participantId} />
        }                 
    };

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
                                        {t('hintRevealNotAllowed')}
                                    </label>
                                </div>
                            </div>
                            <Participants quiz={quiz}></Participants>
                            <QuizStatistics quiz={quiz} closeable={false}></QuizStatistics>
                        </div>
                        <div className="column question box">
                            <h5 className="title is-5">{t('headlineCurrentQuestion')}</h5>
                            { hasPendingQuestion() && questionInteraction() }
                            {<Question quiz={quiz}></Question>}
                        </div>
                    </div>
                </div>
            }
        </div>
    )
};

export default QuizDashboard;
