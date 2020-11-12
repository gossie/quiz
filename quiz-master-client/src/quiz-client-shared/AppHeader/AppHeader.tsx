import React, { useState } from 'react';
import './AppHeader.css';

interface AppHeaderProps {
    title: string;
    quizId?: string;
}

const AppHeader: React.FC<AppHeaderProps> = (props: AppHeaderProps) => {
    const [invitePopupOpen, setInvitePopupOpen] = useState(false);

    const copyUrlToClipboard = () => {
        var urlTextField = document.getElementById("participantUrl")  as HTMLInputElement;;
        urlTextField.select();
        urlTextField.setSelectionRange(0, 99999); /*For mobile devices*/
      
        /* Copy the text inside the text field */
        document.execCommand("copy");
    }

    return (
    <header>
        <nav className="navbar is-dark is-fixed-top">
            <div className="navbar-brand">
                <div className="icon App-logo">
                    <i className="far fa-question-circle"></i>
                </div> 
                {props.title}
            </div>
            <div className="navbar-end">
                <div className="navbar-item">
                    <p className="control">
                    { props.quizId &&
                        <a className="button is-primary" onClick={() => setInvitePopupOpen(true)}>
                            <span>Invite Participants</span>
                        </a>
                    }
                    </p>
                </div>
            </div>
        </nav>
        { invitePopupOpen &&
            <div className="modal is-active">
                <div className="modal-background"></div>
                <div className="modal-card">
                    <header className="modal-card-head">
                        <p className="modal-card-title">Invite Participants</p>
                        <button data-testid="close-button" className="delete" aria-label="close" onClick={() => setInvitePopupOpen(false)}></button>
                    </header>
                    <section className="modal-card-body">
                        <p className="block">Copy the URL and send it to your quiz participants.</p>
                        <div className="field has-addons">
                            <div className="control" style={{width: "100%"}}>
                                <input className="input" type="text" id="participantUrl" value={process.env.REACT_APP_PARTICIPANT_BASE_URL + '?quiz_id=' + props.quizId} readOnly></input>
                            </div>
                            <div className="control">
                                <button className="button is-info" onClick={() => copyUrlToClipboard()}>
                                Copy to Clipboard
                                </button>
                            </div>
                        </div>
                    </section>
                </div>
            </div>
        }
  </header>
)};
export default AppHeader;