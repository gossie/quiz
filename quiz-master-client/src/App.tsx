import React, { useState } from 'react';
import './quiz-client-shared/App.css';
import QuizMaster from './QuizMaster';
import LoginPageWidget from './quiz-client-shared/LoginPageWidget/LoginPageWidget' 
import AppHeader from './quiz-client-shared/AppHeader/AppHeader';


function App() {
    const [quizId, setQuizId] = useState('');

    const quizNameLabel = 'Quiz Name';
    const quizIdLabel = 'Quiz Id';

    const getQuizIdFromUrl = () => {
        const query = new URLSearchParams(window.location.search);
        const id = query.get('quiz_id')
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

    const startQuiz = async (value: any) => {
        const quizResponse = await fetch(`${process.env.REACT_APP_BASE_URL}/api/quiz/`, {
            method: 'POST',
            body: JSON.stringify({
                name: value[quizNameLabel]
            }),
            headers: {
                'Content-Type': 'application/json',
                Accept: 'text/plain'
            }
        });
        const responseStatus = await quizResponse.status;
        if (responseStatus === 201) {
            const responseBody = await quizResponse.text();
            setQuizId(responseBody);
            setQuizIDInURL(responseBody);
        }  
    };

    const joinQuiz = (value: any) => {  
        setQuizIDInURL(value[quizIdLabel]);
        return Promise.resolve(setQuizId(value[quizIdLabel]));
    }

    return (
        <div className="App">
            <AppHeader title="Quiz Master"></AppHeader>
            <div className="App-content">
                { quizId.length > 0
                    ?
                    <QuizMaster quizId={quizId}></QuizMaster>
                    :
                        <div className="container Login-page">
                            <LoginPageWidget title="Create a Quiz" inputLabels={[quizNameLabel]} buttonLabel="Start!" onSubmit={startQuiz}></LoginPageWidget>
                            <LoginPageWidget title="Open Quiz" inputValues={{[quizIdLabel]: getQuizIdFromUrl()}} inputLabels={[quizIdLabel]} buttonLabel="Join!" onSubmit={joinQuiz}></LoginPageWidget>
                        </div>
                }
            </div>
        </div>
    );
}

export default App;
