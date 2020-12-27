import React, { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Question } from "../quiz";
import './Countdown.scss';

interface CountdownProps {
    question: Question;
}

const Image: React.FC<CountdownProps> = (props: CountdownProps) => {
    const [cssClass, setCssClass] = useState('countdown-bar default');
    const { t } = useTranslation();

    useEffect(() => {
        setCssClass('countdown-bar default');
    }, [props.question.question]);

    useEffect(() => {
        if (props.question.secondsLeft < 10 && props.question.secondsLeft > 0) {
            setCssClass('countdown-bar highlight');
        } else {
            setCssClass('countdown-bar default');
        }
    }, [props.question.secondsLeft]);

    return (
        
        <div className={cssClass}>
            <div className="countdown-bar-inner" style={{width: (Math.max(0, (props.question.secondsLeft)) / (props.question.timeToAnswer) * 100) + '%'}}>
                <div data-testid="question-counter" className="countdown-bar-text">{t('secondsLeft', { seconds: props.question.secondsLeft })}</div>
            </div>
        </div>
    );
}

export default Image;