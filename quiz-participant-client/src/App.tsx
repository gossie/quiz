import React, { useState } from 'react';
import './quiz-client-shared/App.css';
import QuizDashboard from './quiz/QuizDashboard';
import Quiz from './quiz-client-shared/quiz';
import LoginPageWidget from './quiz-client-shared/LoginPageWidget/LoginPageWidget';
import AppHeader from './quiz-client-shared/AppHeader/AppHeader';

function App() {
    const [quizId, setQuizId] = useState(0);
    const [participantName, setParticipantName] = useState('');

    const quizIdLabel: string = 'Quiz ID';
    const playerNameLabel: string = 'Player Name';

    const joinQuiz = async (value) => {

        setQuizId(value[quizIdLabel]);
        setParticipantName(value[playerNameLabel]);
        
        // const participantLink = quiz.links.find(link => link.rel === 'createParticipant').href;
        // const participantResponse = await fetch(`${process.env.REACT_APP_BASE_URL}${participantLink}`, {
        //     method: 'POST',
        //     body: value[playerNameLabel],
        //     headers: {
        //         'Content-Type': 'text/plain',
        //         Accept: 'application/json'
        //     }
        // });
        // const result = await participantResponse.json();
        // setParticipantId(result.participantId);
        // setQuiz(result.quiz);
    };

    return (
        <div className="App">
            <AppHeader title="Quiz"></AppHeader>
            <div className="App-content">
            {quizId > 0 && participantName.length > 0 ? 
                <QuizDashboard quizId={quizId} participantName={participantName}></QuizDashboard> :
                <div className="container Login-page">
                    <LoginPageWidget title="Join a Quiz" inputLabels={[playerNameLabel, quizIdLabel]} buttonLabel="Join!" onSubmit={joinQuiz}></LoginPageWidget> 
                </div>     
            }
            </div>
        </div>
    );
}

export default App;
