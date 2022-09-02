import { useTranslation } from 'react-i18next';
import './AppFooter.scss';

export default function AppFooter() {

    const { t } = useTranslation();

    const trans1 = t('footerQuestion')
    const trans2 = t('footerLink')

    return (
        <footer className="footer">
            <nav className="content has-text-centered">
                <p>
                    <div>
                        <span>{ trans1 }</span> <a href={process.env.REACT_APP_QUIZ_MASTER_BASE_URL} target="_blank" rel="noreferrer">{ trans2 }</a>
                    </div>
                </p>
            </nav>
    </footer>
)};
