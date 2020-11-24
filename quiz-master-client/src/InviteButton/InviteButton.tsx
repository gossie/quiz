import React, { useState } from 'react';
import { useTranslation } from 'react-i18next';

interface InviteButtonProps {
    quizId?: string;
}

const InviteButton: React.FC<InviteButtonProps> = (props: InviteButtonProps) => {
    const [invitePopupOpen, setInvitePopupOpen] = useState(false);

    const { t } = useTranslation();

    const copyUrlToClipboard = () => {
        console.debug("COPYYYY")
        var urlTextField = document.getElementById("participantUrl")  as HTMLInputElement;;
        urlTextField.select();
        urlTextField.setSelectionRange(0, 99999); /*For mobile devices*/
      
        /* Copy the text inside the text field */
        document.execCommand("copy");
    }

    return (
        <span>
            <button data-testid="invite-button" className="button is-primary" onClick={() => setInvitePopupOpen(true)}>
                <span>{t('buttonInviteParticipants')}</span>
            </button>
            { invitePopupOpen &&
                <div className="modal is-active">
                    <div className="modal-background"></div>
                    <div className="modal-card">
                        <header className="modal-card-head">
                            <p className="modal-card-title">{t('headlineInviteParticipants')}</p>
                            <button data-testid="close-button" className="delete" aria-label="close" onClick={() => setInvitePopupOpen(false)}></button>
                        </header>
                        <section className="modal-card-body">
                            <p className="block">{t('invitationExplanation')}</p>
                            <div className="field has-addons">
                                <div className="control" style={{ width: "100%" }}>
                                    <input data-testid="participant-url-input" className="input" type="text" id="participantUrl" value={process.env.REACT_APP_PARTICIPANT_BASE_URL + '?quiz_id=' + props.quizId} readOnly></input>
                                </div>
                                <div className="control">
                                    <button data-testid="copy-url-button" className="button is-primary" onClick={() => copyUrlToClipboard()}>
                                        {t('buttonCopy')}
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