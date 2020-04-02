import React, { useState } from 'react';
import './LoginPageWidget.css';

interface JoinWidgetProps {
    onSubmit: Function;
    title: string;
    inputLabel: string;
    buttonLabel: string;
}

const LoginPageWidget: React.FC<JoinWidgetProps> = (props: JoinWidgetProps) => {
    const [inputValue, setInputValue] = useState('');
    
    const onSubmit = () => {
        props.onSubmit(inputValue)
    }

    return (
        <div className="box">
            <h5 className="title is-5">{props.title}</h5>
            <div className="field has-addons">
                <div className="control">
                    <input className="input" type="text" placeholder={props.inputLabel} onChange={(ev) => setInputValue(ev.target.value)}/>
                </div>
                <div className="control">
                    <button className="button is-info" onClick={onSubmit}>
                    {props.buttonLabel}
                    </button>
                </div>
            </div>
        </div>
    )
};
export default LoginPageWidget;