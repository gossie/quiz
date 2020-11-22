import React, { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Question } from "../quiz-client-shared/quiz";

interface ImageProps {
    question: Question;
}

const Image: React.FC<ImageProps> = (props: ImageProps) => {
    const [imageCssClass, setImageCssClass] = useState('question-image invisible');
    const [timerCssClass, setTimerCssClass] = useState('');
    const [time, setTime] = useState(3);

    const { t } = useTranslation();

    useEffect(() => {
        const timer = setTimeout(() => {
            if (time === 0) {
                console.debug('render image');
                setImageCssClass('question-image');
                setTimerCssClass('invisible');
            } else {
                console.debug('counting down');
                setTime(oldTime => oldTime - 1);
            }
        }, 1000);
        return () => clearTimeout(timer);
    }, [time]);

    return (
        <div className="image-wrapper">
            { imageCssClass.includes('invisible') && <span className={timerCssClass}>{time}</span> }
            <img src={props.question.imagePath} alt={t('imageAlt')} className={imageCssClass}></img>
        </div>
    );
}

export default Image;