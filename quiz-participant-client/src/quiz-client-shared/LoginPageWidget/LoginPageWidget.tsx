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
    const fields = props.inputLabels.map((label) => 
        <div key={label} className="control">
            <input className="input" type="text" placeholder={label} onChange={(ev) => setInputValue({...inputValue, ...{[label]: ev.target.value}})}/>
        </div>
    );

    const onSubmit = () => {
        props.onSubmit(inputValue)
    }

    return (
        <div className="box">
            <h5 className="title is-5">{props.title}</h5>
            <div className="field has-addons">
                {fields}
                
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