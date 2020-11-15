import React, { useState } from 'react';

interface InviteButtonProps {
    quizId?: string;
}

const InviteButton: React.FC<InviteButtonProps> = (props: InviteButtonProps) => {
    const [invitePopupOpen, setInvitePopupOpen] = useState(false);

    const copyUrlToClipboard = () => {
        console.log("COPYYYY")
        var urlTextField = document.getElementById("participantUrl")  as HTMLInputElement;;
        urlTextField.select();
        urlTextField.setSelectionRange(0, 99999); /*For mobile devices*/
      
        /* Copy the text inside the text field */
        document.execCommand("copy");
    }

    return (
        <span>
            <button data-testid="invite-button" className="button is-primary" onClick={() => setInvitePopupOpen(true)}>
                <span>Invite Participants</span>
            </button>
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
                                <div className="control" style={{ width: "100%" }}>
                                    <input data-testid="participant-url-input" className="input" type="text" id="participantUrl" value={process.env.REACT_APP_PARTICIPANT_BASE_URL + '?quiz_id=' + props.quizId} readOnly></input>
                                </div>
                                <div className="control">
                                    <button data-testid="copy-url-button" className="button is-primary" onClick={() => copyUrlToClipboard()}>
                                        Copy to Clipboard
                                    </button>
                                </div>
                            </div>
                        </section>
                    </div>
                </div>
            }

        </span>
    )
};

export default InviteButton;