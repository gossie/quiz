import React, { useRef, useState } from 'react';
import Quiz from "../quiz";
import './QuizStatistics.scss';

interface QuizStatisticsProps {
    quiz: Quiz;
    closeable: boolean;
    forceOpen?: boolean
    onClose?: () => void;
}

const COLORS = [
    '#FF0000',
    '#00FF00',
    '#0000FF',
    '#FFFF00',
    '#FF00FF',
    '#00FFFF'
]

const QuizStatistics: React.FC<QuizStatisticsProps> = (props: QuizStatisticsProps) => {

    const [closed, setClosed] = useState(false);
    const canvasReference = useRef(null)

    if (canvasReference.current) {
        const canvas: HTMLCanvasElement = canvasReference.current;
        canvas.width = 800;
        canvas.height = 600;
        canvas.style.cssText = 'image-rendering: optimizeSpeed;' + // FireFox < 6.0
                'image-rendering: -moz-crisp-edges;' + // FireFox
                'image-rendering: -o-crisp-edges;' +  // Opera
                'image-rendering: -webkit-crisp-edges;' + // Chrome
                'image-rendering: crisp-edges;' + // Chrome
                'image-rendering: -webkit-optimize-contrast;' + // Safari
                'image-rendering: pixelated; ' + // Future browsers
                '-ms-interpolation-mode: nearest-neighbor;'; // IE

        const ctx: CanvasRenderingContext2D = canvas.getContext('2d');

        const maxPoints = props.quiz.participants.map(p => p.points).reduce((p, c) => p > c ? p : c, 0);

        const questionSpace = 780 / props.quiz.playedQuestions.length;
        const pointSpace = 580 / maxPoints;

        ctx.beginPath();
        ctx.strokeStyle = '#FFFFFF';
        ctx.moveTo(10, 590);
        ctx.lineTo(790, 590);
        ctx.moveTo(10, 590);
        ctx.lineTo(10, 10);
        
        for (let i=1; i<props.quiz.playedQuestions.length; i++) {
            ctx.moveTo(10 + i*questionSpace, 585);
            ctx.lineTo(10 + i*questionSpace, 595);
        }
        
        for (let i=1; i<maxPoints; i++) {
            ctx.moveTo(5, 590 - i*pointSpace);
            ctx.lineTo(15, 590 - i*pointSpace);
        }

        ctx.stroke();
        ctx.closePath()

        props.quiz.quizStatistics.participantStatistics.forEach((participantStatistic, index) => {
            ctx.beginPath();
            ctx.moveTo(10, 590);
            ctx.strokeStyle = COLORS[index];
            let points = 0;
            participantStatistic.questionStatistics.forEach((questionStatistic, questionIndex) => {
                questionStatistic.ratings.forEach(rating => {
                    points += rating === 'CORRECT' ? (questionStatistic.question.points ?? 2) : -1;
                })
                ctx.lineTo(10 + (questionIndex+1)*questionSpace, 590 - points*pointSpace);
            });
            ctx.stroke();
            ctx.closePath();
        });
    }

    const close = () => {
        setClosed(true);
        if (props.onClose) {
            props.onClose();
        }
    };

    return (
        <div>
            { props.quiz.quizStatistics && (props.forceOpen || !closed) && 
                <div data-testid="quiz-statistics" className="modal is-active">
                    <div className="modal-background"></div>
                    <div className="modal-content statistics">
                        <canvas id="statistics-display" ref={canvasReference} width="800" height="600" />
                    </div>
                    { props.closeable && <button data-testid="close-button" className="modal-close is-large" aria-label="close" onClick={close}></button> }
                </div> 
            }
        </div>
    );

}

export default QuizStatistics;
