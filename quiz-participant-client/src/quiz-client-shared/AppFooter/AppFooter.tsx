import React from 'react';
import { useTranslation } from 'react-i18next';
import './AppFooter.scss';

interface AppFooterProps {}

const AppFooter: React.FC<AppFooterProps> = (props: AppFooterProps) => {
    const { t } = useTranslation();

    return (
        <footer className="footer">
            <nav className="content has-text-centered">
                <p>
                    { t('footerQuestion') } <a href={process.env.REACT_APP_QUIZ_MASTER_BASE_URL}>{ t('footerLink') }</a>
                </p>
            </nav>
    </footer>
    )};
export default AppFooter;
