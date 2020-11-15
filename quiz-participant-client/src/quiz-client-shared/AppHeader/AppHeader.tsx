import React from 'react';
import './AppHeader.scss';


interface AppHeaderProps {
    title: string;
}

const AppHeader: React.FC<AppHeaderProps> = (props: AppHeaderProps) => {
    return (
    <header>
        <nav className="navbar is-dark is-fixed-top">
            <div className="navbar-brand">
                <div className="icon app-logo">
                    <i className="far fa-question-circle"></i>
                </div> 
                <div className="app-title">{props.title}</div>
            </div>
        </nav>
  </header>
)};
export default AppHeader;