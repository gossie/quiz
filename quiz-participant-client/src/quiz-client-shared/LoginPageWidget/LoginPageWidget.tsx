import React, { useState } from 'react';
import './LoginPageWidget.css';

interface JoinWidgetProps {
    onSubmit: Function;
    title: string;
    inputLabels: string[];
    buttonLabel: string;
}

const LoginPageWidget: React.FC<JoinWidgetProps> = (props: JoinWidgetProps) => {
    const [inputValue, setInputValue] = useState({});

    const onSubmit = () => {
        props.onSubmit(inputValue)
    }

    const fields = props.inputLabels.map((label, index) => 
        <div key={label} className="control">
            <input data-testid={'field-' + index}
                   className="input"
                   type="text"
                   placeholder={label}
                   onChange={(ev) => setInputValue({...inputValue, ...{[label]: ev.target.value}})}
                   onKeyUp={ev => {if (ev.keyCode === 13) onSubmit()}} />
        </div>
    );

    return (
        <div className="box">
            <h5 className="title is-5">{props.title}</h5>
            <div className="field has-addons">
                {fields}
                
                <div className="control">
                    <button data-testid="submit-button" className="button is-info" onClick={onSubmit}>
                    {props.buttonLabel}
                    </button>
                </div>
            </div>
        </div>
    )
};
export default LoginPageWidget;