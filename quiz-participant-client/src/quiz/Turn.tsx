import React from 'react';
import Quiz from "./quiz";

interface TurnProps {
    quiz: Quiz;
}

const Turn: React.FC<TurnProps> = (props: TurnProps) => {
    return <div>{props.quiz.turn}</div>
};

export default Turn;