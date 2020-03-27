import React, { useState } from 'react';
import './App.css';
import QuizDashboard from './quiz/QuizDashboard';
import Quiz from './quiz/quiz';

function App() {
    const [quiz, setQuiz] = useState({} as Quiz);

    const startQuiz = () => {
        fetch('http://localhost:8080/api/quiz', {
            method: 'POST',
            body: JSON.stringify({
                name: 'Hegarty\'s Quiz'
            }),
            headers: {
                'Content-Type': 'application/json',
                Accept: 'application/json'
            }
        })
        .then(response => response.json())
        .then(json => setQuiz(json))
        .catch(e => console.error(e));
    };

    return (
        <div className="App">
            {Object.keys(quiz).length > 0 ? 
                <QuizDashboard quiz={quiz}></QuizDashboard> :
                <button onClick={startQuiz}>Start Quiz</button>
            }
        </div>
    );
}

export default App;
