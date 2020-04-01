import React, { useState } from 'react';
import './App.css';
import Quiz from './quiz';
import QuizMaster from './QuizMaster';

function App() {
    const [quiz, setQuiz] = useState({} as Quiz);
    const [quizName, setQuizName] = useState('');
    const [quizId, setQuizId] = useState('');

    const startQuiz = async () => {
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

    const joinQuiz = async () => {
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
                   
                    <div className="box">
                        <h5 className="title is-5">Create a quiz</h5>
                        <div className="field has-addons">
                            <div className="control">
                                <input className="input" type="text" placeholder="Quiz Name"  onChange={(ev) => setQuizName(ev.target.value)} />
                            </div>
                            <div className="control">
                                <button className="button is-info" onClick={startQuiz}>
                                Start!
                                </button>
                            </div>
                        </div>
                    </div>

                    <div className="box">
                        <h5 className="title is-5">Join a quiz</h5>
                        <div className="field has-addons">
                            <div className="control">
                                <input className="input" type="text" placeholder="Quiz ID" onChange={(ev) => setQuizId(ev.target.value)}/>
                            </div>
                            <div className="control">
                                <button className="button is-info" onClick={joinQuiz}>
                                Join!
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            }
                
            
        </div>
    );
}

export default App;
