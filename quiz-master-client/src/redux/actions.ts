import { Action } from "redux";
import { ActionType } from "./action-types";

export interface ShowErrorAction extends Action<ActionType> {
    type: ActionType;
    payload: {
        errorMessage: string;
    }
}

export interface ResetErrorAction extends Action<ActionType> {
    type: ActionType;
    payload: {
        errorMessage: string;
    }
}

export function showError(errorMessage: string): ShowErrorAction {
    return {
        type: ActionType.SHOW_ERROR,
        payload: {
            errorMessage: errorMessage
        }
    }
};

export function resetError(): ShowErrorAction {
    return {
        type: ActionType.RESET_ERROR,
        payload: {
            errorMessage: undefined
        }
    }
};
