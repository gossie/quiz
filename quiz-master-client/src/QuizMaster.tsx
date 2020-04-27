import React, { useEffect, useState } from 'react';
import Questions from './Questions/Questions';
import Participants from './quiz-client-shared/Participants/Participants';
import Quiz from './quiz-client-shared/quiz';
import QuizStatistics from './quiz-client-shared/QuizStatistics/QuizStatistics';

interface QuizMasterProps {
    quizId: string;
}

const QuizMaster: React.FC<QuizMasterProps> = (props: QuizMasterProps) => {
    const [quiz, setQuiz] = useState({} as Quiz);
    const [finishButtonCssClasses, setFinishButtonCssClasses] = useState('button is-link')

    useEffect(() => {
        console.debug('register for server sent events');
        const evtSource = new EventSource(`${process.env.REACT_APP_BASE_URL}/api/quiz/${props.quizId}/stream`);
        
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

    const finishQuiz = () => {
        setFinishButtonCssClasses('button is-link is-loading');
        fetch(`${process.env.REACT_APP_BASE_URL}/api/quiz/${props.quizId}`, {
            method: 'POST'
        })
        .finally(() => setFinishButtonCssClasses('button is-link'));
    }

    return (
        <div className="Quiz-dashboard">
            { Object.keys(quiz).length > 0 &&
                <div>
                    <h4 className="title is-4">{quiz.name} (ID: {quiz.id})</h4>
                    <div className="columns">
                        <div className="column participants">
                            <Participants quiz={quiz}></Participants>
                            <button className={finishButtonCssClasses} onClick={finishQuiz}>Finish Quiz</button>
                            <QuizStatistics quiz={quiz}></QuizStatistics>
                        </div>
                        <div className="column question">
                            <Questions quiz={quiz}></Questions>
                        </div>
                    </div>
                </div>
            }
        </div>
    )
}

export default QuizMaster;