import { Action } from "redux";
import { ActionType } from "./action-types";

export interface ErrorAction extends Action<ActionType> {
    type: ActionType;
    payload: {
        errorMessage: string;
    }
}

export function showError(errorMessage: string): ErrorAction {
    return {
        type: ActionType.SHOW_ERROR,
        payload: {
            errorMessage: errorMessage
        }
    }
};

export function resetError(): ErrorAction {
    return {
        type: ActionType.RESET_ERROR,
        payload: {
            errorMessage: undefined
        }
    }
};
