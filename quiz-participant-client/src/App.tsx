import React, { useState } from 'react';
import './App.scss';
import QuizDashboard from './quiz/QuizDashboard';
import LoginPageWidget from './quiz-client-shared/LoginPageWidget/LoginPageWidget';
import AppHeader from './quiz-client-shared/AppHeader/AppHeader';

function App() {
    const [quizId, setQuizId] = useState('');
    const [participantName, setParticipantName] = useState('');
    const [errorMessage, setErrorMessage] = useState('');


    const quizIdLabel: string = 'Quiz ID';
    const playerNameLabel: string = 'Player Name';

    const getQuizIdFromUrl = () => {
        const query = new URLSearchParams(window.location.search);
        const id = query.get('quiz_id');
        return id;
    };

    const setQuizIDInURL = (quizId) => {
        if(window.history && window.history.pushState) {
            const query = new URLSearchParams(window.location.search);
            query.set('quiz_id', quizId);
            var newUrl = window.location.protocol + "//" + window.location.host + window.location.pathname + '?' + query.toString();
            window.history.pushState(null, document.title, newUrl);
        }
    }

    const joinQuiz = async (value) => {
        setQuizId(value[quizIdLabel]);
        setQuizIDInURL(value[quizIdLabel]);
        setParticipantName(value[playerNameLabel]);
    };

    const onQuizNotFound = () => {
        setErrorMessage(`No quiz for id '${quizId}' found`);
        setQuizId('');
        setQuizIDInURL('');
    }

    return (
        <div className="App">
            <AppHeader title="Quiz"></AppHeader>
            <div className="App-content">
            {quizId.length > 0 && participantName.length > 0 ? 
                <QuizDashboard quizId={quizId} participantName={participantName} quizNotFoundHandler={onQuizNotFound}></QuizDashboard> :
                <div className="container Login-page">
                    <div>
                        <LoginPageWidget title="Join a Quiz" inputValues={{[playerNameLabel]: '', [quizIdLabel]: getQuizIdFromUrl()}} inputLabels={[playerNameLabel, quizIdLabel]} buttonLabel="Join!" onSubmit={joinQuiz}></LoginPageWidget> 
                    </div>
                    <div id="error-container" className="has-text-danger">
                        { errorMessage.length > 0 && <div>{errorMessage}</div> }
                    </div>
                </div>
            }
            </div>
        </div>
    );
}

export default App;
