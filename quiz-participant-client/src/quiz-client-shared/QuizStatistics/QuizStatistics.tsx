import React, { useEffect, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
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

    const { t } = useTranslation();
    
    if (canvasReference.current) {
        const canvas: HTMLCanvasElement = canvasReference.current;
        canvas.width = 500;
        canvas.height = 500;
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

        const questionSpace = 480 / props.quiz.quizStatistics.participantStatistics[0].questionStatistics.length;
        const pointSpace = 480 / maxPoints;

        ctx.beginPath();
        ctx.strokeStyle = '#FFFFFF';
        ctx.moveTo(10, 490);
        ctx.lineTo(490, 490);
        ctx.moveTo(10, 490);
        ctx.lineTo(10, 10);
        
        for (let i=1; i<props.quiz.quizStatistics.participantStatistics[0].questionStatistics.length; i++) {
            ctx.moveTo(10 + i*questionSpace, 485);
            ctx.lineTo(10 + i*questionSpace, 495);
        }

        for (let i=1; i<maxPoints; i++) {
            ctx.moveTo(5, 490 - i*pointSpace);
            ctx.lineTo(15, 490 - i*pointSpace);
        }

        ctx.stroke();
        ctx.closePath()

        props.quiz.quizStatistics.participantStatistics.forEach((participantStatistic, index) => {
            ctx.beginPath();
            ctx.moveTo(10, 490);
            ctx.strokeStyle = COLORS[index];
            let points = 0;
            participantStatistic.questionStatistics.forEach((questionStatistic, questionIndex) => {
                questionStatistic.ratings.forEach(rating => {
                    points += rating === 'CORRECT' ? (questionStatistic.question.points ?? 2) : -1;
                })
                ctx.lineTo(10 + (questionIndex+1)*questionSpace, 490 - points*pointSpace);
            });
            ctx.stroke();
            ctx.closePath();
        });
    }

    return (
        <div>
            { props.quiz.quizStatistics && (props.forceOpen || !closed) && 
                <div data-testid="quiz-statistics" className="modal is-active">
                    <div className="modal-background"></div>
                    <div className="modal-content">
                        <canvas id="statistics-display" ref={canvasReference} width="500" height="500" />
                    </div>
                </div> 
            }
        </div>
    );

}

export default QuizStatistics;
