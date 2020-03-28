import React, { useState } from 'react';
import './App.css';
import Quiz from './quiz';
import QuizMaster from './QuizMaster';

function App() {
    const [quiz, setQuiz] = useState({} as Quiz);

    const startQuiz = async () => {
        const quizResponse = await fetch('http://localhost:8080/api/quiz/', {
            method: 'POST',
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

    return (
        <div className="App">
            <header className="App-header">
                { quiz.id
                  ?
                    <QuizMaster quiz={quiz}></QuizMaster>
                  :
                    <p>
                        <button onClick={startQuiz}>Start quiz</button>
                    </p>
                }
                
            </header>
        </div>
    );
}

export default App;
