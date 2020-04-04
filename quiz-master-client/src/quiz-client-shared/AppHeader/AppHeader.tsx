import React from 'react';
import './AppHeader.css';

interface AppHeaderProps {
    title: string;
}

const AppHeader: React.FC<AppHeaderProps> = (props: AppHeaderProps) => {

    return (
        <header className="level App-header">
            <div className="icon App-logo">
                <i className="far fa-question-circle"></i>
            </div> 
            {props.title}
    </header>
    )
};
export default AppHeader;