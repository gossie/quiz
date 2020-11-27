import { ActionType } from "./action-types";
import { ErrorAction } from "./actions";

const initialState = {
    errorMessage: undefined
};

export function handleError(state = initialState, action: ErrorAction) {
    switch (action.type) {
        case ActionType.SHOW_ERROR: {
            return {
                errorMessage: action.payload['errorMessage']
            }
        }
        case ActionType.RESET_ERROR: {
            return {
                errorMessage: undefined
            }
        }
        default:
            return state;
    }
}