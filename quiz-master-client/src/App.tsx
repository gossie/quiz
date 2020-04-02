import React, { useState } from 'react';
import './App.css';
import Quiz from './quiz';
import QuizMaster from './QuizMaster';
import LoginPageWidget from './LoginPageWidget/LoginPageWidget';

function App() {
    const [quiz, setQuiz] = useState({} as Quiz);

    const startQuiz = async (quizName: string) => {
        const quizResponse = await fetch(`${process.env.REACT_APP_BASE_URL}/api/quiz/`, {
            method: 'POST',
            body: JSON.stringify({
                name: quizName
            }),
            headers: {
                'Content-Type': 'application/json',
                Accept: 'application/json'
            }
        });
        const newQuiz: Quiz = await quizResponse.json();
        setQuiz(newQuiz);
    };

    const joinQuiz = async (quizId: string) => {
        const quizResponse = await fetch(`${process.env.REACT_APP_BASE_URL}/api/quiz/${quizId}`, {
            method: 'GET',
            headers: {
                Accept: 'application/json'
            }
        });
        const joinedQuiz: Quiz = await quizResponse.json();
        setQuiz(joinedQuiz)
    };

    return (
        <div className="App">
            <header className="level App-header">
                <div className="icon App-logo">
                    <i className="far fa-question-circle"></i>
                </div> 
                QuizMaster
            </header>
            
            { quiz.id
                ?
                <QuizMaster quiz={quiz}></QuizMaster>
                :
                <div className="container App-content">
                    <LoginPageWidget title="Create a Quiz" inputLabel="Quiz Name" buttonLabel="Start!" onSubmit={startQuiz}></LoginPageWidget>
                    <LoginPageWidget title="Join a Quiz" inputLabel="Quiz ID" buttonLabel="Join!" onSubmit={joinQuiz}></LoginPageWidget> 
                </div>
            }
                
            
        </div>
    );
}

export default App;
