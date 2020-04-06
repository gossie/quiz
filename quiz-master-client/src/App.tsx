import React, { useState } from 'react';
import './quiz-client-shared/App.css';
import QuizMaster from './QuizMaster';
import LoginPageWidget from './quiz-client-shared/LoginPageWidget/LoginPageWidget' 
import AppHeader from './quiz-client-shared/AppHeader/AppHeader';
import Quiz from './quiz-client-shared/quiz';


function App() {
    const [quiz, setQuiz] = useState({} as Quiz);

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
                Accept: 'application/json'
            }
        });
        const newQuiz: Quiz = await quizResponse.json();
        setQuiz(newQuiz);
    };

    const joinQuiz = async (value: any) => {
        const quizResponse = await fetch(`${process.env.REACT_APP_BASE_URL}/api/quiz/${value[quizIdLabel]}`, {
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
            <AppHeader title="Quiz Master"></AppHeader>
            <div className="container App-content">
                { quiz.id
                    ?
                    <QuizMaster quiz={quiz}></QuizMaster>
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
