import React, { useState } from 'react';
import './quiz-client-shared/App.css';
import QuizMaster from './QuizMaster';
import LoginPageWidget from './quiz-client-shared/LoginPageWidget/LoginPageWidget' 
import AppHeader from './quiz-client-shared/AppHeader/AppHeader';


function App() {
    const [quizId, setQuizId] = useState('');

    const quizNameLabel = 'Quiz Name';
    const quizIdLabel = 'Quiz Id';

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
        setQuizId(await quizResponse.text());
    };

    const joinQuiz = (value: any) => {
        setQuizId(value[quizIdLabel]);
        fetch(`${process.env.REACT_APP_BASE_URL}/api/quiz/${value[quizIdLabel]}`, {
            method: 'GET',
            headers: {
                Accept: 'application/json'
            }
        });
    };

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
                            <LoginPageWidget title="Join a Quiz" inputLabels={[quizIdLabel]} buttonLabel="Join!" onSubmit={joinQuiz}></LoginPageWidget>
                        </div>
                }
            </div>
        </div>
    );
}

export default App;
