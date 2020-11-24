import { Action } from "redux";
import { ActionType } from "./action-types";

export interface ShowErrorAction extends Action<ActionType> {
    type: ActionType;
    payload: {
        errorMessage: string;
    }
}

export function showError(errorMessage: string): ShowErrorAction {
    console.debug('lande ich hier?');
    return {
        type: ActionType.SHOW_ERROR,
        payload: {
            errorMessage: errorMessage
        }
    }
};
