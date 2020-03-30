import React, { useState } from 'react';
import './App.css';
import Quiz from './quiz';
import QuizMaster from './QuizMaster';

function App() {
    const [quiz, setQuiz] = useState({} as Quiz);
    const [quizId, setQuizId] = useState('');

    const startQuiz = async () => {
        const quizResponse = await fetch(`${process.env.REACT_APP_BASE_URL}/api/quiz/`, {
            method: 'POST',
            mode: 'no-cors',
            body: JSON.stringify({
                name: 'Hegarty\'s Quiz'
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
            mode: 'no-cors',
            headers: {
                Accept: 'application/json'
            }
        });
        const joinedQuiz: Quiz = await quizResponse.json();
        setQuiz(joinedQuiz)
    };

    return (
        <div className="App">
            <header className="App-header">
                { quiz.id
                  ?
                    <QuizMaster quiz={quiz}></QuizMaster>
                  :
                    <div>
                        <p>
                            <button onClick={startQuiz}>Start quiz</button>
                        </p>
                        <div>
                            <h3>Join quiz</h3>
                            <div>
                                <span><label>Quiz ID</label></span><span><input type="text" onChange={(ev) => setQuizId(ev.target.value)} /></span>
                            </div>
                            <div>
                                <button onClick={joinQuiz}>GO!</button>
                            </div>
                        </div>
                    </div>
                }
                
            </header>
        </div>
    );
}

export default App;
