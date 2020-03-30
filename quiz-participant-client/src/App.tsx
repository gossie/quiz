import React, { useState } from 'react';
import './App.css';
import QuizDashboard from './quiz/QuizDashboard';
import Quiz from './quiz/quiz';

function App() {
    const [quizId, setQuizId] = useState('');
    const [name, setName] = useState('');
    const [quiz, setQuiz] = useState({} as Quiz);
    const [participantId, setParticipantId] = useState(0)

    const joinQuiz = async () => {

        const quizResponse = await fetch(`${process.env.REACT_APP_BASE_URL}/api/quiz/${quizId}`, {
            method: 'GET',
            headers: {
                Accept: 'application/json'
            }
        });
        const quiz: Quiz = await quizResponse.json();
        
        const participantLink = quiz.links.find(link => link.rel === 'createParticipant').href;
        const participantResponse = await fetch(`${process.env.REACT_APP_BASE_URL}${participantLink}`, {
            method: 'POST',
            mode: 'no-cors',
            body: name,
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
            {Object.keys(quiz).length > 0 ? 
                <QuizDashboard quiz={quiz} participantId={participantId}></QuizDashboard> :
                <header className="App-header">
                    <h3>Join quiz</h3>
                    <div>
                        <span><label>Quiz ID</label></span><span><input type="text" onChange={(ev) => setQuizId(ev.target.value)} /></span>
                    </div>
                    <div>
                        <span><label>Name</label></span><span><input type="text" onChange={(ev) => setName(ev.target.value)} /></span>
                    </div>
                    <div>
                        <button onClick={joinQuiz}>GO!</button>
                    </div>
                </header>
            }
        </div>
    );
}

export default App;
