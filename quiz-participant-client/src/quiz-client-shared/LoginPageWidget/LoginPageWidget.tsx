import React, { useState, useEffect } from 'react';
import './LoginPageWidget.css';

export interface InputInformation {
    label: string;
    value?: string;
    cssClass?: string;
}

interface JoinWidgetProps {
    onSubmit: (inputValue: any) => Promise<void>;
    title: string;
    inputInformation: Array<InputInformation>;
    buttonLabel: string;
}

const LoginPageWidget: React.FC<JoinWidgetProps> = (props: JoinWidgetProps) => {
    const [inputValues, setInputValues] = useState({});
    const [css, setCss] = useState('button is-primary');

    const onSubmit = () => {
        setCss('button is-info is-loading');
        props.onSubmit(inputValues)
            .then(() => setCss('button is-info'));
    }

    useEffect(() => {
        setInputValues(
            props.inputInformation.reduce((values, ii) => {
                values[ii.label] = ii.value;
                return values;
            }, {})
        );
    }, [props.inputInformation])

    const fields = props.inputInformation.map((ii, index) => 
        <div key={ii.label}  className="control">
            <input data-testid={'field-' + index}
                   className={`input ${ii.cssClass}`}
                   type="text"
                   placeholder={ii.label}
                   value={inputValues[ii.label]}
                   onChange={(ev) => setInputValues({...inputValues, ...{[ii.label]: ev.target.value}})}
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