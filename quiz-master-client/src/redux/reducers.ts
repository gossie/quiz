import { ActionType } from "./action-types";
import { ShowErrorAction } from "./actions";

const initialState = {
    errorMessage: undefined
};

export function showError(state = initialState, action: ShowErrorAction) {
    switch (action.type) {
        case ActionType.SHOW_ERROR: {
            return {
                errorMessage: action.payload['errorMessage']
            }
        }  
        default:
            return state;
    }
}