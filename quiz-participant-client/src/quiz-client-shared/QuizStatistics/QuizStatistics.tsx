import React, { useRef, useState } from 'react';
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
        canvas.width = 620;
        canvas.height = 465;
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

        const coordinateSystemLeft = 10;
        const coordinateSystemTop = 10;
        const coordinateSystemBottom = canvas.height - 10;
        const coordinateSystemRight = canvas.width - 10;

        const questionSpace = (canvas.width - 20) / props.quiz.quizStatistics.participantStatistics[0].questionStatistics.length;
        const pointSpace = (canvas.height - 20) / maxPoints;

        ctx.beginPath();
        ctx.strokeStyle = '#FFFFFF';
        ctx.moveTo(coordinateSystemLeft, coordinateSystemBottom);
        ctx.lineTo(coordinateSystemRight, coordinateSystemBottom);
        ctx.moveTo(coordinateSystemLeft, coordinateSystemBottom);
        ctx.lineTo(coordinateSystemLeft, coordinateSystemTop);
        
        for (let i=1; i<props.quiz.quizStatistics.participantStatistics[0].questionStatistics.length; i++) {
            ctx.moveTo(coordinateSystemLeft + i*questionSpace, coordinateSystemBottom - 5);
            ctx.lineTo(coordinateSystemLeft + i*questionSpace, coordinateSystemBottom + 5);
        }
        
        for (let i=1; i<maxPoints; i++) {
            ctx.moveTo(coordinateSystemLeft - 5, coordinateSystemBottom - i*pointSpace);
            ctx.lineTo(coordinateSystemLeft + 5, coordinateSystemBottom - i*pointSpace);
        }

        ctx.stroke();
        ctx.closePath()

        props.quiz.quizStatistics.participantStatistics.forEach((participantStatistic, index) => {
            ctx.beginPath();
            ctx.moveTo(coordinateSystemLeft, coordinateSystemBottom);
            ctx.strokeStyle = COLORS[index];
            let points = 0;
            participantStatistic.questionStatistics.forEach((questionStatistic, questionIndex) => {
                questionStatistic.ratings.forEach(rating => {
                    points += rating === 'CORRECT' ? (questionStatistic.question.points ?? 2) : -1;
                })
                ctx.lineTo(coordinateSystemLeft + (questionIndex+1)*questionSpace, coordinateSystemBottom - points*pointSpace);
            });
            ctx.stroke();
            ctx.closePath();
        });
    }

    const determineLegend = () => {
        const trs = props.quiz.quizStatistics.participantStatistics
            .map(p => p.participant)
            .map((p, index) => (
                <tr>
                    <td width="100" style={{backgroundColor: COLORS[index]}}></td>
                    <td width="10" />
                    <td>{p.name}</td>
                    <td width="10" />
                    <td>{p.points} {t('labelPoints')}</td>
                </tr>
            ));
        return (
            <table>
                <tbody>
                    {trs}
                </tbody>
            </table>
        )
    };

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
                    <div className="modal-content">
                        { determineLegend() }
                        <canvas id="statistics-display" ref={canvasReference} width="620" height="465" />
                    </div>
                    { props.closeable && <button data-testid="close-button" className="modal-close is-large" aria-label="close" onClick={close}></button> }
                </div> 
            }
        </div>
    );

}

export default QuizStatistics;
