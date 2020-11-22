import React, { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import Questions from './Questions/Questions';
import Participants from './quiz-client-shared/Participants/Participants';
import Quiz from './quiz-client-shared/quiz';
import QuizStatistics from './quiz-client-shared/QuizStatistics/QuizStatistics';

import './QuizMaster.css';

interface QuizMasterProps {
    quizId: string;
}

const QuizMaster: React.FC<QuizMasterProps> = (props: QuizMasterProps) => {
    const [quiz, setQuiz] = useState({} as Quiz);
    const [finishButtonCssClasses, setFinishButtonCssClasses] = useState('button is-link')
    const [forceStatistics, setForceStatistics] = useState(false);
    
    const { t } = useTranslation();

    useEffect(() => {
        console.debug('register for server sent events');
        const evtSource = new EventSource(`${process.env.REACT_APP_BASE_URL}/api/quiz/${props.quizId}/quiz-master`);
        
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
    }, [props.quizId]);

    const lastChanged = () => new Date(quiz.timestamp).toDateString();
    const expires = () => {
        const expirationDate = new Date(quiz.expirationDate);
        return `${expirationDate.getDate()}.${expirationDate.getMonth() + 1}.${expirationDate.getFullYear()}`
    };

    const finishQuiz = () => {
        setFinishButtonCssClasses('button is-link is-loading');
        fetch(`${process.env.REACT_APP_BASE_URL}/api/quiz/${props.quizId}`, {
            method: 'POST'
        })
        .finally(() => setFinishButtonCssClasses('button is-link'));
    }

    const undo = () => {
        if (quiz.undoPossible) {
            const url = quiz.links.find(link => link.rel === 'undo').href;
            fetch(`${process.env.REACT_APP_BASE_URL}${url}`, {
                method: 'DELETE'
            });
        }
    }

    const redo = () => {
        if (quiz.redoPossible) {
            const url = quiz.links.find(link => link.rel === 'redo').href;
            fetch(`${process.env.REACT_APP_BASE_URL}${url}`, {
                method: 'POST'
            });
        }
    }

    return (
        <div className="Quiz-dashboard">
            { Object.keys(quiz).length > 0
            ?
                <div>
                    <div id="timestamp">{lastChanged()}</div>
                    <div>
                        <h4 className="title is-4">{quiz.name}</h4>
                        <span className={`icon ${quiz.undoPossible ? "clickable has-text-link" : "has-text-grey-light"}`} onClick={() => undo()} title="Undo"><i className="fas fa-undo"></i></span>
                        <span className={`icon ${quiz.redoPossible ? "clickable has-text-link" : "has-text-grey-light"}`} onClick={() => redo()} title="Redo"><i className="fas fa-redo"></i></span>
                    </div>
                    <div className="columns">
                        <div className="column participants box">
                            <Participants quiz={quiz}></Participants>
                            <div id="finish-hint">{t('finishQuizNote')}</div>
                            { quiz.quizStatistics
                            ?
                                <button className={finishButtonCssClasses} onClick={() => setForceStatistics(true)}>Show statistics</button>
                            :
                                <button className={finishButtonCssClasses} onClick={finishQuiz}>Finish Quiz</button>
                            }
                            <QuizStatistics quiz={quiz} closeable={true} forceOpen={forceStatistics} onClose={() => setForceStatistics(false)}></QuizStatistics>
                            <div id="expiration-date">
                                {t('expirationNote', { expirationDate: expires() })}
                            </div> 
                        </div>
                        <div className="column question">
                            <Questions quiz={quiz}></Questions>
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
}

export default QuizMaster;
