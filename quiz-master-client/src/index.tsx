import React, { Suspense } from 'react';
import ReactDOM from 'react-dom';

import { Provider } from "react-redux";
import store from "./redux/store";

import './index.scss';
import App from './App';
import * as serviceWorker from './serviceWorker';
import './i18n';

ReactDOM.render(
    <React.StrictMode>
        <Suspense fallback="loading">
            <Provider store={store}>
                <App />
            </Provider>
        </Suspense>
    </React.StrictMode>,
    document.getElementById('root')
);

// If you want your app to work offline and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: https://bit.ly/CRA-PWA
serviceWorker.unregister();
