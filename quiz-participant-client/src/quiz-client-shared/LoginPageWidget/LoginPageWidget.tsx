import React, { useState } from 'react';
import './LoginPageWidget.css';

interface JoinWidgetProps {
    onSubmit: (inputValue: any) => Promise<void>;
    title: string;
    inputLabels: string[];
    buttonLabel: string;
}

const LoginPageWidget: React.FC<JoinWidgetProps> = (props: JoinWidgetProps) => {
    const [inputValue, setInputValue] = useState({});
    const [css, setCss] = useState('button is-info');

    const onSubmit = () => {
        setCss('button is-info is-loading');
        props.onSubmit(inputValue)
            .then(() => setCss('button is-info'));
    }

    const fields = props.inputLabels.map((label, index) => 
        <div key={label}  className="control">
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
                    <button data-testid="submit-button" className={css} onClick={onSubmit}>
                    {props.buttonLabel}
                    </button>
                </div>
            </div>
        </div>
    )
};
export default LoginPageWidget;