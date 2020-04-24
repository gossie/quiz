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
        console.debug('register for server sent events');
        const evtSource = new EventSource(`${process.env.REACT_APP_BASE_URL}/api/quiz/${quiz.id}/stream`);
        
        evtSource.onerror = (e) => console.error('sse error', e);

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