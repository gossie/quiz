import React, { useState } from 'react';
import './AppHeader.scss';
import InviteButton from '../../InviteButton/InviteButton';


interface AppHeaderProps {
    title: string;
    quizId?: string;
}

const AppHeader: React.FC<AppHeaderProps> = (props: AppHeaderProps) => {
    const [burgerMenuOpen, setBurgerMenuOpen] = useState(false);

    return (
    <header>
        <nav className="navbar is-dark is-fixed-top">
            <div className="navbar-brand">
                <div className="icon app-logo">
                    <i className="far fa-question-circle"></i>
                </div> 
                <div className="app-title">{props.title}</div>
                { props.quizId &&
                    <button className="navbar-burger burger" aria-label="menu" aria-expanded="false" data-target="navbarMenu" onClick={() => setBurgerMenuOpen(!burgerMenuOpen)}>
                        <span aria-hidden="true"></span>
                        <span aria-hidden="true"></span>
                        <span aria-hidden="true"></span>
                    </button>
                }
            </div>
            <div className={'navbar-menu' + (burgerMenuOpen ? ' is-active' : '')} id="navbarMenu">
                <div className="navbar-end">
                    <div className="navbar-item">
                        <p className="control">
                        { props.quizId &&
                            <InviteButton quizId={props.quizId}></InviteButton>
                        }
                        </p>
                    </div>
                </div>
            </div>
        </nav>
        
  </header>
)};
export default AppHeader;