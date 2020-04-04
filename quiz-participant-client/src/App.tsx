import React, { useState } from 'react';
import './quiz-client-shared/App.css';
import QuizDashboard from './quiz/QuizDashboard';
import Quiz from './quiz/quiz';
import LoginPageWidget from './quiz-client-shared/LoginPageWidget/LoginPageWidget';
import AppHeader from './quiz-client-shared/AppHeader/AppHeader';

function App() {
    const [quiz, setQuiz] = useState({} as Quiz);
    const [participantId, setParticipantId] = useState(0)

    const quizIdLabel: string = 'Quiz ID';
    const playerNameLabel: string = 'Player Name';

    const joinQuiz = async (value) => {

        const quizResponse = await fetch(`${process.env.REACT_APP_BASE_URL}/api/quiz/${value[quizIdLabel]}`, {
            method: 'GET',
            headers: {
                Accept: 'application/json'
            }
        });
        const quiz: Quiz = await quizResponse.json();
        
        const participantLink = quiz.links.find(link => link.rel === 'createParticipant').href;
        const participantResponse = await fetch(`${process.env.REACT_APP_BASE_URL}${participantLink}`, {
            method: 'POST',
            body: value[playerNameLabel],
            headers: {
                'Content-Type': 'text/plain',
                Accept: 'application/json'
            }
        });
        const result = await participantResponse.json();
        setParticipantId(result.participantId);
        setQuiz(result.quiz);
    };

    return (
        <div className="App">
            <AppHeader title="Quiz"></AppHeader>
            <div className="container App-content">
            {Object.keys(quiz).length > 0 ? 
                <QuizDashboard quiz={quiz} participantId={participantId}></QuizDashboard> :
                <div className="container Login-page">
                    <LoginPageWidget title="Join a Quiz" inputLabels={[playerNameLabel, quizIdLabel]} buttonLabel="Join!" onSubmit={joinQuiz}></LoginPageWidget> 
                </div>     
            }
            </div>
        </div>
    );
}

export default App;
