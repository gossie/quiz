import React, { useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import Quiz, { Question } from "../quiz";
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
    const quizCanvasReference = useRef(null);
    const questionCanvasReference = useRef(null);

    const { t } = useTranslation();

    if (quizCanvasReference.current) {
        const quizCanvas: HTMLCanvasElement = quizCanvasReference.current;
        quizCanvas.width = 620;
        quizCanvas.height = 465;
        quizCanvas.style.cssText = 'image-rendering: optimizeSpeed;' + // FireFox < 6.0
                'image-rendering: -moz-crisp-edges;' + // FireFox
                'image-rendering: -o-crisp-edges;' +  // Opera
                'image-rendering: -webkit-crisp-edges;' + // Chrome
                'image-rendering: crisp-edges;' + // Chrome
                'image-rendering: -webkit-optimize-contrast;' + // Safari
                'image-rendering: pixelated; ' + // Future browsers
                '-ms-interpolation-mode: nearest-neighbor;'; // IE

        const ctx: CanvasRenderingContext2D = quizCanvas.getContext('2d');

        const maxPoints = props.quiz.participants.map(p => p.points).reduce((p, c) => p > c ? p : c, 0);

        const coordinateSystemLeft = 10;
        const coordinateSystemTop = 10;
        const coordinateSystemBottom = quizCanvas.height - 10;
        const coordinateSystemRight = quizCanvas.width - 10;

        const questionSpace = (quizCanvas.width - 20) / props.quiz.quizStatistics.participantStatistics[0].questionStatistics.length;
        const pointSpace = (quizCanvas.height - 20) / maxPoints;

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

    if (questionCanvasReference.current) {
        const questionCanvas: HTMLCanvasElement = questionCanvasReference.current;
        questionCanvas.width = 620;
        questionCanvas.height = 465;
        questionCanvas.style.cssText = 'image-rendering: optimizeSpeed;' + // FireFox < 6.0
                'image-rendering: -moz-crisp-edges;' + // FireFox
                'image-rendering: -o-crisp-edges;' +  // Opera
                'image-rendering: -webkit-crisp-edges;' + // Chrome
                'image-rendering: crisp-edges;' + // Chrome
                'image-rendering: -webkit-optimize-contrast;' + // Safari
                'image-rendering: pixelated; ' + // Future browsers
                '-ms-interpolation-mode: nearest-neighbor;'; // IE

        const ctx: CanvasRenderingContext2D = questionCanvas.getContext('2d');

        const questionsByCategory = new Map<string, Array<Question>>();

        const onlyUnique = (value: Question, index: number, self: Array<Question>) => {
            return self.findIndex(question => question.id === value.id) === index;
        }

        props.quiz.quizStatistics.participantStatistics
            .flatMap(participantStatistic => participantStatistic.questionStatistics)
            .map(questionStatistic => questionStatistic.question)
            .filter(onlyUnique)
            .forEach(question => {
                const questions = questionsByCategory.get(question.category);
                if (questions) {
                    questions.push(question);
                } else {
                    questionsByCategory.set(question.category, [question]);
                }
            });

        const max = Array.from(questionsByCategory.values())
            .map(arr => arr.length)
            .reduce((length, current) => length > current ? length : current, 0)

        const space = 620 / questionsByCategory.size;
        Array.from(questionsByCategory.entries())
            .forEach((entry, index) => {
                const capitalize = (s: string) => {
                    return s.charAt(0).toUpperCase() + s.slice(1)
                }

                ctx.fillStyle = '#FFFFFF';
                ctx.fillRect(index*space, 460-(entry[1].length * 450 / max), space-5, entry[1].length * 450 / max);
                ctx.fillText(t(`category${capitalize(entry[0])}`), index*space, 457-(entry[1].length * 450 / max), space)
            });
    }

    const determineLegend = () => {
        const trs = props.quiz.quizStatistics.participantStatistics
            .filter(p => p.participant)
            .map(p => p.participant)
            .map((p, index) => ({
                participant: p,
                markup: (
                    <tr>
                        <td width="100">
                            <span style={{backgroundColor: COLORS[index], width: "100%", height: '2px', display: 'inline-block', verticalAlign: 'middle'}} />
                        </td>
                        <td width="10" />
                        <td>{p.name}</td>
                        <td width="10" />
                        <td>{p.points} {t('labelPoints')}</td>
                    </tr>
                )
            }))
            .sort((o1, o2) => o2.participant.points - o1.participant.points)
            .map(o => o.markup);
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
                    <div className="content">
                            <h1 className="title">{t('titleQuizStatistics')}</h1>
                            { determineLegend() }
                            <canvas id="quiz-statistics-display" ref={quizCanvasReference} width="620" height="465" />
                            <h1 className="title">{t('titleQuestionStatistics')}</h1>
                            <canvas id="question-statistics-display" ref={questionCanvasReference} width="620" height="465" />
                        </div>
                    </div>
                    { props.closeable && <button data-testid="close-button" className="modal-close is-large" aria-label="close" onClick={close}></button> }
                </div> 
            }
        </div>
    );

}

export default QuizStatistics;
